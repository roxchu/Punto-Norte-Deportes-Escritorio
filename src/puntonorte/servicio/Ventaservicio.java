package puntonorte.servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import puntonorte.conexion.ConexionBD;
import puntonorte.dao.ClienteDAO;
import puntonorte.dao.DescuentosDAO;
import puntonorte.dao.DetalleVentaDAO;
import puntonorte.dao.ProductoDAO;
import puntonorte.dao.ProductoVarianteDAO;
import puntonorte.dao.StockDAO;
import puntonorte.dao.UbicacionDAO;
import puntonorte.dao.VentasDAO;
import puntonorte.modelo.Cliente;
import puntonorte.modelo.CondicionIva;
import puntonorte.modelo.Descuentos;
import puntonorte.modelo.DetalleVenta;
import puntonorte.modelo.EstadoVenta;
import puntonorte.modelo.MedioPago;
import puntonorte.modelo.Naturaleza;
import puntonorte.modelo.Producto;
import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Ubicacion;
import puntonorte.modelo.Ventas;

/**
 * Reglas de negocio de la venta (facturación): arma el pedido, calcula el total, valida el stock
 * y registra todo en UNA transacción (venta + líneas + descuento de stock). Las pantallas llaman acá
 * (nunca a los DAO) y muestran el mensaje de ReglaNegocioException si algo falla.
 *
 * Cómo se calcula el total (coincide con el boceto):
 *   subtotal lista        = suma de precio × cantidad de cada línea
 *   descuento productos   = suma de los descuentos de cada línea
 *   ajuste por medio pago = % (o monto) del medio de pago, calculado sobre (subtotal - descuento productos);
 *                           negativo si es descuento, positivo si es recargo
 *   TOTAL                 = subtotal lista - descuento productos + ajuste por medio de pago
 *
 * En la tabla ventas queda: subtotal = subtotal lista, descuentoTotal = descuento productos,
 * montoAjustePago = ajuste (con signo), montoTotal = TOTAL.
 *
 * La venta descuenta del SALÓN. Si ahí no alcanza, no se vende: hay que pedir que lo traigan del depósito
 * (transferencia en Stockservicio).
 */
public class Ventaservicio {

    private static final String TABLA = "ventas";
    private static final double MARGEN = 0.005;   // tolerancia para comparar importes en pesos

    private final VentasDAO ventasDAO = new VentasDAO();
    private final DetalleVentaDAO detalleDAO = new DetalleVentaDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final ProductoVarianteDAO varianteDAO = new ProductoVarianteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final DescuentosDAO descuentosDAO = new DescuentosDAO();
    private final puntonorte.dao.VentaDescuentoDAO ventaDescuentoDAO = new puntonorte.dao.VentaDescuentoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final UbicacionDAO ubicacionDAO = new UbicacionDAO();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final FacturacionServicio facturacion = new FacturacionServicio();

    // ============================================================ DATOS DE ENTRADA

    /** Una línea del pedido tal como la arma la pantalla. El precio NO viaja: lo toma el servicio de la BD. */
    public static class LineaPedido {
        public int idVariante;
        public int cantidad;

        public LineaPedido(int idVariante, int cantidad) {
            this.idVariante = idVariante;
            this.cantidad = cantidad;
        }
    }

    /** Todo lo que la pantalla sabe de la venta (diálogo "Pedido actual"). */
    public static class PedidoVenta {
        public final List<LineaPedido> lineas = new ArrayList<>();
        public MedioPago medioPago;
        public Integer idCliente;       // cliente ya registrado (null = no se eligió ninguno)
        public Cliente clienteNuevo;    // si se tildó "Registrar cliente" (nombre, apellido y DNI obligatorios)
        public CondicionIva condicionIva = CondicionIva.CONSUMIDOR_FINAL;   // define el tipo de factura
        public String receptorCuit;           // solo Factura A (Responsable inscripto)
        public String receptorRazonSocial;    // solo Factura A
        public String receptorDomicilio;      // solo Factura A
        public Double montoRecibido;    // solo efectivo
        public String observaciones;

