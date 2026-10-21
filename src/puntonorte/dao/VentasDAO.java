package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.EstadoVenta;
import puntonorte.modelo.MedioPago;
import puntonorte.modelo.Ventas;

/**
 * Cabecera de la venta. Las ventas NUNCA se borran: si hay un error se anulan (estado ANULADA)
 * y si el cliente devuelve algo queda en DEVUELTA, para conservar el historial.
 */
public class VentasDAO {

    private static final String COLUMNAS = "idCliente, idUsuario, idUbicacion, idDescuento, medioPago, "
            + "montoAjustePago, estado, montoRecibido, vuelto, observaciones, fechaHora, "
            + "subtotal, descuentoTotal, montoTotal";

    // =================================================================== BÁSICOS

    // INSERT (versión simple, con su propia conexión)
    public void guardar(Ventas venta) {
        try (Connection con = ConexionBD.conectar()) {
            guardar(con, venta);
        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<Ventas> listar() {
        ArrayList<Ventas> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas ORDER BY fechaHora DESC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

    // UPDATE
    public void modificar(Ventas venta) {
        String sql = "UPDATE ventas SET "
                + "idCliente = ?, idUsuario = ?, idUbicacion = ?, idDescuento = ?, medioPago = ?, "
                + "montoAjustePago = ?, estado = ?, montoRecibido = ?, vuelto = ?, observaciones = ?, "
                + "fechaHora = ?, subtotal = ?, descuentoTotal = ?, montoTotal = ? "
                + "WHERE idVenta = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            cargarParametros(ps, venta);
            ps.setInt(15, venta.getIdVenta());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // (eliminar() se quitó a propósito: las ventas no se borran, se anulan)

    // ================================================================ ESPECIALES
    // Estos métodos LANZAN SQLException para que el servicio sepa si algo falló.

    /**
     * Inserta la venta en una conexión ya abierta (la maneja quien llama) y devuelve el idVenta generado.
     * Así el servicio puede guardar cabecera + líneas + descuento de stock en UNA transacción.
     */
    public int guardar(Connection con, Ventas venta) throws SQLException {
        String sql = "INSERT INTO ventas (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(ps, venta);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("La BD no devolvió el id de la venta.");
                }
                return claves.getInt(1);
            }
        }
    }

    /** Devuelve la venta o null si no existe. */
    public Ventas buscarPorId(int idVenta) throws SQLException {
        String sql = "SELECT * FROM ventas WHERE idVenta = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Ventas entre dos fechas (desde inclusive, hasta exclusive), de la más nueva a la más vieja.
     * Si idUsuario no es null, solo las de ese vendedor (el Vendedor ve únicamente las suyas).
     */
    public ArrayList<Ventas> listarPorFechas(LocalDateTime desde, LocalDateTime hasta, Integer idUsuario)
            throws SQLException {

        String sql = "SELECT * FROM ventas WHERE fechaHora >= ? AND fechaHora < ? "
                + (idUsuario != null ? "AND idUsuario = ? " : "")
                + "ORDER BY fechaHora DESC";

        ArrayList<Ventas> lista = new ArrayList<>();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            if (idUsuario != null) {
                ps.setInt(3, idUsuario);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // ================================================================== PRIVADOS

    /** Carga los 14 parámetros comunes del INSERT y del UPDATE (en el mismo orden que COLUMNAS). */
    private void cargarParametros(PreparedStatement ps, Ventas v) throws SQLException {

        if (v.getIdCliente() != null) {
            ps.setInt(1, v.getIdCliente());
        } else {
            ps.setNull(1, Types.INTEGER);
        }

        ps.setInt(2, v.getIdUsuario());
        ps.setInt(3, v.getIdUbicacion());

        if (v.getIdDescuento() != null) {
            ps.setInt(4, v.getIdDescuento());
        } else {
            ps.setNull(4, Types.INTEGER);
        }

        ps.setString(5, v.getMedioPago().name());
        ps.setDouble(6, v.getMontoAjustePago());
        ps.setString(7, v.getEstado().name());

        if (v.getMontoRecibido() != null) {
            ps.setDouble(8, v.getMontoRecibido());
        } else {
            ps.setNull(8, Types.DECIMAL);
        }

        if (v.getVuelto() != null) {
            ps.setDouble(9, v.getVuelto());
        } else {
            ps.setNull(9, Types.DECIMAL);
        }

        ps.setString(10, v.getObservaciones());
        ps.setTimestamp(11, Timestamp.valueOf(v.getFechaHora()));
        ps.setDouble(12, v.getSubtotal());
        ps.setDouble(13, v.getDescuentoTotal());
        ps.setDouble(14, v.getMontoTotal());
    }

    private Ventas mapear(ResultSet rs) throws SQLException {
        Ventas venta = new Ventas();
        venta.setIdVenta(rs.getInt("idVenta"));

        int idCliente = rs.getInt("idCliente");
        venta.setIdCliente(rs.wasNull() ? null : idCliente);

        venta.setIdUsuario(rs.getInt("idUsuario"));
        venta.setIdUbicacion(rs.getInt("idUbicacion"));

        int idDescuento = rs.getInt("idDescuento");
        venta.setIdDescuento(rs.wasNull() ? null : idDescuento);

        venta.setMedioPago(MedioPago.valueOf(rs.getString("medioPago").toUpperCase()));
        venta.setMontoAjustePago(rs.getDouble("montoAjustePago"));
        venta.setEstado(EstadoVenta.valueOf(rs.getString("estado").toUpperCase()));

        double montoRecibido = rs.getDouble("montoRecibido");
        venta.setMontoRecibido(rs.wasNull() ? null : montoRecibido);

        double vuelto = rs.getDouble("vuelto");
        venta.setVuelto(rs.wasNull() ? null : vuelto);

        venta.setObservaciones(rs.getString("observaciones"));
        venta.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());
        venta.setSubtotal(rs.getDouble("subtotal"));
        venta.setDescuentoTotal(rs.getDouble("descuentoTotal"));
        venta.setMontoTotal(rs.getDouble("montoTotal"));
        return venta;
    }
}