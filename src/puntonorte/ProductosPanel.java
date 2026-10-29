package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Ubicacion;
import puntonorte.servicio.Productoservicio;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.Stockservicio;
import puntonorte.servicio.Permisos;

/** Catálogo conectado a Productoservicio y operaciones de Stockservicio. */
public class ProductosPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JFrame owner;
    private final Productoservicio productos = new Productoservicio();
    private final Stockservicio stock = new Stockservicio();
    private final Runnable actualizarFiltrosCategorias;
    private final JPanel tablaPanel = new JPanel(new BorderLayout());
    private String filtroTexto = "", filtroCategoria = "", filtroSubcategoria = "", filtroTalle = "", filtroColor = "";
    private boolean filtroStockBajo, incluirInactivos, filtroStockSalonBajo;
    private ArrayList<ProductoVariante> filas = new ArrayList<>();
    private JTable tabla;

    public ProductosPanel(JFrame owner) {
        this(owner, () -> {});
    }

    public ProductosPanel(JFrame owner, Runnable actualizarFiltrosCategorias) {
        this.owner = owner;
        this.actualizarFiltrosCategorias = actualizarFiltrosCategorias == null ? () -> {} : actualizarFiltrosCategorias;
        setLayout(new BorderLayout()); setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));
        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.add(crearEncabezado(), BorderLayout.NORTH);
        tablaPanel.setBackground(Estilos.BEIGE);
        contenido.add(ComponentesUI.tarjeta("Catálogo de productos", tablaPanel), BorderLayout.CENTER);
        add(contenido, BorderLayout.CENTER);
        recargar();
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(0, 8)); panel.setOpaque(false); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        JPanel textos = new JPanel(); textos.setOpaque(false); textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(ComponentesUI.titulo("Listado de Productos")); textos.add(Box.createVerticalStrut(5));
        textos.add(ComponentesUI.subtitulo("Productos, variantes, precios y stock."));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); botones.setOpaque(false);
        if (Permisos.puedeCrearProducto()) {
            JButton agregar = boton("＋ Agregar producto", () -> new ProductosDialog(owner, null, this::recargar).setVisible(true));
            botones.add(agregar);
        }
        if (Permisos.puedeGestionarCatalogo()) {
            JButton editar = boton("✎ Editar", () -> editar(seleccion()));
            JButton estado = boton("Habilitar / inhabilitar", () -> cambiarEstado(seleccion()));
            botones.add(editar); botones.add(estado);
        }
        if (Permisos.puedeModificarPrecio()) {
            botones.add(boton("＋ Ubicación", this::crearUbicacion));
        }
        if (Permisos.puedeAjustarStock()) {
            botones.add(boton("Ajustar stock", () -> ajustar(seleccion())));
        }
        if (Permisos.puedeTransferirStock()) {
            botones.add(boton("Transferir", () -> transferir(seleccion())));
        }
        panel.add(textos, BorderLayout.NORTH); panel.add(botones, BorderLayout.SOUTH); return panel;
    }

    private JButton boton(String texto, Runnable accion) { JButton b = ComponentesUI.boton(texto, Estilos.CORAL, Estilos.AZUL); b.addActionListener(e -> accion.run()); return b; }

    public final void recargar() {
        actualizarFiltrosCategorias.run();
        tablaPanel.removeAll();
        ArrayList<Ubicacion> ubicaciones = new ArrayList<>();
        try { filas = productos.listarCatalogo(filtroTexto, idCategoria(filtroCategoria), filtroTalle,
                filtroColor, filtroStockBajo, incluirInactivos); }
        catch (ReglaNegocioException ex) { filas = new ArrayList<>(); }
        if (filtroStockSalonBajo) filas.removeIf(v -> v.getStockSalon() >= 3);
        if (!filtroSubcategoria.isEmpty()) {
            filas.removeIf(v -> v.getCategoria() == null || !filtroSubcategoria.equalsIgnoreCase(v.getCategoria()));
        }
        try { ubicaciones = stock.listarUbicaciones(); }
        catch (ReglaNegocioException ex) { /* la tabla se muestra sin detalle por ubicación */ }

        String[] columnas = new String[9 + ubicaciones.size()];
        String[] fijas = {"SKU", "Producto", "Categoría", "Marca", "Talle", "Color", "Precio", "Stock total"};
        System.arraycopy(fijas, 0, columnas, 0, fijas.length);
        for (int i = 0; i < ubicaciones.size(); i++) columnas[fijas.length + i] = ubicaciones.get(i).getNombre();
        columnas[columnas.length - 1] = "Estado";
        Object[][] datos = new Object[filas.size()][columnas.length];
        for (int i = 0; i < filas.size(); i++) {
            ProductoVariante v = filas.get(i);
            datos[i][0] = v.getSKU(); datos[i][1] = v.getNombreProducto(); datos[i][2] = v.getCategoria();
            datos[i][3] = v.getMarca(); datos[i][4] = v.getTalle(); datos[i][5] = v.getColor();
            datos[i][6] = dinero(v.getPrecioVenta()); datos[i][7] = v.getStockTotal();
            for (int j = 0; j < ubicaciones.size(); j++) {
                datos[i][8 + j] = stock.cantidadActual(v.getIdVariante(), ubicaciones.get(j).getIdUbicacion());
            }
            datos[i][datos[i].length - 1] = v.isActivo() ? "Activo" : "Inactivo";
        }
        tabla = ComponentesUI.tabla(columnas, datos); tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ComponentesUI.atenuarFilasInactivas(tabla, fila -> !filas.get(fila).isActivo());
        int[] anchos = {135, 170, 110, 110, 70, 90, 100, 85};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        for (int i = anchos.length; i < columnas.length - 1; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(columnas.length - 1).setPreferredWidth(80);
        tablaPanel.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER); tablaPanel.revalidate(); tablaPanel.repaint();
    }

    public void aplicarFiltros(String[] filtros) {
        if (filtros == null || filtros.length < 7) return;
        filtroTexto = filtros[0].trim();
        filtroCategoria = "Todas".equals(filtros[1]) ? "" : filtros[1];
        filtroSubcategoria = "Todas".equals(filtros[2]) ? "" : filtros[2];
        filtroTalle = "Todos".equals(filtros[3]) ? "" : filtros[3];
        filtroColor = "Todos".equals(filtros[4]) ? "" : filtros[4];
        filtroStockBajo = Boolean.parseBoolean(filtros[5]);
        incluirInactivos = Boolean.parseBoolean(filtros[6]);
        filtroStockSalonBajo = filtros.length >= 8 && Boolean.parseBoolean(filtros[7]);
        recargar();
    }

    public void seleccionarPrimerProductoStockBajo() {
        if (tabla == null || filas.isEmpty()) return;
        tabla.setRowSelectionInterval(0, 0);
        tabla.scrollRectToVisible(tabla.getCellRect(0, 0, true));
    }

    public ArrayList<String> listarSubcategorias() {
        ArrayList<String> nombres = new ArrayList<>();
        try {
            for (puntonorte.modelo.Categoria categoria : productos.listarCategorias()) {
                if (categoria.getIdCategoriaPadre() != null) nombres.add(categoria.getNombre());
            }
        } catch (RuntimeException ignored) { }
        return nombres;
    }

    private Integer idCategoria(String nombre) {
        if (nombre == null || nombre.isEmpty()) return null;
        try {
            for (puntonorte.modelo.Categoria categoria : productos.listarCategorias())
                if (nombre.equalsIgnoreCase(categoria.getNombre())) return categoria.getIdCategoria();
        } catch (RuntimeException ignored) { }
        return null;
    }

    private ProductoVariante seleccion() {
        if (tabla == null || tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(owner, "Seleccioná una variante de la tabla.", "Falta seleccionar", JOptionPane.WARNING_MESSAGE); return null; }
        return filas.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
    }
    private void editar(ProductoVariante v) { if (v != null) new ProductosDialog(owner, v, this::recargar).setVisible(true); }
    private void cambiarEstado(ProductoVariante v) {
        if (v == null) return;
        String motivo = v.isActivo() ? JOptionPane.showInputDialog(owner, "Motivo de la inhabilitación:") : null;
        if (v.isActivo() && motivo == null) return;
        try { productos.cambiarEstadoVariante(v.getIdVariante(), !v.isActivo(), motivo); recargar(); }
        catch (ReglaNegocioException ex) { error(ex); }
    }
    private void ajustar(ProductoVariante v) {
        if (v == null) return;
        JTextField cantidad = ComponentesUI.campo(), motivo = ComponentesUI.campo();
        JComboBox<Ubicacion> ubicacion = comboUbicaciones();
        JPanel p = formulario("Cantidad (+ suma, - resta)", cantidad, "Ubicación", ubicacion, "Motivo", motivo);
        if (JOptionPane.showConfirmDialog(owner, p, "Ajustar stock · " + v.getSKU(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        if (ubicacion.getSelectedItem() == null) { error(new ReglaNegocioException("Primero creá una ubicación.")); return; }
        try { int nuevo = stock.ajustarStock(v.getIdVariante(), ((Ubicacion) ubicacion.getSelectedItem()).getIdUbicacion(), entero(cantidad, "cantidad"), motivo.getText());
            JOptionPane.showMessageDialog(owner, "Stock actualizado: " + nuevo + " unidades."); recargar(); }
        catch (ReglaNegocioException ex) { error(ex); }
    }
    private void transferir(ProductoVariante v) {
        if (v == null) return;
        JComboBox<Ubicacion> origen = comboUbicaciones(), destino = comboUbicaciones();
        JTextField cantidad = ComponentesUI.campo();
        JPanel p = formulario("Origen", origen, "Destino", destino, "Cantidad", cantidad);
        if (JOptionPane.showConfirmDialog(owner, p, "Transferir stock · " + v.getSKU(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        if (origen.getSelectedItem() == null || destino.getSelectedItem() == null) { error(new ReglaNegocioException("Primero creá al menos dos ubicaciones.")); return; }
        try { stock.transferir(v.getIdVariante(), ((Ubicacion) origen.getSelectedItem()).getIdUbicacion(), ((Ubicacion) destino.getSelectedItem()).getIdUbicacion(), entero(cantidad, "cantidad")); recargar(); }
        catch (ReglaNegocioException ex) { error(ex); }
    }
    private void crearUbicacion() {
        String nombre = JOptionPane.showInputDialog(owner, "Nombre de la ubicación (ej.: Depósito central):", "Nueva ubicación", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null) return;
        try { stock.crearUbicacion(nombre); JOptionPane.showMessageDialog(owner, "Ubicación creada."); }
        catch (ReglaNegocioException ex) { error(ex); }
    }
    private JComboBox<Ubicacion> comboUbicaciones() { return new JComboBox<>(stock.listarUbicaciones().toArray(new Ubicacion[0])); }
    private JPanel formulario(Object... campos) { JPanel p = new JPanel(new java.awt.GridLayout(campos.length / 2, 2, 8, 8)); for (int i = 0; i < campos.length; i += 2) { p.add(new JLabel((String) campos[i])); p.add((java.awt.Component) campos[i + 1]); } return p; }
    private int entero(JTextField campo, String nombre) { try { return Integer.parseInt(campo.getText().trim()); } catch (NumberFormatException e) { throw new ReglaNegocioException("Ingresá un número entero válido para " + nombre + "."); } }
    private void error(ReglaNegocioException ex) { JOptionPane.showMessageDialog(owner, ex.getMessage(), "No se pudo realizar la acción", JOptionPane.WARNING_MESSAGE); }
    private String dinero(double valor) { return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(valor); }
}
