package puntonorte.modelo;

public class DetallePedidoCompra {
	private int idDetPedido;
	private int idPedido;
	private int idVariante;
	private String SKUProveedor;
	private int cantidadPedida;
	private double costoAcordado;
	
	public DetallePedidoCompra() {
		
	}
	
	public DetallePedidoCompra(int idDetPedido, int idPedido, int idVariante, String SKUProveedor, int cantidadPedida, double costoAcordado) {
	this.idDetPedido = idDetPedido;
	this.idPedido = idPedido;
	this.idVariante = idVariante;
	this.SKUProveedor = SKUProveedor;
	this.cantidadPedida = cantidadPedida;
	this.costoAcordado = costoAcordado;
	}

	public int getIdDetPedido() {
		return idDetPedido;
	}

	public void setIdDetPedido(int idDetPedido) {
		this.idDetPedido = idDetPedido;
	}

	public int getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(int idPedido) {
		this.idPedido = idPedido;
	}

	public int getIdVariante() {
		return idVariante;
	}

	public void setIdVariante(int idVariante) {
		this.idVariante = idVariante;
	}

	public String getSKUProveedor() {
		return SKUProveedor;
	}

	public void setSKUProveedor(String sKUProveedor) {
		SKUProveedor = sKUProveedor;
	}

	public int getCantidadPedida() {
		return cantidadPedida;
	}

	public void setCantidadPedida(int cantidadPedida) {
		this.cantidadPedida = cantidadPedida;
	}

	public double getCostoAcordado() {
		return costoAcordado;
	}

	public void setCostoAcordado(double costoAcordado) {
		this.costoAcordado = costoAcordado;
	}
}