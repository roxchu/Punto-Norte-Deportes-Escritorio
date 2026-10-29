package puntonorte.servicio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import puntonorte.dao.RolDAO;
import puntonorte.dao.UsuarioDAO;
import puntonorte.modelo.EstadoUsuario;
import puntonorte.modelo.Rol;
import puntonorte.modelo.Usuario;

/**
 * Reglas de negocio del módulo Usuarios: login, solicitudes, aprobación y cambios de estado.
 * No conoce Swing. Las pantallas lo llaman y muestran el mensaje de ReglaNegocioException si algo falla.
 */
public class UsuarioService {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final String TABLA = "usuarios";
    private static final String CODIGO_PRUEBA = "123456";
    private static final java.util.Set<String> CUENTAS_PRUEBA = java.util.Set.of(
            "gerente.prueba@puntonorte.test",
            "nahuellatella@gmail.com",
            "supervisor@puntonorte.com");

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final EmailService emailService = new EmailService();

    private Map<Integer, String> cacheRoles;

    // ------------------------------------------------------------------ LOGIN

    public Usuario autenticar(String correo, String codigo) {

        if (correo == null || correo.isBlank() || codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException("Ingresá el correo y el código de acceso.");
        }

        ReglaNegocioException credenciales =
                new ReglaNegocioException("Correo o código de acceso incorrecto.");

        Usuario u = usuarioDAO.buscarPorCorreo(correo.trim());
        if (u == null) {
            auditoria.registrarSinUsuario("Intento de sesión fallido", TABLA,
                    "Correo no registrado: " + correo.trim());
            throw credenciales;
        }
        // Se registra cualquier intento fallido de una cuenta existente, incluso
        // si la cuenta todavía no puede ingresar por su estado.
        boolean codigoValido = esCuentaPrueba(u.getCorreo())
                ? CODIGO_PRUEBA.equals(codigo.trim())
                : Seguridad.coincide(u.getCorreo(), codigo, u.getPasswordHash());
        if (!codigoValido) {
            auditoria.registrarComo(u.getIdUsuario(), "Intento de sesión fallido", TABLA,
                    u.getIdUsuario(), null, null, null, "Código incorrecto");
            throw credenciales;
        }
        if (u.getActivo() == EstadoUsuario.PENDIENTE) {
            auditoria.registrarComo(u.getIdUsuario(), "Intento de sesión fallido", TABLA,
                    u.getIdUsuario(), null, null, null, "Cuenta pendiente de aprobación");
            throw new ReglaNegocioException("Tu solicitud todavía está pendiente de aprobación.");
        }
        if (u.getActivo() == EstadoUsuario.PAUSADO) {
            auditoria.registrarComo(u.getIdUsuario(), "Intento de sesión fallido", TABLA,
                    u.getIdUsuario(), null, null, null, "Cuenta pausada");
            throw new ReglaNegocioException("Tu cuenta está pausada. Consultá con un Supervisor.");
        }
        if (u.getActivo() == EstadoUsuario.DESACTIVADO) {
            auditoria.registrarComo(u.getIdUsuario(), "Intento de sesión fallido", TABLA,
                    u.getIdUsuario(), null, null, null, "Cuenta desactivada");
            throw new ReglaNegocioException("Tu cuenta está desactivada.");
        }

        Sesion.iniciar(u, nombreRol(u.getIdRol()));
        auditoria.registrar("Inicio de sesión", TABLA, u.getIdUsuario(), null, null, null,
                nombreCompleto(u));
        return u;
    }

    public void cerrarSesion() {
        if (Sesion.activa()) {
            auditoria.registrar("Cierre de sesión", TABLA, Sesion.getIdUsuario(), null, null, null,
                    Sesion.getNombreCompleto());
        }
        Sesion.cerrar();
    }

    // -------------------------------------------------------------- SOLICITUD

    public void solicitarCuenta(String dni, String telefono, String nombre, String apellido,
            String correo, String nombreRol) {

        dni = dni == null ? "" : dni.replaceAll("[.\\s-]", "");
        nombre = nombre == null ? "" : nombre.trim();
        apellido = apellido == null ? "" : apellido.trim();
        correo = correo == null ? "" : correo.trim();
        telefono = telefono == null ? "" : telefono.trim();

        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty()) {
            throw new ReglaNegocioException("DNI, nombre, apellido y correo son obligatorios.");
        }
        if (!dni.matches("\\d{7,8}")) {
            throw new ReglaNegocioException("El DNI debe tener 7 u 8 números.");
        }
        if (!CORREO.matcher(correo).matches()) {
            throw new ReglaNegocioException("El correo electrónico no es válido.");
        }
        if (!telefono.isEmpty() && !telefono.matches("[\\d\\s()+-]{6,20}")) {
            throw new ReglaNegocioException("El teléfono no es válido.");
        }

