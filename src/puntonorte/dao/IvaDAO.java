package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import puntonorte.conexion.ConexionBD;

/** Consulta la alícuota general configurada en la base. */
public class IvaDAO {
    private static final double IVA_GENERAL_POR_DEFECTO = 21.0;

    public double porcentajeGeneral() {
        String sql = "SELECT porcentaje FROM porcentajes_iva WHERE activo = 1 AND porcentaje = 21.00 ORDER BY idIva LIMIT 1";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            return r.next() ? r.getDouble("porcentaje") : IVA_GENERAL_POR_DEFECTO;
        } catch (SQLException e) {
            // Permite abrir el formulario con la alícuota general argentina mientras se aplica la migración.
            return IVA_GENERAL_POR_DEFECTO;
        }
    }
}
