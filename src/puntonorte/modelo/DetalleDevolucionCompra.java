package puntonorte.modelo;

public class DetalleDevolucionCompra {
	private int idDetDevolucionCompra;
	private int idDevolucion;
	private int idVariante;
	private int cantidad;
	private String motivo;
	
	public DetalleDevolucionCompra() {
		
	}
	
	public DetalleDevolucionCompra(int idDetDevolucionCompra, int idDevolucion, int idVariante, int cantidad, String motivo) {
	this.idDetDevolucionCompra = idDetDevolucionCompra;
	this.idDevolucion = idDevolucion;
	this.idVariante = idVariante;
	this.cantidad = cantidad;
	this.motivo = motivo;
	}

	public int getIdDetDevolucCompra() {
		return idDetDevolucionCompra;
	}

	public void setIdDetDevolucCompra(int idDetDevolucCompra) {
		this.idDetDevolucionCompra = idDetDevolucCompra;
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

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
}