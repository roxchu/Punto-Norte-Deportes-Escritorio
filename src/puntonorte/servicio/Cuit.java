package puntonorte.servicio;

/**
 * Validación de CUIT/CUIL: 11 dígitos, el último es un dígito verificador que se calcula con
 * los 10 anteriores (módulo 11). No usa la base de datos, así que se puede probar sola.
 */
public final class Cuit {

    private static final int[] PESOS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private Cuit() {
    }

    /** Deja solo los números: "20-12345678-6" -> "20123456786". */
    public static String normalizar(String crudo) {
        return crudo == null ? "" : crudo.replaceAll("\\D", "");
    }

    public static boolean esValido(String crudo) {
        String d = normalizar(crudo);
        if (d.length() != 11) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += (d.charAt(i) - '0') * PESOS[i];
        }
        int verificador = 11 - (suma % 11);
        if (verificador == 11) {
            verificador = 0;
        } else if (verificador == 10) {
            verificador = 9;
        }
        return verificador == d.charAt(10) - '0';
    }

    /** Lanza ReglaNegocioException con un mensaje claro si el CUIT no sirve. */
    public static void validar(String crudo) {
        String d = normalizar(crudo);
        if (d.isEmpty()) {
            throw new ReglaNegocioException("Ingresá el CUIT del cliente (Factura A).");
        }
        if (d.length() != 11) {
            throw new ReglaNegocioException("El CUIT debe tener 11 números.");
        }
        if (!esValido(d)) {
            throw new ReglaNegocioException("El CUIT no es válido: revisá los números (el dígito verificador no coincide).");
        }
    }

    /** "20123456786" -> "20-12345678-6". Si no tiene 11 dígitos lo devuelve como vino. */
    public static String formatear(String crudo) {
        String d = normalizar(crudo);
        if (d.length() != 11) {
            return crudo == null ? "" : crudo;
        }
        return d.substring(0, 2) + "-" + d.substring(2, 10) + "-" + d.substring(10);
    }
}
