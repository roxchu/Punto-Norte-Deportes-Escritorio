package puntonorte.modelo;

public class Producto {
    private int idProducto;
    private int idCategoria;
    private int idMarca;
    private String nombre;
    private String descripcion;
    private boolean activo = true; // false = inhabilitado (no se borra, queda el historial)

    public Producto() {
    }

    public Producto(int idProducto, int idCategoria, int idMarca, String nombre, String descripcion){
    this.idProducto = idProducto;
    this.idCategoria = idCategoria;
    this.idMarca = idMarca;
    this.nombre = nombre;
    this.descripcion = descripcion;
    }


	public int getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(int idProducto) {
		this.idProducto = idProducto;
	}

	public int getIdMarca() {
		return idMarca;
	}

	public void setIdMarca(int idMarca) {
		this.idMarca = idMarca;
	}

	public int getIdCategoria() {
		return idCategoria;
	}

	public void setIdCategoria(int idCategoria) {
		this.idCategoria = idCategoria;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}