package puntonorte.servicio;

import java.sql.SQLException;

import puntonorte.dao.ProductoDAO;
import puntonorte.dao.ProductoVarianteDAO;
import puntonorte.dao.StockDAO;
import puntonorte.dao.UbicacionDAO;
import puntonorte.modelo.Producto;
import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Ubicacion;

/**
 * Reglas de negocio del stock: ajustes manuales (con motivo y auditoría) y transferencias
 * entre depósito y salón. Nunca deja el stock en negativo.
 *
 * La auditoría de stock se guarda con tabla "stock" (módulo Stock del historial) y el id de la variante.
 */
public class Stockservicio {

    private static final String TABLA = "stock";

    private final StockDAO stockDAO = new StockDAO();
    private final ProductoVarianteDAO varianteDAO = new ProductoVarianteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final UbicacionDAO ubicacionDAO = new UbicacionDAO();
    private final AuditoriaService auditoria = new AuditoriaService();

    // ================================================================= CONSULTAS

    /** Cantidad de una variante en una ubicación (0 si nunca tuvo). Para mostrar en los diálogos. */
    public int cantidadActual(int idVariante, int idUbicacion) {
        exigirSesion();
        try {
            return stockDAO.obtenerCantidad(idVariante, idUbicacion);
        } catch (SQLException e) {
            throw errorBD("consultar el stock", e);
        }
    }

    public int stockTotal(int idVariante) {
        exigirSesion();
        try {
            return stockDAO.obtenerStockTotal(idVariante);
        } catch (SQLException e) {
            throw errorBD("consultar el stock", e);
        }
    }

    /** Para el cartel "Productos con stock bajo" del panel de inicio. */
    public int contarStockBajo() {
        exigirSesion();
        try {
            return stockDAO.contarVariantesConStockBajo();
        } catch (SQLException e) {
            throw errorBD("consultar el stock bajo", e);
        }
    }

    /** Ubicaciones configuradas para llenar los combos del panel. */
    public java.util.ArrayList<Ubicacion> listarUbicaciones() {
        exigirSesion();
        return ubicacionDAO.listar();
    }

    public int crearUbicacion(String nombre) {
        exigirSesion();
        if (!Permisos.esSupervisorOGerente(Sesion.getNombreRol())) {
            throw new ReglaNegocioException("No tenés permiso para crear ubicaciones.");
        }
        nombre = limpio(nombre);
        if (nombre.isEmpty()) throw new ReglaNegocioException("El nombre de la ubicación es obligatorio.");
        try {
            if (ubicacionDAO.existeNombre(nombre)) {
                throw new ReglaNegocioException("Ya existe una ubicación llamada '" + nombre + "'.");
            }
            int id = ubicacionDAO.guardarSeguro(nombre);
            auditoria.registrar("Alta de ubicación", TABLA, id, null, nombre, null, nombre);
            return id;
        } catch (SQLException e) {
            throw errorBD("crear la ubicación", e);
        }
    }

    // ================================================================== AJUSTE

