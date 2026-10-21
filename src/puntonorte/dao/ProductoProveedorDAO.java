package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.ProductoProveedor;

public class ProductoProveedorDAO {

    // INSERT
    public void guardar(ProductoProveedor pp) {
        String sql = "INSERT INTO producto_proveedor (idProducto, idProveedor) VALUES (?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, pp.getIdProducto());
            ps.setInt(2, pp.getIdProveedor());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<ProductoProveedor> listar() {
        ArrayList<ProductoProveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto_proveedor";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductoProveedor pp = new ProductoProveedor();
                pp.setIdProducto(rs.getInt("idProducto"));
                pp.setIdProveedor(rs.getInt("idProveedor"));
                lista.add(pp);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // SELECT filtrado: útil para "¿qué proveedores tiene este producto?"
    public ArrayList<ProductoProveedor> listarPorProducto(int idProducto) {
        ArrayList<ProductoProveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto_proveedor WHERE idProducto = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductoProveedor pp = new ProductoProveedor();
                pp.setIdProducto(rs.getInt("idProducto"));
                pp.setIdProveedor(rs.getInt("idProveedor"));
                lista.add(pp);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar por producto: " + e.getMessage());
        }

        return lista;
    }

    // No hay modificar(): las dos únicas columnas son la clave primaria,
    // no hay ningún otro campo que se pueda "actualizar".
    // Para cambiar una relación: eliminar() la vieja + guardar() la nueva.

    // DELETE — necesita los dos IDs, porque juntos son la clave primaria
    public void eliminar(int idProducto, int idProveedor) {
        String sql = "DELETE FROM producto_proveedor WHERE idProducto = ? AND idProveedor = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idProducto);
            ps.setInt(2, idProveedor);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
}