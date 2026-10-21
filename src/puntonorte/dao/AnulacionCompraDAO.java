package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.AnulacionCompra; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   anulacioncompra -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> anulacioncompra -> ArrayList             (listar)
 */
public class AnulacionCompraDAO {

    // INSERT
    public void guardar(AnulacionCompra anulacioncompra) {
        String sql = "INSERT INTO anulacion_compra "
                + "(idPedido, usrSolicita, usrAutoriza, fechaHora, motivo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, anulacioncompra.getIdPedido());
            ps.setInt(2, anulacioncompra.getUsrSolicita());
            ps.setInt(3, anulacioncompra.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(anulacioncompra.getFechaHora()));
            ps.setString(5, anulacioncompra.getMotivo());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<AnulacionCompra> listar() {
        ArrayList<AnulacionCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM anulacion_compra";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AnulacionCompra anulacioncompra = new AnulacionCompra();
                anulacioncompra.setIdAnulacion(rs.getInt("idAnulacion")); // TODO: reemplazar por setters reales
                anulacioncompra.setIdPedido(rs.getInt("idPedido"));
                anulacioncompra.setUsrSolicita(rs.getInt("usrSolicita"));
                anulacioncompra.setUsrAutoriza(rs.getInt("usrAutoriza"));
                anulacioncompra.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());
                anulacioncompra.setMotivo(rs.getString("motivo"));

                lista.add(anulacioncompra);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(AnulacionCompra anulacioncompra) {
        String sql = "UPDATE anulacion_compra SET "
                + "idPedido = ?, usrSolicita = ?, usrAutoriza = ?, fechaHora = ?, motivo = ? "
                + "WHERE idAnulacion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, anulacioncompra.getIdPedido());
            ps.setInt(2, anulacioncompra.getUsrSolicita());
            ps.setInt(3, anulacioncompra.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(anulacioncompra.getFechaHora()));
            ps.setString(5, anulacioncompra.getMotivo());
            ps.setInt(6, anulacioncompra.getIdAnulacion());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM anulacion_compra WHERE idAnulacion = ?";

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