        public void agregar(int idVariante, int cantidad) {
            lineas.add(new LineaPedido(idVariante, cantidad));
        }

        /** Deja el pedido en blanco (después de cobrar o de cancelar). */
        public void limpiar() {
            lineas.clear();
            medioPago = null;
            idCliente = null;
            clienteNuevo = null;
            condicionIva = CondicionIva.CONSUMIDOR_FINAL;
            receptorCuit = null;
            receptorRazonSocial = null;
            receptorDomicilio = null;
            montoRecibido = null;
            observaciones = null;
        }
    }

    // ============================================================ DATOS DE SALIDA

    /** Una línea ya calculada, lista para las tablas del pedido y de la revisión. */
    public static class LineaResumen {
        public final int idVariante;
        public final String sku;
        public final String descripcion;       // "Remera Streetwear · L/Negro"
        public final int cantidad;
        public final double precioUnitario;
        public final double subtotal;          // precio × cantidad, sin descuento
        public final double descuento;
        public final Integer idDescuento;

        LineaResumen(int idVariante, String sku, String descripcion, int cantidad,
                double precioUnitario, double subtotal, double descuento, Integer idDescuento) {
            this.idVariante = idVariante;
            this.sku = sku;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
            this.descuento = descuento;
            this.idDescuento = idDescuento;
        }
    }

    /** Resultado del cálculo: lo que muestran "Pedido actual" y "Revisión de venta". */
    public static class ResumenVenta {
        public final List<LineaResumen> lineas = new ArrayList<>();
        public double subtotalLista;
        public double descuentoProductos;
        public double descuentoGeneral;
        public double ajustePago;               // negativo = descuento por medio de pago, positivo = recargo
        public double total;
        public Integer idDescuentoPago;         // el descuento/recargo del medio de pago que se aplicó (o null)
        public Integer idDescuentoGeneral;      // regla general aplicada automáticamente
        public String etiquetaPago;             // "Efectivo (10% de descuento)"
        public Double montoRecibido;
        public Double vuelto;                   // null si no es efectivo o todavía no alcanza
        public boolean requiereDocumento;       // true = el total llega al tope: hay que pedir el DNI del comprador
    }

    // ================================================================== CONSULTAS

