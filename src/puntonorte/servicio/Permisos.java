package puntonorte.servicio;

import java.text.Normalizer;

/** Reglas de "quién puede qué", según lo que pidió el cliente en las entrevistas. */
public final class Permisos {

    private Permisos() {
    }

    /** Minúsculas y sin tildes, para que "Depósito" y "deposito" sean lo mismo. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    public static boolean esGerente(String rol) {
        return normalizar(rol).equals("gerente");
    }

    public static boolean esSupervisor(String rol) {
        return normalizar(rol).equals("supervisor");
    }

    public static boolean esSupervisorOGerente(String rol) {
        return esGerente(rol) || esSupervisor(rol);
    }

    public static boolean puedeGestionarUsuarios() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    public static boolean puedeGestionarDescuentos() {
        return esSupervisor(Sesion.getNombreRol());
    }

    public static boolean puedeVerHistorial() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    /** Altas, ediciones, cambios de precio y habilitar/inhabilitar productos y variantes. */
    public static boolean puedeGestionarCatalogo() {
        String rol = Sesion.getNombreRol();
        return esSupervisorOGerente(rol) || normalizar(rol).equals("encargado");
    }

    /** Costos y márgenes: solo Supervisor y Gerente (el Vendedor nunca los ve). */
    public static boolean puedeVerCostos() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    /** Ajustes manuales de stock: solo Supervisor y Gerente. */
    public static boolean puedeAjustarStock() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    /**
     * Mover mercadería entre depósito y salón (no cambia el total): Depósito, Encargado, Supervisor y Gerente.
     * El Vendedor no mueve stock.
     */
    public static boolean puedeTransferirStock() {
        String rol = Sesion.getNombreRol();
        return esSupervisorOGerente(rol)
                || normalizar(rol).equals("encargado")
                || normalizar(rol).contains("deposito");
    }

    /** Qué botones de la barra superior ve cada rol. */
    public static boolean puedeVerModulo(String modulo) {
        if (normalizar(Sesion.getNombreRol()).equals("vendedor")) {
            return modulo.equals("INICIO") || modulo.equals("PRODUCTOS") || modulo.equals("VENTAS");
        }
        if (modulo.equals("USUARIOS") || modulo.equals("HISTORIAL")) {
            return esSupervisorOGerente(Sesion.getNombreRol());
        }
        if (modulo.equals("PROVEEDORES")) {
            String rol = normalizar(Sesion.getNombreRol());
            return esSupervisorOGerente(rol) || rol.equals("encargado") || rol.contains("deposito");
        }
        return true;
    }

    /** Registrar ventas: Vendedor, Encargado, Supervisor y Gerente. El personal de Depósito NO vende. */
    public static boolean puedeVender() {
        String rol = normalizar(Sesion.getNombreRol());
        return rol.equals("vendedor") || rol.equals("encargado") || esSupervisorOGerente(rol);
    }

    /** Ver las ventas de todos: Encargado, Supervisor y Gerente. El Vendedor solo ve las suyas. */
    public static boolean puedeVerTodasLasVentas() {
        String rol = Sesion.getNombreRol();
        return esSupervisorOGerente(rol) || normalizar(rol).equals("encargado");
    }
}
