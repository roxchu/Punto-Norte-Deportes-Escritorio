package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.ProductoVariante;

/**
 * Las variantes NUNCA se borran: se inhabilitan con cambiarActivo().
 * Los métodos básicos (guardar / listar / modificar) quedan como estaban.
 * También resuelve la consulta del catálogo (buscarCatalogo).
 * Los métodos "especiales" de más abajo LANZAN SQLException para que el servicio sepa si algo falló.
 */
public class ProductoVarianteDAO {

    // =================================================================== BÁSICOS

    // INSERT (la columna activo toma su valor por defecto = 1)
    public void guardar(ProductoVariante productovariante) {
        String sql = "INSERT INTO producto_variantes "
                + "(idProducto, talle, color, sku, codigoBarras, precioVenta, costoRef, stockMinimo, stockMaximo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, productovariante.getIdProducto());
            ps.setString(2, productovariante.getTalle());
            ps.setString(3, productovariante.getColor());
            ps.setString(4, productovariante.getSKU());
            ps.setString(5, productovariante.getCodigoBarras());
            ps.setDouble(6, productovariante.getPrecioVenta());
            ps.setDouble(7, productovariante.getCostoRef());
            ps.setInt(8, productovariante.getStockMinimo());
            ps.setInt(9, productovariante.getStockMaximo());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT (todas, activas e inactivas)
    public ArrayList<ProductoVariante> listar() {
        ArrayList<ProductoVariante> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto_variantes";

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

    // UPDATE (no toca la columna activo: para eso está cambiarActivo)
    public void modificar(ProductoVariante productovariante) {
        String sql = "UPDATE producto_variantes SET "
                + "idProducto = ?, talle = ?, color = ?, sku = ?, codigoBarras = ?, precioVenta = ?, costoRef = ?, stockMinimo = ?, stockMaximo = ? "
                + "WHERE idVariante = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, productovariante.getIdProducto());
            ps.setString(2, productovariante.getTalle());
            ps.setString(3, productovariante.getColor());
            ps.setString(4, productovariante.getSKU());
            ps.setString(5, productovariante.getCodigoBarras());
            ps.setDouble(6, productovariante.getPrecioVenta());
            ps.setDouble(7, productovariante.getCostoRef());
            ps.setInt(8, productovariante.getStockMinimo());
            ps.setInt(9, productovariante.getStockMaximo());
            ps.setInt(10, productovariante.getIdVariante());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // (eliminar() se quitó a propósito: no se borran registros, se inhabilitan)

    // ================================================================ ESPECIALES

    /**
     * Inserta una variante usando una conexión que ya está abierta (la maneja quien llama),
     * para poder guardarla junto con su producto en una misma transacción.
     * Devuelve el idVariante generado.
     */
    public int guardar(Connection con, ProductoVariante v) throws SQLException {
        String sql = "INSERT INTO producto_variantes "
                + "(idProducto, talle, color, sku, codigoBarras, precioVenta, costoRef, stockMinimo, stockMaximo, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, v.getIdProducto());
            ps.setString(2, v.getTalle());
            ps.setString(3, v.getColor());
            ps.setString(4, v.getSKU());
            ps.setString(5, v.getCodigoBarras());
            ps.setDouble(6, v.getPrecioVenta());
            ps.setDouble(7, v.getCostoRef());
            ps.setInt(8, v.getStockMinimo());
            ps.setInt(9, v.getStockMaximo());
            ps.setBoolean(10, v.isActivo());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("La BD no devolvió el id de la variante.");
                }
                return claves.getInt(1);
            }
        }
    }

    /** Devuelve la variante o null si no existe. */
    public ProductoVariante buscarPorId(int idVariante) throws SQLException {
        return buscarUna("SELECT * FROM producto_variantes WHERE idVariante = ?", idVariante);
    }

    /** Devuelve la variante o null si no existe. */
    public ProductoVariante buscarPorSku(String sku) throws SQLException {
        return buscarUna("SELECT * FROM producto_variantes WHERE sku = ?", sku);
    }

    public ArrayList<ProductoVariante> listarPorProducto(int idProducto, boolean incluirInactivas)
            throws SQLException {

        String sql = "SELECT * FROM producto_variantes WHERE idProducto = ?"
                + (incluirInactivas ? "" : " AND activo = 1")
                + " ORDER BY talle, color";

        ArrayList<ProductoVariante> lista = new ArrayList<>();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** ¿Ya hay otra variante con este SKU? Al editar, pasar el id propio para no chocar consigo misma (0 si es nueva). */
    public boolean existeSku(String sku, int idVarianteExcluida) throws SQLException {
        return existe("sku", sku, idVarianteExcluida);
    }

    public boolean existeCodigoBarras(String codigoBarras, int idVarianteExcluida) throws SQLException {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return false;   // el código de barras es opcional
        }
        return existe("codigoBarras", codigoBarras, idVarianteExcluida);
    }

