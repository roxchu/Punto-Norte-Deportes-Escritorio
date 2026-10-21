package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.DetalleVenta;

/**
 * Líneas de una venta. Nunca se borran ni se modifican: son el comprobante histórico
 * (el precio queda congelado en precioHistorico).
 *
 * Recorrido:
 *   DetalleVenta -> DAO -> SQL -> PreparedStatement -> MySQL        (guardar)
 *   MySQL -> ResultSet -> DetalleVenta -> ArrayList                 (listar)
 */
public class DetalleVentaDAO {

    // =================================================================== BÁSICOS

    // INSERT (versión simple, con su propia conexión)
    public void guardar(DetalleVenta d) {
        try (Connection con = ConexionBD.conectar()) {
            guardar(con, d);
        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<DetalleVenta> listar() {
        ArrayList<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_ventas";

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

    // (modificar() y eliminar() se quitaron a propósito: el comprobante no se toca.
    //  Si hay un error se hace una anulación o una devolución, no se edita la línea.)

    // ================================================================ ESPECIALES
    // Estos métodos LANZAN SQLException para que el servicio sepa si algo falló.

    /**
     * Inserta una línea usando una conexión ya abierta (la maneja quien llama), para que
     * la venta, sus líneas y el descuento de stock se confirmen juntos o no se confirme nada.
     */
    public void guardar(Connection con, DetalleVenta d) throws SQLException {
        String sql = "INSERT INTO detalle_ventas "
                + "(idVenta, idVariante, idDescuento, cantidad, precioHistorico, montoDescuentoAplicado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, d.getIdVenta());
            ps.setInt(2, d.getIdVariante());

            if (d.getIdDescuento() != null) {
                ps.setInt(3, d.getIdDescuento());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, d.getCantidad());
            ps.setDouble(5, d.getPrecioHistorico());
            ps.setDouble(6, d.getMontoDescuentoAplicado());
            ps.executeUpdate();
        }
    }

    /** Todas las líneas de una venta (para el comprobante y para las devoluciones). */
    public ArrayList<DetalleVenta> listarPorVenta(int idVenta) throws SQLException {
        String sql = "SELECT * FROM detalle_ventas WHERE idVenta = ? ORDER BY idDetVenta";
        ArrayList<DetalleVenta> lista = new ArrayList<>();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // ================================================================== PRIVADOS

    private DetalleVenta mapear(ResultSet rs) throws SQLException {
        DetalleVenta d = new DetalleVenta();
        d.setIdDetVenta(rs.getInt("idDetVenta"));
        d.setIdVenta(rs.getInt("idVenta"));
        d.setIdVariante(rs.getInt("idVariante"));

        int idDescuento = rs.getInt("idDescuento");
        d.setIdDescuento(rs.wasNull() ? null : idDescuento);

        d.setCantidad(rs.getInt("cantidad"));
        d.setPrecioHistorico(rs.getDouble("precioHistorico"));
        d.setMontoDescuentoAplicado(rs.getDouble("montoDescuentoAplicado"));
        return d;
    }
}