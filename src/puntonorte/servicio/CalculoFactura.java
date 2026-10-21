package puntonorte.servicio;

import java.util.List;

/**
 * Cálculos de IVA de las facturas. Todos los precios de Punto Norte incluyen IVA (21%).
 * Clase sin base de datos ni pantallas: se puede probar sola (ver CalculoFacturaPruebas).
 */
public final class CalculoFactura {

    public static final double ALICUOTA_IVA = 0.21;

    private CalculoFactura() {
    }

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /** Neto (sin IVA) de un importe que ya incluye el IVA: 121,00 -> 100,00. */
    public static double neto(double conIva) {
        return redondear(conIva / (1 + ALICUOTA_IVA));
    }

    /** IVA contenido en un importe que ya lo incluye: 121,00 -> 21,00. */
    public static double iva(double conIva) {
        return redondear(conIva - neto(conIva));
    }

    /**
     * Desglose de una Factura A. Garantiza que cierre al centavo:
     * sumaLineasNeto - descuentoNeto + ajusteNeto = netoTotal, y netoTotal + iva = total.
     */
    public static class DesgloseA {
        public final double sumaLineasNeto;   // suma de los renglones, sin IVA
        public final double descuentoNeto;    // descuentos (producto + general), sin IVA; >0 = descuento
        public final double ajusteNeto;       // ajuste por medio de pago, sin IVA; <0 = descuento, >0 = recargo
        public final double netoTotal;        // neto gravado
        public final double iva;
        public final double total;

        DesgloseA(double sumaLineasNeto, double descuentoNeto, double ajusteNeto,
                double netoTotal, double iva, double total) {
            this.sumaLineasNeto = sumaLineasNeto;
            this.descuentoNeto = descuentoNeto;
            this.ajusteNeto = ajusteNeto;
            this.netoTotal = netoTotal;
            this.iva = iva;
            this.total = total;
        }
    }

    /** Desglose calculando neto e IVA a partir del total. */
    public static DesgloseA desgloseA(List<Double> importesConIva, double ajustePago, double total) {
        double netoTotal = neto(total);
        return desgloseA(importesConIva, ajustePago, netoTotal, redondear(total - netoTotal), total);
    }

    /** Desglose usando el neto y el IVA ya guardados (el comprobante emitido no cambia nunca). */
    public static DesgloseA desgloseA(List<Double> importesConIva, double ajustePago,
            double netoTotal, double iva, double total) {

        double sumaLineas = 0;
        for (double importe : importesConIva) {
            sumaLineas += neto(importe);
        }
        sumaLineas = redondear(sumaLineas);

        double ajusteNeto = ajustePago < 0 ? -neto(-ajustePago) : neto(ajustePago);
        // El descuento es lo que falta para que el neto cierre exacto (absorbe los centavos de redondeo).
        double descuentoNeto = redondear(sumaLineas + ajusteNeto - netoTotal);

        return new DesgloseA(sumaLineas, descuentoNeto, ajusteNeto, netoTotal, iva, total);
    }
}
