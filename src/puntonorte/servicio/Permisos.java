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

    /** Encargado, Supervisor y Gerente pueden resolver solicitudes operativas. */
    public static boolean puedeResolverSolicitudesOperativas(String rol) {
        return esSupervisorOGerente(rol) || normalizar(rol).equals("encargado");
    }

    public static boolean puedeGestionarUsuarios() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    public static boolean puedeGestionarDescuentos() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    public static boolean puedeVerHistorial() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    /** Altas, ediciones, cambios de precio y habilitar/inhabilitar productos y variantes. */
    public static boolean puedeGestionarCatalogo() {
        String rol = Sesion.getNombreRol();
        return esSupervisorOGerente(rol) || normalizar(rol).equals("encargado");
    }

    /** Precios de venta y costos comerciales: solo Supervisor y Gerente. */
    public static boolean puedeModificarPrecio() {
        return esSupervisorOGerente(Sesion.getNombreRol());
    }

    /** Alta de productos y variantes: requiere definir precios, por eso es administrativa. */
    public static boolean puedeCrearProducto() {
        return puedeModificarPrecio();
    }

    /** Proveedores y órdenes de compra: gestión administrativa; Encargado y Depósito pueden consultar/recibir. */
    public static boolean puedeGestionarCompras() {
        return esSupervisorOGerente(Sesion.getNombreRol());
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
        String rol = normalizar(Sesion.getNombreRol());
        if (rol.equals("vendedor"))
            return modulo.equals("INICIO") || modulo.equals("PRODUCTOS")
                    || modulo.equals("VENTAS") || modulo.equals("CUENTA");
        if (rol.contains("deposito"))
            return modulo.equals("INICIO") || modulo.equals("PRODUCTOS")
                    || modulo.equals("PROVEEDORES") || modulo.equals("CUENTA");
        if (rol.equals("encargado"))
            return modulo.equals("INICIO") || modulo.equals("PRODUCTOS")
                    || modulo.equals("PROVEEDORES") || modulo.equals("VENTAS") || modulo.equals("CUENTA");
        return esSupervisorOGerente(rol);
    }

    /** Según la entrevista, solo Vendedor registra ventas; los demás perfiles las consultan o supervisan. */
    public static boolean puedeVender() {
        String rol = normalizar(Sesion.getNombreRol());
        return rol.equals("vendedor");
    }

    /** Ver las ventas de todos: Encargado, Supervisor y Gerente. El Vendedor solo ve las suyas. */
    public static boolean puedeVerTodasLasVentas() {
        String rol = Sesion.getNombreRol();
        return esSupervisorOGerente(rol) || normalizar(rol).equals("encargado");
    }
}
