package puntonorte.modelo;
import java.time.LocalDate;


public class Descuentos {
	private int idDescuento;
	private String nombre;
	private Naturaleza naturaleza;
	private String tipo;
	private MedioPago medioPago;
	private double valor;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private boolean activo;
	// Asociación de pantalla con un producto; vive en descuento_producto, no en descuentos.
	private Integer idProducto;
	private String productoAsociado;
	private Integer cantidadCompra;
	private Integer cantidadPaga;
	
	public Descuentos() {
		
	}
	
	public Descuentos(int idDescuento, String nombre, Naturaleza naturaleza, String tipo, MedioPago medioPago, double valor, LocalDate fechaInicio, LocalDate fechaFin, boolean activo){
	this.idDescuento = idDescuento;
	this.nombre = nombre;
	this.naturaleza = naturaleza;
	this.tipo = tipo;
	this.medioPago = medioPago;
	this.valor = valor;
	this.fechaInicio = fechaInicio;
	this.fechaFin = fechaFin;
	this.activo = activo;
	}

	public int getIdDescuento() {
		return idDescuento;
	}

	public void setIdDescuento(int idDescuento) {
		this.idDescuento = idDescuento;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Naturaleza getNaturaleza() {
		return naturaleza;
	}

	public void setNaturaleza(Naturaleza naturaleza) {
		this.naturaleza = naturaleza;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public MedioPago getMedioPago() {
		return medioPago;
	}

	public void setMedioPago(MedioPago medioPago) {
		this.medioPago = medioPago;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Integer getIdProducto() { return idProducto; }
	public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
	public String getProductoAsociado() { return productoAsociado; }
	public void setProductoAsociado(String productoAsociado) { this.productoAsociado = productoAsociado; }
	public Integer getCantidadCompra() { return cantidadCompra; }
	public void setCantidadCompra(Integer cantidadCompra) { this.cantidadCompra = cantidadCompra; }
	public Integer getCantidadPaga() { return cantidadPaga; }
	public void setCantidadPaga(Integer cantidadPaga) { this.cantidadPaga = cantidadPaga; }
}