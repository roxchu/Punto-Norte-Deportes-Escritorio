package puntonorte.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import puntonorte.conexion.ConexionBD;

/** Consultas reales que alimentan el panel de inicio. */
public class InicioService {

    public static final class Pedido {
        public final int id;
        public final String proveedor;
        public final String estado;
        public Pedido(int id, String proveedor, String estado) {
            this.id = id;
            this.proveedor = proveedor;
            this.estado = estado;
        }
    }

    public static final class Operacion {
        public final LocalDateTime fecha;
        public final String usuario, rol, operacion, modulo, detalle;
        public Operacion(LocalDateTime fecha,String usuario,String rol,String operacion,String modulo,String detalle){
            this.fecha=fecha;this.usuario=usuario;this.rol=rol;this.operacion=operacion;this.modulo=modulo;this.detalle=detalle;
        }
    }

    public static final class Datos {
        public boolean disponible = true;
        public int productos;
        public int stockBajo;
        public int ventasHoy;
        public int pedidosAbiertos;
        public int solicitudesUsuarios;
        public final List<Pedido> pedidos = new ArrayList<>();
        public final List<Operacion> operaciones = new ArrayList<>();
    }

    public Datos cargar() {
        if (!Sesion.activa()) throw new ReglaNegocioException("No hay una sesión iniciada.");
        Datos datos = new Datos();
        String filtroVenta = Permisos.puedeVerTodasLasVentas() ? "" : " AND idUsuario = ?";
        String sqlVentas = "SELECT COUNT(*) FROM ventas WHERE fechaHora >= ? AND fechaHora < ? "
                + "AND estado IN ('Pagada','Entregada')" + filtroVenta;
        try (Connection c = ConexionBD.conectar()) {
            datos.productos = contar(c, "SELECT COUNT(DISTINCT p.idProducto) FROM productos p "
                    + "JOIN producto_variantes v ON v.idProducto=p.idProducto "
                    + "WHERE p.activo=1 AND v.activo=1");
            datos.stockBajo = contar(c, "SELECT COUNT(*) FROM (SELECT v.idVariante "
                    + "FROM producto_variantes v JOIN productos p ON p.idProducto=v.idProducto "
                    + "LEFT JOIN stock s ON s.idVariante=v.idVariante "
                    + "WHERE v.activo=1 AND p.activo=1 GROUP BY v.idVariante,v.stockMinimo "
                    + "HAVING COALESCE(SUM(s.cantidad),0) <= COALESCE(v.stockMinimo,0)) variantes_bajas");
            try (PreparedStatement p = c.prepareStatement(sqlVentas)) {
                p.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                p.setDate(2, java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
                if (!Permisos.puedeVerTodasLasVentas()) p.setInt(3, Sesion.getIdUsuario());
                try (ResultSet r = p.executeQuery()) { r.next(); datos.ventasHoy = r.getInt(1); }
            }
            if (Permisos.puedeVerModulo("PROVEEDORES")) {
                datos.pedidosAbiertos = contar(c, "SELECT COUNT(*) FROM pedidos_compra WHERE estado IN ('Pendiente','Parcial')");
            }
            if (Permisos.esSupervisorOGerente(Sesion.getNombreRol())) {
                datos.solicitudesUsuarios = contar(c, "SELECT COUNT(*) FROM usuarios WHERE activo='PENDIENTE'");
            }
            if (Permisos.puedeVerModulo("PROVEEDORES")) {
                String pedidos = "SELECT pc.idPedido, COALESCE(pr.razonSocial,pr.nombre,'Proveedor') proveedor, pc.estado "
                        + "FROM pedidos_compra pc LEFT JOIN proveedor pr ON pr.idProveedor=pc.idProveedor "
                        + "WHERE pc.estado IN ('Pendiente','Parcial') ORDER BY pc.fechaEmision DESC, pc.idPedido DESC LIMIT 5";
                try (PreparedStatement p = c.prepareStatement(pedidos); ResultSet r = p.executeQuery()) {
                    while (r.next()) datos.pedidos.add(new Pedido(r.getInt("idPedido"),
                            r.getString("proveedor"), r.getString("estado")));
                }
            }
            cargarUltimasOperaciones(c,datos);
            return datos;
        } catch (SQLException e) {
            throw new ReglaNegocioException("No se pudieron cargar los datos del inicio. Revisá la conexión con la base.");
        }
    }

    /** Consulta auditoría y ventas por separado para que un problema en una fuente no oculte la otra. */
    private void cargarUltimasOperaciones(Connection c,Datos datos)throws SQLException {
        boolean todas=Permisos.puedeVerTodasLasVentas();
        String filtro=todas?"":" AND a.idUsuario = ?";
        String auditoria="SELECT a.fechaHora,COALESCE(CONCAT(u.nombre,' ',u.apellido),'(sin cuenta)') usuario,"
                +"COALESCE(r.nombreRol,'-') rol,a.accionRealizada operacion,a.tablaAfectada modulo,COALESCE(a.detalleCambio,'') detalle "
                +"FROM auditoria a LEFT JOIN usuarios u ON u.idUsuario=a.idUsuario LEFT JOIN roles r ON r.idRol=u.idRol "
                +"WHERE NOT (LOWER(a.tablaAfectada)='ventas' AND a.accionRealizada='Venta registrada')"+filtro
                +" ORDER BY a.fechaHora DESC LIMIT 20";
        try(PreparedStatement p=c.prepareStatement(auditoria)){
            if(!todas)p.setInt(1,Sesion.getIdUsuario());
            try(ResultSet r=p.executeQuery()){while(r.next())datos.operaciones.add(new Operacion(
                    r.getTimestamp(1).toLocalDateTime(),r.getString("usuario"),r.getString("rol"),
                    r.getString("operacion"),r.getString("modulo"),r.getString("detalle")));}
        } catch (SQLException ignorada) {
            // La auditoría es complementaria: un problema allí no debe ocultar ventas reales.
        }
        String filtroVenta=todas?"":" AND v.idUsuario = ?";
        String ventas="SELECT v.fechaHora,COALESCE(CONCAT(u.nombre,' ',u.apellido),'(sin cuenta)') usuario,"
                +"COALESCE(r.nombreRol,'-') rol,v.idVenta,v.montoTotal,v.medioPago "
                +"FROM ventas v LEFT JOIN usuarios u ON u.idUsuario=v.idUsuario "
                +"LEFT JOIN roles r ON r.idRol=u.idRol WHERE 1=1"+filtroVenta+" ORDER BY v.fechaHora DESC LIMIT 20";
        try(PreparedStatement p=c.prepareStatement(ventas)){
            if(!todas)p.setInt(1,Sesion.getIdUsuario());
            try(ResultSet r=p.executeQuery()){while(r.next())datos.operaciones.add(new Operacion(
                    r.getTimestamp("fechaHora").toLocalDateTime(),r.getString("usuario"),r.getString("rol"),
                    "Venta registrada","ventas","Venta #"+r.getInt("idVenta")+" · "+r.getBigDecimal("montoTotal")+" · "+r.getString("medioPago")));}
        }
        datos.operaciones.sort(Comparator.comparing((Operacion op)->op.fecha).reversed());
        if(datos.operaciones.size()>20)datos.operaciones.subList(20,datos.operaciones.size()).clear();
    }

    private int contar(Connection c, String sql) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            int total = 0;
            while (r.next()) total += r.getInt(1);
            return total;
        }
    }
}
