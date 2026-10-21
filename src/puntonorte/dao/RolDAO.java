package puntonorte.dao;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Rol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RolDAO {

    // INSERT
    public void guardar(Rol rol) {
        String sql = "INSERT INTO roles (nombreRol, descripcion) VALUES (?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rol.getNombreRol());
            ps.setString(2, rol.getDescripcion());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // SELECT
    public ArrayList<Rol> listar() {
        ArrayList<Rol> lista = new ArrayList<>();
        String sql = "SELECT * FROM roles";

        try (Connection con = ConexionBD.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // UPDATE
    public void modificar(Rol rol) {
        String sql = "UPDATE roles SET nombreRol = ?, descripcion = ? WHERE idRol = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rol.getNombreRol());
            ps.setString(2, rol.getDescripcion());
            ps.setInt(3, rol.getIdRol());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void eliminar(int idRol) {
        String sql = "DELETE FROM roles WHERE idRol = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    // Busca un rol por su id. Se usa para mostrar el nombre del rol ("Supervisor", "Gerente")
    // en pantallas que solo tienen el idRol a mano (como la tabla de usuarios), sin escribir un JOIN.
    }
    public Rol buscarPorId(int idRol) {
        String sql = "SELECT * FROM roles WHERE idRol = ?";
        Rol rol = null;

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rol = mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rol;
    }

    // Método auxiliar: convierte una fila del ResultSet en un objeto Rol.
    private Rol mapear(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("idRol"));
        rol.setNombreRol(rs.getString("nombreRol"));
        rol.setDescripcion(rs.getString("descripcion"));
        return rol;
    }
}