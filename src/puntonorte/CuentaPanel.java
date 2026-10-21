package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CuentaPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public CuentaPanel() {

        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        contenido.add(ComponentesUI.titulo("Mi cuenta"));
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(ComponentesUI.subtitulo(
                "Información de la cuenta."
        ));
        contenido.add(Box.createVerticalStrut(20));
        contenido.add(crearDatos());

        add(contenido, BorderLayout.NORTH);
    }

    private JPanel crearDatos() {

        JPanel caja = new JPanel(new GridLayout(6, 2, 0, 0));
        caja.setBackground(Estilos.BEIGE);
        caja.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, Estilos.AZUL));
        caja.setMaximumSize(new Dimension(720, 320));
        caja.setAlignmentX(LEFT_ALIGNMENT);

        agregarFila(caja, "DNI", "40.111.222");
        agregarFila(caja, "Nombre y apellido", "Joaquin Maini");
        agregarFila(caja, "Correo electrónico", "supervisor@puntonorte.com");
        agregarFila(caja, "Teléfono", "11 5555-1000");
        agregarFila(caja, "Rol", "Supervisor");
        agregarFila(caja, "Estado", "Activo");

        return caja;
    }

    private void agregarFila(JPanel panel, String titulo, String valor) {

        JLabel t = new JLabel(titulo);
        t.setFont(Estilos.FUENTE_NEGRITA);
        t.setBackground(new java.awt.Color(0xDD, 0xD6, 0xCC));
        t.setOpaque(true);
        t.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel v = new JLabel(valor);
        v.setFont(Estilos.FUENTE);
        v.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        panel.add(t);
        panel.add(v);
    }
}