    /** Descuentos que se pueden elegir en una línea (los de medio de pago se aplican solos). */
    public ArrayList<Descuentos> listarDescuentosDeProducto() {
        exigirVender();
        ArrayList<Descuentos> resultado = new ArrayList<>();
        try {
            for (Descuentos d : descuentosDAO.listarVigentes(LocalDate.now())) {
                if (d.getMedioPago() == null && d.getNaturaleza() == Naturaleza.DESCUENTO
                        && ("por producto".equalsIgnoreCase(d.getTipo()) || "especiales".equalsIgnoreCase(d.getTipo()))) {
                    resultado.add(d);
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw errorBD("cargar los descuentos", e);
        }
    }

    /** Descuentos que el vendedor puede aplicar a la variante seleccionada, ligados a su producto. */
    public ArrayList<Descuentos> listarDescuentosDeProducto(int idVariante) {
        exigirVender();
        try {
            return descuentosDAO.listarVigentesPorVariante(LocalDate.now(), idVariante);
        } catch (SQLException e) {
            throw errorBD("cargar los descuentos del producto", e);
        }
    }

    /** Descuentos generales o porcentuales que se pueden aplicar al total de la venta. */
    public ArrayList<Descuentos> listarDescuentosGenerales() {
        exigirVender();
        try { return descuentosDAO.listarDescuentosGenerales(LocalDate.now()); }
        catch (SQLException e) { throw errorBD("cargar los descuentos generales", e); }
    }

    /** Variantes habilitadas disponibles en salón para el catálogo de venta. */
    public ArrayList<ProductoVariante> listarCatalogo(String texto) {
        exigirVender();
        ProductoVariante.Filtro filtro = new ProductoVariante.Filtro();
        filtro.setTexto(texto);
        try {
            ArrayList<ProductoVariante> resultado = varianteDAO.buscarCatalogo(filtro);
            ArrayList<ProductoVariante> disponibles = new ArrayList<>();
            for (ProductoVariante v : resultado) if (v.getStockSalon() > 0) disponibles.add(v);
            return disponibles;
        } catch (SQLException e) {
            throw errorBD("consultar el catálogo de ventas", e);
        }
    }

    /** Texto para el combo de descuentos de producto: "Liquidación (15%)" o "Cupón (3.000 pesos)". */
    public String etiquetaDescuento(Descuentos d) {
        if ("especiales".equalsIgnoreCase(d.getTipo())) {
            return d.getNombre() + " (comprá " + d.getCantidadCompra() + ", pagá " + d.getCantidadPaga() + ")";
        }
        boolean porcentaje = Permisos.normalizar(d.getTipo()).startsWith("porc");
        return d.getNombre() + " (" + (porcentaje ? numero(d.getValor()) + "%" : dinero(d.getValor())) + ")";
    }

    /** Texto para el combo de pago: "Efectivo (10% de descuento)", "Tarjeta de débito", etc. */
    public String etiquetaMedioPago(MedioPago medio) {
        exigirVender();
        try {
            return etiqueta(medio, descuentoDePago(medio, descuentosDAO.listarVigentes(LocalDate.now())));
        } catch (SQLException e) {
            throw errorBD("cargar los medios de pago", e);
        }
    }

    /** Ventas entre dos fechas (ambas inclusive). El Vendedor solo ve las suyas. */
    public ArrayList<Ventas> listarVentas(LocalDate desde, LocalDate hasta) {
        exigirSesion();
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new ReglaNegocioException("El rango de fechas no es válido.");
        }
        Integer filtroUsuario = Permisos.puedeVerTodasLasVentas() ? null : Sesion.getIdUsuario();
        try {
            return ventasDAO.listarPorFechas(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), filtroUsuario);
        } catch (SQLException e) {
            throw errorBD("consultar las ventas", e);
        }
    }

    /** Líneas de una venta (comprobante). El Vendedor solo puede ver las de sus propias ventas. */
    public ArrayList<DetalleVenta> lineasDeVenta(int idVenta) {
        exigirSesion();
        try {
            Ventas venta = ventasDAO.buscarPorId(idVenta);
            if (venta == null) {
                throw new ReglaNegocioException("La venta #" + idVenta + " no existe.");
            }
            if (!Permisos.puedeVerTodasLasVentas() && venta.getIdUsuario() != Sesion.getIdUsuario()) {
                throw new ReglaNegocioException("No tenés permiso para ver esa venta.");
            }
            return detalleDAO.listarPorVenta(idVenta);
        } catch (SQLException e) {
            throw errorBD("consultar la venta", e);
        }
    }

    // ================================================================== CÁLCULO

    /**
     * Valida el pedido y calcula todos los importes SIN guardar nada. La pantalla la usa para
     * mostrar "Pedido actual" y "Revisión de venta". Lanza ReglaNegocioException si algo no cierra
     * (stock insuficiente, variante inhabilitada, descuento vencido, etc.).
     */
    public ResumenVenta calcular(PedidoVenta pedido) {
        exigirVender();
        validarPedido(pedido);
        try {
            return armarResumen(pedido, idSalon());
        } catch (SQLException e) {
            throw errorBD("calcular la venta", e);
        }
    }

    // ================================================================== COBRO

