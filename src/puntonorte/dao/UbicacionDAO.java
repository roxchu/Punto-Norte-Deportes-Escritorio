package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Ubicacion;

/** DAO de ubicaciones configurables; ya no está limitado a un enum de dos valores. */
public class UbicacionDAO {
    public ArrayList<Ubicacion> listar() {
        ArrayList<Ubicacion> lista = new ArrayList<>();
        String sql = "SELECT idUbicacion, nombre FROM ubicacion ORDER BY nombre";
        try (Connection con = ConexionBD.conectar(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.out.println("Error al listar ubicaciones: " + e.getMessage()); }
        return lista;
    }

    public int guardarSeguro(String nombre) throws SQLException {
        String sql = "INSERT INTO ubicacion (nombre) VALUES (?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) throw new SQLException("La BD no devolvió el id de la ubicación.");
                return claves.getInt(1);
            }
        }
    }

    public boolean existeNombre(String nombre) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ubicacion WHERE LOWER(nombre) = LOWER(?)")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    private Ubicacion mapear(ResultSet rs) throws SQLException {
        return new Ubicacion(rs.getInt("idUbicacion"), rs.getString("nombre"));
    }
}
