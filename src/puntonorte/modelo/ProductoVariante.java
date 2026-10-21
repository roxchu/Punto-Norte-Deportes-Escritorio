package puntonorte.modelo;

import java.util.List;

public class ProductoVariante {
    private int idVariante;
    private int idProducto;
    private String talle;
    private String color;
    private String SKU;
    private String codigoBarras;
    private double precioVenta;
    private double costoRef;
    // Precio específico del proveedor seleccionado (null cuando todavía no se informó).
    private Double costoProveedor;
    private int cantidadPendientePedido;
    private int stockMinimo;
    private int stockMaximo;
    private boolean activo = true; // false = inhabilitado (no se borra, queda el historial)

    // ---- Solo para mostrar el catálogo: NO son columnas de producto_variantes.
    // Los completa ProductoVarianteDAO.buscarCatalogo(); en el resto de los métodos quedan vacíos.
    private String nombreProducto;
    private String descripcionProducto;
    private String marca;
    private String categoria;
    private int stockTotal;
    private int stockSalon;
    private int stockDeposito;

   public ProductoVariante() {
   }

   public ProductoVariante(int idVariante, int idProducto, String talle, String color, String SKU, String codigoBarras, double precioVenta, double costoRef, int stockMinimo, int stockMaximo){
    this.idVariante = idVariante;
    this.idProducto = idProducto;
    this.talle =  talle;
    this.color =  color;
    this.SKU = SKU;
    this.codigoBarras = codigoBarras;
    this.precioVenta = precioVenta;
    this.costoRef = costoRef;
    this.stockMinimo = stockMinimo;
    this.stockMaximo = stockMaximo;
   }

   public int getIdVariante() {
	return idVariante;
   }

   public void setIdVariante(int idVariante) {
	this.idVariante = idVariante;
   }

   public int getIdProducto() {
	return idProducto;
   }

   public void setIdProducto(int idProducto) {
	this.idProducto = idProducto;
   }

   public String getTalle() {
	return talle;
   }

   public void setTalle(String talle) {
	this.talle = talle;
   }

   public String getColor() {
	return color;
   }

   public void setColor(String color) {
	this.color = color;
   }

   public String getSKU() {
	return SKU;
   }

   public void setSKU(String sKU) {
	SKU = sKU;
   }

   public String getCodigoBarras() {
	return codigoBarras;
   }

   public void setCodigoBarras(String codigoBarras) {
	this.codigoBarras = codigoBarras;
   }

   public double getPrecioVenta() {
	return precioVenta;
   }

   public void setPrecioVenta(double precioVenta) {
	this.precioVenta = precioVenta;
   }

   public double getCostoRef() {
	return costoRef;
   }

   public void setCostoRef(double costoRef) {
	this.costoRef = costoRef;
   }

   public Double getCostoProveedor() { return costoProveedor; }
   public void setCostoProveedor(Double costoProveedor) { this.costoProveedor = costoProveedor; }
   public int getCantidadPendientePedido() { return cantidadPendientePedido; }
   public void setCantidadPendientePedido(int cantidadPendientePedido) { this.cantidadPendientePedido = cantidadPendientePedido; }

   public int getStockMinimo() {
	return stockMinimo;
   }

   public void setStockMinimo(int stockMinimo) {
	this.stockMinimo = stockMinimo;
   }

   public int getStockMaximo() {
	return stockMaximo;
   }

   public void setStockMaximo(int stockMaximo) {
	this.stockMaximo = stockMaximo;
   }
   

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

   @Override
   public String toString() {
       return (nombreProducto == null ? "Producto" : nombreProducto) + " · " + SKU
               + " · Talle " + (talle == null ? "-" : talle)
               + " · " + (color == null ? "-" : color)
               + (cantidadPendientePedido > 0 ? " · Pendientes " + cantidadPendientePedido : "");
   }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(int stockTotal) {
        this.stockTotal = stockTotal;
    }

    public int getStockSalon() {
        return stockSalon;
    }

    public void setStockSalon(int stockSalon) {
        this.stockSalon = stockSalon;
    }

    public int getStockDeposito() {
        return stockDeposito;
    }

    public void setStockDeposito(int stockDeposito) {
        this.stockDeposito = stockDeposito;
    }

    /** Stock bajo = el total (salón + depósito) llegó al mínimo definido para esta variante. */
    public boolean isStockBajo() {
        return stockTotal <= stockMinimo;
    }

    /**
     * Criterios de búsqueda del catálogo. Todo es opcional: null / vacío / false = "no filtrar".
     * idsCategoria incluye la categoría elegida y todas sus subcategorías (lo arma el servicio).
     * Uso: ProductoVariante.Filtro f = new ProductoVariante.Filtro();
     */
    public static class Filtro {

        private String texto;
        private List<Integer> idsCategoria;
        private String talle;
        private String color;
        private boolean soloStockBajo;
        private boolean soloConStock;
        private boolean incluirInactivos;   // false (por defecto) = oculta lo inhabilitado

        public String getTexto() {
            return texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }

        public List<Integer> getIdsCategoria() {
            return idsCategoria;
        }

        public void setIdsCategoria(List<Integer> idsCategoria) {
            this.idsCategoria = idsCategoria;
        }

        public String getTalle() {
            return talle;
        }

        public void setTalle(String talle) {
            this.talle = talle;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public boolean isSoloStockBajo() {
            return soloStockBajo;
        }

        public void setSoloStockBajo(boolean soloStockBajo) {
            this.soloStockBajo = soloStockBajo;
        }

        public boolean isSoloConStock() {
            return soloConStock;
        }

        public void setSoloConStock(boolean soloConStock) {
            this.soloConStock = soloConStock;
        }

        public boolean isIncluirInactivos() {
            return incluirInactivos;
        }

        public void setIncluirInactivos(boolean incluirInactivos) {
            this.incluirInactivos = incluirInactivos;
        }
    }
}
