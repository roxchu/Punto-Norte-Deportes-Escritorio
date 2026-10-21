package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.DetalleDevolucionCompra; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   detalledevolucioncompra -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> detalledevolucioncompra -> ArrayList             (listar)
 */
public class DetalleDevolucionCompraDAO {

    // INSERT
    public void guardar(DetalleDevolucionCompra detalledevolucioncompra) {
        String sql = "INSERT INTO detalle_dev_compra "
                + "(idDevolucion, idVariante, cantidad, motivo) "
                + "VALUES (?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detalledevolucioncompra.getIdDevolucion()); // TODO: reemplazar por getters reales
            ps.setInt(2, detalledevolucioncompra.getIdVariante());
            ps.setInt(3, detalledevolucioncompra.getCantidad());
            ps.setString(4, detalledevolucioncompra.getMotivo());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<DetalleDevolucionCompra> listar() {
        ArrayList<DetalleDevolucionCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_dev_compra";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetalleDevolucionCompra detalledevolucioncompra = new DetalleDevolucionCompra();
                detalledevolucioncompra.setIdDetDevolucCompra(rs.getInt("idDetDevolucCompra")); // TODO: reemplazar por setters reales
                detalledevolucioncompra.setIdDevolucion(rs.getInt("idDevolucion"));
                detalledevolucioncompra.setIdVariante(rs.getInt("idVariante"));
                detalledevolucioncompra.setCantidad(rs.getInt("cantidad"));
                detalledevolucioncompra.setMotivo(rs.getString("motivo"));
                lista.add(detalledevolucioncompra);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(DetalleDevolucionCompra detalledevolucioncompra) {
        String sql = "UPDATE detalle_dev_compra SET "
                + "idDevolucion = ?, idVariante = ?, cantidad = ?, motivo = ? "
                + "WHERE idDetDevolucCompra = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detalledevolucioncompra.getIdDevolucion()); // TODO: reemplazar por getters reales
            ps.setInt(2, detalledevolucioncompra.getIdVariante());
            ps.setInt(3, detalledevolucioncompra.getCantidad());
            ps.setString(4, detalledevolucioncompra.getMotivo());
            ps.setInt(5, detalledevolucioncompra.getIdDetDevolucCompra()); // TODO: getter del id real

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM detalle_dev_compra WHERE idDetDevolucCompra = ?";

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


