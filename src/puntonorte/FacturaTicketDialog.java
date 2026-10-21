package puntonorte;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import puntonorte.servicio.FacturacionServicio;
import puntonorte.servicio.ReglaNegocioException;

/** Muestra el ticket de la factura pre-armada de una venta. Solo lee: no modifica nada. */
public class FacturaTicketDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    /** Punto de entrada: arma el ticket y, si algo falla, avisa en lugar de abrir una ventana vacía. */
    public static void mostrar(Window owner, int idVenta) {
        try {
            FacturacionServicio servicio = new FacturacionServicio();
            String texto = servicio.textoTicket(servicio.armarComprobante(idVenta));
            new FacturaTicketDialog(owner, idVenta, texto).setVisible(true);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Comprobante", JOptionPane.WARNING_MESSAGE);
        }
    }

    private FacturaTicketDialog(Window owner, int idVenta, String texto) {
        super(owner, "Comprobante · venta #" + idVenta, ModalityType.APPLICATION_MODAL);
        setSize(640, 660);
        setMinimumSize(new java.awt.Dimension(600, 560));
        setLocationRelativeTo(owner);

        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setCaretPosition(0);

        JPanel base = new JPanel(new BorderLayout(0, 12));
        base.setBackground(Estilos.FONDO);
        base.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        base.add(ComponentesUI.titulo("Comprobante"), BorderLayout.NORTH);
        base.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton copiar = ComponentesUI.boton("Copiar", Estilos.CORAL, Estilos.AZUL);
        JButton cerrar = ComponentesUI.botonBorde("Cerrar");
        copiar.addActionListener(e ->
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null));
        cerrar.addActionListener(e -> dispose());

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sur.setOpaque(false);
        sur.add(copiar);
        sur.add(cerrar);
        base.add(sur, BorderLayout.SOUTH);

        setContentPane(base);
    }
}