package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import puntonorte.modelo.Usuario;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.UsuarioService;

/** Diálogo de aprobación conectado al usuario seleccionado, no a datos de ejemplo. */
public class AprobarUsuarioDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final UsuarioService usuarioService = new UsuarioService();

    public AprobarUsuarioDialog(Window owner, Usuario usuarioSeleccionado,
            List<String> rolesAsignables, Runnable alFinalizar) {
        super(owner, "Aprobar solicitud", ModalityType.APPLICATION_MODAL);
        setSize(450, 360);
        setMinimumSize(new Dimension(420, 330));
        setLocationRelativeTo(owner);

        JPanel panel = new JPanel();
        panel.setBackground(Estilos.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titulo = ComponentesUI.titulo("Aprobar solicitud");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel usuario = new JLabel(usuarioSeleccionado.getNombre() + " "
                + usuarioSeleccionado.getApellido() + " · " + usuarioSeleccionado.getCorreo());
        usuario.setFont(Estilos.FUENTE);
        usuario.setForeground(Estilos.TEXTO_SECUNDARIO);
        usuario.setAlignmentX(LEFT_ALIGNMENT);

        String rolSolicitado = usuarioService.nombreRol(usuarioSeleccionado.getIdRol());
        JLabel rolElegido = new JLabel(rolSolicitado);
        rolElegido.setFont(Estilos.FUENTE);
        JPanel rolPanel = ComponentesUI.filaFormulario("Rol asignado", rolElegido);
        rolPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        JLabel aviso = new JLabel("<html>Al aceptar se enviará un código al correo indicado. "
                + "Si el envío falla, la solicitud quedará pendiente.</html>");
        aviso.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
        aviso.setForeground(Estilos.TEXTO_SECUNDARIO);
        aviso.setAlignmentX(LEFT_ALIGNMENT);

        JPanel acciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.setAlignmentX(LEFT_ALIGNMENT);
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JButton cancelar = ComponentesUI.boton("Cancelar", Estilos.BLANCO, Estilos.AZUL);
        cancelar.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        JButton aceptar = ComponentesUI.boton("✓ Aceptar solicitud", Estilos.CORAL, Estilos.AZUL);
        cancelar.addActionListener(e -> dispose());
        aceptar.addActionListener(e -> aprobar(usuarioSeleccionado, rolElegido.getText(),
                aceptar, cancelar, alFinalizar));
        acciones.add(cancelar);
        acciones.add(aceptar);

        panel.add(titulo); panel.add(Box.createVerticalStrut(6)); panel.add(usuario);
        panel.add(Box.createVerticalStrut(20)); panel.add(rolPanel); panel.add(Box.createVerticalStrut(14));
        panel.add(aviso); panel.add(Box.createVerticalStrut(18)); panel.add(acciones);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
    }

    private void aprobar(Usuario usuario, String rol, JButton aceptar, JButton cancelar, Runnable alFinalizar) {
        aceptar.setEnabled(false);
        cancelar.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                usuarioService.aprobar(usuario.getIdUsuario(), rol);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(AprobarUsuarioDialog.this,
                            "Solicitud aprobada y código enviado a " + usuario.getCorreo() + ".",
                            "Cuenta aprobada", JOptionPane.INFORMATION_MESSAGE);
                    alFinalizar.run();
                    dispose();
                } catch (Exception ex) {
                    Throwable causa = ex.getCause();
                    String mensaje = causa instanceof ReglaNegocioException ? causa.getMessage()
                            : "No se pudo aprobar la solicitud. Revisá la conexión y el correo.";
                    JOptionPane.showMessageDialog(AprobarUsuarioDialog.this, mensaje, "No se pudo aprobar",
                            JOptionPane.WARNING_MESSAGE);
                    aceptar.setEnabled(true);
                    cancelar.setEnabled(true);
                }
            }
        }.execute();
    }
}
