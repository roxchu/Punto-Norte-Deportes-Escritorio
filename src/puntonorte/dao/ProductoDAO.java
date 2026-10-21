package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Producto;
import puntonorte.modelo.ProductoVariante;

/**
 * Los productos NUNCA se borran: se inhabilitan con cambiarActivo().
 * Inhabilitar un producto no toca sus variantes: al estar el producto inactivo,
 * el catálogo oculta todo lo que cuelga de él, y al reactivarlo vuelve cada variante como estaba.
 */
public class ProductoDAO {

    // =================================================================== BÁSICOS

    // INSERT (la columna activo toma su valor por defecto = 1)
    public void guardar(Producto producto) {
        String sql = "INSERT INTO productos "
                + "(idCategoria, idMarca, nombre, descripcion) "
                + "VALUES (?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, producto.getIdCategoria());
            ps.setInt(2, producto.getIdMarca());
            ps.setString(3, producto.getNombre());
            ps.setString(4, producto.getDescripcion());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT (todos, activos e inactivos)
    public ArrayList<Producto> listar() {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";

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

    /** Productos activos para formularios con conexión explícita de errores SQL. */
    public ArrayList<Producto> listarActivosSeguro() throws SQLException {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE activo = 1 ORDER BY nombre";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // UPDATE (no toca la columna activo: para eso está cambiarActivo)
    public void modificar(Producto producto) {
        String sql = "UPDATE productos SET "
                + "idCategoria = ?, idMarca = ?, nombre = ?, descripcion = ? "
                + "WHERE idProducto = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, producto.getIdCategoria());
            ps.setInt(2, producto.getIdMarca());
            ps.setString(3, producto.getNombre());
            ps.setString(4, producto.getDescripcion());
            ps.setInt(5, producto.getIdProducto());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // (eliminar() se quitó a propósito: no se borran registros, se inhabilitan)

    // ================================================================ ESPECIALES

    /**
     * Guarda el producto y todas sus variantes en UNA transacción: o queda todo, o no queda nada.
     * Devuelve el idProducto generado y completa los ids en los objetos recibidos.
     * Si falla (por ejemplo SKU repetido, error 1062 de MySQL) lanza SQLException y no deja nada a medias.
     */
    public int guardarConVariantes(Producto producto, List<ProductoVariante> variantes, int idProveedor) throws SQLException {

        if (variantes == null || variantes.isEmpty()) {
            throw new IllegalArgumentException("Un producto necesita al menos una variante.");
        }

        String sql = "INSERT INTO productos (idCategoria, idMarca, nombre, descripcion, activo) "
                   + "VALUES (?, ?, ?, ?, ?)";

        ProductoVarianteDAO varianteDAO = new ProductoVarianteDAO();

        try (Connection con = ConexionBD.conectar()) {

            con.setAutoCommit(false);

            try {
                int idProducto;

                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, producto.getIdCategoria());
                    ps.setInt(2, producto.getIdMarca());
                    ps.setString(3, producto.getNombre());
                    ps.setString(4, producto.getDescripcion());
                    ps.setBoolean(5, producto.isActivo());
                    ps.executeUpdate();

                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException("La BD no devolvió el id del producto.");
                        }
                        idProducto = claves.getInt(1);
                    }
                }

                for (ProductoVariante v : variantes) {
                    v.setIdProducto(idProducto);
                    v.setIdVariante(varianteDAO.guardar(con, v));
                }

                try (PreparedStatement relacion = con.prepareStatement(
                        "INSERT INTO producto_proveedor (idProducto, idProveedor) VALUES (?, ?)")) {
                    relacion.setInt(1, idProducto);
                    relacion.setInt(2, idProveedor);
                    relacion.executeUpdate();
                }

                con.commit();
                producto.setIdProducto(idProducto);
                return idProducto;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Devuelve el producto o null si no existe. */
    public Producto buscarPorId(int idProducto) throws SQLException {
        String sql = "SELECT * FROM productos WHERE idProducto = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * ¿Ya hay otro producto con ese nombre y esa marca? (probable duplicado).
     * Al editar, pasar el id propio para no chocar consigo mismo (0 si es nuevo).
     */
    public boolean existeProducto(String nombre, int idMarca, int idProductoExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE nombre = ? AND idMarca = ? AND idProducto <> ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idMarca);
            ps.setInt(3, idProductoExcluido);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Inhabilita (false) o vuelve a habilitar (true). Devuelve false si no existía el producto. */
    public boolean cambiarActivo(int idProducto, boolean activo) throws SQLException {
        String sql = "UPDATE productos SET activo = ? WHERE idProducto = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    // ================================================================== PRIVADOS

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("idProducto"));
        producto.setIdCategoria(rs.getInt("idCategoria"));
        producto.setIdMarca(rs.getInt("idMarca"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setActivo(rs.getBoolean("activo"));
        return producto;
    }
}