        Rol rol = rolPorNombre(nombreRol);
        if (!esRolSolicitable(rol.getNombreRol())) {
            throw new ReglaNegocioException("Ese rol no se puede solicitar.");
        }

        if (usuarioDAO.buscarPorCorreo(correo) != null) {
            throw new ReglaNegocioException("Ya existe una cuenta o solicitud con ese correo.");
        }
        for (Usuario existente : usuarioDAO.listar()) {
            String dniExistente = existente.getDni();
            if (dniExistente != null && dni.equals(dniExistente.replaceAll("[.\\s-]", ""))) {
                throw new ReglaNegocioException("Ya existe una cuenta o solicitud con ese DNI.");
            }
        }

        Usuario u = new Usuario();
        u.setIdRol(rol.getIdRol());
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setDni(dni);
        u.setTelefono(telefono);
        u.setCorreo(correo);
        u.setFechaIngreso(LocalDate.now());
        u.setPuestoUsername(rol.getNombreRol());
        u.setUsername(correo);
        u.setPasswordHash("");                 // todavía no tiene código
        u.setActivo(EstadoUsuario.PENDIENTE);
        usuarioDAO.guardar(u);

        // Los DAO no avisan si fallan, así que verificamos que quedó guardado.
        Usuario guardado = usuarioDAO.buscarPorCorreo(correo);
        if (guardado == null) {
            throw new ReglaNegocioException("No se pudo registrar la solicitud. Revisá la conexión con la base de datos.");
        }