    /**
     * Confirma el cobro. En UNA transacción: registra al cliente (si corresponde), guarda la venta,
     * guarda cada línea con su precio histórico y descuenta el stock del salón. Si cualquier paso falla
     * no queda nada a medias. Devuelve el número de venta.
     */
    public int confirmar(PedidoVenta pedido) {

        exigirVender();
        validarPedido(pedido);
        validarReceptor(pedido);

        int idSalon = idSalon();
        ResumenVenta r;
        try {
            r = armarResumen(pedido, idSalon);
        } catch (SQLException e) {
            throw errorBD("calcular la venta", e);
        }

        if (pedido.medioPago == MedioPago.EFECTIVO
                && (pedido.montoRecibido == null || pedido.montoRecibido + MARGEN < r.total)) {
            throw new ReglaNegocioException("El monto recibido no alcanza para cubrir el total ("
                    + dinero(r.total) + ").");
        }

        // Desde el tope (10 millones) la factura B exige identificar al comprador y registrarlo.
        if (r.requiereDocumento) {
            if (pedido.clienteNuevo == null) {
                throw new ReglaNegocioException("El total llega a " + dinero(FacturacionServicio.TOPE_IDENTIFICACION)
                        + " o más: registrá al cliente con su nombre, apellido y DNI.");
            }
            facturacion.validarDocumento(pedido.clienteNuevo.getDni());
        }

        int idVenta;
        Connection con = null;
        try {
            con = ConexionBD.conectar();
            con.setAutoCommit(false);

            Ventas venta = new Ventas();
            venta.setIdCliente(resolverCliente(con, pedido));
            venta.setIdUsuario(Sesion.getIdUsuario());
            venta.setIdUbicacion(idSalon);
            venta.setIdDescuento(r.idDescuentoPago);
            venta.setMedioPago(pedido.medioPago);
            venta.setEstado(EstadoVenta.PAGADA);
            venta.setMontoAjustePago(r.ajustePago);
            venta.setMontoRecibido(pedido.medioPago == MedioPago.EFECTIVO ? r.montoRecibido : null);
            venta.setVuelto(pedido.medioPago == MedioPago.EFECTIVO ? r.vuelto : null);
            venta.setFechaHora(LocalDateTime.now());
            venta.setSubtotal(r.subtotalLista);
            venta.setDescuentoTotal(redondear(r.descuentoProductos + r.descuentoGeneral));
            venta.setMontoTotal(r.total);
            venta.setObservaciones(limpio(pedido.observaciones).isEmpty() ? null : limpio(pedido.observaciones));

            idVenta = ventasDAO.guardar(con, venta);

            // Comprobante (Factura A o B) con número y CAE simulados, en la misma transacción que la venta.
            FacturacionServicio.Receptor receptor;
            if (pedido.condicionIva == CondicionIva.RESPONSABLE_INSCRIPTO) {
                receptor = FacturacionServicio.Receptor.deEmpresa(
                        pedido.receptorCuit, pedido.receptorRazonSocial, pedido.receptorDomicilio);
            } else if (pedido.clienteNuevo != null) {
                String nombreFiscal = (limpio(pedido.clienteNuevo.getNombre()) + " "
                        + limpio(pedido.clienteNuevo.getApellido())).trim();
                receptor = FacturacionServicio.Receptor.deConsumidor(nombreFiscal, pedido.clienteNuevo.getDni());
            } else {
                receptor = null;     // consumidor final / monotributista sin identificar
            }
            facturacion.registrarComprobante(con, idVenta, pedido.condicionIva, receptor, r.total);
            if (r.idDescuentoGeneral != null && r.descuentoGeneral > 0) {
                ventaDescuentoDAO.guardar(con, idVenta, r.idDescuentoGeneral, r.descuentoGeneral);
            }

            for (LineaResumen l : r.lineas) {
                // Este descuento es el que protege del "dos vendedores vendiendo lo último": bloquea la fila.
                int nueva = stockDAO.ajustarStock(con, l.idVariante, idSalon, -l.cantidad);
                if (nueva < 0) {
                    throw new ReglaNegocioException("El stock de " + l.descripcion
                            + " cambió mientras se cobraba y ya no alcanza. Volvé a armar el pedido.");
                }

                DetalleVenta d = new DetalleVenta();
                d.setIdVenta(idVenta);
                d.setIdVariante(l.idVariante);
                d.setIdDescuento(l.idDescuento);
                d.setCantidad(l.cantidad);
                d.setPrecioHistorico(l.precioUnitario);
                d.setMontoDescuentoAplicado(l.descuento);
                detalleDAO.guardar(con, d);
            }

            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw errorBD("registrar la venta", e);
        } catch (RuntimeException e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }

        // Fuera de la transacción: si la auditoría fallara, la venta igual quedó registrada.
        auditoria.registrar("Venta registrada", TABLA, idVenta, null, dinero(r.total), null,
                "Venta #" + idVenta + " · " + r.lineas.size() + " línea(s) · " + r.etiquetaPago);
        return idVenta;
    }

