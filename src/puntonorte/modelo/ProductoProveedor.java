package puntonorte.modelo;

public class ProductoProveedor {

    private int idProducto;
    private int idProveedor;

    public ProductoProveedor() {
    }

    public ProductoProveedor(int idProducto, int idProveedor) {
        this.idProducto = idProducto;
        this.idProveedor = idProveedor;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }
}