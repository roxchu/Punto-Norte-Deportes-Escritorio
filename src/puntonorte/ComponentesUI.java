package puntonorte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.function.IntPredicate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicButtonUI;

public final class ComponentesUI {

    private ComponentesUI() {
    }

    public static JButton boton(String texto, Color fondo, Color textoColor) {

        JButton boton = new JButton(texto);
        boton.setFont(Estilos.FUENTE_NEGRITA);
        boton.setBackground(fondo);
        boton.setForeground(textoColor);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setUI(new BasicButtonUI());

        return boton;
    }

    public static JButton botonBorde(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(Estilos.FUENTE_NEGRITA);
        boton.setForeground(Estilos.TEXTO);
        boton.setBackground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.AZUL_CLARO),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setUI(new BasicButtonUI());
        return boton;
    }

    public static JTextField campo() {

        JTextField campo = new JTextField();
        campo.setFont(Estilos.FUENTE);
        campo.setEditable(true);
        campo.setEnabled(true);
        campo.setFocusable(true);
        campo.setBackground(Color.WHITE);
        campo.setForeground(Estilos.TEXTO);
        campo.setPreferredSize(new Dimension(180, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.AZUL_CLARO),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        return campo;
    }

    public static JComboBox<String> combo(String... opciones) {

        JComboBox<String> combo = new JComboBox<>(opciones);
        combo.setFont(Estilos.FUENTE);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(180, 34));

        return combo;
    }

    public static JTextArea areaTexto() {

        JTextArea area = new JTextArea(4, 20);
        area.setFont(Estilos.FUENTE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.AZUL_CLARO),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        return area;
    }

    public static JTable tabla(String[] columnas, Object[][] datos) {

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setFont(Estilos.FUENTE);
        tabla.setRowHeight(30);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(Estilos.AZUL_CLARO);
        tabla.setBackground(Estilos.BEIGE);
        tabla.setForeground(Estilos.TEXTO);
        tabla.setFillsViewportHeight(true);

        tabla.getTableHeader().setPreferredSize(new Dimension(20, 34));

        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setFont(Estilos.FUENTE_NEGRITA);
        encabezado.setForeground(Estilos.TEXTO);
        encabezado.setBackground(Color.WHITE);
        encabezado.setOpaque(true);
        encabezado.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        tabla.getTableHeader().setDefaultRenderer(encabezado);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        renderer.setForeground(Estilos.TEXTO);
        
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        return tabla;
    }

    public static JScrollPane scrollTabla(JTable tabla) {

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        return scroll;
    }

    /** Atenúa filas inactivas sin deshabilitar ni bloquear su selección e interacción. */
    public static void atenuarFilasInactivas(JTable tabla, IntPredicate filaInactiva) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focused, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, selected, focused, row, column);
                if (!selected) {
                    int modelRow = table.convertRowIndexToModel(row);
                    c.setForeground(filaInactiva.test(modelRow)
                            ? new Color(0x8A, 0x8A, 0x8A) : Estilos.TEXTO);
                }
                return c;
            }
        };
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(renderer);
        }
    }

    public static JLabel titulo(String texto) {

        JLabel label = new JLabel(texto);
        label.setFont(Estilos.TITULO);
        label.setForeground(Estilos.TEXTO);

        return label;
    }

    public static JLabel subtitulo(String texto) {

        JLabel label = new JLabel(texto);
        label.setFont(Estilos.SUBTITULO);
        label.setForeground(Estilos.TEXTO_SECUNDARIO);

        return label;
    }

    public static JLabel etiqueta(String texto) {

        JLabel label = new JLabel(texto);
        label.setFont(Estilos.FUENTE_NEGRITA);
        label.setForeground(Estilos.TEXTO);

        return label;
    }

    public static JPanel bloqueTabla(String titulo, JTable tabla) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Estilos.BEIGE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(0xD0, 0xC9, 0xBE)));

        JLabel cabecera = new JLabel(titulo);
        cabecera.setFont(Estilos.FUENTE_NEGRITA);
        cabecera.setForeground(Color.WHITE);
        cabecera.setBackground(Estilos.AZUL);
        cabecera.setOpaque(true);
        cabecera.setBorder(BorderFactory.createEmptyBorder(11, 13, 11, 13));

        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(scrollTabla(tabla), BorderLayout.CENTER);

        return panel;
    }

    public static JPanel filaFormulario(String label, Component campo) {

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(etiqueta(label), BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);

        return panel;
    }

    public static JLabel etiquetaCentro(String texto) {

        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(Estilos.FUENTE);
        label.setForeground(Estilos.TEXTO_SECUNDARIO);

        return label;
    }
}
