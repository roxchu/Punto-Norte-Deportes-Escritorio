package puntonorte.modelo;

import java.time.LocalDate;

/**
 * Columnas de facturación de la tabla ventas. Los datos del receptor son una COPIA tomada al
 * momento de la venta: si después se edita el cliente, el comprobante no cambia.
 * (No se agregaron a Ventas.java para no tocar el modelo que ya funciona.)
 */
public class DatosFiscalesVenta {
    private String tipoComprobante;        // "A" o "B"; null = venta sin facturar (anteriores a la migración)
    private String estadoFactura;          // BORRADOR / EMITIDA / ANULADA
    private Integer puntoVenta;
    private Integer numeroComprobante;     // null hasta que ARCA numere el comprobante
    private CondicionIva condicionIva;
    private String receptorNombre;
    private String receptorTipoDoc;        // SIN_IDENTIFICAR / DNI / CUIL / CDI / CUIT / PASAPORTE
    private String receptorNumeroDoc;
    private String receptorDomicilio;
    private Double montoNeto;
    private Double montoIva;               // en Factura B es el "IVA contenido"
    private String cae;
    private LocalDate vencimientoCae;

    public DatosFiscalesVenta() { }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getEstadoFactura() { return estadoFactura; }
    public void setEstadoFactura(String estadoFactura) { this.estadoFactura = estadoFactura; }

    public Integer getPuntoVenta() { return puntoVenta; }
    public void setPuntoVenta(Integer puntoVenta) { this.puntoVenta = puntoVenta; }

    public Integer getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(Integer numeroComprobante) { this.numeroComprobante = numeroComprobante; }

    public CondicionIva getCondicionIva() { return condicionIva; }
    public void setCondicionIva(CondicionIva condicionIva) { this.condicionIva = condicionIva; }

    public String getReceptorNombre() { return receptorNombre; }
    public void setReceptorNombre(String receptorNombre) { this.receptorNombre = receptorNombre; }

    public String getReceptorTipoDoc() { return receptorTipoDoc; }
    public void setReceptorTipoDoc(String receptorTipoDoc) { this.receptorTipoDoc = receptorTipoDoc; }

    public String getReceptorNumeroDoc() { return receptorNumeroDoc; }
    public void setReceptorNumeroDoc(String receptorNumeroDoc) { this.receptorNumeroDoc = receptorNumeroDoc; }

    public String getReceptorDomicilio() { return receptorDomicilio; }
    public void setReceptorDomicilio(String receptorDomicilio) { this.receptorDomicilio = receptorDomicilio; }

    public Double getMontoNeto() { return montoNeto; }
    public void setMontoNeto(Double montoNeto) { this.montoNeto = montoNeto; }

    public Double getMontoIva() { return montoIva; }
    public void setMontoIva(Double montoIva) { this.montoIva = montoIva; }

    public String getCae() { return cae; }
    public void setCae(String cae) { this.cae = cae; }

    public LocalDate getVencimientoCae() { return vencimientoCae; }
    public void setVencimientoCae(LocalDate vencimientoCae) { this.vencimientoCae = vencimientoCae; }
}