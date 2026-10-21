package puntonorte.modelo;
import java.time.LocalDateTime;

public class AnulacionVenta {
	private int idAnulacion;
	private int idVenta;
	private int usrSolicita;
	private int usrAutoriza;
	private LocalDateTime fechaHora;
	private String motivo;

	public AnulacionVenta() {
		
	}
	
	public AnulacionVenta(int idAnulacion, int idVenta, int usrSolicita, int usrAutoriza, LocalDateTime fechaHora, String motivo){
	this.idAnulacion = idAnulacion;
	this.idVenta = idVenta;
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

	public int getIdVenta() {
		return idVenta;
	}

	public void setIdVenta(int idVenta) {
		this.idVenta = idVenta;
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