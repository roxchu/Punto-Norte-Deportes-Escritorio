package puntonorte.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import puntonorte.dao.AuditoriaDAO;
import puntonorte.dao.RolDAO;
import puntonorte.dao.UsuarioDAO;
import puntonorte.modelo.Auditoria;
import puntonorte.modelo.Rol;
import puntonorte.modelo.Usuario;

/** Único punto por el que se escribe y se lee la auditoría. */
public class AuditoriaService {

    /** Una fila ya lista para mostrar en la tabla del historial. */
    public static class Fila {
        public final LocalDateTime fecha;
        public final String usuario;
        public final String rol;
        public final String operacion;
        public final String modulo;
        public final String detalle;

        Fila(LocalDateTime fecha, String usuario, String rol, String operacion, String modulo, String detalle) {
            this.fecha = fecha;
            this.usuario = usuario;
            this.rol = rol;
            this.operacion = operacion;
            this.modulo = modulo;
            this.detalle = detalle;
        }
    }

    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    /** Registra una acción hecha por el usuario que tiene la sesión abierta. */
    public void registrar(String accion, String tabla, int idRegistro,
            String valorAnterior, String valorNuevo, String motivo, String detalle) {
        registrarComo(Sesion.getIdUsuario(), accion, tabla, idRegistro, valorAnterior, valorNuevo, motivo, detalle);
    }

    /** Igual, pero indicando el usuario (para el login fallido o la solicitud, donde no hay sesión). */
    public void registrarComo(int idUsuario, String accion, String tabla, int idRegistro,
            String valorAnterior, String valorNuevo, String motivo, String detalle) {
        Auditoria a = new Auditoria();
        a.setIdUsuario(idUsuario);
        a.setUsrAutoriza(idUsuario);
        a.setFechaHora(LocalDateTime.now());
        a.setAccionRealizada(accion);
        a.setTablaAfectada(tabla);
        a.setIdRegistroAfectado(idRegistro);
        a.setValorAnterior(valorAnterior);
        a.setValorNuevo(valorNuevo);
        a.setMotivo(motivo);
        a.setDetalleCambio(detalle);
        auditoriaDAO.guardar(a);
    }

    /** Registra intentos cuyo correo no pertenece a una cuenta. */
    public void registrarSinUsuario(String accion, String tabla, String detalle) {
        Auditoria a = new Auditoria();
        a.setIdUsuario(null);
        a.setUsrAutoriza(null);
        a.setFechaHora(LocalDateTime.now());
        a.setAccionRealizada(accion);
        a.setTablaAfectada(tabla);
        a.setIdRegistroAfectado(0);
        a.setDetalleCambio(detalle);
        auditoriaDAO.guardar(a);
    }

    public List<Fila> listar(String usuario, String modulo, String operacion, LocalDate fecha) {
        if (!Permisos.puedeVerHistorial()) {
            throw new ReglaNegocioException("No tenés permiso para ver el historial.");
        }
        List<Auditoria> registros = auditoriaDAO.listarPorFiltros(usuario, modulo, operacion, fecha);
        return convertir(registros);
    }

    public List<Fila> ultimas(int cantidad) {
        List<Fila> todas = listar(null, null, null, null);
        return todas.size() > cantidad ? todas.subList(0, cantidad) : todas;
    }

    /** Actividad propia para el panel de inicio, también disponible para el rol Vendedor. */
    public List<Fila> ultimasDeSesion(int cantidad) {
        if (!Sesion.activa()) throw new ReglaNegocioException("No hay una sesión iniciada.");
        List<Fila> propias = convertir(auditoriaDAO.listarPorUsuario(Sesion.getIdUsuario()));
        return propias.size() > cantidad ? propias.subList(0, cantidad) : propias;
    }

    private List<Fila> convertir(List<Auditoria> registros) {
        Map<Integer, Usuario> usuarios = new HashMap<>();
        for (Usuario u : usuarioDAO.listar()) {
            usuarios.put(u.getIdUsuario(), u);
        }
        Map<Integer, String> roles = new HashMap<>();
        for (Rol r : rolDAO.listar()) {
            roles.put(r.getIdRol(), r.getNombreRol());
        }

        List<Auditoria> ordenadas = new ArrayList<>(registros);
        ordenadas.sort(Comparator.comparing(Auditoria::getFechaHora).reversed());

        List<Fila> filas = new ArrayList<>();
        for (Auditoria a : ordenadas) {
            Usuario u = usuarios.get(a.getIdUsuario());
            String nombre = u == null ? "(sin cuenta)" : u.getNombre() + " " + u.getApellido();
            String rol = u == null ? "-" : roles.getOrDefault(u.getIdRol(), "-");
            filas.add(new Fila(a.getFechaHora(), nombre, rol, a.getAccionRealizada(),
                    capitalizar(a.getTablaAfectada()), armarDetalle(a)));
        }
        return filas;
    }

    private String armarDetalle(Auditoria a) {
        StringBuilder sb = new StringBuilder();
        if (a.getDetalleCambio() != null && !a.getDetalleCambio().isBlank()) {
            sb.append(a.getDetalleCambio());
        }
        if (a.getValorAnterior() != null || a.getValorNuevo() != null) {
            sb.append(" · ")
              .append(a.getValorAnterior() == null ? "-" : a.getValorAnterior())
              .append(" → ")
              .append(a.getValorNuevo() == null ? "-" : a.getValorNuevo());
        }
        if (a.getMotivo() != null && !a.getMotivo().isBlank()) {
            sb.append(" · Motivo: ").append(a.getMotivo());
        }
        return sb.toString();
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "-";
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
