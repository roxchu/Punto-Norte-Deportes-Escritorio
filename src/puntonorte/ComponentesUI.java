package puntonorte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.function.IntPredicate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

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

    /** Aplica a las pestañas internas el mismo azul/coral que la barra superior. */
    public static void estilizarPestanas(JTabbedPane pestanas) {
        pestanas.setFont(Estilos.FUENTE);
        pestanas.setOpaque(true);
        pestanas.setBackground(Estilos.FONDO);
        pestanas.setUI(new BasicTabbedPaneUI() {
            {
                tabInsets = new Insets(0, 16, 0, 16);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                tabAreaInsets = new Insets(0, 0, 0, 0);
                contentBorderInsets = new Insets(1, 1, 1, 1);
            }

            @Override
            protected void paintTabArea(Graphics g, int placement, int selectedIndex) {
                int height = calculateTabAreaHeight(placement, runCount, maxTabHeight);
                g.setColor(Estilos.FONDO);
                g.fillRect(0, 0, tabPane.getWidth(), height);
                super.paintTabArea(g, placement, selectedIndex);
                g.setColor(Estilos.AZUL_CLARO);
                g.drawLine(0, 0, tabPane.getWidth(), 0);
                for (int i = 0; i < tabPane.getTabCount() - 1; i++) {
                    java.awt.Rectangle bounds = getTabBounds(i, new java.awt.Rectangle());
                    int x = bounds.x + bounds.width;
                    g.drawLine(x, 0, x, height - 1);
                }
            }

            @Override
            protected void paintTabBackground(Graphics g, int placement, int index,
                    int x, int y, int width, int height, boolean selected) {
                g.setColor(Estilos.FONDO);
                g.fillRect(x, y, width, height);
            }

            @Override
            protected void paintText(Graphics g, int placement, java.awt.Font font,
                    FontMetrics metrics, int index, String title, java.awt.Rectangle textRect,
                    boolean selected) {
                g.setFont(Estilos.FUENTE);
                g.setColor(selected ? Estilos.CORAL : Estilos.AZUL);
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }

        });
    }

    public static JPanel filaBotonCentrada(JButton boton, int ancho, int alto) {
        boton.setPreferredSize(new Dimension(ancho, alto));
        boton.setMaximumSize(new Dimension(ancho, alto));
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        fila.setOpaque(false);
        fila.setPreferredSize(new Dimension(ancho, alto));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        fila.add(boton);
        return fila;
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

    /** Tarjeta de sección para agrupar una tabla u otro contenido del módulo. */
    public static JPanel tarjeta(String titulo, Component interior) {
        JPanel exterior = new JPanel(new BorderLayout(0, 10));
        exterior.setBackground(Estilos.BEIGE);
        exterior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 1, 1, 1, Estilos.AZUL),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        if (titulo != null && !titulo.isBlank()) {
            JLabel encabezado = new JLabel(titulo);
            encabezado.setFont(Estilos.FUENTE_NEGRITA);
            encabezado.setForeground(Estilos.AZUL);
            encabezado.setBorder(BorderFactory.createEmptyBorder(0, 1, 5, 0));
            exterior.add(encabezado, BorderLayout.NORTH);
        }
        exterior.add(interior, BorderLayout.CENTER);
        return exterior;
    }

    /** Crea indicadores con etiquetas dinámicas; cada panel actualiza sus valores. */
    public static JPanel resumenVisual(String[] etiquetas, JLabel[] cantidades) {
        JPanel fila = new JPanel(new java.awt.GridLayout(1, etiquetas.length, 12, 0));
        fila.setOpaque(false);
        fila.setPreferredSize(new Dimension(1, 62));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        for (int i = 0; i < etiquetas.length; i++) {
            JPanel caja = new JPanel();
            caja.setBackground(Estilos.BEIGE);
            caja.setLayout(new javax.swing.BoxLayout(caja, javax.swing.BoxLayout.Y_AXIS));
            caja.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(4, 1, 1, 1, Estilos.AZUL),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            JLabel nombre = new JLabel(etiquetas[i]);
            nombre.setFont(Estilos.FUENTE_PEQUENA);
            nombre.setForeground(Estilos.TEXTO_SECUNDARIO);
            JLabel numero = new JLabel("0");
            numero.setFont(new Font("Tahoma", Font.BOLD, 18));
            numero.setForeground(Estilos.TEXTO);
            caja.add(nombre);
            caja.add(javax.swing.Box.createVerticalStrut(2));
            caja.add(numero);
            cantidades[i] = numero;
            fila.add(caja);
        }
        return fila;
    }

    /** Estilo de encabezado azul para tablas de módulos que usan modelos propios. */
    public static void estilizarTablaExistente(JTable tabla) {
        tabla.setFont(Estilos.FUENTE);
        tabla.setForeground(Estilos.TEXTO);
        tabla.setBackground(Estilos.BEIGE);
        tabla.setSelectionForeground(Estilos.AZUL);
        tabla.setSelectionBackground(Estilos.AZUL_CLARO);
        tabla.setRowHeight(30);
        tabla.setGridColor(Estilos.AZUL_CLARO);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(Estilos.FUENTE_NEGRITA);
        tabla.getTableHeader().setBackground(Estilos.AZUL);
        tabla.getTableHeader().setForeground(Estilos.BLANCO);
        tabla.getTableHeader().setOpaque(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setFont(Estilos.FUENTE_NEGRITA);
        encabezado.setForeground(Estilos.BLANCO);
        encabezado.setBackground(Estilos.AZUL);
        encabezado.setOpaque(true);
        encabezado.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        tabla.getTableHeader().setDefaultRenderer(encabezado);
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
