package puntonorte.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.CondicionIva;
import puntonorte.modelo.DatosFiscalesVenta;

/** Columnas de facturación de la tabla ventas (tipo, receptor, neto, IVA, CAE...). */
public class ComprobanteDAO {

    /**
     * Siguiente número de comprobante para un tipo y punto de venta (1, 2, 3...). Usa FOR UPDATE
     * para que dos ventas simultáneas no tomen el mismo número. Debe llamarse con la conexión de la
     * transacción de cobro.
     */
    public int siguienteNumero(Connection con, String tipoComprobante, int puntoVenta) throws SQLException {
        String sql = "SELECT COALESCE(MAX(numeroComprobante), 0) FROM ventas "
                + "WHERE tipoComprobante = ? AND puntoVenta = ? FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipoComprobante);
            ps.setInt(2, puntoVenta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) + 1;
            }
        }
    }

    /**
     * Completa los datos fiscales de una venta recién insertada, usando la MISMA conexión de la
     * transacción de cobro: si algo falla, se deshace todo junto con la venta.
     */
    public void guardarComprobante(Connection con, int idVenta, DatosFiscalesVenta d) throws SQLException {
        String sql = "UPDATE ventas SET tipoComprobante = ?, estadoFactura = ?, puntoVenta = ?, "
                + "numeroComprobante = ?, condicionIvaReceptor = ?, receptorNombre = ?, receptorTipoDoc = ?, "
                + "receptorNumeroDoc = ?, receptorDomicilio = ?, montoNeto = ?, montoIva = ?, cae = ?, "
                + "vencimientoCae = ? WHERE idVenta = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getTipoComprobante());
            ps.setString(2, d.getEstadoFactura());
            ps.setInt(3, d.getPuntoVenta());
            ps.setInt(4, d.getNumeroComprobante());
            ps.setString(5, d.getCondicionIva().name());
            ps.setString(6, d.getReceptorNombre());
            ps.setString(7, d.getReceptorTipoDoc());
            ps.setString(8, d.getReceptorNumeroDoc());
            ps.setString(9, d.getReceptorDomicilio());
            ps.setDouble(10, d.getMontoNeto());
            ps.setDouble(11, d.getMontoIva());
            ps.setString(12, d.getCae());
            ps.setDate(13, Date.valueOf(d.getVencimientoCae()));
            ps.setInt(14, idVenta);
            ps.executeUpdate();
        }
    }

    /**
     * Última Factura A emitida a ese CUIT: sirve para autocompletar razón social y domicilio
     * cuando el mismo cliente vuelve a comprar. Devuelve null si nunca se le facturó.
     */
    public DatosFiscalesVenta buscarUltimoReceptorPorCuit(String cuitNormalizado) throws SQLException {
        String sql = "SELECT receptorNombre, receptorDomicilio FROM ventas "
                + "WHERE tipoComprobante = 'A' AND receptorTipoDoc = 'CUIT' AND receptorNumeroDoc = ? "
                + "ORDER BY idVenta DESC LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cuitNormalizado);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DatosFiscalesVenta d = new DatosFiscalesVenta();
                d.setReceptorNombre(rs.getString("receptorNombre"));
                d.setReceptorDomicilio(rs.getString("receptorDomicilio"));
                return d;
            }
        }
    }

    /** Datos fiscales de una venta, o null si la venta no existe. */
    public DatosFiscalesVenta buscarPorVenta(int idVenta) throws SQLException {
        String sql = "SELECT tipoComprobante, estadoFactura, puntoVenta, numeroComprobante, condicionIvaReceptor, "
                + "receptorNombre, receptorTipoDoc, receptorNumeroDoc, receptorDomicilio, montoNeto, montoIva, cae, vencimientoCae "
                + "FROM ventas WHERE idVenta = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DatosFiscalesVenta d = new DatosFiscalesVenta();
                d.setTipoComprobante(rs.getString("tipoComprobante"));
                d.setEstadoFactura(rs.getString("estadoFactura"));

                int puntoVenta = rs.getInt("puntoVenta");
                d.setPuntoVenta(rs.wasNull() ? null : puntoVenta);

                int numero = rs.getInt("numeroComprobante");
                d.setNumeroComprobante(rs.wasNull() ? null : numero);

                String condicion = rs.getString("condicionIvaReceptor");
                d.setCondicionIva(condicion == null ? null : CondicionIva.valueOf(condicion));

                d.setReceptorNombre(rs.getString("receptorNombre"));
                d.setReceptorTipoDoc(rs.getString("receptorTipoDoc"));
                d.setReceptorNumeroDoc(rs.getString("receptorNumeroDoc"));
                d.setReceptorDomicilio(rs.getString("receptorDomicilio"));

                double neto = rs.getDouble("montoNeto");
                d.setMontoNeto(rs.wasNull() ? null : neto);

                double iva = rs.getDouble("montoIva");
                d.setMontoIva(rs.wasNull() ? null : iva);

                d.setCae(rs.getString("cae"));
                Date vencimiento = rs.getDate("vencimientoCae");
                d.setVencimientoCae(vencimiento == null ? null : vencimiento.toLocalDate());
                return d;
            }
        }
    }
}
