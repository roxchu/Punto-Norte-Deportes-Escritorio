package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.UsuarioService;

public class RegistroFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final UsuarioService usuarioService = new UsuarioService();

    public RegistroFrame() {

        setTitle("Punto Norte Deportes - Solicitar acceso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 650));
        setSize(820, 690);
        setLocationRelativeTo(null);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilos.FONDO);
        fondo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(fondo);

        JPanel caja = new JPanel(new BorderLayout());
        caja.setBackground(Estilos.BLANCO);
        caja.setPreferredSize(new Dimension(700, 600));
        caja.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xCF, 0xC8, 0xBD)));

        JLabel cabecera = new JLabel("Solicitud de acceso");
        cabecera.setOpaque(true);
        cabecera.setBackground(Estilos.AZUL);
        cabecera.setForeground(Estilos.CORAL);
        cabecera.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 21));
        cabecera.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        caja.add(cabecera, BorderLayout.NORTH);
        caja.add(crearFormulario(), BorderLayout.CENTER);

        fondo.add(caja);
    }

    private JPanel crearFormulario() {

        JPanel contenedor = new JPanel(new GridBagLayout());
        contenedor.setBackground(Estilos.BLANCO);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(560, 470));

        JLabel titulo = ComponentesUI.titulo("Solicitar cuenta");
        JLabel descripcion = ComponentesUI.subtitulo(
                "La solicitud queda pendiente hasta que un Supervisor o Gerente la apruebe.");

        JTextField dni = ComponentesUI.campo();
        JTextField telefono = ComponentesUI.campo();
        JTextField nombre = ComponentesUI.campo();
        JTextField apellido = ComponentesUI.campo();
        JTextField correo = ComponentesUI.campo();

        java.util.List<String> rolesDisponibles = usuarioService.rolesSolicitables();
        JComboBox<String> rol = ComponentesUI.combo(rolesDisponibles.toArray(new String[0]));

        JPanel fila1 = filaDoble(
                ComponentesUI.filaFormulario("DNI", dni),
                ComponentesUI.filaFormulario("Teléfono (opcional)", telefono)
        );

        JPanel fila2 = filaDoble(
                ComponentesUI.filaFormulario("Nombre", nombre),
                ComponentesUI.filaFormulario("Apellido", apellido)
        );

        JPanel correoPanel = ComponentesUI.filaFormulario("Correo electrónico", correo);
        correoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JPanel rolPanel = ComponentesUI.filaFormulario("Rol solicitado", rol);
        rolPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JButton enviar = ComponentesUI.boton("→ Enviar solicitud", Estilos.CORAL, Estilos.AZUL);

        JButton volver = ComponentesUI.boton("← Volver al inicio de sesión", Estilos.BLANCO, Estilos.AZUL);
        volver.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));

        enviar.addActionListener(e -> {
            try {
                usuarioService.solicitarCuenta(dni.getText(), telefono.getText(), nombre.getText(),
                        apellido.getText(), correo.getText(), (String) rol.getSelectedItem());
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Solicitud enviada. Un Supervisor o Gerente debe aprobarla; "
                                + "recién entonces recibirás el código por correo.",
                        "Solicitud registrada", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                dispose();
            } catch (ReglaNegocioException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo enviar",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        });

        volver.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        form.add(titulo);
        form.add(Box.createVerticalStrut(6));
        form.add(descripcion);
        form.add(Box.createVerticalStrut(22));
        form.add(fila1);
        form.add(Box.createVerticalStrut(10));
        form.add(fila2);
        form.add(Box.createVerticalStrut(10));
        form.add(correoPanel);
        form.add(Box.createVerticalStrut(10));
        form.add(rolPanel);
        form.add(Box.createVerticalStrut(20));
        form.add(ComponentesUI.filaBotonCentrada(enviar, 320, 38));
        form.add(Box.createVerticalStrut(9));
        form.add(ComponentesUI.filaBotonCentrada(volver, 320, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 30, 20, 30);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        contenedor.add(form, gbc);

        return contenedor;
    }

    private JPanel filaDoble(JPanel izquierda, JPanel derecha) {

        JPanel fila = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        fila.add(izquierda);
        fila.add(derecha);

        return fila;
    }
}
