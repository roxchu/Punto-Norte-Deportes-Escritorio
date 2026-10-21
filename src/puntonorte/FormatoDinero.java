package puntonorte;

import java.util.Locale;

/** Formato de pesos para las pantallas: "$ 74.385" (con centavos solo si hacen falta). */
final class FormatoDinero {

    private FormatoDinero() {
    }

    static String pesos(double valor) {
        boolean entero = Math.abs(valor - Math.rint(valor)) < 0.005;
        return String.format(Locale.forLanguageTag("es-AR"), entero ? "$ %,.0f" : "$ %,.2f", valor);
    }
}