    // ================================================================== PRIVADOS

    private ResumenVenta armarResumen(PedidoVenta pedido, int idSalon) throws SQLException {

        // Una variante repetida en dos líneas se suma en una sola (así el control de stock es correcto).
        Map<Integer, LineaPedido> unicas = new LinkedHashMap<>();
        for (LineaPedido l : pedido.lineas) {
            LineaPedido previa = unicas.get(l.idVariante);
            if (previa == null) {
                unicas.put(l.idVariante, new LineaPedido(l.idVariante, l.cantidad));
            } else {
                previa.cantidad += l.cantidad;
            }
        }

        List<Descuentos> vigentes = descuentosDAO.listarVigentes(LocalDate.now());
        ResumenVenta r = new ResumenVenta();

        for (LineaPedido l : unicas.values()) {

            ProductoVariante v = varianteDAO.buscarPorId(l.idVariante);
            if (v == null) {
                throw new ReglaNegocioException("Una de las variantes del pedido ya no existe.");
            }
            Producto producto = productoDAO.buscarPorId(v.getIdProducto());
            if (!v.isActivo() || producto == null || !producto.isActivo()) {
                throw new ReglaNegocioException("La variante " + v.getSKU() + " está inhabilitada.");
            }

            String descripcion = producto.getNombre() + " · " + v.getTalle() + "/" + v.getColor();

            int enSalon = stockDAO.obtenerCantidad(l.idVariante, idSalon);
            if (enSalon < l.cantidad) {
                int total = stockDAO.obtenerStockTotal(l.idVariante);
                String extra = total > enSalon
                        ? " Hay " + (total - enSalon) + " en depósito: pedí que lo traigan al salón."
                        : "";
                throw new ReglaNegocioException("No hay stock suficiente de " + descripcion + " en el salón: hay "
                        + enSalon + " y el pedido pide " + l.cantidad + "." + extra);
            }

            double precio = v.getPrecioVenta();
            double bruto = redondear(precio * l.cantidad);

            // Las reglas las define el supervisor. Se aplica automáticamente la más conveniente
            // entre las reglas vigentes asociadas al producto; no se acumulan entre sí.
            double descLinea = 0;
            Integer idDescuentoAplicado = null;
            for (Descuentos d : descuentosDAO.listarVigentesPorVariante(LocalDate.now(), l.idVariante)) {
                double candidato = descuentoDeProducto(d, l.cantidad, precio);
                if (candidato > descLinea) {
                    descLinea = candidato;
                    idDescuentoAplicado = d.getIdDescuento();
                }
            }

            r.lineas.add(new LineaResumen(l.idVariante, v.getSKU(), descripcion, l.cantidad,
                    precio, bruto, descLinea, idDescuentoAplicado));
            r.subtotalLista += bruto;
            r.descuentoProductos += descLinea;
        }

        r.subtotalLista = redondear(r.subtotalLista);
        r.descuentoProductos = redondear(r.descuentoProductos);

        // Descuento o recargo del medio de pago, sobre lo que queda después de los descuentos de producto.
        double base = r.subtotalLista - r.descuentoProductos;
        Descuentos generalAplicado = null;
        for (Descuentos d : vigentes) {
            if (d.getNaturaleza() != Naturaleza.DESCUENTO || d.getMedioPago() != null
                    || !("generales".equalsIgnoreCase(d.getTipo()) || "porcentuales".equalsIgnoreCase(d.getTipo()))) continue;
            double candidato = Math.min(calcularMonto(d, base), base);
            if (candidato > r.descuentoGeneral) {
                r.descuentoGeneral = candidato;
                generalAplicado = d;
            }
        }
        if (generalAplicado != null && r.descuentoGeneral > 0) {
            r.idDescuentoGeneral = generalAplicado.getIdDescuento();
            base = redondear(base - r.descuentoGeneral);
        }
        Descuentos pago = descuentoDePago(pedido.medioPago, vigentes);
        if (pago != null) {
            double monto = calcularMonto(pago, base);
            r.ajustePago = pago.getNaturaleza() == Naturaleza.RECARGO ? monto : -monto;
            r.idDescuentoPago = pago.getIdDescuento();
        }
        r.etiquetaPago = etiqueta(pedido.medioPago, pago);
        r.total = redondear(Math.max(0, base + r.ajustePago));
        r.requiereDocumento = FacturacionServicio.requiereDocumento(pedido.condicionIva, r.total);

        if (pedido.medioPago == MedioPago.EFECTIVO && pedido.montoRecibido != null) {
            r.montoRecibido = pedido.montoRecibido;
            r.vuelto = pedido.montoRecibido + MARGEN >= r.total
                    ? redondear(Math.max(0, pedido.montoRecibido - r.total))
                    : null;
        }
        return r;
    }

