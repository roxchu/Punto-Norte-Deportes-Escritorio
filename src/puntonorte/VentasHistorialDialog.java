package puntonorte;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import puntonorte.modelo.Ventas;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.Ventaservicio;
import puntonorte.servicio.Permisos;

/** Consulta ventas con el filtro de permisos aplicado por Ventaservicio. */
public class VentasHistorialDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Ventaservicio servicio;
    private final JTextField desde = ComponentesUI.campo();
    private final JTextField hasta = ComponentesUI.campo();
    private final JTable tabla = new JTable();
    private ArrayList<Ventas> resultados = new ArrayList<>();

    public VentasHistorialDialog(JFrame owner, Ventaservicio servicio) {
        super(owner, Permisos.puedeVerTodasLasVentas() ? "Ventas realizadas" : "Mis ventas", true);
        this.servicio = servicio;
        setSize(900, 560);
        setMinimumSize(new java.awt.Dimension(760, 480));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Estilos.FONDO);

        JPanel base = new JPanel(new BorderLayout(0, 12));
        base.setBackground(Estilos.FONDO);
        base.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        base.add(ComponentesUI.titulo(Permisos.puedeVerTodasLasVentas() ? "Ventas realizadas" : "Mis ventas"), BorderLayout.NORTH);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        desde.setText(LocalDate.now().withDayOfMonth(1).toString());
        hasta.setText(LocalDate.now().toString());
        desde.setColumns(10);
        hasta.setColumns(10);
        JButton consultar = ComponentesUI.boton("Consultar", Estilos.CORAL, Estilos.AZUL);
        filtros.add(new JLabel("Desde (AAAA-MM-DD):")); filtros.add(desde);
        filtros.add(new JLabel("Hasta (AAAA-MM-DD):")); filtros.add(hasta);
        filtros.add(consultar);
        consultar.addActionListener(e -> recargar());

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER);
        base.add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.setOpaque(false);
        JButton comprobante = ComponentesUI.boton("Ver comprobante", Estilos.CORAL, Estilos.AZUL);
        comprobante.addActionListener(e -> verComprobante());
        JButton cerrar = ComponentesUI.botonBorde("Cerrar");
        cerrar.addActionListener(e -> dispose());
        sur.add(comprobante);
        sur.add(cerrar);
        base.add(sur, BorderLayout.SOUTH);
        setContentPane(base);
        recargar();
    }

    private void verComprobante() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= resultados.size()) {
            JOptionPane.showMessageDialog(this, "Seleccioná una venta de la lista.", "Comprobante", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FacturaTicketDialog.mostrar(this, resultados.get(tabla.convertRowIndexToModel(fila)).getIdVenta());
    }

    /**
     * Descuento o recargo por medio de pago, con su porcentaje: "- $ 15.500 (10%)".
     * Los ajustes por medio de pago son siempre porcentuales, y se calculan sobre lo que queda
     * después de los descuentos (subtotal - descuentos), así que el porcentaje se puede reconstruir.
     */
    private String textoAjustePago(Ventas v) {
        double ajuste = v.getMontoAjustePago();
        if (Math.abs(ajuste) < 0.005) {
            return "—";
        }
        double base = v.getSubtotal() - v.getDescuentoTotal();
        String porcentaje = "";
        if (base > 0) {
            double pct = Math.round(Math.abs(ajuste) / base * 10000.0) / 100.0;
            String numero = pct == Math.rint(pct) ? String.valueOf((long) pct) : String.valueOf(pct).replace('.', ',');
            porcentaje = " (" + numero + "%)";
        }
        return (ajuste < 0 ? "- " : "+ ") + FormatoDinero.pesos(Math.abs(ajuste)) + porcentaje;
    }

    private void recargar() {
        try {
            LocalDate fechaDesde = LocalDate.parse(desde.getText().trim());
            LocalDate fechaHasta = LocalDate.parse(hasta.getText().trim());
            resultados = servicio.listarVentas(fechaDesde, fechaHasta);
            Object[][] filas = new Object[resultados.size()][8];
            for (int i = 0; i < resultados.size(); i++) {
                Ventas v = resultados.get(i);
                filas[i] = new Object[] {v.getIdVenta(), v.getFechaHora().format(FECHA_HORA), v.getMedioPago(),
                        v.getEstado(), FormatoDinero.pesos(v.getSubtotal()), FormatoDinero.pesos(v.getDescuentoTotal()),
                        textoAjustePago(v), FormatoDinero.pesos(v.getMontoTotal())};
            }
            tabla.setModel(new DefaultTableModel(filas,
                    new String[] {"N° venta", "Fecha", "Medio de pago", "Estado", "Subtotal", "Descuentos", "Ajuste por pago", "Total"}) {
                private static final long serialVersionUID = 1L;
                @Override public boolean isCellEditable(int row, int column) { return false; }
            });
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Usá el formato de fecha AAAA-MM-DD.", "Rango de fechas", JOptionPane.WARNING_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ventas", JOptionPane.WARNING_MESSAGE);
        }
    }
}