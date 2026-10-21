package puntonorte.servicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import puntonorte.dao.DescuentosDAO;
import puntonorte.dao.ProductoDAO;
import puntonorte.modelo.Descuentos;
import puntonorte.modelo.Producto;

/** Reglas de alta y baja lógica del catálogo de descuentos. */
public class DescuentosServicio {
    private static final List<String> TIPOS = Arrays.asList("porcentuales", "por producto", "especiales", "generales");
    private final DescuentosDAO dao = new DescuentosDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final AuditoriaService auditoria = new AuditoriaService();

    public ArrayList<Descuentos> listar() {
        exigirPermiso();
        try { return dao.listarTodosSeguro(); }
        catch (SQLException e) { throw error("listar descuentos", e); }
    }

    public int crear(Descuentos d) {
        return crear(d, d == null ? null : d.getIdProducto());
    }

    public int crear(Descuentos d, Integer idProducto) {
        exigirPermiso();
        if (d == null || d.getNombre() == null || d.getNombre().trim().isEmpty())
            throw new ReglaNegocioException("El nombre del descuento es obligatorio.");
        d.setNombre(d.getNombre().trim());
        if (d.getNaturaleza() == null) throw new ReglaNegocioException("Elegí descuento o recargo.");
        if (!TIPOS.contains(d.getTipo())) throw new ReglaNegocioException("El tipo de descuento no es válido.");
        boolean productoOEspecial = "por producto".equals(d.getTipo()) || "especiales".equals(d.getTipo());
        if (productoOEspecial && idProducto == null)
            throw new ReglaNegocioException("Elegí el producto al que se aplicará la regla.");
        if (productoOEspecial && d.getNaturaleza() != puntonorte.modelo.Naturaleza.DESCUENTO)
            throw new ReglaNegocioException("Esta regla debe ser de naturaleza descuento, no recargo.");
        if (productoOEspecial && d.getMedioPago() != null)
            throw new ReglaNegocioException("La regla por producto no debe depender del medio de pago.");
        if ("generales".equals(d.getTipo()) && d.getMedioPago() != null)
            throw new ReglaNegocioException("Un descuento general se aplica a la venta, no a un medio de pago.");
        if (d.getMedioPago() != null && !"porcentuales".equals(d.getTipo()))
            throw new ReglaNegocioException("Los ajustes por medio de pago deben ser porcentuales.");
        if (!productoOEspecial && idProducto != null)
            throw new ReglaNegocioException("Solo las reglas por producto o especiales se asocian a un producto.");
        if ("especiales".equals(d.getTipo())) {
            if (d.getCantidadCompra() == null || d.getCantidadPaga() == null
                    || d.getCantidadCompra() < 2 || d.getCantidadPaga() < 1
                    || d.getCantidadPaga() >= d.getCantidadCompra())
                throw new ReglaNegocioException("Definí una promoción válida: comprá más unidades que las que se pagan.");
            d.setValor(0);
        } else if (d.getCantidadCompra() != null || d.getCantidadPaga() != null) {
            d.setCantidadCompra(null);
            d.setCantidadPaga(null);
        }
        if (!"especiales".equals(d.getTipo()) && (!Double.isFinite(d.getValor()) || d.getValor() <= 0))
            throw new ReglaNegocioException("El valor debe ser mayor que cero.");
        if ("porcentuales".equals(d.getTipo()) && d.getValor() > 100)
            throw new ReglaNegocioException("El porcentaje debe estar entre 0 y 100.");
        if (d.getFechaInicio() != null && d.getFechaFin() != null && d.getFechaFin().isBefore(d.getFechaInicio()))
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la de inicio.");
        d.setActivo(true);
        try {
            if (idProducto != null) {
                Producto producto = productoDAO.buscarPorId(idProducto);
                if (producto == null || !producto.isActivo())
                    throw new ReglaNegocioException("El producto elegido no existe o está inhabilitado.");
            }
            int id = idProducto == null ? dao.guardarSeguro(d) : dao.guardarSeguro(d, idProducto);
            auditoria.registrar("Descuento creado", "descuentos", id, null,
                    d.getNombre() + " · " + d.getTipo(), null, null);
            return id;
        } catch (SQLException e) { throw error("guardar el descuento", e); }
    }

    public ArrayList<Producto> listarProductosActivos() {
        exigirPermiso();
        try { return productoDAO.listarActivosSeguro(); }
        catch (SQLException e) { throw error("cargar productos para el descuento", e); }
    }

    public ArrayList<Descuentos> listarDescuentosGenerales() {
        if (!Sesion.activa() || !Permisos.puedeVender())
            throw new ReglaNegocioException("No tenés permiso para registrar ventas.");
        try { return dao.listarDescuentosGenerales(java.time.LocalDate.now()); }
        catch (SQLException e) { throw error("cargar descuentos generales", e); }
    }

    public void cambiarActivo(int id, boolean activo) {
        exigirPermiso();
        try {
            if (!dao.cambiarActivoSeguro(id, activo)) throw new ReglaNegocioException("El descuento ya no existe.");
            auditoria.registrar(activo ? "Descuento activado" : "Descuento desactivado", "descuentos", id,
                    activo ? "Inactivo" : "Activo", activo ? "Activo" : "Inactivo", null, null);
        } catch (SQLException e) { throw error("actualizar el descuento", e); }
    }

    private void exigirPermiso() {
        if (!Sesion.activa()) throw new ReglaNegocioException("No hay una sesión iniciada.");
        if (!Permisos.puedeGestionarDescuentos()) throw new ReglaNegocioException("Solo el Supervisor puede gestionar descuentos.");
    }

    private ReglaNegocioException error(String accion, SQLException e) {
        return new ReglaNegocioException("No se pudo " + accion + ": " + e.getMessage());
    }
}
