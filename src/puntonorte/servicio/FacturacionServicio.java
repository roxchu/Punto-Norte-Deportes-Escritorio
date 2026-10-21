package puntonorte.servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import puntonorte.dao.ComprobanteDAO;
import puntonorte.dao.DetalleVentaDAO;
import puntonorte.dao.EmisorDAO;
import puntonorte.dao.ProductoDAO;
import puntonorte.dao.ProductoVarianteDAO;
import puntonorte.dao.VentasDAO;
import puntonorte.modelo.CondicionIva;
import puntonorte.modelo.DatosFiscalesVenta;
import puntonorte.modelo.DetalleVenta;
import puntonorte.modelo.Emisor;
import puntonorte.modelo.Producto;
import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Ventas;

/**
 * Reglas de la facturación: Factura A (Responsable inscripto) y Factura B (Consumidor final y
 * Monotributista).
 *
 *  - El tipo de factura sale de la condición del cliente (CondicionIva), no se elige a mano.
 *  - Factura A: exige CUIT válido, razón social y domicilio. El IVA va discriminado.
 *  - Factura B: el IVA está incluido en el precio. Desde $ 10.000.000 hay que identificar al
 *    comprador con DNI y registrarlo.
 *  - Al cobrar se guarda el comprobante con un número correlativo y un CAE SIMULADOS (no hay
 *    conexión con ARCA). Mientras SIMULADO sea true, el ticket dice "SIN VALIDEZ FISCAL".
 *
 * Esta clase NO usa Ventaservicio (Ventaservicio la usa a ella), para evitar dependencias circulares.
 */
public class FacturacionServicio {

    /** Desde este importe, al Consumidor final / Monotributista hay que pedirle el DNI. */
    public static final double TOPE_IDENTIFICACION = 10_000_000.00;

    /** true = el número y el CAE son de prueba. Cuando se conecte ARCA de verdad, pasar a false. */
    private static final boolean SIMULADO = true;

    private static final int ANCHO = 58;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentasDAO ventasDAO = new VentasDAO();
    private final DetalleVentaDAO detalleDAO = new DetalleVentaDAO();
    private final ProductoVarianteDAO varianteDAO = new ProductoVarianteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final EmisorDAO emisorDAO = new EmisorDAO();
    private final ComprobanteDAO comprobanteDAO = new ComprobanteDAO();

    // ============================================================ DATOS DE ENTRADA Y SALIDA

    /** A quién se le factura. Se crea con deConsumidor (Factura B) o deEmpresa (Factura A). */
    public static class Receptor {
        public final String nombre;
        public final String tipoDoc;      // SIN_IDENTIFICAR / DNI / CUIT
        public final String numeroDoc;
        public final String domicilio;

        private Receptor(String nombre, String tipoDoc, String numeroDoc, String domicilio) {
            this.nombre = nombre;
            this.tipoDoc = tipoDoc;
            this.numeroDoc = numeroDoc;
            this.domicilio = domicilio;
        }

        /** Factura B: el nombre y el DNI son opcionales (null o vacío = sin identificar). */
        public static Receptor deConsumidor(String nombre, String dni) {
            String n = limpio(nombre);
            String d = normalizarDocumento(dni);
            return new Receptor(n.isEmpty() ? null : n, d.isEmpty() ? "SIN_IDENTIFICAR" : "DNI",
                    d.isEmpty() ? null : d, null);
        }

        /** Factura A: CUIT, razón social y domicilio. */
        public static Receptor deEmpresa(String cuit, String razonSocial, String domicilio) {
            return new Receptor(limpio(razonSocial), "CUIT", Cuit.normalizar(cuit), limpio(domicilio));
        }
    }

    /** Una línea del comprobante (precio final, con IVA incluido). */
    public static class LineaComprobante {
        public final String descripcion;
        public final int cantidad;
        public final double precioUnitario;
        public final double importe;

        LineaComprobante(String descripcion, int cantidad, double precioUnitario, double importe) {
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.importe = importe;
        }
    }

    /** Todo lo que lleva el ticket, ya reunido. */
    public static class Comprobante {
        public Emisor emisor;
        public DatosFiscalesVenta fiscal;
        public Ventas venta;
        public final List<LineaComprobante> lineas = new ArrayList<>();
    }

    // ================================================================== REGLAS

    /** ¿Hay que pedir el DNI del comprador? Solo en Factura B y desde el tope. */
    public static boolean requiereDocumento(CondicionIva condicion, double total) {
        boolean esB = condicion == null || condicion.getTipoComprobante() == 'B';
        return esB && total >= TOPE_IDENTIFICACION;
    }

