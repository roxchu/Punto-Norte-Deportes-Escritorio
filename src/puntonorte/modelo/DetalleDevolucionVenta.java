package puntonorte.modelo;

public class DetalleDevolucionVenta {
	private int idDetDevolucion;
	private int idDevolucion;
	private int idVariante;
	private int idUbicacion;
	private int cantidad;
	
	public DetalleDevolucionVenta(){
		
	}
	
	public DetalleDevolucionVenta(int idDetDevolucion, int idDevolucion, int idVariante, int idUbicacion, int cantidad){
	this.idDetDevolucion = idDetDevolucion;
	this.idDevolucion = idDevolucion;
	this.idVariante = idVariante;
	this.idUbicacion = idUbicacion;
	this.cantidad = cantidad;
	}

	public int getIdDetDevolucion() {
		return idDetDevolucion;
	}

	public void setIdDetDevolucion(int idDetDevolucion) {
		this.idDetDevolucion = idDetDevolucion;
	}

	public int getIdDevolucion() {
		return idDevolucion;
	}

	public void setIdDevolucion(int idDevolucion) {
		this.idDevolucion = idDevolucion;
	}

	public int getIdVariante() {
		return idVariante;
	}

	public void setIdVariante(int idVariante) {
		this.idVariante = idVariante;
	}

	public int getIdUbicacion() {
		return idUbicacion;
	}

	public void setIdUbicacion(int idUbicacion) {
		this.idUbicacion = idUbicacion;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
}