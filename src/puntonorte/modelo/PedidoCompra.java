package puntonorte.modelo;
import java.time.LocalDate;

public class PedidoCompra {
	private int idPedido;
	private int idProveedor;
	private int idUsuario;
	private LocalDate fechaEmision;
	private EstadoPedidoCompra estado;
	private String linkSeguimiento;
	
	public PedidoCompra() {
		
	}
	
	public PedidoCompra(int idPedido, int idProveedor, int idUsuario, LocalDate fechaEmision, EstadoPedidoCompra estado, String linkSeguimiento) {
	this.idPedido = idPedido;
	this.idProveedor = idProveedor;
	this.idUsuario = idUsuario;
	this.fechaEmision = fechaEmision;
	this.estado = estado;
	this.linkSeguimiento = linkSeguimiento;
	}

	public int getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}

	public int getIdProveedor() {
		return idProveedor;
	}

	public void setIdProveedor(int idProveedor) {
		this.idProveedor = idProveedor;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public LocalDate getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(LocalDate fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public EstadoPedidoCompra getEstado() {
		return estado;
	}

	public void setEstado(EstadoPedidoCompra estado) {
		this.estado = estado;
	}

	public String getLinkSeguimiento() {
		return linkSeguimiento;
	}

	public void setLinkSeguimiento(String linkSeguimiento) {
		this.linkSeguimiento = linkSeguimiento;
	}
}