        auditoria.registrarComo(guardado.getIdUsuario(), "Solicitud de cuenta", TABLA,
                guardado.getIdUsuario(), null, EstadoUsuario.PENDIENTE.name(), null,
                nombre + " " + apellido + " solicitó el rol " + rol.getNombreRol());
    }

    // ---------------------------------------------------------------- LISTADOS

    public ArrayList<Usuario> listarPorEstado(EstadoUsuario estado) {
        exigirGestionUsuarios();
        return usuarioDAO.listarPorEstado(estado);
    }

    public int contarPendientes() {
        if (!Permisos.puedeGestionarUsuarios()) {
            return 0;
        }
        return usuarioDAO.listarPorEstado(EstadoUsuario.PENDIENTE).size();
    }

    public String nombreRol(int idRol) {
        return roles().getOrDefault(idRol, "-");
    }

    /** Roles que una persona puede pedir al registrarse (nunca Supervisor ni Gerente). */
    public List<String> rolesSolicitables() {
        List<String> lista = new ArrayList<>();
        for (String nombre : roles().values()) {
            if (esRolSolicitable(nombre)) {
                lista.add(nombre);
            }
        }
        return lista;
    }

    /** Roles que el usuario actual puede asignar al aprobar, según su jerarquía. */
    public List<String> rolesAsignables() {
        List<String> lista = new ArrayList<>();
        for (String nombre : roles().values()) {
            if (puedeAsignarRol(nombre)) {
                lista.add(nombre);
            }
        }
        return lista;
    }

    /** Cambia el rol de un usuario activo desde el panel de usuarios. */
    public void cambiarRolUsuario(int idUsuario, String nombreRol) {
        exigirGestionUsuarios();
        Usuario u = buscarPorId(idUsuario);
        exigirPuedeAdministrar(u);
        if (u.getActivo() != EstadoUsuario.ACTIVO) {
            throw new ReglaNegocioException("Solo se puede cambiar el rol de usuarios activos.");
        }
        Rol rol = rolPorNombre(nombreRol);
        if (!puedeAsignarRol(rol.getNombreRol())) {
            throw new ReglaNegocioException("No podés asignar el rol " + rol.getNombreRol()
                    + " porque no está disponible para tu cuenta.");
        }
        String anterior = nombreRol(u.getIdRol());
        u.setIdRol(rol.getIdRol());
        u.setPuestoUsername(rol.getNombreRol());
        usuarioDAO.modificar(u);
        Usuario actualizado = buscarPorId(idUsuario);
        if (actualizado.getIdRol() != rol.getIdRol()) {
            throw new ReglaNegocioException("No se pudo guardar el nuevo rol de la solicitud.");
        }
        auditoria.registrar("Rol de usuario cambiado", TABLA, idUsuario, anterior,
                rol.getNombreRol(), null, nombreCompleto(u));
    }

    // ------------------------------------------------------------ OPERACIONES

    /** Aprueba una solicitud. Devuelve el código en claro, para mandarlo por mail (no se guarda). */
    public String aprobar(int idUsuario, String nombreRol) {
        exigirGestionUsuarios();
        Usuario u = buscarPorId(idUsuario);
        exigirPuedeAdministrar(u);

        if (u.getActivo() != EstadoUsuario.PENDIENTE) {
            throw new ReglaNegocioException("La solicitud ya no está pendiente.");
        }
        Rol rol = rolPorNombre(nombreRol);
        if (!puedeAsignarRol(rol.getNombreRol())) {
            throw new ReglaNegocioException("No podés asignar el rol " + rol.getNombreRol()
                    + " porque es igual o superior al tuyo.");
        }

        String rolAnterior = nombreRol(u.getIdRol());
        String codigo = esCuentaPrueba(u.getCorreo()) ? CODIGO_PRUEBA : Seguridad.generarCodigo();

        // No se activa la cuenta si no se logró entregar su código de acceso.
        if (!esCuentaPrueba(u.getCorreo())) {
            try {
                emailService.enviarCodigoAcceso(u.getCorreo(), codigo);
            } catch (Exception e) {
                throw new ReglaNegocioException(
                        "No se pudo enviar el código a " + u.getCorreo()
                        + ". La solicitud sigue pendiente. Revisá config.properties y volvé a intentar.");
            }
        }

        u.setIdRol(rol.getIdRol());
        u.setPuestoUsername(rol.getNombreRol());
        u.setPasswordHash(Seguridad.hash(u.getCorreo(), codigo));
        u.setFechaIngreso(LocalDate.now());
        u.setActivo(EstadoUsuario.ACTIVO);
        usuarioDAO.modificar(u);

        verificarEstado(idUsuario, EstadoUsuario.ACTIVO);

        auditoria.registrar("Solicitud aprobada", TABLA, idUsuario,
                "PENDIENTE (" + rolAnterior + ")", "ACTIVO (" + rol.getNombreRol() + ")", null,
                nombreCompleto(u));
        return codigo;
    }

    public void rechazar(int idUsuario, String motivo) {
        transicion(idUsuario, EnumSet.of(EstadoUsuario.PENDIENTE), EstadoUsuario.DESACTIVADO,
                "Solicitud rechazada", motivo, true);
    }

    public void pausar(int idUsuario, String motivo) {
        transicion(idUsuario, EnumSet.of(EstadoUsuario.ACTIVO), EstadoUsuario.PAUSADO,
                "Usuario pausado", motivo, true);
    }

    public void desactivar(int idUsuario, String motivo) {
        transicion(idUsuario, EnumSet.of(EstadoUsuario.ACTIVO, EstadoUsuario.PAUSADO),
                EstadoUsuario.DESACTIVADO, "Usuario desactivado", motivo, true);
    }

    /** Devuelve un código nuevo solo si el usuario nunca había recibido uno; si no, devuelve null. */
    public String reactivar(int idUsuario) {
        return transicion(idUsuario, EnumSet.of(EstadoUsuario.PAUSADO, EstadoUsuario.DESACTIVADO),
                EstadoUsuario.ACTIVO, "Usuario reactivado", null, false);
    }

    /** Genera un código nuevo para un usuario activo (por si el mail no llegó). Devuelve el código en claro. */
    public String reenviarCodigo(int idUsuario) {
        exigirGestionUsuarios();
        Usuario u = buscarPorId(idUsuario);
        exigirPuedeAdministrar(u);
        if (u.getActivo() != EstadoUsuario.ACTIVO) {
            throw new ReglaNegocioException("Solo se puede reenviar el código a usuarios activos.");
        }

        String codigo = esCuentaPrueba(u.getCorreo()) ? CODIGO_PRUEBA : Seguridad.generarCodigo();
        String hash = Seguridad.hash(u.getCorreo(), codigo);
        if (!esCuentaPrueba(u.getCorreo())) {
            try {
                emailService.enviarCodigoAcceso(u.getCorreo(), codigo);
            } catch (Exception e) {
                throw new ReglaNegocioException("No se pudo enviar el código. El código anterior sigue vigente.");
            }
        }
        usuarioDAO.actualizarCodigoAcceso(idUsuario, hash);

        if (!hash.equals(buscarPorId(idUsuario).getPasswordHash())) {
            throw new ReglaNegocioException("No se pudo guardar el nuevo código.");
        }
        auditoria.registrar("Código de acceso regenerado", TABLA, idUsuario, null, null, null,
                nombreCompleto(u));
        return codigo;
    }

    // ---------------------------------------------------------------- PRIVADOS

    private String transicion(int idUsuario, EnumSet<EstadoUsuario> permitidos, EstadoUsuario nuevo,
            String accion, String motivo, boolean motivoObligatorio) {

        exigirGestionUsuarios();
        if (motivoObligatorio && (motivo == null || motivo.isBlank())) {
            throw new ReglaNegocioException("Indicá el motivo.");
        }

        Usuario u = buscarPorId(idUsuario);
        exigirPuedeAdministrar(u);

        if (!permitidos.contains(u.getActivo())) {
            throw new ReglaNegocioException(
                    "El usuario está " + u.getActivo().name().toLowerCase()
                    + " y no se puede realizar esta acción.");
        }

        EstadoUsuario anterior = u.getActivo();
        usuarioDAO.cambiarEstado(idUsuario, nuevo);

        String codigo = null;
        if (nuevo == EstadoUsuario.ACTIVO
                && (u.getPasswordHash() == null || u.getPasswordHash().isBlank())) {
            codigo = esCuentaPrueba(u.getCorreo()) ? CODIGO_PRUEBA : Seguridad.generarCodigo();
            usuarioDAO.actualizarCodigoAcceso(idUsuario, Seguridad.hash(u.getCorreo(), codigo));
        }

        verificarEstado(idUsuario, nuevo);

        auditoria.registrar(accion, TABLA, idUsuario, anterior.name(), nuevo.name(),
                motivo == null || motivo.isBlank() ? null : motivo.trim(), nombreCompleto(u));
        return codigo;
    }

    private boolean esCuentaPrueba(String correo) {
        return correo != null && CUENTAS_PRUEBA.contains(correo.trim().toLowerCase(java.util.Locale.ROOT));
    }

    private void exigirGestionUsuarios() {
        if (!Permisos.puedeGestionarUsuarios()) {
            throw new ReglaNegocioException("No tenés permiso para gestionar usuarios.");
        }
    }

    /** Nadie toca su propia cuenta ni a usuarios de igual o mayor jerarquía. */
    private void exigirPuedeAdministrar(Usuario objetivo) {
        if (objetivo.getIdUsuario() == Sesion.getIdUsuario()) {
            throw new ReglaNegocioException("No podés modificar tu propia cuenta.");
        }
        int jerarquiaActual = jerarquia(Sesion.getNombreRol());
        int jerarquiaObjetivo = jerarquia(nombreRol(objetivo.getIdRol()));
        if (jerarquiaObjetivo >= jerarquiaActual && !Permisos.esGerente(Sesion.getNombreRol())) {
            throw new ReglaNegocioException("No podés modificar usuarios con un rol igual o superior al tuyo.");
        }
    }

    /** Los DAO no propagan errores de SQL, así que releemos para confirmar que el cambio se hizo. */
    private void verificarEstado(int idUsuario, EstadoUsuario esperado) {
        if (buscarPorId(idUsuario).getActivo() != esperado) {
            throw new ReglaNegocioException(
                    "No se pudo guardar el cambio. Revisá la consola y que la columna 'activo' acepte el estado "
                    + esperado.name() + ".");
        }
    }

    private Usuario buscarPorId(int idUsuario) {
        for (Usuario u : usuarioDAO.listar()) {
            if (u.getIdUsuario() == idUsuario) {
                return u;
            }
        }
        throw new ReglaNegocioException("El usuario ya no existe.");
    }

    private Rol rolPorNombre(String nombre) {
        String nombreNormalizado = Permisos.normalizar(nombre);
        for (Rol r : rolDAO.listar()) {
            String rolNormalizado = Permisos.normalizar(r.getNombreRol());
            if (rolNormalizado.equals(nombreNormalizado)
                    || (nombreNormalizado.contains("deposito") && rolNormalizado.contains("deposito"))) {
                return r;
            }
        }
        throw new ReglaNegocioException("El rol '" + nombre + "' no existe.");
    }

    private boolean esRolSolicitable(String nombreRol) {
        String n = Permisos.normalizar(nombreRol);
        return n.equals("vendedor") || n.contains("deposito");
    }

    private boolean puedeAsignarRol(String rolDestino) {
        if (!Permisos.puedeGestionarUsuarios()) return false;
        if (Permisos.esGerente(Sesion.getNombreRol()) && Permisos.esGerente(rolDestino)) return true;
        return jerarquia(rolDestino) < jerarquia(Sesion.getNombreRol());
    }

    private int jerarquia(String nombreRol) {
        String n = Permisos.normalizar(nombreRol);
        if (n.equals("gerente")) return 5;
        if (n.equals("supervisor")) return 4;
        if (n.equals("encargado")) return 3;
        if (n.equals("vendedor") || n.contains("deposito")) return 1;
        return Integer.MAX_VALUE;
    }

    private Map<Integer, String> roles() {
        if (cacheRoles == null) {
            cacheRoles = new LinkedHashMap<>();
            for (Rol r : rolDAO.listar()) {
                cacheRoles.put(r.getIdRol(), r.getNombreRol());
            }
        }
        return cacheRoles;
    }

    private String nombreCompleto(Usuario u) {
        return u.getNombre() + " " + u.getApellido();
    }
}
