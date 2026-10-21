package puntonorte.modelo;
import java.time.LocalDateTime;


public class RecepcionCompra {
	private int idRecepcion;
	private int idPedido;
	private int idUbicacion;
	private int idUsuarioRecepciona;
	private String numComprobante;
	private LocalDateTime fechaRecepcion;
	
	public RecepcionCompra() {
		
	}
	
	public RecepcionCompra(int idRecepcion, int idPedido, int idUbicacion, int idUsuarioRecepciona, String numComprobante, LocalDateTime fechaRecepcion){
	this.idRecepcion = idRecepcion;
	this.idPedido = idPedido;
	this.idUbicacion = idUbicacion;
	this.idUsuarioRecepciona = idUsuarioRecepciona;
	this.numComprobante = numComprobante;
	this.fechaRecepcion = fechaRecepcion;
	}

	public int getIdRecepcion() {
		return idRecepcion;
	}

	public void setIdRecepcion(int idRecepcion) {
		this.idRecepcion = idRecepcion;
	}

	public int getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}

	public int getIdUbicacion() {
		return idUbicacion;
	}

	public void setIdUbicacion(int idUbicacion) {
		this.idUbicacion = idUbicacion;
	}

	public int getIdUsuarioRecepciona() {
		return idUsuarioRecepciona;
	}

	public void setIdUsuarioRecepciona(int idUsuarioRecepciona) {
		this.idUsuarioRecepciona = idUsuarioRecepciona;
	}

	public String getNumComprobante() {
		return numComprobante;
	}

	public void setNumComprobante(String numComprobante) {
		this.numComprobante = numComprobante;
	}

	public LocalDateTime getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(LocalDateTime fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}
}