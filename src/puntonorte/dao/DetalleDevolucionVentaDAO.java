package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.DetalleDevolucionVenta; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

/**
 * PLANTILLA DE DAO (estilo visto en clase: guardar / listar).
 *
 * Pasos para adaptarla a cada clase del modelo:
 *   1) Renombrar el archivo, ej: ProductoDAO.java
 *   2) Reemplazar NOMBRE_CLASE por la clase real (ej: Producto)
 *   3) Reemplazar nombre_tabla por el nombre real de la tabla
 *   4) Reemplazar columna1, columna2... por las columnas reales
 *   5) Reemplazar los getX()/setX() por los atributos reales de la clase
 *
 * Recorrido a seguir (no memorizar, seguir el camino):
 *   detalledevolucionventa -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> detalledevolucionventa -> ArrayList             (listar)
 */
public class DetalleDevolucionVentaDAO {

    // INSERT
    public void guardar(DetalleDevolucionVenta detalledevolucionventa) {
        String sql = "INSERT INTO detalle_devolucion "
                + "(idDevolucion, idVariante, idUbicacion, cantidad) "
                + "VALUES (?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detalledevolucionventa.getIdDevolucion()); // TODO: reemplazar por getters reales
            ps.setInt(2, detalledevolucionventa.getIdVariante());
            ps.setInt(3, detalledevolucionventa.getIdUbicacion());
            ps.setInt(4, detalledevolucionventa.getCantidad());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<DetalleDevolucionVenta> listar() {
        ArrayList<DetalleDevolucionVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_devolucion";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetalleDevolucionVenta detalledevolucionventa = new DetalleDevolucionVenta();
                detalledevolucionventa.setIdDetDevolucion(rs.getInt("idDetDevolucion")); // TODO: reemplazar por setters reales
                detalledevolucionventa.setIdDevolucion(rs.getInt("idDevolucion"));
                detalledevolucionventa.setIdVariante(rs.getInt("idVariante"));
                detalledevolucionventa.setIdUbicacion(rs.getInt("idUbicacion"));
                detalledevolucionventa.setCantidad(rs.getInt("cantidad"));
                lista.add(detalledevolucionventa);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(DetalleDevolucionVenta detalledevolucionventa) {
        String sql = "UPDATE detalle_devolucion SET "
                + "idDevolucion = ?, idVariante = ?, idUbicacion = ?, cantidad = ? "
                + "WHERE idDetDevolucion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detalledevolucionventa.getIdDevolucion()); // TODO: reemplazar por getters reales
            ps.setInt(2, detalledevolucionventa.getIdVariante());
            ps.setInt(3, detalledevolucionventa.getIdUbicacion());
            ps.setInt(4, detalledevolucionventa.getCantidad());
            ps.setInt(5, detalledevolucionventa.getIdDetDevolucion()); // TODO: getter del id real

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM detalle_devolucion WHERE idDetDevolucion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
}


