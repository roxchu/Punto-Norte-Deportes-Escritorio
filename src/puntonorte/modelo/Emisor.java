package puntonorte.modelo;

import java.time.LocalDate;

/** Datos fijos de Punto Norte Deportes que van en el encabezado de toda factura (tabla emisor_config). */
public class Emisor {
    private String razonSocial;
    private String cuit;
    private String condicionIva;
    private String domicilioComercial;
    private String localidad;
    private String ingresosBrutos;
    private LocalDate inicioActividades;
    private int puntoVenta;

    public Emisor() { }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }

    public String getCondicionIva() { return condicionIva; }
    public void setCondicionIva(String condicionIva) { this.condicionIva = condicionIva; }

    public String getDomicilioComercial() { return domicilioComercial; }
    public void setDomicilioComercial(String domicilioComercial) { this.domicilioComercial = domicilioComercial; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getIngresosBrutos() { return ingresosBrutos; }
    public void setIngresosBrutos(String ingresosBrutos) { this.ingresosBrutos = ingresosBrutos; }

    public LocalDate getInicioActividades() { return inicioActividades; }
    public void setInicioActividades(LocalDate inicioActividades) { this.inicioActividades = inicioActividades; }

    public int getPuntoVenta() { return puntoVenta; }
    public void setPuntoVenta(int puntoVenta) { this.puntoVenta = puntoVenta; }
}
