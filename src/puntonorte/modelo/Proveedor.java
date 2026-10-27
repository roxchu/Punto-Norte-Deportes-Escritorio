package puntonorte.modelo;

public class Proveedor{
    private int idProveedor;
    private String nombre;
    private String razonSocial;
    private String CUIT;
    private String telefono;
    private String correo;
    private String condicionPago = "Contado";

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

    public Proveedor(int idProveedor, String nombre, String razonSocial, String CUIT, String telefono,
            String correo, String condicionPago) {
        this(idProveedor, nombre, razonSocial, CUIT, telefono, correo);
        this.condicionPago = condicionPago == null || condicionPago.isBlank() ? "Contado" : condicionPago;
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

	public String getCondicionPago() { return condicionPago; }
	public void setCondicionPago(String condicionPago) {
		this.condicionPago = condicionPago == null || condicionPago.isBlank() ? "Contado" : condicionPago.trim();
	}

	@Override
	public String toString() { return razonSocial == null || razonSocial.isBlank() ? "Proveedor" : razonSocial; }

	public void setCorreo(String correo) {
		this.correo = correo;
	}
}