    /** Valida el DNI (7 u 8 números; acepta puntos y espacios). Lanza ReglaNegocioException si no sirve. */
    public void validarDocumento(String dni) {
        if (!normalizarDocumento(dni).matches("\\d{7,8}")) {
            throw new ReglaNegocioException("El total llega a " + dinero(TOPE_IDENTIFICACION)
                    + " o más: ingresá un DNI válido del comprador (7 u 8 números).");
        }
    }

    /** Datos obligatorios de la Factura A. Lanza ReglaNegocioException con el motivo si algo falta. */
    public void validarReceptorA(String cuit, String razonSocial, String domicilio) {
        Cuit.validar(cuit);
        String razon = limpio(razonSocial);
        String dom = limpio(domicilio);
        if (razon.isEmpty()) {
            throw new ReglaNegocioException("Ingresá la razón social del cliente (Factura A).");
        }
        if (razon.length() > 150) {
            throw new ReglaNegocioException("La razón social es demasiado larga (máximo 150 caracteres).");
        }
        if (dom.isEmpty()) {
            throw new ReglaNegocioException("Ingresá el domicilio del cliente (Factura A).");
        }
        if (dom.length() > 150) {
            throw new ReglaNegocioException("El domicilio es demasiado largo (máximo 150 caracteres).");
        }
    }

