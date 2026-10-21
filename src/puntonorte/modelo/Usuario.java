package puntonorte.modelo;
import java.time.LocalDate;


public class Usuario{
    private int idUsuario;
    private int idRol;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String correo;
    private LocalDate fechaIngreso;
    private String puestoUsername;
    private String username;
    private String passwordHash;
    private EstadoUsuario activo;

    public Usuario() {
    }

    public Usuario(int idUsuario, int idRol, String nombre, String apellido, String dni, String telefono, String correo, LocalDate fechaIngreso, String puestoUsername, String username, String passwordHash, EstadoUsuario activo){
    this.idUsuario = idUsuario;
    this.idRol = idRol;
    this.nombre = nombre;
    this.apellido = apellido;
    this.dni = dni;
    this.telefono = telefono;
    this.correo = correo;
    this.fechaIngreso = fechaIngreso;
    this.puestoUsername = puestoUsername;
    this.username = username;
    this.passwordHash = passwordHash;
    this.activo = activo;
    }

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public int getIdRol() {
		return idRol;
	}

	public void setIdRol(int idRol) {
		this.idRol = idRol;
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

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
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

	public LocalDate getFechaIngreso() {
		return fechaIngreso;
	}

	public void setFechaIngreso(LocalDate fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}

	public String getPuestoUsername() {
		return puestoUsername;
	}

	public void setPuestoUsername(String puestoUsername) {
		this.puestoUsername = puestoUsername;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public EstadoUsuario getActivo() {
		return activo;
	}

	public void setActivo(EstadoUsuario activo) {
		this.activo = activo;
	}
}