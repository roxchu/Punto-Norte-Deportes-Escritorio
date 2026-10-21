package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.DetallePedidoCompra; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   detallepedidocompra -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> detallepedidocompra -> ArrayList             (listar)
 */
public class DetallePedidoCompraDAO {

    // INSERT
    public void guardar(DetallePedidoCompra detallepedidocompra) {
        String sql = "INSERT INTO detalle_ped_compra "
                + "(idPedido, idVariante, skuProveedor, cantidadPedida, costoAcordado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detallepedidocompra.getIdPedido()); // TODO: reemplazar por getters reales
            ps.setInt(2, detallepedidocompra.getIdVariante());
            ps.setString(3, detallepedidocompra.getSKUProveedor());
            ps.setInt(4, detallepedidocompra.getCantidadPedida());
            ps.setDouble(5, detallepedidocompra.getCostoAcordado());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<DetallePedidoCompra> listar() {
        ArrayList<DetallePedidoCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_ped_compra";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetallePedidoCompra detallepedidocompra = new DetallePedidoCompra();
                detallepedidocompra.setIdDetPedido(rs.getInt("idDetPedido")); // TODO: reemplazar por setters reales
                detallepedidocompra.setIdPedido(rs.getInt("idPedido"));
                detallepedidocompra.setIdVariante(rs.getInt("idVariante"));
                detallepedidocompra.setSKUProveedor(rs.getString("skuProveedor"));
                detallepedidocompra.setCantidadPedida(rs.getInt("cantidadPedida"));
                detallepedidocompra.setCostoAcordado(rs.getDouble("costoAcordado"));
                lista.add(detallepedidocompra);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(DetallePedidoCompra detallepedidocompra) {
        String sql = "UPDATE detalle_ped_compra SET "
                + "idPedido = ?, idVariante = ?, skuProveedor = ?, cantidadPedida = ?, costoAcordado = ? "
                + "WHERE idDetPedido = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, detallepedidocompra.getIdPedido()); // TODO: reemplazar por getters reales
            ps.setInt(2, detallepedidocompra.getIdVariante());
            ps.setString(3, detallepedidocompra.getSKUProveedor());
            ps.setInt(4, detallepedidocompra.getCantidadPedida());
            ps.setDouble(5, detallepedidocompra.getCostoAcordado());
            ps.setInt(6, detallepedidocompra.getIdDetPedido()); // TODO: getter del id real

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM detalle_ped_compra WHERE idDetPedido = ?";

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


