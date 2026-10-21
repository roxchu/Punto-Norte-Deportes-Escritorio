package puntonorte.servicio;

/** Error "esperable" (validación o permiso). El mensaje se le muestra tal cual al usuario. */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}