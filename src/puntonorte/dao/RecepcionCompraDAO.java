package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.RecepcionCompra; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   recepcioncompra -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> recepcioncompra -> ArrayList             (listar)
 */
public class RecepcionCompraDAO {

    // INSERT
    public void guardar(RecepcionCompra recepcioncompra) {
        String sql = "INSERT INTO recepciones_compra "
                + "(idPedido, idUbicacion, idUsuarioRecepciona, numComprobante, fechaRecepcion) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, recepcioncompra.getIdPedido()); // TODO: reemplazar por getters reales
            ps.setInt(2, recepcioncompra.getIdUbicacion());
            ps.setInt(3, recepcioncompra.getIdUsuarioRecepciona());
            ps.setString(4, recepcioncompra.getNumComprobante());
            ps.setTimestamp(5, Timestamp.valueOf(recepcioncompra.getFechaRecepcion()));
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<RecepcionCompra> listar() {
        ArrayList<RecepcionCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM recepciones_compra";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RecepcionCompra recepcioncompra = new RecepcionCompra();
                recepcioncompra.setIdRecepcion(rs.getInt("idRecepcion")); // TODO: reemplazar por setters reales
                recepcioncompra.setIdPedido(rs.getInt("idPedido"));
                recepcioncompra.setIdUbicacion(rs.getInt("idUbicacion"));
                recepcioncompra.setIdUsuarioRecepciona(rs.getInt("idUsuarioRecepciona"));
                recepcioncompra.setNumComprobante(rs.getString("numComprobante"));
                recepcioncompra.setFechaRecepcion(rs.getTimestamp("fechaRecepcion").toLocalDateTime());
                lista.add(recepcioncompra);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(RecepcionCompra recepcioncompra) {
        String sql = "UPDATE recepciones_compra SET "
                + "idPedido = ?, idUbicacion = ?, idUsuarioRecepciona = ?, numComprobante = ?, fechaRecepcion  = ? "
                + "WHERE idRecepcion = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, recepcioncompra.getIdPedido()); // TODO: reemplazar por getters reales
            ps.setInt(2, recepcioncompra.getIdUbicacion());
            ps.setInt(3, recepcioncompra.getIdUsuarioRecepciona());
            ps.setString(4, recepcioncompra.getNumComprobante());
            ps.setTimestamp(5, Timestamp.valueOf(recepcioncompra.getFechaRecepcion()));
            ps.setInt(6, recepcioncompra.getIdRecepcion()); // TODO: getter del id real

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM recepciones_compra WHERE idRecepcion = ?";

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