    /**
     * Si ya se le facturó a ese CUIT, devuelve su razón social y domicilio para autocompletar.
     * Es una ayuda: si no hay datos o falla la consulta, devuelve null y se carga a mano.
     */
    public Receptor buscarReceptorPorCuit(String cuitCrudo) {
        if (!Sesion.activa() || !Cuit.esValido(cuitCrudo)) {
            return null;
        }
        String cuit = Cuit.normalizar(cuitCrudo);
        try {
            DatosFiscalesVenta previo = comprobanteDAO.buscarUltimoReceptorPorCuit(cuit);
            return previo == null ? null
                    : Receptor.deEmpresa(cuit, previo.getReceptorNombre(), previo.getReceptorDomicilio());
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Guarda el comprobante de la venta: número correlativo y CAE simulado. Se llama desde
     * Ventaservicio.confirmar, dentro de la transacción de cobro (por eso recibe la conexión ya abierta).
     *
     * @param receptor Factura A: obligatorio (CUIT, razón social y domicilio). Factura B: puede ser null.
     */
    public void registrarComprobante(Connection con, int idVenta, CondicionIva condicion,
            Receptor receptor, double total) throws SQLException {

        CondicionIva c = condicion == null ? CondicionIva.CONSUMIDOR_FINAL : condicion;
        boolean esA = c.getTipoComprobante() == 'A';
        if (esA && (receptor == null || !"CUIT".equals(receptor.tipoDoc))) {
            throw new ReglaNegocioException("La Factura A necesita el CUIT, la razón social y el domicilio del cliente.");
        }

        Emisor emisor = emisorDAO.obtener();
        String tipo = String.valueOf(c.getTipoComprobante());
        int puntoVenta = emisor == null ? 1 : emisor.getPuntoVenta();
        int numero = comprobanteDAO.siguienteNumero(con, tipo, puntoVenta);

        // Los precios incluyen IVA: neto e IVA se separan del total. En B el IVA se informa como "contenido".
        double neto = CalculoFactura.neto(total);
        double iva = CalculoFactura.redondear(total - neto);

        DatosFiscalesVenta d = new DatosFiscalesVenta();
        d.setTipoComprobante(tipo);
        d.setEstadoFactura("EMITIDA");
        d.setPuntoVenta(puntoVenta);
        d.setNumeroComprobante(numero);
        // CAE de mentira: 14 dígitos que empiezan con 9999 para que se note que no es real.
        // Lleva el código del tipo (1 = A, 6 = B) para que A y B nunca repitan el mismo CAE.
        d.setCae(String.format("9999%s%03d%06d", esA ? "1" : "6", puntoVenta % 1000, numero % 1000000));
        d.setVencimientoCae(LocalDate.now().plusDays(10));
        d.setCondicionIva(c);

        if (esA) {
            d.setReceptorNombre(receptor.nombre);
            d.setReceptorTipoDoc("CUIT");
            d.setReceptorNumeroDoc(receptor.numeroDoc);
            d.setReceptorDomicilio(receptor.domicilio);
        } else {
            boolean hayNombre = receptor != null && receptor.nombre != null;
            d.setReceptorNombre(hayNombre ? receptor.nombre
                    : (c == CondicionIva.CONSUMIDOR_FINAL ? "A CONSUMIDOR FINAL" : "MONOTRIBUTISTA (SIN IDENTIFICAR)"));
            d.setReceptorTipoDoc(receptor == null ? "SIN_IDENTIFICAR" : receptor.tipoDoc);
            d.setReceptorNumeroDoc(receptor == null ? null : receptor.numeroDoc);
        }
        d.setMontoNeto(neto);
        d.setMontoIva(iva);

        comprobanteDAO.guardarComprobante(con, idVenta, d);
    }

    // ================================================================== CONSULTA

    /** Reúne emisor + datos fiscales + venta + líneas. Respeta el permiso: el Vendedor ve solo lo suyo. */
    public Comprobante armarComprobante(int idVenta) {
        if (!Sesion.activa()) {
            throw new ReglaNegocioException("No hay una sesión iniciada.");
        }
        try {
            Ventas venta = ventasDAO.buscarPorId(idVenta);
            if (venta == null) {
                throw new ReglaNegocioException("La venta #" + idVenta + " no existe.");
            }
            if (!Permisos.puedeVerTodasLasVentas() && venta.getIdUsuario() != Sesion.getIdUsuario()) {
                throw new ReglaNegocioException("No tenés permiso para ver esa venta.");
            }

            DatosFiscalesVenta fiscal = comprobanteDAO.buscarPorVenta(idVenta);
            if (fiscal == null || fiscal.getTipoComprobante() == null) {
                throw new ReglaNegocioException("La venta #" + idVenta
                        + " se registró antes de activar la facturación: no tiene comprobante.");
            }

            Emisor emisor = emisorDAO.obtener();
            if (emisor == null) {
                throw new ReglaNegocioException("Faltan los datos del emisor (tabla emisor_config).");
            }

            Comprobante c = new Comprobante();
            c.emisor = emisor;
            c.fiscal = fiscal;
            c.venta = venta;

            for (DetalleVenta d : detalleDAO.listarPorVenta(idVenta)) {
                ProductoVariante v = varianteDAO.buscarPorId(d.getIdVariante());
                Producto p = v == null ? null : productoDAO.buscarPorId(v.getIdProducto());
                String descripcion = (p == null ? "Artículo" : p.getNombre())
                        + (v == null ? "" : " " + v.getTalle() + "/" + v.getColor());
                c.lineas.add(new LineaComprobante(descripcion, d.getCantidad(), d.getPrecioHistorico(),
                        CalculoFactura.redondear(d.getPrecioHistorico() * d.getCantidad())));
            }
            return c;

        } catch (SQLException e) {
            throw new ReglaNegocioException("No se pudo armar el comprobante. Detalle: " + e.getMessage());
        }
    }

    // ================================================================== TICKET

    /** Arma el texto del ticket (ancho fijo, para mostrar con letra monoespaciada). */
    public String textoTicket(Comprobante c) {
        StringBuilder sb = new StringBuilder();
        agregarCabecera(sb, c);
        agregarReceptor(sb, c.fiscal);
        if ("A".equals(c.fiscal.getTipoComprobante())) {
            agregarCuerpoA(sb, c);
        } else {
            agregarCuerpoB(sb, c);
        }
        agregarPie(sb, c.fiscal);
        return sb.toString();
    }

    private void agregarCabecera(StringBuilder sb, Comprobante c) {
        Emisor e = c.emisor;
        DatosFiscalesVenta f = c.fiscal;
        String doble = repetir('=', ANCHO);

        sb.append(doble).append('\n');
        sb.append(izqDer(" " + e.getRazonSocial(), "FACTURA " + f.getTipoComprobante() + " ")).append('\n');
        sb.append(" CUIT: ").append(e.getCuit()).append('\n');
        sb.append(" IVA: ").append(e.getCondicionIva()).append('\n');
        sb.append(" Domicilio: ").append(e.getDomicilioComercial()).append('\n');
        if (e.getIngresosBrutos() != null && !e.getIngresosBrutos().isEmpty()) {
            sb.append(" Ing. Brutos: ").append(e.getIngresosBrutos()).append('\n');
        }
        if (e.getInicioActividades() != null) {
            sb.append(" Inicio de actividades: ").append(e.getInicioActividades().format(FECHA)).append('\n');
        }
        sb.append(repetir('-', ANCHO)).append('\n');

        String numero = f.getNumeroComprobante() == null
                ? "(pendiente de ARCA)" : String.format("%08d", f.getNumeroComprobante());
        String puntoVenta = f.getPuntoVenta() == null ? "----" : String.format("%04d", f.getPuntoVenta());
        sb.append(" Pto. de venta: ").append(puntoVenta).append("   N°: ").append(numero).append('\n');
        sb.append(" Venta interna: #").append(c.venta.getIdVenta()).append('\n');
        sb.append(" Fecha: ").append(c.venta.getFechaHora().format(FECHA_HORA)).append('\n');
        sb.append(repetir('-', ANCHO)).append('\n');
    }

    private void agregarReceptor(StringBuilder sb, DatosFiscalesVenta f) {
        boolean esA = "A".equals(f.getTipoComprobante());
        sb.append(esA ? " Razón soc.: " : " Cliente   : ").append(f.getReceptorNombre()).append('\n');
        if (esA) {
            sb.append(" CUIT      : ").append(Cuit.formatear(f.getReceptorNumeroDoc())).append('\n');
        }
        sb.append(" Cond. IVA : ").append(f.getCondicionIva() == null ? "-" : f.getCondicionIva().getTexto()).append('\n');
        if (esA && f.getReceptorDomicilio() != null && !f.getReceptorDomicilio().isEmpty()) {
            sb.append(" Domicilio : ").append(f.getReceptorDomicilio()).append('\n');
        }
        if (!esA && f.getReceptorNumeroDoc() != null && !f.getReceptorNumeroDoc().isEmpty()) {
            sb.append(" ").append(String.format("%-9s", f.getReceptorTipoDoc())).append(" : ")
              .append(f.getReceptorNumeroDoc()).append('\n');
        }
        sb.append(repetir('-', ANCHO)).append('\n');
    }

    /** Factura B: precios finales (IVA incluido) y el IVA contenido al final. */
    private void agregarCuerpoB(StringBuilder sb, Comprobante c) {
        Ventas v = c.venta;
        DatosFiscalesVenta f = c.fiscal;

        sb.append(String.format(" %-4s %-24s %12s %13s%n", "Cant", "Descripción", "P.Unit", "Importe"));
        for (LineaComprobante l : c.lineas) {
            List<String> partes = partir(l.descripcion, 24);
            sb.append(String.format(" %-4d %-24s %12s %13s%n",
                    l.cantidad, partes.get(0), dinero2(l.precioUnitario), dinero2(l.importe)));
            for (int i = 1; i < partes.size(); i++) {
                sb.append("      ").append(partes.get(i)).append('\n');   // talle y color no se cortan
            }
        }
        sb.append(repetir('-', ANCHO)).append('\n');

        sb.append(izqDer(" Subtotal", dinero(v.getSubtotal()) + " ")).append('\n');
        if (v.getDescuentoTotal() > 0) {
            sb.append(izqDer(" Descuentos", "- " + dinero(v.getDescuentoTotal()) + " ")).append('\n');
        }
        if (v.getMontoAjustePago() < 0) {
            sb.append(izqDer(" Descuento por medio de pago", "- " + dinero(-v.getMontoAjustePago()) + " ")).append('\n');
        } else if (v.getMontoAjustePago() > 0) {
            sb.append(izqDer(" Recargo por medio de pago", "+ " + dinero(v.getMontoAjustePago()) + " ")).append('\n');
        }
        sb.append(izqDer(" TOTAL", dinero(v.getMontoTotal()) + " ")).append('\n');
        if (f.getMontoIva() != null) {
            sb.append(izqDer(" IVA contenido (21%)", dinero(f.getMontoIva()) + " ")).append('\n');
        }
        sb.append(" Medio de pago: ").append(textoMedioPago(v)).append('\n');
        sb.append(repetir('-', ANCHO)).append('\n');
    }

    /** Factura A: precios SIN IVA, neto gravado, IVA discriminado y total. */
    private void agregarCuerpoA(StringBuilder sb, Comprobante c) {
        Ventas v = c.venta;
        DatosFiscalesVenta f = c.fiscal;

        sb.append(String.format(" %-4s %-20s %14s %14s%n", "Cant", "Descripción", "P.Unit neto", "Importe neto"));
        List<Double> importes = new ArrayList<>();
        for (LineaComprobante l : c.lineas) {
            importes.add(l.importe);
            List<String> partes = partir(l.descripcion, 20);
            sb.append(String.format(" %-4d %-20s %14s %14s%n", l.cantidad, partes.get(0),
                    dinero2(CalculoFactura.neto(l.precioUnitario)), dinero2(CalculoFactura.neto(l.importe))));
            for (int i = 1; i < partes.size(); i++) {
                sb.append("      ").append(partes.get(i)).append('\n');
            }
        }
        sb.append(repetir('-', ANCHO)).append('\n');

        // Se usan el neto y el IVA guardados al emitir (si faltan, se calculan del total).
        CalculoFactura.DesgloseA d = (f.getMontoNeto() != null && f.getMontoIva() != null)
                ? CalculoFactura.desgloseA(importes, v.getMontoAjustePago(), f.getMontoNeto(), f.getMontoIva(), v.getMontoTotal())
                : CalculoFactura.desgloseA(importes, v.getMontoAjustePago(), v.getMontoTotal());

        sb.append(izqDer(" Subtotal neto", dinero(d.sumaLineasNeto) + " ")).append('\n');
        if (d.descuentoNeto > 0) {
            sb.append(izqDer(" Descuentos (neto)", "- " + dinero(d.descuentoNeto) + " ")).append('\n');
        } else if (d.descuentoNeto < 0) {
            sb.append(izqDer(" Recargos (neto)", "+ " + dinero(-d.descuentoNeto) + " ")).append('\n');
        }
        if (d.ajusteNeto < 0) {
            sb.append(izqDer(" Desc. por medio de pago (neto)", "- " + dinero(-d.ajusteNeto) + " ")).append('\n');
        } else if (d.ajusteNeto > 0) {
            sb.append(izqDer(" Recargo por medio de pago (neto)", "+ " + dinero(d.ajusteNeto) + " ")).append('\n');
        }
        sb.append(izqDer(" Neto gravado", dinero(d.netoTotal) + " ")).append('\n');
        sb.append(izqDer(" IVA 21%", dinero(d.iva) + " ")).append('\n');
        sb.append(izqDer(" TOTAL", dinero(d.total) + " ")).append('\n');
        sb.append(" Medio de pago: ").append(textoMedioPago(v)).append('\n');
        sb.append(repetir('-', ANCHO)).append('\n');
    }

    private void agregarPie(StringBuilder sb, DatosFiscalesVenta f) {
        String cae = f.getCae() == null ? "pendiente de ARCA" : f.getCae();
        String vence = f.getVencimientoCae() == null ? "pendiente" : f.getVencimientoCae().format(FECHA);
        sb.append(" CAE: ").append(cae).append('\n');
        sb.append(" Vto. CAE: ").append(vence).append('\n');
        if (SIMULADO || !"EMITIDA".equals(f.getEstadoFactura())) {
            sb.append('\n').append(" *** COMPROBANTE DE PRUEBA - SIN VALIDEZ FISCAL ***").append('\n');
        }
        sb.append(repetir('=', ANCHO)).append('\n');
    }

    // ================================================================== PRIVADOS

    private String textoMedioPago(Ventas v) {
        switch (v.getMedioPago()) {
            case EFECTIVO: return "Efectivo";
            case DEBITO: return "Tarjeta de débito";
            case CREDITO: return "Tarjeta de crédito";
            default: return "Transferencia";
        }
    }

    private static String limpio(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private static String normalizarDocumento(String dni) {
        return dni == null ? "" : dni.replace(".", "").replace(" ", "").trim();
    }

    /** Parte un texto en renglones de hasta "ancho" caracteres, cortando entre palabras. */
    private List<String> partir(String texto, int ancho) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : (texto == null ? "" : texto.trim()).split("\\s+")) {
            while (palabra.length() > ancho) {              // una palabra más larga que la columna se corta
                if (actual.length() > 0) {
                    lineas.add(actual.toString());
                    actual.setLength(0);
                }
                lineas.add(palabra.substring(0, ancho));
                palabra = palabra.substring(ancho);
            }
            if (actual.length() > 0 && actual.length() + 1 + palabra.length() > ancho) {
                lineas.add(actual.toString());
                actual.setLength(0);
            }
            if (actual.length() > 0) {
                actual.append(' ');
            }
            actual.append(palabra);
        }
        if (actual.length() > 0 || lineas.isEmpty()) {
            lineas.add(actual.toString());
        }
        return lineas;
    }

    /** Izquierda y derecha en la misma línea, rellenando con espacios hasta ANCHO. */
    private String izqDer(String izquierda, String derecha) {
        int espacios = Math.max(1, ANCHO - izquierda.length() - derecha.length());
        return izquierda + repetir(' ', espacios) + derecha;
    }

    private String repetir(char caracter, int veces) {
        return new String(new char[veces]).replace('\0', caracter);
    }

    private String dinero(double valor) {
        return "$ " + dinero2(valor);
    }

    private String dinero2(double valor) {
        return String.format(Locale.forLanguageTag("es-AR"), "%,.2f", valor);
    }
}