package puntonorte.modelo;

public class DetalleRecepcion {
	private int idDetRecepcion;
	private int idRecepcion;
	private int idVariante;
	private int cantidadRecibida;
	private String observacion;
	
	public DetalleRecepcion() {
		
	}
	
	public DetalleRecepcion(int idDetRecepcion, int idRecepcion, int idVariante, int cantidadRecibida, String observacion) {
	this.idDetRecepcion = idDetRecepcion;
	this.idRecepcion = idRecepcion;
	this.idVariante = idVariante;
	this.cantidadRecibida = cantidadRecibida;
	this.observacion = observacion;
	
	}

	public int getIdDetRecepcion() {
		return idDetRecepcion;
	}

	public void setIdDetRecepcion(int idDetRecepcion) {
		this.idDetRecepcion = idDetRecepcion;
	}

	public int getIdRecepcion() {
		return idRecepcion;
	}

	public void setIdRecepcion(int idRecepcion) {
		this.idRecepcion = idRecepcion;
	}

	public int getIdVariante() {
		return idVariante;
	}

	public void setIdVariante(int idVariante) {
		this.idVariante = idVariante;
	}

	public int getCantidadRecibida() {
		return cantidadRecibida;
	}

	public void setCantidadRecibida(int cantidadRecibida) {
		this.cantidadRecibida = cantidadRecibida;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
}