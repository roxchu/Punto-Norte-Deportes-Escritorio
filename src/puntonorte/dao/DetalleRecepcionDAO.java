package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.DetalleRecepcion; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   detallerecepcion -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> detallerecepcion -> ArrayList             (listar)
 */
public class DetalleRecepcionDAO {

    // INSERT
    public void guardar(DetalleRecepcion detallerecepcion) {
        String sql = "INSERT INTO detalle_recepcion "
                + "(idRecepcion, idVariante, cantidadRecibida, observacion) "
                + "VALUES (?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detallerecepcion.getIdRecepcion()); // TODO: reemplazar por getters reales
            ps.setInt(2, detallerecepcion.getIdVariante());
            ps.setInt(3, detallerecepcion.getCantidadRecibida());
            ps.setString(4, detallerecepcion.getObservacion());
            
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<DetalleRecepcion> listar() {
        ArrayList<DetalleRecepcion> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_recepcion";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetalleRecepcion detallerecepcion = new DetalleRecepcion();
                detallerecepcion.setIdDetRecepcion(rs.getInt("idDetRecepcion")); // TODO: reemplazar por setters reales
                detallerecepcion.setIdRecepcion(rs.getInt("idRecepcion"));
                detallerecepcion.setIdVariante(rs.getInt("idVariante"));
                detallerecepcion.setCantidadRecibida(rs.getInt("cantidadRecibida"));
                detallerecepcion.setObservacion(rs.getString("observacion"));
                lista.add(detallerecepcion);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(DetalleRecepcion detallerecepcion) {
        String sql = "UPDATE detalle_recepcion SET "
                + "idRecepcion = ?, idVariante = ?, cantidadRecibida = ?, observacion = ? "
                + "WHERE idDetRecepcion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detallerecepcion.getIdRecepcion()); // TODO: reemplazar por getters reales
            ps.setInt(2, detallerecepcion.getIdVariante());
            ps.setInt(3, detallerecepcion.getCantidadRecibida());
            ps.setString(4, detallerecepcion.getObservacion());
            ps.setInt(5, detallerecepcion.getIdDetRecepcion()); // TODO: getter del id real

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM detalle_recepcion WHERE idDetRecepcion = ?";

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


