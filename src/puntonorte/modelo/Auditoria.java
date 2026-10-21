package puntonorte.modelo;
import java.time.LocalDateTime;

public class Auditoria {
    private int idAuditoria;
    private Integer idUsuario;
    private Integer usrAutoriza;
    private LocalDateTime fechaHora;
    private String accionRealizada;
    private String tablaAfectada;
    private String detalleCambio;
    private String motivo;
    private String valorNuevo;
    private String valorAnterior;
    private Integer idRegistroAfectado;
 
    public Auditoria() {
    }
   
    public Auditoria(int idAuditoria, int idUsuario, LocalDateTime fechaHora, String accionRealizada, String tablaAfectada, String detalleCambio, String motivo, String valorNuevo, String valorAnterior, int idRegistroAfectado, int usrAutoriza){

    this.idAuditoria = idAuditoria;
    this.idUsuario = idUsuario;
    this.fechaHora = fechaHora;
    this.usrAutoriza = usrAutoriza;
    this.valorNuevo = valorNuevo;
    this.valorAnterior = valorAnterior;
    this.idRegistroAfectado = idRegistroAfectado;
    this.accionRealizada = accionRealizada;
    this.tablaAfectada = tablaAfectada;
    this.detalleCambio = detalleCambio;
    }

	public int getIdAuditoria() {
		return idAuditoria;
	}

	public void setIdAuditoria(int idAuditoria) {
		this.idAuditoria = idAuditoria;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getUsrAutoriza() {
		return usrAutoriza;
	}

	public void setUsrAutoriza(Integer usrAutoriza) {
		this.usrAutoriza = usrAutoriza;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getAccionRealizada() {
		return accionRealizada;
	}

	public void setAccionRealizada(String accionRealizada) {
		this.accionRealizada = accionRealizada;
	}

	public String getTablaAfectada() {
		return tablaAfectada;
	}

	public void setTablaAfectada(String tablaAfectada) {
		this.tablaAfectada = tablaAfectada;
	}

	public String getDetalleCambio() {
		return detalleCambio;
	}

	public void setDetalleCambio(String detalleCambio) {
		this.detalleCambio = detalleCambio;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public String getValorNuevo() {
		return valorNuevo;
	}

	public void setValorNuevo(String valorNuevo) {
		this.valorNuevo = valorNuevo;
	}

	public String getValorAnterior() {
		return valorAnterior;
	}

	public void setValorAnterior(String valorAnterior) {
		this.valorAnterior = valorAnterior;
	}

	public int getIdRegistroAfectado() {
		return idRegistroAfectado;
	}

	public void setIdRegistroAfectado(int idRegistroAfectado) {
		this.idRegistroAfectado = idRegistroAfectado;
	}
}