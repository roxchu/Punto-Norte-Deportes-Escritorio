package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Proveedor; // TODO: importar la clase real, ej: puntonorte.modelo.Producto

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
 *   proveedor -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> proveedor -> ArrayList             (listar)
 */
public class ProveedorDAO {

    /** Consulta segura usada por reglas de negocio que exigen un proveedor existente. */
    public boolean existe(int idProveedor) throws SQLException {
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement("SELECT 1 FROM proveedor WHERE idProveedor = ?")) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // INSERT
    public void guardar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedor "
                + "(razonSocial, nombre, cuit, telefono, correo, condicionPago) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, proveedor.getRazonSocial()); // TODO: reemplazar por getters reales
            ps.setString(2, proveedor.getNombre());
            ps.setString(3, proveedor.getCUIT());
            ps.setString(4, proveedor.getTelefono());
            ps.setString(5, proveedor.getCorreo());
            ps.setString(6, proveedor.getCondicionPago());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<Proveedor> listar() {
        ArrayList<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setIdProveedor(rs.getInt("idProveedor")); // TODO: reemplazar por setters reales
                proveedor.setRazonSocial(rs.getString("razonSocial"));
                proveedor.setNombre(rs.getString("nombre"));
                proveedor.setCUIT(rs.getString("cuit"));
                proveedor.setTelefono(rs.getString("telefono"));
                proveedor.setCorreo(rs.getString("correo"));
                proveedor.setCondicionPago(rs.getString("condicionPago"));
                lista.add(proveedor);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(Proveedor proveedor) {
        String sql = "UPDATE proveedor SET "
                + "razonSocial = ?, nombre = ?, cuit = ?, telefono = ?, correo = ?, condicionPago = ? "
                + "WHERE idProveedor = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, proveedor.getRazonSocial()); // TODO: reemplazar por getters reales
            ps.setString(2, proveedor.getNombre());
            ps.setString(3, proveedor.getCUIT());
            ps.setString(4, proveedor.getTelefono());
            ps.setString(5, proveedor.getCorreo());
            ps.setString(6, proveedor.getCondicionPago());
            ps.setInt(7, proveedor.getIdProveedor()); // TODO: getter del id real

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE idProveedor = ?";

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


