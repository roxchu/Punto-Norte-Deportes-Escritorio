package puntonorte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Auditoria;

public class AuditoriaDAO {

    // INSERT
    public void guardar(Auditoria auditoria) {
        String sql = "INSERT INTO auditoria "
                + "(idUsuario, usrAutoriza, fechaHora, accionRealizada, tablaAfectada, idRegistroAfectado, valorAnterior, valorNuevo, motivo, detalleCambio) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setObject(1, auditoria.getIdUsuario());
            ps.setObject(2, auditoria.getUsrAutoriza());
            ps.setTimestamp(3, Timestamp.valueOf(auditoria.getFechaHora()));
            ps.setString(4, auditoria.getAccionRealizada());
            ps.setString(5, auditoria.getTablaAfectada());
            ps.setInt(6, auditoria.getIdRegistroAfectado());
            ps.setString(7, auditoria.getValorAnterior());
            ps.setString(8, auditoria.getValorNuevo());
            ps.setString(9, auditoria.getMotivo());
            ps.setString(10, auditoria.getDetalleCambio());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }
    
    // SELECT
    public ArrayList<Auditoria> listar() {
        ArrayList<Auditoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM auditoria";

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
    public void modificar(Auditoria auditoria) {
        String sql = "UPDATE auditoria SET "
                + "idUsuario = ?, usrAutoriza = ?, fechaHora = ?, accionRealizada = ?, tablaAfectada = ?, idRegistroAfectado = ?, valorAnterior = ?, valorNuevo = ?, motivo = ?, detalleCambio = ? "
                + "WHERE idAuditoria = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, auditoria.getIdUsuario());
            ps.setInt(2, auditoria.getUsrAutoriza());
            ps.setTimestamp(3, Timestamp.valueOf(auditoria.getFechaHora()));
            ps.setString(4, auditoria.getAccionRealizada());
            ps.setString(5, auditoria.getTablaAfectada());
            ps.setInt(6, auditoria.getIdRegistroAfectado());
            ps.setString(7, auditoria.getValorAnterior());
            ps.setString(8, auditoria.getValorNuevo());
            ps.setString(9, auditoria.getMotivo());
            ps.setString(10, auditoria.getDetalleCambio());
            ps.setInt(11, auditoria.getIdAuditoria());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int id) {
        String sql = "DELETE FROM auditoria WHERE idAuditoria = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
 // Método de conveniencia: arma y guarda un registro de auditoría con lo mínimo necesario.
    // Pensado para llamarlo seguido (login, logout, cada edición) sin construir el objeto Auditoria a mano cada vez.
    public void registrar(int idUsuario, String accionRealizada, String tablaAfectada, String detalleCambio) {
        Auditoria auditoria = new Auditoria();
        auditoria.setIdUsuario(idUsuario);
        auditoria.setUsrAutoriza(idUsuario); // por defecto, el mismo usuario autoriza su propia acción
        auditoria.setFechaHora(java.time.LocalDateTime.now());
        auditoria.setAccionRealizada(accionRealizada);
        auditoria.setTablaAfectada(tablaAfectada);
        auditoria.setDetalleCambio(detalleCambio);
        auditoria.setMotivo(null);
        auditoria.setValorAnterior(null);
        auditoria.setValorNuevo(null);
        auditoria.setIdRegistroAfectado(0);

        guardar(auditoria);
    }

    // Lista filtrando por usuario / módulo / operación / fecha, todos opcionales (podés pasar null
    // en los que no quieras filtrar). Arma el WHERE de a poco según qué filtros vinieron con datos.
    public ArrayList<Auditoria> listarPorFiltros(String usuario, String modulo, String operacion, LocalDate fecha) {
        ArrayList<Auditoria> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT a.* FROM auditoria a LEFT JOIN usuarios u ON a.idUsuario = u.idUsuario WHERE 1=1");
        ArrayList<Object> valores = new ArrayList<>();

        if (usuario != null && !usuario.isBlank()) {
            String[] partes = usuario.trim().split("\\s+");
            for (String parte : partes) {
                sql.append(" AND (u.nombre LIKE ? OR u.apellido LIKE ? OR u.correo LIKE ?)");
                String comodin = "%" + parte + "%";
                valores.add(comodin);
                valores.add(comodin);
                valores.add(comodin);
            }
        }

        if (modulo != null && !modulo.isBlank()) {
            sql.append(" AND a.tablaAfectada = ?");
            valores.add(modulo);
        }

        if (operacion != null && !operacion.isBlank()) {
            sql.append(" AND a.accionRealizada LIKE ?");
            valores.add("%" + operacion + "%");
        }

        if (fecha != null) {
            sql.append(" AND DATE(a.fechaHora) = ?");
            valores.add(java.sql.Date.valueOf(fecha));
        }

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql.toString());

            for (int i = 0; i < valores.size(); i++) {
                ps.setObject(i + 1, valores.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar por filtros: " + e.getMessage());
        }

        return lista;
    }

    public ArrayList<Auditoria> listarPorUsuario(int idUsuario) {
        ArrayList<Auditoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM auditoria WHERE idUsuario = ? ORDER BY fechaHora DESC";
        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error al listar actividad del usuario: " + e.getMessage());
        }
        return lista;
    }

    // Método auxiliar: convierte una fila del ResultSet en un objeto Auditoria.
    private Auditoria mapear(ResultSet rs) throws SQLException {
        Auditoria auditoria = new Auditoria();
        auditoria.setIdAuditoria(rs.getInt("idAuditoria"));
        auditoria.setIdUsuario((Integer) rs.getObject("idUsuario"));
        auditoria.setUsrAutoriza(rs.getInt("usrAutoriza"));
        auditoria.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());
        auditoria.setAccionRealizada(rs.getString("accionRealizada"));
        auditoria.setTablaAfectada(rs.getString("tablaAfectada"));
        auditoria.setIdRegistroAfectado(rs.getInt("idRegistroAfectado"));
        auditoria.setValorAnterior(rs.getString("valorAnterior"));
        auditoria.setValorNuevo(rs.getString("valorNuevo"));
        auditoria.setMotivo(rs.getString("motivo"));
        auditoria.setDetalleCambio(rs.getString("detalleCambio"));
        return auditoria;
    }
}
