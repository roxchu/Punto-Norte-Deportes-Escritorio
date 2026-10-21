package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Categoria;

public class CategoriaDAO {

    /** Alta usada por el servicio: propaga el error y devuelve el id generado. */
    public int guardarSeguro(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, idCategoriaPadre) VALUES (?, ?)";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre());
            if (categoria.getIdCategoriaPadre() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, categoria.getIdCategoriaPadre());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) throw new SQLException("La BD no devolvió el id de la categoría.");
                return claves.getInt(1);
            }
        }
    }

    public boolean existeNombre(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categoria WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    // INSERT
    public void guardar(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, idCategoriaPadre) VALUES (?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, categoria.getNombre());

            // idCategoriaPadre puede ser null (categoría raíz, sin padre)
            if (categoria.getIdCategoriaPadre() != null) {
                ps.setInt(2, categoria.getIdCategoriaPadre());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<Categoria> listar() {
        ArrayList<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categoria";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("idCategoria"));
                categoria.setNombre(rs.getString("nombre"));

                // Leer una columna que puede ser NULL:
                int padre = rs.getInt("idCategoriaPadre");
                categoria.setIdCategoriaPadre(rs.wasNull() ? null : padre);

                lista.add(categoria);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, idCategoriaPadre = ? WHERE idCategoria = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, categoria.getNombre());

            if (categoria.getIdCategoriaPadre() != null) {
                ps.setInt(2, categoria.getIdCategoriaPadre());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setInt(3, categoria.getIdCategoria());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int idCategoria) {
        String sql = "DELETE FROM categoria WHERE idCategoria = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idCategoria);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
}