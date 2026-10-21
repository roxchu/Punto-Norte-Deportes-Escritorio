package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.AnulacionVenta; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   anulacionventa -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> anulacionventa -> ArrayList             (listar)
 */
public class AnulacionVentaDAO {

    // INSERT
    public void guardar(AnulacionVenta anulacionventa) {
        String sql = "INSERT INTO anulaciones "
                + "(idVenta, usrSolicita, usrAutoriza, fechaHora, motivo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, anulacionventa.getIdVenta()); // TODO: reemplazar por getters reales
            ps.setInt(2, anulacionventa.getUsrSolicita());
            ps.setInt(3, anulacionventa.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(anulacionventa.getFechaHora()));
            ps.setString(5, anulacionventa.getMotivo());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<AnulacionVenta> listar() {
        ArrayList<AnulacionVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM anulaciones";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AnulacionVenta anulacionventa = new AnulacionVenta();
                anulacionventa.setIdAnulacion(rs.getInt("idAnulacion")); // TODO: reemplazar por setters reales
                anulacionventa.setIdVenta(rs.getInt("idVenta"));
                anulacionventa.setUsrSolicita(rs.getInt("usrSolicita"));
                anulacionventa.setUsrAutoriza(rs.getInt("usrAutoriza"));
                anulacionventa.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());
                anulacionventa.setMotivo(rs.getString("motivo"));

                lista.add(anulacionventa);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(AnulacionVenta anulacionventa) {
        String sql = "UPDATE anulaciones SET "
                + "idVenta = ?, usrSolicita = ?, usrAutoriza = ?, fechaHora = ?, motivo = ? "
                + "WHERE idAnulacion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, anulacionventa.getIdVenta());
            ps.setInt(2, anulacionventa.getUsrSolicita());
            ps.setInt(3, anulacionventa.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(anulacionventa.getFechaHora()));
            ps.setString(5, anulacionventa.getMotivo());
            ps.setInt(6, anulacionventa.getIdAnulacion());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM anulaciones WHERE idAnulacion = ?";

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

