package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Guarda descuentos aplicados al total de una venta en la misma transacción de cobro. */
public class VentaDescuentoDAO {
    public void guardar(Connection con, int idVenta, int idDescuento, double monto) throws SQLException {
        String sql = "INSERT INTO venta_descuento (idVenta, idDescuento, montoAplicado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, idDescuento);
            ps.setDouble(3, monto);
            ps.executeUpdate();
        }
    }
}
