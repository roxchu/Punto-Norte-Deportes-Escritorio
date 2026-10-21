package puntonorte;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import puntonorte.modelo.Descuentos;
import puntonorte.servicio.DescuentosServicio;
import puntonorte.servicio.ReglaNegocioException;

/** Administración del catálogo de descuentos, solo visible para Supervisor/Gerencia. */
public class DescuentosPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final JFrame owner;
    private final DescuentosServicio servicio = new DescuentosServicio();
    private final JTable tabla = new JTable();
    private ArrayList<Descuentos> descuentos = new ArrayList<>();

    public DescuentosPanel(JFrame owner) {
        this.owner = owner;
        setLayout(new BorderLayout(0, 16));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        add(ComponentesUI.titulo("Gestión de descuentos"), BorderLayout.NORTH);
        tabla.setRowHeight(30);
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(ComponentesUI.subtitulo("Creá descuentos y desactivá los que ya no deban ofrecerse. El historial de ventas se conserva."), BorderLayout.NORTH);
        centro.add(ComponentesUI.bloqueTabla("Descuentos registrados", tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        JButton actualizar = ComponentesUI.botonBorde("Actualizar");
        JButton activar = ComponentesUI.botonBorde("Activar / desactivar");
        JButton nuevo = ComponentesUI.boton("＋ Nuevo descuento", Estilos.CORAL, Estilos.AZUL);
        actualizar.addActionListener(e -> recargar());
        activar.addActionListener(e -> cambiarEstado());
        nuevo.addActionListener(e -> new DescuentoDialog(owner, this::recargar).setVisible(true));
        acciones.add(actualizar); acciones.add(activar); acciones.add(nuevo);
        add(acciones, BorderLayout.SOUTH);
        recargar();
    }

    public final void recargar() {
        try {
            descuentos = servicio.listar();
            Object[][] filas = new Object[descuentos.size()][10];
            for (int i = 0; i < descuentos.size(); i++) {
                Descuentos d = descuentos.get(i);
                filas[i] = new Object[] {d.getNombre(), d.getTipo(), d.getNaturaleza(),
                        d.getProductoAsociado() == null ? "—" : d.getProductoAsociado(),
                        reglaEspecial(d), d.getMedioPago() == null ? "—" : d.getMedioPago(),
                        "especiales".equalsIgnoreCase(d.getTipo()) ? "—" : d.getValor(),
                        fecha(d.getFechaInicio()), fecha(d.getFechaFin()), d.isActivo() ? "Activo" : "Inactivo"};
            }
            tabla.setModel(new DefaultTableModel(filas, new String[] {"Nombre", "Tipo", "Naturaleza", "Producto", "Promoción", "Medio de pago", "Valor", "Desde", "Hasta", "Estado"}) {
                private static final long serialVersionUID = 1L;
                @Override public boolean isCellEditable(int row, int column) { return false; }
            });
            ComponentesUI.atenuarFilasInactivas(tabla,
                    fila -> !descuentos.get(fila).isActivo());
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Descuentos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fecha(java.time.LocalDate fecha) { return fecha == null ? "Sin límite" : fecha.format(FECHA); }

    private String reglaEspecial(Descuentos d) {
        return "especiales".equalsIgnoreCase(d.getTipo())
                ? "Comprá " + d.getCantidadCompra() + ", pagá " + d.getCantidadPaga() : "—";
    }

    private void cambiarEstado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Seleccioná un descuento."); return; }
        Descuentos d = descuentos.get(tabla.convertRowIndexToModel(fila));
        boolean nuevoEstado = !d.isActivo();
        String accion = nuevoEstado ? "activar" : "desactivar";
        if (JOptionPane.showConfirmDialog(this, "¿Querés " + accion + " '" + d.getNombre() + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { servicio.cambiarActivo(d.getIdDescuento(), nuevoEstado); recargar(); }
        catch (ReglaNegocioException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Descuentos", JOptionPane.WARNING_MESSAGE); }
    }
}
