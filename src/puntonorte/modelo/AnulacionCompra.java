package puntonorte.modelo;
import java.time.LocalDateTime;

public class AnulacionCompra {
	private int idAnulacion;
	private int idPedido;
	private int usrSolicita;
	private int usrAutoriza;
	private LocalDateTime fechaHora;
	private String motivo;
	
	public AnulacionCompra() {
		
	}
	
	public AnulacionCompra(int idAnulacion, int idPedido, int usrSolicita, int usrAutoriza, LocalDateTime fechaHora, String motivo) {
	this.idAnulacion = idAnulacion;
	this.idPedido = idPedido;
	this.usrSolicita = usrSolicita;
	this.usrAutoriza = usrAutoriza;
	this.fechaHora = fechaHora;
	this.motivo = motivo;
	}

	public int getIdAnulacion() {
		return idAnulacion;
	}

	public void setIdAnulacion(int idAnulacion) {
		this.idAnulacion = idAnulacion;
	}

	public int getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}

	public int getUsrSolicita() {
		return usrSolicita;
	}

	public void setUsrSolicita(int usrSolicita) {
		this.usrSolicita = usrSolicita;
	}

	public int getUsrAutoriza() {
		return usrAutoriza;
	}

	public void setUsrAutoriza(int usrAutoriza) {
		this.usrAutoriza = usrAutoriza;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
}