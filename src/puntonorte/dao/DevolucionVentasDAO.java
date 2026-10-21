package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.DevolucionVenta;
import puntonorte.modelo.TipoResolucion;// TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   devolucionventa -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> devolucionventa -> ArrayList             (listar)
 */
public class DevolucionVentasDAO {

    // INSERT
    public void guardar(DevolucionVenta devolucionventa) {
        String sql = "INSERT INTO devoluciones "
                + "(idVenta, usrSolicita, usrAutoriza, fechaHora, tipoResolucion, motivo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, devolucionventa.getIdVenta()); // TODO: reemplazar por getters reales
            ps.setInt(2, devolucionventa.getUsrSolicita());
            ps.setInt(3, devolucionventa.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(devolucionventa.getFechaHora()));
            ps.setString(5, devolucionventa.getTipoResolucion() != null ? devolucionventa.getTipoResolucion().name() : null);
            ps.setString(6, devolucionventa.getMotivo());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<DevolucionVenta> listar() {
        ArrayList<DevolucionVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM devoluciones";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DevolucionVenta devolucionventa = new DevolucionVenta();
                devolucionventa.setIdDevolucion(rs.getInt("idDevolucion")); // TODO: reemplazar por setters reales
                devolucionventa.setIdVenta(rs.getInt("idVenta"));
                devolucionventa.setUsrSolicita(rs.getInt("usrSolicita"));
                devolucionventa.setUsrAutoriza(rs.getInt("usrAutoriza"));
                devolucionventa.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());
                String tipoTexto = rs.getString("tipoResolucion");
                devolucionventa.setTipoResolucion(tipoTexto != null ? TipoResolucion.valueOf(tipoTexto.toUpperCase()) : null);
                devolucionventa.setMotivo(rs.getString("motivo"));
                lista.add(devolucionventa);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(DevolucionVenta devolucionventa) {
        String sql = "UPDATE devoluciones SET "
                + "idVenta = ?, usrSolicita = ?, usrAutoriza = ?, fechaHora = ?, tipoResolucion = ?, motivo = ? "
                + "WHERE idDevolucion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, devolucionventa.getIdVenta()); // TODO: reemplazar por getters reales
            ps.setInt(2, devolucionventa.getUsrSolicita());
            ps.setInt(3, devolucionventa.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(devolucionventa.getFechaHora()));
            ps.setString(5, devolucionventa.getTipoResolucion() != null ? devolucionventa.getTipoResolucion().name() : null);
            ps.setString(6, devolucionventa.getMotivo());
            ps.setInt(7, devolucionventa.getIdDevolucion());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM devoluciones WHERE idDevolucion = ?";

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

