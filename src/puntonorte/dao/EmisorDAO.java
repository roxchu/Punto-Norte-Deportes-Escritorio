package puntonorte.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Emisor;

/** Lee los datos fijos del emisor (una sola fila, idEmisor = 1). */
public class EmisorDAO {

    /** Devuelve el emisor o null si la tabla está vacía. */
    public Emisor obtener() throws SQLException {
        String sql = "SELECT * FROM emisor_config WHERE idEmisor = 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }
            Emisor e = new Emisor();
            e.setRazonSocial(rs.getString("razonSocial"));
            e.setCuit(rs.getString("cuit"));
            e.setCondicionIva(rs.getString("condicionIva"));
            e.setDomicilioComercial(rs.getString("domicilioComercial"));
            e.setIngresosBrutos(rs.getString("ingresosBrutos"));
            Date inicio = rs.getDate("inicioActividades");
            e.setInicioActividades(inicio == null ? null : inicio.toLocalDate());
            e.setPuntoVenta(rs.getInt("puntoVenta"));
            return e;
        }
    }
}