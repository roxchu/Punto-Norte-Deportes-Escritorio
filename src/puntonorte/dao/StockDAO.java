package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Stock;

/**
 * Stock por variante y ubicación (una fila por cada combinación).
 * Las filas de stock NUNCA se borran: cuando no queda mercadería, la cantidad es 0.
 *
 * IMPORTANTE: para mover cantidades NO usar modificar() (pisa el valor y, si dos usuarios
 * trabajan a la vez, se pierde un movimiento). Usar ajustarStock() o transferir(), que suman o restan
 * de forma segura y nunca dejan la cantidad en negativo.
 */
public class StockDAO {

    // =================================================================== BÁSICOS

    // INSERT
    public void guardar(Stock stock) {
        String sql = "INSERT INTO stock "
                + "(idVariante, idUbicacion, cantidad) "
                + "VALUES (?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, stock.getIdVariante());
            ps.setInt(2, stock.getIdUbicacion());
            ps.setInt(3, stock.getCantidad());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<Stock> listar() {
        ArrayList<Stock> lista = new ArrayList<>();
        String sql = "SELECT * FROM stock";

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

    // UPDATE (pisa la cantidad: para movimientos usar ajustarStock / transferir)
    public void modificar(Stock stock) {
        String sql = "UPDATE stock SET "
                + "idVariante = ?, idUbicacion = ?, cantidad = ? "
                + "WHERE idStock = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, stock.getIdVariante());
            ps.setInt(2, stock.getIdUbicacion());
            ps.setInt(3, stock.getCantidad());
            ps.setInt(4, stock.getIdStock());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // (eliminar() se quitó a propósito: no se borran registros, el stock vacío queda en 0)

    // ================================================================ ESPECIALES
    // Estos métodos LANZAN SQLException para que el servicio sepa si algo falló.

    /** Cantidad de una variante en una ubicación. Devuelve 0 si todavía no hay fila. */
    public int obtenerCantidad(int idVariante, int idUbicacion) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) FROM stock WHERE idVariante = ? AND idUbicacion = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVariante);
            ps.setInt(2, idUbicacion);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Suma de todas las ubicaciones (salón + depósito). */
    public int obtenerStockTotal(int idVariante) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) FROM stock WHERE idVariante = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVariante);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public ArrayList<Stock> listarPorVariante(int idVariante) throws SQLException {
        String sql = "SELECT * FROM stock WHERE idVariante = ?";
        ArrayList<Stock> lista = new ArrayList<>();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVariante);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Suma (cantidad positiva) o resta (negativa) stock de una variante en una ubicación.
     * Bloquea la fila mientras trabaja, así dos usuarios a la vez no se pisan.
     *
     * @return la cantidad NUEVA que quedó, o -1 si el movimiento dejaría el stock en negativo
     *         (en ese caso no se cambia nada).
     */
    public int ajustarStock(int idVariante, int idUbicacion, int delta) throws SQLException {

        try (Connection con = ConexionBD.conectar()) {

            con.setAutoCommit(false);

            try {
                int nueva = aplicar(con, idVariante, idUbicacion, delta);

                if (nueva < 0) {
                    con.rollback();
                } else {
                    con.commit();
                }
                return nueva;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Mueve mercadería de una ubicación a otra (por ejemplo depósito -> salón) en UNA transacción:
     * o se descuenta y se suma, o no pasa nada.
     *
     * @return true si se hizo; false si en el origen no hay cantidad suficiente.
     */
    public boolean transferir(int idVariante, int idOrigen, int idDestino, int cantidad) throws SQLException {

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a transferir debe ser mayor a 0.");
        }
        if (idOrigen == idDestino) {
            throw new IllegalArgumentException("El origen y el destino no pueden ser el mismo.");
        }

        try (Connection con = ConexionBD.conectar()) {

            con.setAutoCommit(false);

            try {
                if (aplicar(con, idVariante, idOrigen, -cantidad) < 0) {
                    con.rollback();
                    return false;
                }
                aplicar(con, idVariante, idDestino, cantidad);

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Cuántas variantes habilitadas tienen el stock total en o por debajo de su mínimo (para el panel de inicio). */
    public int contarVariantesConStockBajo() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT v.idVariante FROM producto_variantes v "
                + "JOIN productos p ON p.idProducto = v.idProducto "
                + "LEFT JOIN stock s ON s.idVariante = v.idVariante "
                + "WHERE v.activo = 1 AND p.activo = 1 "
                + "GROUP BY v.idVariante, v.stockMinimo "
                + "HAVING COALESCE(SUM(s.cantidad), 0) <= v.stockMinimo) bajos";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }

    // ================================================================== PRIVADOS

    /**
     * Hace el movimiento dentro de una transacción que ya está abierta.
     * Devuelve la cantidad nueva, o -1 (sin tocar nada) si quedaría negativa.
     * Si todavía no existe la fila de esa variante en esa ubicación, la crea.
     */
    private int aplicar(Connection con, int idVariante, int idUbicacion, int delta) throws SQLException {

        String buscar = "SELECT idStock, cantidad FROM stock WHERE idVariante = ? AND idUbicacion = ? FOR UPDATE";

        boolean existe = false;
        int idStock = 0;
        int actual = 0;

        try (PreparedStatement ps = con.prepareStatement(buscar)) {
            ps.setInt(1, idVariante);
            ps.setInt(2, idUbicacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    existe = true;
                    idStock = rs.getInt(1);
                    actual = rs.getInt(2);
                }
            }
        }

        int nueva = actual + delta;
        if (nueva < 0) {
            return -1;
        }

        if (existe) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE stock SET cantidad = ? WHERE idStock = ?")) {
                ps.setInt(1, nueva);
                ps.setInt(2, idStock);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO stock (idVariante, idUbicacion, cantidad) VALUES (?, ?, ?)")) {
                ps.setInt(1, idVariante);
                ps.setInt(2, idUbicacion);
                ps.setInt(3, nueva);
                ps.executeUpdate();
            }
        }
        return nueva;
    }

    private Stock mapear(ResultSet rs) throws SQLException {
        Stock stock = new Stock();
        stock.setIdStock(rs.getInt("idStock"));
        stock.setIdVariante(rs.getInt("idVariante"));
        stock.setIdUbicacion(rs.getInt("idUbicacion"));
        stock.setCantidad(rs.getInt("cantidad"));
        return stock;
    }

    // ================================================== PARA OTRAS TRANSACCIONES (ventas)

    /**
     * Igual que ajustarStock(idVariante, idUbicacion, delta) pero usando una conexión que YA está abierta
     * y con autoCommit apagado (la maneja quien llama). No hace commit ni rollback: eso lo decide el servicio,
     * para que descontar stock + guardar la venta sea todo-o-nada.
     *
     * @return la cantidad nueva, o -1 (sin tocar nada) si el stock quedaría en negativo.
     */
    public int ajustarStock(Connection con, int idVariante, int idUbicacion, int delta) throws SQLException {
        return aplicar(con, idVariante, idUbicacion, delta);
    }
}