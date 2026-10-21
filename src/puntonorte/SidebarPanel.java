package puntonorte;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.function.Consumer;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import puntonorte.servicio.Sesion;
import puntonorte.modelo.Categoria;

public class SidebarPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final CardLayout filtrosLayout;
    private final JPanel filtros;
    private JTextField productoBuscar;
    private JComboBox<String> productoCategoria, productoTalle, productoColor;
    private JComboBox<String> productoSubcategoria;
    private JCheckBox productoStockBajo, productoInactivos;
    private Consumer<String[]> aplicarFiltrosProductos = valores -> {};
    private JTextField ventaBuscar, usuarioBuscar;
    private JComboBox<String> ventaCategoria, ventaSubcategoria, ventaTalle, ventaColor;
    private JCheckBox ventasSoloDisponibles;
    private Consumer<String[]> aplicarFiltrosVentas = valores -> {};
    private Consumer<String> aplicarFiltroUsuarios = valor -> {};
    private JTextField historialUsuario, historialOperacion, historialFecha;
    private JComboBox<String> historialModulo;
    private Consumer<String[]> aplicarFiltrosHistorial = valores -> {};
    private List<Categoria> categoriasFiltro = new java.util.ArrayList<>();

    public SidebarPanel(Runnable onCuenta, Runnable onSalir) {

        setLayout(new BorderLayout());
        setBackground(Estilos.AZUL);
        setPreferredSize(new Dimension(Estilos.ANCHO_SIDEBAR, 100));

        JPanel superior = new SidebarScrollContent();
        superior.setOpaque(false);
        superior.setBorder(BorderFactory.createEmptyBorder(14, 10, 10, 10));
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));

        superior.add(crearLogo());
        superior.add(Box.createVerticalStrut(10));
        superior.add(crearUsuario(onCuenta, onSalir));
        superior.add(Box.createVerticalStrut(18));

        filtrosLayout = new CardLayout();
        filtros = new JPanel(filtrosLayout);
        filtros.setOpaque(false);

        filtros.add(crearInicio(), "INICIO");
        filtros.add(crearProductos(), "PRODUCTOS");
        filtros.add(crearProveedores(), "PROVEEDORES");
        filtros.add(crearVentas(), "VENTAS");
        filtros.add(crearDescuentos(), "DESCUENTOS");
        filtros.add(crearHistorial(), "HISTORIAL");
        filtros.add(crearUsuarios(), "USUARIOS");
        filtros.add(crearCuenta(), "CUENTA");

        superior.add(filtros);

        JButton accesibilidad = ComponentesUI.boton(
                "Accesibilidad",
                Estilos.AZUL,
                Color.WHITE
        );
        accesibilidad.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        accesibilidad.setPreferredSize(new Dimension(190, 34));
        accesibilidad.setMaximumSize(new Dimension(190, 34));

        JPanel pieDesplazable = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pieDesplazable.setOpaque(false);
        pieDesplazable.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        pieDesplazable.setBorder(BorderFactory.createEmptyBorder(18, 0, 12, 0));
        pieDesplazable.add(accesibilidad);
        superior.add(pieDesplazable);

        JScrollPane scrollSuperior = new JScrollPane(superior,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSuperior.setBorder(null);
        scrollSuperior.setOpaque(false);
        scrollSuperior.getViewport().setOpaque(false);
        add(scrollSuperior, BorderLayout.CENTER);
    }

    private JPanel crearLogo() {

        JPanel caja = new JPanel(new BorderLayout());
        caja.setBackground(Estilos.FONDO);
        caja.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        caja.setPreferredSize(new Dimension(180, 95));
        caja.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel logo = new JLabel("", JLabel.CENTER);
        javax.swing.ImageIcon icono = cargarLogo(160, 72);

        if (icono.getIconWidth() > 0) {
            logo.setIcon(icono);
        } else {
            logo.setText("<html><center><b>PUNTO NORTE<br>DEPORTES</b></center></html>");
            logo.setForeground(Estilos.AZUL);
            logo.setFont(Estilos.FUENTE_NEGRITA);
        }

        caja.add(logo, BorderLayout.CENTER);

        return caja;
    }

    private javax.swing.ImageIcon cargarLogo(int ancho, int alto) {
        java.net.URL recurso = SidebarPanel.class.getResource("/puntonorte/logoPuntoNorte.jpeg");
        if (recurso == null) return new javax.swing.ImageIcon();
        Image original = new javax.swing.ImageIcon(recurso).getImage();
        int anchoFinal = original.getWidth(null);
        int altoFinal = original.getHeight(null);
        double escala = Math.min((double) ancho / anchoFinal, (double) alto / altoFinal);
        Image imagen = original.getScaledInstance(Math.max(1, (int) (anchoFinal * escala)),
                Math.max(1, (int) (altoFinal * escala)), Image.SCALE_SMOOTH);
        return new javax.swing.ImageIcon(imagen);
    }

    private static class SidebarScrollContent extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 22; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(22, (orientation == javax.swing.SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width) - 22);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private JPanel crearUsuario(Runnable onCuenta, Runnable onSalir) {

        JPanel panel = new JPanel();
        panel.setBackground(Estilos.AZUL_OSCURO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel nombre = new JLabel(Sesion.getNombreCompleto());
        nombre.setForeground(Color.WHITE);
        nombre.setFont(Estilos.FUENTE_NEGRITA);
        nombre.setAlignmentX(CENTER_ALIGNMENT);
        nombre.setHorizontalAlignment(JLabel.CENTER);
        nombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, nombre.getPreferredSize().height));

        JLabel rol = new JLabel(Sesion.getNombreRol());
        rol.setForeground(Estilos.AZUL_CLARO);
        rol.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
        rol.setAlignmentX(CENTER_ALIGNMENT);
        rol.setHorizontalAlignment(JLabel.CENTER);
        rol.setMaximumSize(new Dimension(Integer.MAX_VALUE, rol.getPreferredSize().height));

        JButton cuenta = ComponentesUI.boton("Cuenta", Estilos.AZUL_CLARO, Estilos.AZUL);
        JButton salir = ComponentesUI.boton("Salir", Estilos.ROJO, Color.WHITE);

        cuenta.addActionListener(e -> onCuenta.run());
        salir.addActionListener(e -> onSalir.run());

        JPanel acciones = new JPanel(new GridLayout(1, 2, 6, 0));
        acciones.setOpaque(false);
        acciones.setAlignmentX(CENTER_ALIGNMENT);
        acciones.setPreferredSize(new Dimension(164, 32));
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        acciones.add(cuenta);
        acciones.add(salir);

        panel.add(nombre);
        panel.add(Box.createVerticalStrut(3));
        panel.add(rol);
        panel.add(Box.createVerticalStrut(10));
        panel.add(acciones);

        return panel;
    }

    private JPanel crearInicio() {

        JPanel panel = baseSeccion("Panel del empleado");
        panel.add(texto("Actividad reciente del sistema."));

        return panel;
    }

    private JPanel crearProductos() {

        JPanel panel = baseSeccion("Filtros");

        panel.add(etiquetaBlanca("Buscar producto"));
        productoBuscar = campoSidebar();
        panel.add(productoBuscar);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Categoría"));
        productoCategoria = comboSidebar("Todas");
        panel.add(productoCategoria);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Subcategoría"));
        productoSubcategoria = comboSidebar("Todas");
        productoSubcategoria.setEnabled(false);
        productoCategoria.addActionListener(e -> actualizarSubcategorias(productoCategoria, productoSubcategoria));
        panel.add(productoSubcategoria);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Talle"));
        productoTalle = comboSidebar("Todos", "S", "M", "L", "XL", "42");
        panel.add(productoTalle);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Color"));
        productoColor = comboSidebar("Todos", "Negro", "Blanco", "Rojo", "Azul", "Gris");
        panel.add(productoColor);

        productoStockBajo = new JCheckBox("Solo stock bajo");
        prepararCheck(productoStockBajo);
        panel.add(Box.createVerticalStrut(9));
        panel.add(productoStockBajo);

        productoInactivos = new JCheckBox("Mostrar inactivos");
        prepararCheck(productoInactivos);
        panel.add(Box.createVerticalStrut(5));
        panel.add(productoInactivos);

        JButton aplicar = ComponentesUI.boton("⌕ Aplicar filtros", Estilos.CORAL, Estilos.AZUL);
        aplicar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton limpiar = ComponentesUI.boton("↺ Limpiar", Estilos.AZUL, Color.WHITE);
        limpiar.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        limpiar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        panel.add(Box.createVerticalStrut(12));
        panel.add(aplicar);
        panel.add(Box.createVerticalStrut(7));
        panel.add(limpiar);
        aplicar.addActionListener(e -> enviarFiltrosProductos());
        limpiar.addActionListener(e -> {
            productoBuscar.setText(""); productoCategoria.setSelectedIndex(0);
            productoSubcategoria.setSelectedIndex(0); productoTalle.setSelectedIndex(0);
            productoColor.setSelectedIndex(0); productoStockBajo.setSelected(false);
            productoInactivos.setSelected(false); enviarFiltrosProductos();
        });

        return panel;
    }

    private JPanel crearProveedores() {

        JPanel panel = baseSeccion("Proveedores");

        panel.add(etiquetaBlanca("Buscar"));
        panel.add(campoSidebar());

        panel.add(espacio());
        panel.add(etiquetaBlanca("Estado del pedido"));
        panel.add(comboSidebar(
                "Todos",
                "Solicitado",
                "Preparando",
                "En camino",
                "Recibido",
                "Cancelado"
        ));

        JButton limpiar = ComponentesUI.boton("↺ Limpiar", Estilos.AZUL, Color.WHITE);
        limpiar.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        limpiar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        panel.add(Box.createVerticalStrut(12));
        panel.add(limpiar);

        return panel;
    }

    private JPanel crearVentas() {

        JPanel panel = baseSeccion("Catálogo");

        panel.add(etiquetaBlanca("Buscar producto"));
        ventaBuscar = campoSidebar();
        panel.add(ventaBuscar);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Categoría"));
        ventaCategoria = comboSidebar("Todas");
        panel.add(ventaCategoria);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Subcategoría"));
        ventaSubcategoria = comboSidebar("Todas");
        ventaSubcategoria.setEnabled(false);
        ventaCategoria.addActionListener(e -> actualizarSubcategorias(ventaCategoria, ventaSubcategoria));
        panel.add(ventaSubcategoria);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Talle"));
        ventaTalle = comboSidebar("Todos", "S", "M", "L", "XL", "42");
        panel.add(ventaTalle);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Color"));
        ventaColor = comboSidebar("Todos", "Negro", "Blanco", "Rojo", "Azul", "Gris");
        panel.add(ventaColor);

        ventasSoloDisponibles = new JCheckBox("Solo disponibles");
        prepararCheck(ventasSoloDisponibles);
        ventasSoloDisponibles.setSelected(false);

        panel.add(Box.createVerticalStrut(9));
        panel.add(ventasSoloDisponibles);

        JButton aplicar = ComponentesUI.boton("⌕ Aplicar filtros", Estilos.CORAL, Estilos.AZUL);
        aplicar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JButton limpiar = ComponentesUI.boton("↺ Limpiar", Estilos.AZUL, Color.WHITE);
        limpiar.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        limpiar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        panel.add(Box.createVerticalStrut(12));
        panel.add(aplicar);
        panel.add(Box.createVerticalStrut(7));
        panel.add(limpiar);
        aplicar.addActionListener(e -> enviarFiltrosVentas());
        ventaBuscar.addActionListener(e -> enviarFiltrosVentas());
        limpiar.addActionListener(e -> {
            ventaBuscar.setText(""); ventaCategoria.setSelectedIndex(0);
            ventaSubcategoria.setSelectedIndex(0); ventaTalle.setSelectedIndex(0);
            ventaColor.setSelectedIndex(0); ventasSoloDisponibles.setSelected(false); enviarFiltrosVentas();
        });

        return panel;
    }

    private JPanel crearDescuentos() {
        JPanel panel = baseSeccion("Descuentos");
        panel.add(texto("Administración de descuentos exclusiva del Supervisor."));
        panel.add(Box.createVerticalStrut(10));
        panel.add(texto("Las bajas se realizan desactivando el descuento para conservar el historial."));
        return panel;
    }

    private JPanel crearHistorial() {

        JPanel panel = baseSeccion("Filtros");

        panel.add(etiquetaBlanca("Usuario"));
        historialUsuario = campoSidebar();
        panel.add(historialUsuario);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Módulo"));
        historialModulo = comboSidebar(
                "Todos",
                "Productos",
                "Proveedores",
                "Ventas",
                "Usuarios",
                "Stock"
        );
        panel.add(historialModulo);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Operación"));
        historialOperacion = campoSidebar();
        panel.add(historialOperacion);

        panel.add(espacio());
        panel.add(etiquetaBlanca("Fecha"));
        historialFecha = campoSidebar();
        historialFecha.setToolTipText("Formato: dd/MM/aaaa");
        panel.add(historialFecha);

        JButton aplicar = ComponentesUI.boton("⌕ Aplicar filtros", Estilos.CORAL, Estilos.AZUL);
        aplicar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton limpiar = ComponentesUI.boton("↺ Limpiar", Estilos.AZUL, Color.WHITE);
        limpiar.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        limpiar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        panel.add(Box.createVerticalStrut(12));
        panel.add(aplicar);
        panel.add(Box.createVerticalStrut(7));
        panel.add(limpiar);
        aplicar.addActionListener(e -> enviarFiltrosHistorial());
        limpiar.addActionListener(e -> {
            historialUsuario.setText(""); historialModulo.setSelectedIndex(0);
            historialOperacion.setText(""); historialFecha.setText(""); enviarFiltrosHistorial();
        });

        return panel;
    }

    private JPanel crearUsuarios() {

        JPanel panel = baseSeccion("Usuarios");
        panel.add(texto("Solicitudes y usuarios del sistema."));
        panel.add(espacio());
        panel.add(etiquetaBlanca("Buscar usuario"));
        usuarioBuscar = campoSidebar();
        panel.add(usuarioBuscar);
        JButton aplicar = ComponentesUI.boton("⌕ Buscar", Estilos.CORAL, Estilos.AZUL);
        aplicar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JButton limpiar = ComponentesUI.boton("↺ Limpiar", Estilos.AZUL, Color.WHITE);
        limpiar.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        limpiar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(Box.createVerticalStrut(10)); panel.add(aplicar);
        panel.add(Box.createVerticalStrut(7)); panel.add(limpiar);
        aplicar.addActionListener(e -> aplicarFiltroUsuarios.accept(usuarioBuscar.getText()));
        usuarioBuscar.addActionListener(e -> aplicarFiltroUsuarios.accept(usuarioBuscar.getText()));
        limpiar.addActionListener(e -> { usuarioBuscar.setText(""); aplicarFiltroUsuarios.accept(""); });

        return panel;
    }

    private JPanel crearCuenta() {

        JPanel panel = baseSeccion("Mi cuenta");
        panel.add(texto("Datos del usuario."));

        return panel;
    }

    private JPanel baseSeccion(String titulo) {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(titulo);
        label.setForeground(Estilos.CORAL);
        label.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 18));
        label.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(16));

        return panel;
    }

    private JLabel etiquetaBlanca(String texto) {

        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        label.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    private JLabel texto(String texto) {

        JLabel label = new JLabel("<html>" + texto + "</html>");
        label.setForeground(new Color(0xDB, 0xE7, 0xF3));
        label.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 12));
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    private JTextField campoSidebar() {

        JTextField campo = ComponentesUI.campo();
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        campo.setAlignmentX(LEFT_ALIGNMENT);

        return campo;
    }

    private JComboBox<String> comboSidebar(String... opciones) {

        JComboBox<String> combo = ComponentesUI.combo(opciones);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        combo.setAlignmentX(LEFT_ALIGNMENT);

        return combo;
    }

    private void prepararCheck(JCheckBox check) {

        check.setOpaque(false);
        check.setForeground(Color.WHITE);
        check.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
        check.setAlignmentX(LEFT_ALIGNMENT);
    }

    private Box.Filler espacio() {

        return new Box.Filler(
                new Dimension(0, 9),
                new Dimension(0, 9),
                new Dimension(0, 9)
        );
    }

    public void mostrarSeccion(String modulo) {

        filtrosLayout.show(filtros, modulo);
    }

    public void setAplicarFiltrosProductos(Consumer<String[]> accion) {
        aplicarFiltrosProductos = accion == null ? valores -> {} : accion;
    }

    public void setOpcionesCategorias(List<Categoria> opciones) {
        categoriasFiltro = opciones == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(opciones);
        cargarCategorias(productoCategoria);
        cargarCategorias(ventaCategoria);
        actualizarSubcategorias(productoCategoria, productoSubcategoria);
        actualizarSubcategorias(ventaCategoria, ventaSubcategoria);
    }

    private void cargarCategorias(JComboBox<String> combo) {
        if (combo == null) return;
        String seleccionado = String.valueOf(combo.getSelectedItem());
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        modelo.addElement("Todas");
        for (Categoria c : categoriasFiltro) {
            if (c.getIdCategoriaPadre() == null) modelo.addElement(c.getNombre());
        }
        combo.setModel(modelo);
        combo.setSelectedItem(seleccionado);
        if (combo.getSelectedIndex() < 0) combo.setSelectedIndex(0);
    }

    private void actualizarSubcategorias(JComboBox<String> categoria, JComboBox<String> subcategoria) {
        if (categoria == null || subcategoria == null) return;
        String subcategoriaSeleccionada = String.valueOf(subcategoria.getSelectedItem());
        String nombre = String.valueOf(categoria.getSelectedItem());
        Categoria padre = null;
        if (!"Todas".equals(nombre)) for (Categoria c : categoriasFiltro)
            if (c.getNombre().equalsIgnoreCase(nombre) && c.getIdCategoriaPadre() == null) { padre = c; break; }
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        modelo.addElement("Todas");
        if (padre != null) for (Categoria c : categoriasFiltro)
            if (c.getIdCategoriaPadre() != null && c.getIdCategoriaPadre().intValue() == padre.getIdCategoria())
                modelo.addElement(c.getNombre());
        subcategoria.setModel(modelo);
        subcategoria.setSelectedItem(subcategoriaSeleccionada);
        if (subcategoria.getSelectedIndex() < 0) subcategoria.setSelectedIndex(0);
        subcategoria.setEnabled(padre != null && modelo.getSize() > 1);
    }

    public void setAplicarFiltrosVentas(Consumer<String[]> accion) {
        aplicarFiltrosVentas = accion == null ? valores -> {} : accion;
    }

    public void setAplicarFiltroUsuarios(Consumer<String> accion) {
        aplicarFiltroUsuarios = accion == null ? valor -> {} : accion;
    }

    public void setAplicarFiltrosHistorial(Consumer<String[]> accion) {
        aplicarFiltrosHistorial = accion == null ? valores -> {} : accion;
    }

    private void enviarFiltrosProductos() {
        aplicarFiltrosProductos.accept(new String[] {productoBuscar.getText(),
                String.valueOf(productoCategoria.getSelectedItem()),
                String.valueOf(productoSubcategoria.getSelectedItem()),
                String.valueOf(productoTalle.getSelectedItem()),
                String.valueOf(productoColor.getSelectedItem()),
                String.valueOf(productoStockBajo.isSelected()),
                String.valueOf(productoInactivos.isSelected())});
    }

    private void enviarFiltrosVentas() {
        aplicarFiltrosVentas.accept(new String[] {ventaBuscar.getText(),
                String.valueOf(ventaCategoria.getSelectedItem()),
                String.valueOf(ventaSubcategoria.getSelectedItem()),
                String.valueOf(ventaTalle.getSelectedItem()),
                String.valueOf(ventaColor.getSelectedItem()),
                String.valueOf(ventasSoloDisponibles.isSelected())});
    }

    private void enviarFiltrosHistorial() {
        aplicarFiltrosHistorial.accept(new String[] {historialUsuario.getText(),
                String.valueOf(historialModulo.getSelectedItem()), historialOperacion.getText(), historialFecha.getText()});
    }
}
