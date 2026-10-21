package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.EstadoUsuario;
import puntonorte.modelo.Usuario; 
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
 *   Objeto -> DAO -> SQL -> PreparedStatement -> MySQL   (guardar / modificar / eliminar)
 *   MySQL -> ResultSet -> Objeto -> ArrayList             (listar)
 */
public class UsuarioDAO {

    // INSERT
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios "
                + "(idRol, nombre, apellido, dni, telefono, correo, fechaIngreso, puestoUsername, username, passwordHash, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, usuario.getIdRol()); 
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getApellido());
            ps.setString(4, usuario.getDni());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getCorreo());
            ps.setDate(7, java.sql.Date.valueOf(usuario.getFechaIngreso()));    // LocalDate -> java.sql.Date
            ps.setString(8, usuario.getPuestoUsername());
            ps.setString(9, usuario.getUsername());
            ps.setString(10, usuario.getPasswordHash());
            ps.setString(11, usuario.getActivo().name());                       // enum -> String

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<Usuario> listar() {
        ArrayList<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(Usuario usuario) {
        String sql = "UPDATE usuarios SET "
                + "idRol = ?, nombre = ?, apellido = ?, dni = ?, telefono = ?, correo = ?, fechaIngreso = ?, puestoUsername = ?, username = ?, passwordHash = ?, activo = ? "
                + "WHERE idUsuario = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, usuario.getIdRol()); // TODO: reemplazar por getters reales
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getApellido());
            ps.setString(4, usuario.getDni());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getCorreo());
            ps.setDate(7, java.sql.Date.valueOf(usuario.getFechaIngreso()));
            ps.setString(8, usuario.getPuestoUsername());
            ps.setString(9, usuario.getUsername());
            ps.setString(10, usuario.getPasswordHash());
            ps.setString(11, usuario.getActivo().name());
            ps.setInt(12, usuario.getIdUsuario()); // TODO: getter del id real

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE idUsuario = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
 // LOGIN: valida correo + código de acceso. Devuelve el Usuario si coincide, o null si no existe / no coincide.
    public Usuario login(String correo, String codigoAcceso) {
        String sql = "SELECT * FROM usuarios WHERE correo = ? AND passwordHash = ?";
        Usuario usuario = null;

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, correo);
            ps.setString(2, codigoAcceso);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                usuario = mapear(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error al validar login: " + e.getMessage());
        }

        return usuario;
    }

    // Busca un usuario por correo (para chequear duplicados antes de crear una cuenta nueva)
    public Usuario buscarPorCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        Usuario usuario = null;

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, correo);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                usuario = mapear(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar por correo: " + e.getMessage());
        }

        return usuario;
    }

    // Cambia solo el estado del usuario (Pendiente / Activo / Pausado / Desactivado).
    // No pide el objeto completo porque los botones Pausar/Desactivar/Activar solo tienen el id a mano.
    public void cambiarEstado(int idUsuario, EstadoUsuario nuevoEstado) {
        String sql = "UPDATE usuarios SET activo = ? WHERE idUsuario = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idUsuario);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al cambiar estado: " + e.getMessage());
        }
    }

    // Lista los usuarios que están en un estado puntual. Cada pestaña del panel
    // (Solicitudes/Activos/Pausados/Desactivados) llama a este método con su estado.
    public ArrayList<Usuario> listarPorEstado(EstadoUsuario estado) {
        ArrayList<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE activo = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, estado.name());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar por estado: " + e.getMessage());
        }

        return lista;
    }

    // Genera un código de acceso aleatorio de 6 dígitos. No toca la BD:
    // es solo el "número", el guardado se hace aparte con actualizarCodigoAcceso().
    public String generarCodigoAcceso() {
        Random random = new Random();
        int numero = random.nextInt(1_000_000); // 0 a 999999
        return String.format("%06d", numero);   // completa con ceros a la izquierda (ej: "004821")
    }

    // Guarda en la BD el código recién generado (después de habérselo mandado por mail con EmailService)
    public void actualizarCodigoAcceso(int idUsuario, String nuevoCodigo) {
        String sql = "UPDATE usuarios SET passwordHash = ? WHERE idUsuario = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, nuevoCodigo);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al actualizar código de acceso: " + e.getMessage());
        }
    }

    // Método auxiliar: convierte una fila del ResultSet en un objeto Usuario.
    // Usado por listar(), login(), buscarPorCorreo() y listarPorEstado() para no repetir el mismo mapeo 4 veces.
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("idUsuario"));
        usuario.setIdRol(rs.getInt("idRol"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setDni(rs.getString("dni"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setFechaIngreso(rs.getDate("fechaIngreso").toLocalDate());
        usuario.setPuestoUsername(rs.getString("puestoUsername"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("passwordHash"));
        usuario.setActivo(EstadoUsuario.valueOf(rs.getString("activo").toUpperCase()));
        return usuario;
    }
}