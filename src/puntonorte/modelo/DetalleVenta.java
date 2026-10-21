package puntonorte.modelo;

public class DetalleVenta {
	private int idDetVenta;
	private int idVenta;
	private int idVariante;
	private Integer idDescuento;          // null = la línea no tiene descuento
	private int cantidad;
	private double precioHistorico;       // precio unitario al momento de la venta (no cambia nunca)
	private double montoDescuentoAplicado;

	public DetalleVenta(){
	}

	public DetalleVenta(int idDetVenta, int idVenta, int idVariante, Integer idDescuento, int cantidad, double precioHistorico, double montoDescuentoAplicado){
		this.idDetVenta = idDetVenta;
		this.idVenta = idVenta;
		this.idVariante = idVariante;
		this.idDescuento = idDescuento;
		this.cantidad = cantidad;
		this.precioHistorico = precioHistorico;
		this.montoDescuentoAplicado = montoDescuentoAplicado;
	}

	public int getIdDetVenta() { return idDetVenta; }
	public void setIdDetVenta(int idDetVenta) { this.idDetVenta = idDetVenta; }

	public int getIdVenta() { return idVenta; }
	public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

	public int getIdVariante() { return idVariante; }
	public void setIdVariante(int idVariante) { this.idVariante = idVariante; }

	public Integer getIdDescuento() { return idDescuento; }
	public void setIdDescuento(Integer idDescuento) { this.idDescuento = idDescuento; }

	public int getCantidad() { return cantidad; }
	public void setCantidad(int cantidad) { this.cantidad = cantidad; }

	public double getPrecioHistorico() { return precioHistorico; }
	public void setPrecioHistorico(double precioHistorico) { this.precioHistorico = precioHistorico; }

	public double getMontoDescuentoAplicado() { return montoDescuentoAplicado; }
	public void setMontoDescuentoAplicado(double montoDescuentoAplicado) { this.montoDescuentoAplicado = montoDescuentoAplicado; }
}