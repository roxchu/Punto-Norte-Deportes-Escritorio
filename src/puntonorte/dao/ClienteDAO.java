package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Cliente;

/**
 * DAO de Cliente. Sigue el mismo recorrido visto en clase:
 * Cliente -> ClienteDAO -> SQL -> PreparedStatement -> MySQL
 *
 * Tabla supuesta: cliente(id_cliente, nombre, apellido, telefono, email, dni)
 * Ajustar nombres de tabla/columnas si en tu base son distintos.
 */
public class ClienteDAO {

    // INSERT
    public void guardar(Cliente cliente) {
        String sql = "INSERT INTO cliente "
                + "(nombre, apellido, telefono, correo, dni) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getDni());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar cliente: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<Cliente> listar() {
        ArrayList<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getInt("idCliente"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setApellido(rs.getString("apellido"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setCorreo(rs.getString("correo"));
                cliente.setDni(rs.getString("dni"));
                clientes.add(cliente);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
        }

        return clientes;
    }

    // UPDATE
    public void modificar(Cliente cliente) {
        String sql = "UPDATE cliente SET "
                + "nombre = ?, apellido = ?, telefono = ?, correo = ?, dni = ? "
                + "WHERE idCliente = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getDni());
            ps.setInt(6, cliente.getIdCliente());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar cliente: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int idCliente) {
        String sql = "DELETE FROM cliente WHERE idCliente = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, idCliente);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar cliente: " + e.getMessage());
        }
    }

    // ================================================================ ESPECIALES
    // Estos métodos LANZAN SQLException para que el servicio sepa si algo falló.

    /** Devuelve el cliente con ese DNI o null si no existe (usa la conexión que ya está abierta). */
    public Cliente buscarPorDni(Connection con, String dni) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE dni = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getInt("idCliente"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setApellido(rs.getString("apellido"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setCorreo(rs.getString("correo"));
                cliente.setDni(rs.getString("dni"));
                return cliente;
            }
        }
    }

    /** Inserta el cliente en una conexión ya abierta y devuelve el idCliente generado. */
    public int guardar(Connection con, Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, apellido, telefono, correo, dni) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getDni());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("La BD no devolvió el id del cliente.");
                }
                return claves.getInt(1);
            }
        }
    }
}