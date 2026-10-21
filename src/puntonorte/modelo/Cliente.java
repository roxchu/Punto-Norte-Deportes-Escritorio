package puntonorte.modelo;

public class Cliente {
	private int idCliente;
	private String nombre;
	private String apellido;
	private String telefono;
	private String correo;
	private String dni;
	
	public Cliente() {
		
	}
	
	public Cliente(int idCliente, String nombre, String apellido, String telefono, String correo, String dni){
	this.idCliente = idCliente;
	this.nombre = nombre;
	this.apellido = apellido;
	this.telefono = telefono;
	this.correo = correo;
	this.dni = dni;
	}

	public int getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
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

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}
}