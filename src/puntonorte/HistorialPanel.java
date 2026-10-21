package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JOptionPane;

import puntonorte.servicio.AuditoriaService;
import puntonorte.servicio.ReglaNegocioException;

/** Historial alimentado por la tabla auditoria. */
public class HistorialPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JPanel tablaPanel = new JPanel(new BorderLayout());
    private final AuditoriaService auditoriaService = new AuditoriaService();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private String filtroUsuario, filtroModulo, filtroOperacion;
    private LocalDate filtroFecha;

    public HistorialPanel() {
        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.add(ComponentesUI.titulo("Historial de Operaciones"));
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(ComponentesUI.subtitulo("Movimientos realizados en el sistema."));
        contenido.add(Box.createVerticalStrut(18));
        tablaPanel.setBackground(Estilos.BEIGE);
        tablaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        contenido.add(tablaPanel);
        add(contenido, BorderLayout.CENTER);
        recargar();
    }

    public final void recargar() {
        tablaPanel.removeAll();
        String[] columnas = {"Fecha", "Usuario", "Rol", "Operación", "Módulo", "Detalle"};
        Object[][] datos;
        try {
            List<AuditoriaService.Fila> filas = auditoriaService.listar(filtroUsuario, filtroModulo, filtroOperacion, filtroFecha);
            datos = new Object[filas.size()][6];
            for (int i = 0; i < filas.size(); i++) {
                AuditoriaService.Fila f = filas.get(i);
                datos[i] = new Object[] {f.fecha.format(FECHA), f.usuario, f.rol, f.operacion, f.modulo, f.detalle};
            }
        } catch (ReglaNegocioException ex) {
            datos = new Object[0][6];
        }
        JTable tabla = ComponentesUI.tabla(columnas, datos);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {130, 140, 95, 180, 110, 360};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        tablaPanel.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER);
        tablaPanel.revalidate(); tablaPanel.repaint();
    }

    public void aplicarFiltros(String[] filtros) {
        if (filtros == null || filtros.length < 4) return;
        filtroUsuario = vacio(filtros[0]);
        filtroModulo = vacio(filtros[1]);
        if ("Todos".equalsIgnoreCase(filtroModulo)) filtroModulo = null;
        if (filtroModulo != null) filtroModulo = filtroModulo.toLowerCase(java.util.Locale.ROOT);
        filtroOperacion = vacio(filtros[2]);
        String fecha = filtros[3] == null ? "" : filtros[3].trim();
        try {
            filtroFecha = fecha.isEmpty() ? null : LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingresá la fecha con formato dd/MM/aaaa.",
                    "Fecha no válida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        recargar();
    }

    private String vacio(String valor) {
        return valor == null || valor.isBlank() || "Todos".equalsIgnoreCase(valor.trim()) ? null : valor.trim();
    }
}
