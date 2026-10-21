package puntonorte.modelo;

public class Proveedor{
    private int idProveedor;
    private String nombre;
    private String razonSocial;
    private String CUIT;
    private String telefono;
    private String correo;

    public Proveedor(){
    }
 
    public Proveedor(int idProveedor, String nombre, String razonSocial, String CUIT, String telefono, String correo){
    this.idProveedor = idProveedor;
    this.nombre = nombre;
    this.razonSocial = razonSocial;
    this.CUIT = CUIT;
    this.telefono = telefono;
    this.correo = correo;
    }

	public int getIdProveedor() {
		return idProveedor;
	}

	public void setIdProveedor(int idProveedor) {
		this.idProveedor = idProveedor;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getCUIT() {
		return CUIT;
	}

	public void setCUIT(String cUIT) {
		CUIT = cUIT;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCorreo() {
		return correo;
	}

	@Override
	public String toString() { return nombre == null ? "Proveedor" : nombre; }

	public void setCorreo(String correo) {
		this.correo = correo;
	}
}
