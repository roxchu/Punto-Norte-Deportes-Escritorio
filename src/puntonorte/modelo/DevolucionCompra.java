package puntonorte.modelo;
import java.time.LocalDateTime;

public class DevolucionCompra {
	private int idDevolucion;
	private int idPedido;
	private int usrSolicita;
	private int usrAutoriza;
	private LocalDateTime fechaHora;
	private TipoResolucion tipoResolucion;
	private String motivo;
	
	public DevolucionCompra() {
		
	}
	
	public DevolucionCompra(int idDevolucion, int idPedido, int usrSolicita, int usrAutoriza, LocalDateTime fechaHora, TipoResolucion tipoResolucion, String motivo){
	this.idDevolucion = idDevolucion;
	this.idPedido = idPedido;
	this.usrSolicita = usrSolicita;
	this.usrAutoriza = usrAutoriza;
	this.fechaHora = fechaHora; 
	this.tipoResolucion = tipoResolucion;
	this.motivo = motivo;
	}

	public int getIdDevolucion() {
		return idDevolucion;
	}

	public void setIdDevolucion(int idDevolucion) {
		this.idDevolucion = idDevolucion;
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

	public TipoResolucion getTipoResolucion() {
		return tipoResolucion;
	}

	public void setTipoResolucion(TipoResolucion tipoResolucion) {
		this.tipoResolucion = tipoResolucion;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
}