    /**
     * Ajuste manual: suma (delta positivo) o resta (negativo) unidades. Solo Supervisor y Gerente.
     * Exige motivo (rotura, diferencia de conteo, etc.) y queda en la auditoría con la cantidad
     * anterior, la nueva, el motivo y el usuario.
     *
     * @return la cantidad que quedó en esa ubicación
     */
    public int ajustarStock(int idVariante, int idUbicacion, int delta, String motivo) {

        exigirSesion();
        if (!Permisos.puedeAjustarStock()) {
            throw new ReglaNegocioException("No tenés permiso para ajustar el stock.");
        }
        if (delta == 0) {
            throw new ReglaNegocioException("El ajuste no puede ser 0.");
        }
        motivo = limpio(motivo);
        if (motivo.isEmpty()) {
            throw new ReglaNegocioException("Indicá el motivo del ajuste.");
        }
        try {
            ProductoVariante v = exigirVarianteHabilitada(idVariante);
            Ubicacion ubicacion = exigirUbicacion(idUbicacion);

            int nueva = stockDAO.ajustarStock(idVariante, idUbicacion, delta);

            if (nueva < 0) {
                int hay = stockDAO.obtenerCantidad(idVariante, idUbicacion);
                throw new ReglaNegocioException("El stock no puede quedar en negativo: en "
                        + nombre(ubicacion) + " hay " + hay + " y querés restar " + (-delta) + ".");
            }

            int anterior = nueva - delta;
            auditoria.registrar("Ajuste de stock", TABLA, idVariante,
                    String.valueOf(anterior), String.valueOf(nueva), motivo,
                    v.getSKU() + " · " + nombre(ubicacion) + " · " + (delta > 0 ? "+" : "") + delta);
            return nueva;

        } catch (SQLException e) {
            throw errorBD("ajustar el stock", e);
        }
    }

    // ============================================================== TRANSFERENCIA

    /**
     * Mueve mercadería de una ubicación a otra (por ejemplo depósito -> salón). El total no cambia.
     * Pueden hacerlo Depósito, Encargado, Supervisor y Gerente. Queda en la auditoría.
     */
    public void transferir(int idVariante, int idOrigen, int idDestino, int cantidad) {

        exigirSesion();
        if (!Permisos.puedeTransferirStock()) {
            throw new ReglaNegocioException("No tenés permiso para mover stock.");
        }
        if (idOrigen == idDestino) {
            throw new ReglaNegocioException("El origen y el destino no pueden ser el mismo.");
        }
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad a mover debe ser mayor a 0.");
        }

        try {
            ProductoVariante v = exigirVarianteHabilitada(idVariante);
            Ubicacion origen = exigirUbicacion(idOrigen);
            Ubicacion destino = exigirUbicacion(idDestino);

            if (!stockDAO.transferir(idVariante, idOrigen, idDestino, cantidad)) {
                int hay = stockDAO.obtenerCantidad(idVariante, idOrigen);
                throw new ReglaNegocioException("No alcanza el stock: en " + nombre(origen)
                        + " hay " + hay + " y querés mover " + cantidad + ".");
            }

            auditoria.registrar("Transferencia de stock", TABLA, idVariante, null, null, null,
                    v.getSKU() + " · " + cantidad + " u. · " + nombre(origen) + " → " + nombre(destino));

        } catch (SQLException e) {
            throw errorBD("mover el stock", e);
        }
    }

    // ================================================================== PRIVADOS

    private void exigirSesion() {
        if (!Sesion.activa()) {
            throw new ReglaNegocioException("No hay una sesión iniciada.");
        }
    }

    /** Solo se mueve stock de variantes cuyo producto y variante están habilitados. */
    private ProductoVariante exigirVarianteHabilitada(int idVariante) throws SQLException {
        ProductoVariante v = varianteDAO.buscarPorId(idVariante);
        if (v == null) {
            throw new ReglaNegocioException("La variante ya no existe.");
        }
        Producto p = productoDAO.buscarPorId(v.getIdProducto());
        if (!v.isActivo() || p == null || !p.isActivo()) {
            throw new ReglaNegocioException("La variante " + v.getSKU() + " está inhabilitada.");
        }
        return v;
    }

    private Ubicacion exigirUbicacion(int idUbicacion) {
        for (Ubicacion u : ubicacionDAO.listar()) {
            if (u.getIdUbicacion() == idUbicacion) {
                return u;
            }
        }
        throw new ReglaNegocioException("La ubicación seleccionada ya no existe.");
    }

    private String nombre(Ubicacion u) {
        return u.getNombre();
    }

    private String limpio(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private ReglaNegocioException errorBD(String accion, SQLException e) {
        return new ReglaNegocioException("No se pudo " + accion + ". Detalle: " + e.getMessage());
    }
}
