package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import puntonorte.modelo.MedioPago;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.Ventaservicio;
import puntonorte.servicio.Ventaservicio.PedidoVenta;
import puntonorte.servicio.Ventaservicio.ResumenVenta;

/** Confirmación final del cobro. El guardado y las validaciones quedan en Ventaservicio. */
public class RevisionVentaDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final Ventaservicio servicio;
    private final PedidoVenta pedido;
    private final JLabel error = new JLabel(" ");

    public RevisionVentaDialog(Window owner, Ventaservicio servicio, PedidoVenta pedido) {
        super(owner, "Revisión de venta", ModalityType.APPLICATION_MODAL);
        this.servicio = servicio;
        this.pedido = pedido;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 360);
        setMinimumSize(new Dimension(460, 320));
        setLocationRelativeTo(owner);

        JPanel base = new JPanel(new BorderLayout(0, 16));
        base.setBackground(Estilos.FONDO);
        base.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        JPanel detalle = new JPanel();
        detalle.setOpaque(false);
        detalle.setLayout(new BoxLayout(detalle, BoxLayout.Y_AXIS));
        JLabel titulo = ComponentesUI.titulo("Revisá la venta");
        detalle.add(titulo);
        detalle.add(Box.createVerticalStrut(16));
        ResumenVenta r;
        try { r = servicio.calcular(pedido); }
        catch (ReglaNegocioException ex) { r = null; error.setText(ex.getMessage()); }
        if (r != null) {
            for (Ventaservicio.LineaResumen l : r.lineas) {
                detalle.add(new JLabel(l.cantidad + " × " + l.descripcion + " — " + FormatoDinero.pesos(l.subtotal)));
                detalle.add(Box.createVerticalStrut(6));
            }
            detalle.add(Box.createVerticalStrut(8));
            detalle.add(new JLabel("Medio de pago: " + r.etiquetaPago));
            detalle.add(new JLabel("Subtotal: " + FormatoDinero.pesos(r.subtotalLista)));
            detalle.add(new JLabel("Descuentos por producto: " + FormatoDinero.pesos(r.descuentoProductos)));
            detalle.add(new JLabel("Descuento general: " + FormatoDinero.pesos(r.descuentoGeneral)));
            detalle.add(new JLabel("Ajuste por medio de pago: " + FormatoDinero.pesos(r.ajustePago)));
            detalle.add(new JLabel("Total: " + FormatoDinero.pesos(r.total)));
            if (pedido.medioPago == MedioPago.EFECTIVO && r.vuelto != null)
                detalle.add(new JLabel("Vuelto: " + FormatoDinero.pesos(r.vuelto)));
        }
        error.setForeground(Estilos.ROJO);
        JPanel sur = new JPanel(new BorderLayout(0, 8));
        sur.setOpaque(false);
        sur.add(error, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUI.botonBorde("Volver");
        JButton confirmar = ComponentesUI.boton("Confirmar cobro", Estilos.CORAL, Estilos.AZUL);
        cancelar.addActionListener(e -> dispose());
        confirmar.setEnabled(r != null);
        confirmar.addActionListener(e -> confirmar());
        botones.add(cancelar);
        botones.add(confirmar);
        sur.add(botones, BorderLayout.SOUTH);
        base.add(detalle, BorderLayout.CENTER);
        base.add(sur, BorderLayout.SOUTH);
        setContentPane(base);
    }

    private void confirmar() {
        try {
            int idVenta = servicio.confirmar(pedido);
            pedido.limpiar();
            JOptionPane.showMessageDialog(this, "Venta #" + idVenta + " registrada correctamente.",
                    "Venta registrada", JOptionPane.INFORMATION_MESSAGE);
            FacturaTicketDialog.mostrar(this, idVenta);   // ticket de la factura recién generada
            dispose();
        } catch (ReglaNegocioException ex) {
            error.setText(ex.getMessage());
        }
    }
}