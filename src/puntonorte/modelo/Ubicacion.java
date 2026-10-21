package puntonorte.modelo;

/** Ubicación física configurable (por ejemplo, Salón, Depósito central o Depósito 2). */
public class Ubicacion {
    private int idUbicacion;
    private String nombre;

    public Ubicacion() { }
    public Ubicacion(int idUbicacion, String nombre) { this.idUbicacion = idUbicacion; this.nombre = nombre; }
    public int getIdUbicacion() { return idUbicacion; }
    public void setIdUbicacion(int idUbicacion) { this.idUbicacion = idUbicacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    @Override public String toString() { return nombre; }
}
