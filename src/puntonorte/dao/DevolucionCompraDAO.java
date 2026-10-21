package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.DevolucionCompra;
import puntonorte.modelo.TipoResolucion;

public class DevolucionCompraDAO {

    // INSERT
    public void guardar(DevolucionCompra devolucion) {
        String sql = "INSERT INTO devoluciones_compra "
                + "(idPedido, usrSolicita, usrAutoriza, fechaHora, tipoResolucion, motivo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, devolucion.getIdPedido());
            ps.setInt(2, devolucion.getUsrSolicita());
            ps.setInt(3, devolucion.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(devolucion.getFechaHora()));

            // tipoResolucion: nullable -> setString admite null directo, no hace falta setNull
            ps.setString(5, devolucion.getTipoResolucion() != null ? devolucion.getTipoResolucion().name() : null);

            ps.setString(6, devolucion.getMotivo()); // String ya admite null solo

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<DevolucionCompra> listar() {
        ArrayList<DevolucionCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM devoluciones_compra";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DevolucionCompra devolucion = new DevolucionCompra();
                devolucion.setIdDevolucion(rs.getInt("idDevolucion"));
                devolucion.setIdPedido(rs.getInt("idPedido"));
                devolucion.setUsrSolicita(rs.getInt("usrSolicita"));
                devolucion.setUsrAutoriza(rs.getInt("usrAutoriza"));
                devolucion.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());

                // tipoResolucion: nullable -> el String ya viene null si la columna es NULL
                String tipoTexto = rs.getString("tipoResolucion");
                devolucion.setTipoResolucion(tipoTexto != null ? TipoResolucion.valueOf(tipoTexto.toUpperCase()) : null);

                devolucion.setMotivo(rs.getString("motivo"));

                lista.add(devolucion);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(DevolucionCompra devolucion) {
        String sql = "UPDATE devoluciones_compra SET "
                + "idPedido = ?, usrSolicita = ?, usrAutoriza = ?, fechaHora = ?, tipoResolucion = ?, motivo = ? "
                + "WHERE idDevolucion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, devolucion.getIdPedido());
            ps.setInt(2, devolucion.getUsrSolicita());
            ps.setInt(3, devolucion.getUsrAutoriza());
            ps.setTimestamp(4, Timestamp.valueOf(devolucion.getFechaHora()));
            ps.setString(5, devolucion.getTipoResolucion() != null ? devolucion.getTipoResolucion().name() : null);
            ps.setString(6, devolucion.getMotivo());
            ps.setInt(7, devolucion.getIdDevolucion());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int idDevolucion) {
        String sql = "DELETE FROM devoluciones_compra WHERE idDevolucion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idDevolucion);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
}