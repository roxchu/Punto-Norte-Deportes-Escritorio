package puntonorte;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        // Abre la conexión a la base en segundo plano mientras se muestra el login.
        puntonorte.conexion.ConexionBD.precalentar();

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Usa el estilo por defecto si no se puede cargar el del sistema.
            }

            new LoginFrame().setVisible(true);
        });
    }
}

