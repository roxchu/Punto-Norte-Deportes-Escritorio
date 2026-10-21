package puntonorte.servicio;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Responsable únicamente de mandar correos.
 * La cuenta y la clave se leen de config.properties (junto al programa), no del código.
 * Lanza excepción si falla, para que quien lo llama pueda avisar al usuario.
 * Se debe llamar desde un hilo aparte (ver EnvioCodigo), porque tarda.
 */
public class EmailService {

    private static final String ARCHIVO_CONFIG = "config.properties";

    public void enviarCodigoAcceso(String correoDestino, String codigo) throws MessagingException {

        final Properties config = cargarConfig();
        final String remitente = config.getProperty("mail.remitente", "").trim();
        final String clave = config.getProperty("mail.clave", "").trim();

        if (remitente.isEmpty() || clave.isEmpty()) {
            throw new IllegalStateException("Falta mail.remitente o mail.clave en " + ARCHIVO_CONFIG);
        }

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host", config.getProperty("mail.host", "smtp.gmail.com"));
        propiedades.put("mail.smtp.port", config.getProperty("mail.puerto", "587"));
        propiedades.put("mail.smtp.connectiontimeout", "8000");
        propiedades.put("mail.smtp.timeout", "8000");

        Session sesion = Session.getInstance(propiedades, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        });

        Message mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(remitente));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        mensaje.setSubject("Tu código de acceso - Punto Norte Deportes");
        mensaje.setText("Tu código de acceso es: " + codigo
                + "\n\nIngresalo en la pantalla de inicio de sesión junto con este correo."
                + "\nSi no lo solicitaste, ignorá este mensaje.");

        Transport.send(mensaje);
    }

    private Properties cargarConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(ARCHIVO_CONFIG)) {
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se encontró " + ARCHIVO_CONFIG
                    + " (copiá config.properties.example y completalo).");
        }
        return p;
    }
}