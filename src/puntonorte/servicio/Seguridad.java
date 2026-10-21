package puntonorte.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** Generación y hash del código de acceso. En la BD nunca se guarda el código en claro. */
public final class Seguridad {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Seguridad() {
    }

    public static String generarCodigo() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /** SHA-256 de "correo:codigo" (el correo funciona como sal). Da 64 caracteres. */
    public static String hash(String correo, String codigo) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((correo.trim().toLowerCase() + ":" + codigo.trim())
                    .getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean coincide(String correo, String codigo, String hashGuardado) {
        if (hashGuardado == null || hashGuardado.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(correo, codigo).getBytes(StandardCharsets.UTF_8),
                hashGuardado.getBytes(StandardCharsets.UTF_8));
    }

    /** Uso: java puntonorte.servicio.Seguridad correo codigo  -> imprime el hash para cargar a mano en la BD. */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: Seguridad <correo> <codigo>");
            return;
        }
        System.out.println(hash(args[0], args[1]));
    }
}