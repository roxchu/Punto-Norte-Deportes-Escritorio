package puntonorte.modelo;

/** Condición del cliente frente al IVA. Define qué tipo de factura le corresponde. */
public enum CondicionIva {
    CONSUMIDOR_FINAL("Consumidor final", 'B'),
    MONOTRIBUTISTA("Monotributista", 'B'),
    RESPONSABLE_INSCRIPTO("Responsable inscripto", 'A');

    private final String texto;
    private final char tipoComprobante;

    CondicionIva(String texto, char tipoComprobante) {
        this.texto = texto;
        this.tipoComprobante = tipoComprobante;
    }

    public String getTexto() { return texto; }
    public char getTipoComprobante() { return tipoComprobante; }
}