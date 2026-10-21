package puntonorte.servicio;

import java.util.Arrays;

/** Pruebas rápidas sin base de datos. Run As -> Java Application: debe imprimir "TODO OK". */
public class CalculoFacturaPruebas {

    private static int fallas = 0;

    public static void main(String[] args) {
        // --- CUIT ---
        verificar(Cuit.esValido("23-17203305-9"), "CUIT válido con guiones");
        verificar(Cuit.esValido("23172033059"), "CUIT válido sin guiones");
        verificar(!Cuit.esValido("23-17203305-8"), "CUIT con dígito verificador incorrecto");
        verificar(!Cuit.esValido("2317203305"), "CUIT de 10 dígitos");
        verificar(!Cuit.esValido(""), "CUIT vacío");
        verificar(!Cuit.esValido(null), "CUIT null");
        verificar("23-17203305-9".equals(Cuit.formatear("23172033059")), "formato con guiones");
        try {
            Cuit.validar("20-12345678-0");
            verificar(false, "validar debía lanzar excepción");
        } catch (ReglaNegocioException esperada) {
            verificar(true, "validar lanza excepción con CUIT inválido");
        }

        // --- IVA ---
        verificar(CalculoFactura.neto(121.00) == 100.00, "neto de 121 = 100");
        verificar(CalculoFactura.iva(121.00) == 21.00, "IVA de 121 = 21");

        // Ejemplo del documento: 2 remeras de 30.250 + mochila de 72.600, 10% de descuento.
        // total = 133.100 - 13.310 = 119.790  ->  neto 99.000, IVA 20.790
        CalculoFactura.DesgloseA a = CalculoFactura.desgloseA(Arrays.asList(60500.00, 72600.00), 0, 119790.00);
        verificar(a.sumaLineasNeto == 110000.00, "suma de renglones neto = 110.000 (fue " + a.sumaLineasNeto + ")");
        verificar(a.descuentoNeto == 11000.00, "descuento neto = 11.000 (fue " + a.descuentoNeto + ")");
        verificar(a.ajusteNeto == 0.0, "sin ajuste de pago");
        verificar(a.netoTotal == 99000.00, "neto gravado = 99.000 (fue " + a.netoTotal + ")");
        verificar(a.iva == 20790.00, "IVA = 20.790 (fue " + a.iva + ")");
        verificar(CalculoFactura.redondear(a.netoTotal + a.iva) == a.total, "neto + IVA = total");

        // Con descuento por medio de pago (efectivo -10%): total = 100.000 - 10.000 = 90.000
        CalculoFactura.DesgloseA b = CalculoFactura.desgloseA(Arrays.asList(100000.00), -10000.00, 90000.00);
        verificar(CalculoFactura.redondear(b.sumaLineasNeto - b.descuentoNeto + b.ajusteNeto) == b.netoTotal,
                "el neto cierra con ajuste de pago");
        verificar(b.ajusteNeto < 0, "ajuste de pago negativo = descuento");

        // Importes que no dividen exacto: el neto tiene que seguir cerrando al centavo.
        CalculoFactura.DesgloseA c = CalculoFactura.desgloseA(Arrays.asList(1000.00, 2500.00, 999.99), 0, 4499.99);
        verificar(CalculoFactura.redondear(c.sumaLineasNeto - c.descuentoNeto + c.ajusteNeto) == c.netoTotal,
                "cierra con importes con centavos");
        verificar(CalculoFactura.redondear(c.netoTotal + c.iva) == c.total, "neto + IVA = total con centavos");

        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S)");
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) {
            fallas++;
            System.out.println("FALLA: " + descripcion);
        }
    }
}