    /** Factura A: verifica CUIT, razón social y domicilio. Para B no exige nada. */
    public void validarReceptor(PedidoVenta p) {
        if (p != null && p.condicionIva == CondicionIva.RESPONSABLE_INSCRIPTO) {
            facturacion.validarReceptorA(p.receptorCuit, p.receptorRazonSocial, p.receptorDomicilio);
        }
    }

    /** Autocompletar: razón social y domicilio de la última Factura A emitida a ese CUIT (o null). */
    public FacturacionServicio.Receptor buscarReceptorPorCuit(String cuit) {
        return facturacion.buscarReceptorPorCuit(cuit);
    }

    private void validarPedido(PedidoVenta p) {
        if (p == null || p.lineas.isEmpty()) {
            throw new ReglaNegocioException("El pedido está vacío: agregá al menos un producto.");
        }
        for (LineaPedido l : p.lineas) {
            if (l.cantidad <= 0) {
                throw new ReglaNegocioException("Las cantidades deben ser mayores a 0.");
            }
        }
        if (p.medioPago == null) {
            throw new ReglaNegocioException("Elegí el método de pago.");
        }
        if (p.montoRecibido != null && (p.montoRecibido < 0 || p.montoRecibido.isNaN() || p.montoRecibido.isInfinite())) {
            throw new ReglaNegocioException("El monto recibido no es válido.");
        }
        if (p.idCliente == null && p.clienteNuevo != null) {
            Cliente c = p.clienteNuevo;
            if (limpio(c.getNombre()).isEmpty() || limpio(c.getApellido()).isEmpty()) {
                throw new ReglaNegocioException("Para registrar al cliente hacen falta nombre y apellido.");
            }
            if (limpio(c.getDni()).isEmpty()) {
                throw new ReglaNegocioException("Para registrar al cliente hace falta el DNI.");
            }
        }
    }

    /** Cliente de la venta: uno elegido, uno nuevo (o el que ya existe con ese DNI), o null = Consumidor final. */
    private Integer resolverCliente(Connection con, PedidoVenta p) throws SQLException {
        if (p.idCliente != null) {
            return p.idCliente;
        }
        if (p.clienteNuevo == null) {
            return null;
        }
        Cliente c = p.clienteNuevo;
        c.setNombre(limpio(c.getNombre()));
        c.setApellido(limpio(c.getApellido()));
        c.setDni(limpio(c.getDni()));
        c.setTelefono(limpio(c.getTelefono()));
        c.setCorreo(limpio(c.getCorreo()));

        Cliente existente = clienteDAO.buscarPorDni(con, c.getDni());
        return existente != null ? existente.getIdCliente() : clienteDAO.guardar(con, c);
    }

