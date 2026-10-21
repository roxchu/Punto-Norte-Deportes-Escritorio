package puntonorte.modelo;
import java.time.LocalDateTime;

public class Ventas {
	private int idVenta;
	private Integer idCliente;
	private int idUsuario;
	private int idUbicacion;
	private Integer idDescuento;
	private MedioPago medioPago;
	private EstadoVenta estado;
	private double montoAjustePago;
	private Double montoRecibido;
	private Double vuelto;
	private LocalDateTime fechaHora;
	private double subtotal;
	private double descuentoTotal;
	private double montoTotal;
	private String observaciones;

	public Ventas() {
		
	}
	
	public Ventas(int idVenta, Integer idCliente, int idUsuario, int idUbicacion, MedioPago medioPago, EstadoVenta estado, Double montoRecibido, Double vuelto, LocalDateTime fechaHora, double subtotal, double descuentoTotal, double montoTotal, String observaciones, Integer idDescuento, double montoAjustePago){
	this.idVenta = idVenta;
	this.idCliente = idCliente;
	this.idUsuario = idUsuario;
	this.idDescuento = idDescuento;
	this.idUbicacion = idUbicacion;
	this.medioPago = medioPago;
	this.estado = estado;
	this.montoRecibido = montoRecibido;
	this.vuelto = vuelto;
	this.fechaHora = fechaHora;
	this.subtotal = subtotal;
	this.descuentoTotal = descuentoTotal;
	this.montoTotal = montoTotal;
	this.observaciones = observaciones;
	this.montoAjustePago = montoAjustePago;
	}

	public int getIdVenta() {
		return idVenta;
	}

	public void setIdVenta(int idVenta) {
		this.idVenta = idVenta;
	}

	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public int getIdUbicacion() {
		return idUbicacion;
	}

	public void setIdUbicacion(int idUbicacion) {
		this.idUbicacion = idUbicacion;
	}

	public MedioPago getMedioPago() {
		return medioPago;
	}

	public void setMedioPago(MedioPago medioPago) {
		this.medioPago = medioPago;
	}

	public EstadoVenta getEstado() {
		return estado;
	}

	public void setEstado(EstadoVenta estado) {
		this.estado = estado;
	}

	public Double getMontoRecibido() {
		return montoRecibido;
	}

	public void setMontoRecibido(Double montoRecibido) {
		this.montoRecibido = montoRecibido;
	}

	public Double getVuelto() {
		return vuelto;
	}

	public void setVuelto(Double vuelto) {
		this.vuelto = vuelto;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}

	public double getDescuentoTotal() {
		return descuentoTotal;
	}

	public void setDescuentoTotal(double descuentoTotal) {
		this.descuentoTotal = descuentoTotal;
	}

	public double getMontoTotal() {
		return montoTotal;
	}

	public void setMontoTotal(double montoTotal) {
		this.montoTotal = montoTotal;
	}

	public Integer getIdDescuento() {
		return idDescuento;
	}

	public void setIdDescuento(Integer idDescuento) {
		this.idDescuento = idDescuento;
	}

	public double getMontoAjustePago() {
		return montoAjustePago;
	}

	public void setMontoAjustePago(double montoAjustePago) {
		this.montoAjustePago = montoAjustePago;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}
}