    /** Cambia solo el precio. Devuelve false si no existía la variante. */
    public boolean actualizarPrecio(int idVariante, double nuevoPrecio) throws SQLException {
        return actualizar("UPDATE producto_variantes SET precioVenta = ? WHERE idVariante = ?",
                nuevoPrecio, idVariante);
    }

    /** Inhabilita (false) o vuelve a habilitar (true). Devuelve false si no existía la variante. */
    public boolean cambiarActivo(int idVariante, boolean activo) throws SQLException {
        return actualizar("UPDATE producto_variantes SET activo = ? WHERE idVariante = ?",
                activo, idVariante);
    }

    // ============================================================ CATÁLOGO (consulta)

    /**
     * Devuelve las variantes listas para mostrar en la tabla: con nombre de producto, marca, categoría
     * y stock sumado por ubicación, todo en una sola consulta. Se filtra con ProductoVariante.Filtro.
     * Por defecto solo trae lo habilitado (producto Y variante activos).
     */
    public ArrayList<ProductoVariante> buscarCatalogo(ProductoVariante.Filtro filtro) throws SQLException {

        List<Object> parametros = new ArrayList<>();
        String sql = construirSql(filtro, parametros);

        ArrayList<ProductoVariante> lista = new ArrayList<>();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCatalogo(rs));
                }
            }
        }
        return lista;
    }

    /** Talles que existen en la BD (para el combo del filtro), en vez de tenerlos escritos a mano. */
    public ArrayList<String> listarTalles() throws SQLException {
        return listarDistintos("talle");
    }

    public ArrayList<String> listarColores() throws SQLException {
        return listarDistintos("color");
    }

    // ================================================================== PRIVADOS

    private ProductoVariante buscarUna(String sql, Object parametro) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** 'columna' viene siempre de este mismo archivo, nunca de lo que escribe el usuario. */
    private boolean existe(String columna, String valor, int idExcluida) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto_variantes WHERE " + columna + " = ? AND idVariante <> ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, valor);
            ps.setInt(2, idExcluida);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private boolean actualizar(String sql, Object valor, int idVariante) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, valor);
            ps.setInt(2, idVariante);
            return ps.executeUpdate() > 0;
        }
    }

    private ProductoVariante mapear(ResultSet rs) throws SQLException {
        ProductoVariante v = new ProductoVariante();
        v.setIdVariante(rs.getInt("idVariante"));
        v.setIdProducto(rs.getInt("idProducto"));
        v.setTalle(rs.getString("talle"));
        v.setColor(rs.getString("color"));
        v.setSKU(rs.getString("sku"));
        v.setCodigoBarras(rs.getString("codigoBarras"));
        v.setPrecioVenta(rs.getDouble("precioVenta"));
        v.setCostoRef(rs.getDouble("costoRef"));
        v.setStockMinimo(rs.getInt("stockMinimo"));
        v.setStockMaximo(rs.getInt("stockMaximo"));
        v.setActivo(rs.getBoolean("activo"));
        return v;
    }

    /** Visible dentro del paquete para poder probar el SQL armado. */
    static String construirSql(ProductoVariante.Filtro f, List<Object> parametros) {

        StringBuilder sql = new StringBuilder(
                "SELECT v.idVariante, p.idProducto, p.nombre, p.descripcion, "
              + "m.nombre AS marca, c.nombre AS categoria, "
              + "v.talle, v.color, v.sku, v.codigoBarras, v.precioVenta, v.costoRef, "
              + "v.stockMinimo, v.stockMaximo, "
              + "CASE WHEN v.activo = 1 AND p.activo = 1 THEN 1 ELSE 0 END AS activo, "
              + "COALESCE(SUM(s.cantidad), 0) AS stockTotal, "
              + "COALESCE(SUM(CASE WHEN u.nombre = 'SALON' THEN s.cantidad END), 0) AS stockSalon, "
              + "COALESCE(SUM(CASE WHEN u.nombre = 'DEPOSITO' THEN s.cantidad END), 0) AS stockDeposito "
              + "FROM producto_variantes v "
              + "JOIN productos p ON p.idProducto = v.idProducto "
              + "JOIN marca m ON m.idMarca = p.idMarca "
              + "JOIN categoria c ON c.idCategoria = p.idCategoria "
              + "LEFT JOIN stock s ON s.idVariante = v.idVariante "
              + "LEFT JOIN ubicacion u ON u.idUbicacion = s.idUbicacion ");

        List<String> condiciones = new ArrayList<>();

        // Por defecto solo se ve lo habilitado. Los inhabilitados se piden a propósito.
        if (f == null || !f.isIncluirInactivos()) {
            condiciones.add("v.activo = 1 AND p.activo = 1");
        }

        if (f != null) {

            if (hay(f.getTexto())) {
                condiciones.add("(p.nombre LIKE ? OR p.descripcion LIKE ? OR v.sku LIKE ? "
                              + "OR v.codigoBarras LIKE ? OR m.nombre LIKE ?)");
                String comodin = "%" + f.getTexto().trim() + "%";
                for (int i = 0; i < 5; i++) {
                    parametros.add(comodin);
                }
            }

            if (f.getIdsCategoria() != null && !f.getIdsCategoria().isEmpty()) {
                StringBuilder marcas = new StringBuilder();
                for (int i = 0; i < f.getIdsCategoria().size(); i++) {
                    marcas.append(i == 0 ? "?" : ", ?");
                    parametros.add(f.getIdsCategoria().get(i));
                }
                condiciones.add("p.idCategoria IN (" + marcas + ")");
            }

            if (hay(f.getTalle())) {
                condiciones.add("v.talle = ?");
                parametros.add(f.getTalle().trim());
            }

            if (hay(f.getColor())) {
                condiciones.add("v.color = ?");
                parametros.add(f.getColor().trim());
            }
        }

        if (!condiciones.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", condiciones)).append(' ');
        }

        // Se agrupa por todo lo que no es una suma, para que funcione también con ONLY_FULL_GROUP_BY.
        sql.append("GROUP BY v.idVariante, p.idProducto, p.nombre, p.descripcion, m.nombre, c.nombre, "
                 + "v.talle, v.color, v.sku, v.codigoBarras, v.precioVenta, v.costoRef, "
                 + "v.stockMinimo, v.stockMaximo, v.activo, p.activo ");

        // Los filtros por stock van en HAVING porque dependen de la suma.
        List<String> having = new ArrayList<>();
        if (f != null && f.isSoloStockBajo()) {
            having.add("COALESCE(SUM(s.cantidad), 0) <= v.stockMinimo");
        }
        if (f != null && f.isSoloConStock()) {
            having.add("COALESCE(SUM(s.cantidad), 0) > 0");
        }
        if (!having.isEmpty()) {
            sql.append("HAVING ").append(String.join(" AND ", having)).append(' ');
        }

        sql.append("ORDER BY p.nombre, v.talle, v.color");
        return sql.toString();
    }

    private ProductoVariante mapearCatalogo(ResultSet rs) throws SQLException {
        ProductoVariante v = new ProductoVariante();
        v.setIdVariante(rs.getInt("idVariante"));
        v.setIdProducto(rs.getInt("idProducto"));
        v.setNombreProducto(rs.getString("nombre"));
        v.setDescripcionProducto(rs.getString("descripcion"));
        v.setMarca(rs.getString("marca"));
        v.setCategoria(rs.getString("categoria"));
        v.setTalle(rs.getString("talle"));
        v.setColor(rs.getString("color"));
        v.setSKU(rs.getString("sku"));
        v.setCodigoBarras(rs.getString("codigoBarras"));
        v.setPrecioVenta(rs.getDouble("precioVenta"));
        v.setCostoRef(rs.getDouble("costoRef"));
        v.setStockMinimo(rs.getInt("stockMinimo"));
        v.setStockMaximo(rs.getInt("stockMaximo"));
        v.setActivo(rs.getInt("activo") == 1);
        v.setStockTotal(rs.getInt("stockTotal"));
        v.setStockSalon(rs.getInt("stockSalon"));
        v.setStockDeposito(rs.getInt("stockDeposito"));
        return v;
    }

    private ArrayList<String> listarDistintos(String columna) throws SQLException {
        // 'columna' viene siempre de este mismo archivo, nunca del usuario.
        String sql = "SELECT DISTINCT " + columna + " FROM producto_variantes "
                   + "WHERE " + columna + " IS NOT NULL AND " + columna + " <> '' ORDER BY " + columna;

        ArrayList<String> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString(1));
            }
        }
        return lista;
    }

    private static boolean hay(String texto) {
        return texto != null && !texto.isBlank();
    }
}