    /** Id de la ubicación "Salón" (las ventas descuentan de ahí). */
    private int idSalon() {
        for (Ubicacion u : ubicacionDAO.listar()) {
            if (Permisos.normalizar(String.valueOf(u.getNombre())).equals("salon")) {
                return u.getIdUbicacion();
            }
        }
        throw new ReglaNegocioException("No está configurada la ubicación 'Salón'.");
    }

    private Descuentos descuentoDePago(MedioPago medio, List<Descuentos> vigentes) {
        for (Descuentos d : vigentes) {
            if (d.getMedioPago() == medio && "porcentuales".equalsIgnoreCase(d.getTipo())) {
                return d;
            }
        }
        return null;
    }

    private double descuentoDeProducto(Descuentos d, int cantidad, double precio) {
        if (d.getNaturaleza() != Naturaleza.DESCUENTO || d.getMedioPago() != null) return 0;
        if ("por producto".equalsIgnoreCase(d.getTipo())) {
            return Math.min(redondear(calcularMonto(d, precio) * cantidad), redondear(precio * cantidad));
        }
        if ("especiales".equalsIgnoreCase(d.getTipo())) {
            int compra = d.getCantidadCompra() == null ? 0 : d.getCantidadCompra();
            int paga = d.getCantidadPaga() == null ? 0 : d.getCantidadPaga();
            if (compra < 2 || paga < 1 || paga >= compra) return 0;
            int unidadesGratis = (cantidad / compra) * (compra - paga);
            return redondear(precio * unidadesGratis);
        }
        return 0;
    }

    private Descuentos buscarVigente(List<Descuentos> vigentes, int idDescuento) {
        for (Descuentos d : vigentes) {
            if (d.getIdDescuento() == idDescuento) {
                return d;
            }
        }
        return null;
    }

    /**
     * Monto que representa el descuento/recargo sobre una base. Si el tipo es porcentual ("PORCENTAJE")
     * es valor% de la base; cualquier otro tipo se toma como monto fijo en pesos.
     */
    private double calcularMonto(Descuentos d, double base) {
        boolean porcentaje = Permisos.normalizar(d.getTipo()).startsWith("porc");
        return redondear(porcentaje ? base * d.getValor() / 100.0 : d.getValor());
    }

    private String etiqueta(MedioPago medio, Descuentos d) {
        String nombre;
        switch (medio) {
            case EFECTIVO: nombre = "Efectivo"; break;
            case DEBITO: nombre = "Tarjeta de débito"; break;
            case CREDITO: nombre = "Tarjeta de crédito"; break;
            default: nombre = "Transferencia"; break;
        }
        if (d == null) {
            return nombre;
        }
        boolean porcentaje = Permisos.normalizar(d.getTipo()).startsWith("porc");
        String valor = porcentaje ? numero(d.getValor()) + "%" : dinero(d.getValor());
        String tipo = d.getNaturaleza() == Naturaleza.RECARGO ? "de recargo" : "de descuento";
        return nombre + " (" + valor + " " + tipo + ")";
    }

    private void exigirSesion() {
        if (!Sesion.activa()) {
            throw new ReglaNegocioException("No hay una sesión iniciada.");
        }
    }

    private void exigirVender() {
        exigirSesion();
        if (!Permisos.puedeVender()) {
            throw new ReglaNegocioException("No tenés permiso para registrar ventas.");
        }
    }

    private void deshacer(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignorada) {
                // si el rollback falla, al cerrar la conexión MySQL descarta la transacción igual
            }
        }
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignorada) {
                // nada más que hacer
            }
        }
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String limpio(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String numero(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }

    private String dinero(double valor) {
        return String.format(Locale.forLanguageTag("es-AR"), "$ %,.2f", valor);
    }

    private ReglaNegocioException errorBD(String accion, SQLException e) {
        return new ReglaNegocioException("No se pudo " + accion + ". Detalle: " + e.getMessage());
    }
}
