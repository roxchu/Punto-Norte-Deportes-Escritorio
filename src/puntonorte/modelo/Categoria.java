package puntonorte.modelo;

public class Categoria {

    private int idCategoria;
    private String nombre;
    private Integer idCategoriaPadre; // Integer, no int: puede ser null (categoría raíz)

    public Categoria() {
    }

    public Categoria(int idCategoria, String nombre, Integer idCategoriaPadre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.idCategoriaPadre = idCategoriaPadre;
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

    public Integer getIdCategoriaPadre() {
        return idCategoriaPadre;
    }

    public void setIdCategoriaPadre(Integer idCategoriaPadre) {
        this.idCategoriaPadre = idCategoriaPadre;
    }
}