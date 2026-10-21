package puntonorte.modelo;

public class Stock{
    private int idStock;
    private int idVariante;
    private int idUbicacion;
    private int cantidad;

    public Stock(){
    }

   public Stock(int idStock, int idVariante, int idUbicacion, int cantidad){
   this.idStock = idStock;
   this.idVariante = idVariante;
   this.idUbicacion = idUbicacion;
   this.cantidad = cantidad;
   }

   public int getIdStock() {
	return idStock;
   }

   public void setIdStock(int idStock) {
	this.idStock = idStock;
   }

   public int getIdUbicacion() {
	return idUbicacion;
}

   public void setIdUbicacion(int idUbicacion) {
	this.idUbicacion = idUbicacion;
   }

   public int getIdVariante() {
	return idVariante;
   }

   public void setIdVariante(int idVariante) {
	this.idVariante = idVariante;
   }

   public int getCantidad() {
	return cantidad;
   }

   public void setCantidad(int cantidad) {
	this.cantidad = cantidad;
   }
}