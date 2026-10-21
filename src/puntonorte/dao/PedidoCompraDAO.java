package puntonorte.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.EstadoPedidoCompra;
import puntonorte.modelo.PedidoCompra;

public class PedidoCompraDAO {

    // INSERT
    public void guardar(PedidoCompra pedido) {
        String sql = "INSERT INTO pedidos_compra "
                + "(idProveedor, idUsuario, fechaEmision, estado, linkSeguimiento) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, pedido.getIdProveedor());
            ps.setInt(2, pedido.getIdUsuario());
            ps.setDate(3, Date.valueOf(pedido.getFechaEmision())); // fechaEmision es LocalDate (solo fecha)
            ps.setString(4, pedido.getEstado().name());            // estado: NOT NULL, siempre tiene valor
            ps.setString(5, pedido.getLinkSeguimiento());          // String ya admite null solo

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<PedidoCompra> listar() {
        ArrayList<PedidoCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedidos_compra";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PedidoCompra pedido = new PedidoCompra();
                pedido.setIdPedido(rs.getInt("idPedido"));
                pedido.setIdProveedor(rs.getInt("idProveedor"));
                pedido.setIdUsuario(rs.getInt("idUsuario"));
                pedido.setFechaEmision(rs.getDate("fechaEmision").toLocalDate());
                pedido.setEstado(EstadoPedidoCompra.valueOf(rs.getString("estado").toUpperCase()));
                pedido.setLinkSeguimiento(rs.getString("linkSeguimiento"));

                lista.add(pedido);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(PedidoCompra pedido) {
        String sql = "UPDATE pedidos_compra SET "
                + "idProveedor = ?, idUsuario = ?, fechaEmision = ?, estado = ?, linkSeguimiento = ? "
                + "WHERE idPedido = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, pedido.getIdProveedor());
            ps.setInt(2, pedido.getIdUsuario());
            ps.setDate(3, Date.valueOf(pedido.getFechaEmision()));
            ps.setString(4, pedido.getEstado().name());
            ps.setString(5, pedido.getLinkSeguimiento());
            ps.setInt(6, pedido.getIdPedido());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int idPedido) {
        String sql = "DELETE FROM pedidos_compra WHERE idPedido = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idPedido);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
}