package puntonorte.servicio;

import puntonorte.modelo.Usuario;

/** Guarda quién está logueado. Es la única fuente de verdad para "usuario actual". */
public final class Sesion {

    private static Usuario usuario;
    private static String nombreRol;

    private Sesion() {
    }

    public static void iniciar(Usuario u, String rol) {
        usuario = u;
        nombreRol = rol;
    }

    public static void cerrar() {
        usuario = null;
        nombreRol = null;
    }

    public static boolean activa() {
        return usuario != null;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static int getIdUsuario() {
        if (usuario == null) {
            throw new ReglaNegocioException("No hay una sesión iniciada.");
        }
        return usuario.getIdUsuario();
    }

    public static String getNombreRol() {
        return nombreRol == null ? "" : nombreRol;
    }

    public static String getNombreCompleto() {
        return usuario == null ? "" : usuario.getNombre() + " " + usuario.getApellido();
    }
}