package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Categoria;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.Ventaservicio;
import puntonorte.servicio.Ventaservicio.PedidoVenta;
import puntonorte.servicio.Permisos;
import puntonorte.servicio.Productoservicio;

public class VentasPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JFrame owner;
    private final Ventaservicio servicio = new Ventaservicio();
    private final PedidoVenta pedido = new PedidoVenta();
    private final Productoservicio catalogoService = new Productoservicio();
    private JTable tabla;
    private String textoBusqueda = "";
    private ArrayList<ProductoVariante> catalogo = new ArrayList<>();
    private JButton verPedido;
    private String filtroCategoria = "", filtroSubcategoria = "", filtroTalle = "", filtroColor = "";
    private boolean soloDisponibles = true;

    public VentasPanel(JFrame owner) {
        this.owner = owner;
        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        if (!Permisos.puedeVender()) {
            JPanel consulta = new JPanel(new BorderLayout(0, 14));
            consulta.setOpaque(false);
            consulta.add(ComponentesUI.titulo("Consulta de ventas"), BorderLayout.NORTH);
            consulta.add(ComponentesUI.subtitulo("Este perfil puede consultar las ventas registradas, pero no cobrar."), BorderLayout.CENTER);
            JButton historial = ComponentesUI.boton("Ventas realizadas", Estilos.CORAL, Estilos.AZUL);
            historial.addActionListener(e -> new VentasHistorialDialog(owner, servicio).setVisible(true));
            JPanel accion = new JPanel(new FlowLayout(FlowLayout.LEFT));
            accion.setOpaque(false);
            accion.add(historial);
            consulta.add(accion, BorderLayout.SOUTH);
            add(consulta, BorderLayout.NORTH);
            return;
        }

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.add(ComponentesUI.titulo("Registro de Venta"));
        titulos.add(Box.createVerticalStrut(4));
        titulos.add(ComponentesUI.subtitulo("Seleccioná productos disponibles en el salón y prepará el pedido."));
        verPedido = ComponentesUI.boton("Ver pedido (0)", Estilos.CORAL, Estilos.AZUL);
        verPedido.addActionListener(e -> abrirPedido());
        encabezado.add(titulos, BorderLayout.WEST);
        JPanel accionesCabecera = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesCabecera.setOpaque(false);
        JButton historial = ComponentesUI.botonBorde(Permisos.puedeVerTodasLasVentas()
                ? "Ventas realizadas" : "Mis ventas");
        historial.addActionListener(e -> new VentasHistorialDialog(owner, servicio).setVisible(true));
        accionesCabecera.add(historial);
        accionesCabecera.add(verPedido);
        encabezado.add(accionesCabecera, BorderLayout.EAST);
        contenido.add(encabezado);
        contenido.add(Box.createVerticalStrut(16));

        tabla = new JTable();
        tabla.setRowHeight(30);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JButton agregar = ComponentesUI.boton("+ Agregar al pedido", Estilos.AZUL, java.awt.Color.WHITE);
        agregar.addActionListener(e -> agregarSeleccionado());
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) agregarSeleccionado(); }
        });
        JPanel catalogoVisual = new JPanel(new BorderLayout(0, 8));
        catalogoVisual.setOpaque(false);
        catalogoVisual.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER);
        JPanel accionesCatalogo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        accionesCatalogo.setOpaque(false);
        accionesCatalogo.add(agregar);
        catalogoVisual.add(accionesCatalogo, BorderLayout.SOUTH);
        contenido.add(ComponentesUI.tarjeta("Productos disponibles en salón", catalogoVisual));
        add(contenido, BorderLayout.CENTER);
        recargar();
    }

    public void recargar() {
        if (!Permisos.puedeVender()) return;
        try {
            catalogo = servicio.listarCatalogo(textoBusqueda);
            aplicarFiltrosCatalogo();
            Object[][] filas = new Object[catalogo.size()][5];
            for (int i = 0; i < catalogo.size(); i++) {
                ProductoVariante v = catalogo.get(i);
                filas[i] = new Object[] {v.getSKU(), v.getNombreProducto(), v.getTalle() + " · " + v.getColor(),
                        v.getStockSalon(), FormatoDinero.pesos(v.getPrecioVenta())};
            }
            tabla.setModel(new DefaultTableModel(filas, new String[] {"SKU", "Producto", "Variante", "Stock salón", "Precio"}) {
                private static final long serialVersionUID = 1L;
                @Override public boolean isCellEditable(int row, int col) { return false; }
            });
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ventas", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void aplicarFiltros(String[] filtros) {
        if (filtros == null || filtros.length < 6) return;
        textoBusqueda = filtros[0] == null ? "" : filtros[0];
        filtroCategoria = "Todas".equals(filtros[1]) ? "" : filtros[1];
        filtroSubcategoria = "Todas".equals(filtros[2]) ? "" : filtros[2];
        filtroTalle = "Todos".equals(filtros[3]) ? "" : filtros[3];
        filtroColor = "Todos".equals(filtros[4]) ? "" : filtros[4];
        soloDisponibles = Boolean.parseBoolean(filtros[5]);
        recargar();
    }

    private void aplicarFiltrosCatalogo() {
        final Map<Integer, Categoria> porId = new HashMap<>();
        try {
            for (Categoria categoria : catalogoService.listarCategorias()) porId.put(categoria.getIdCategoria(), categoria);
        } catch (RuntimeException ignored) { }
        catalogo.removeIf(variante -> {
            if (soloDisponibles && variante.getStockSalon() <= 0) return true;
            if (!filtroSubcategoria.isEmpty() && !filtroSubcategoria.equalsIgnoreCase(variante.getCategoria())) return true;
            if (!filtroCategoria.isEmpty() && !categoriaDentroDe(variante.getCategoria(), filtroCategoria, porId)) return true;
            if (!filtroTalle.isEmpty() && !filtroTalle.equalsIgnoreCase(variante.getTalle())) return true;
            return !filtroColor.isEmpty() && !filtroColor.equalsIgnoreCase(variante.getColor());
        });
    }

    private boolean categoriaDentroDe(String nombreVariante, String nombreBuscado, Map<Integer, Categoria> porId) {
        if (nombreVariante == null) return false;
        Categoria actual = null;
        for (Categoria categoria : porId.values()) {
            if (nombreVariante.equalsIgnoreCase(categoria.getNombre())) { actual = categoria; break; }
        }
        while (actual != null) {
            if (nombreBuscado.equalsIgnoreCase(actual.getNombre())) return true;
            actual = actual.getIdCategoriaPadre() == null ? null : porId.get(actual.getIdCategoriaPadre());
        }
        return nombreVariante.equalsIgnoreCase(nombreBuscado);
    }

    private void agregarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= catalogo.size()) {
            JOptionPane.showMessageDialog(this, "Seleccioná una variante del catálogo.", "Ventas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ProductoVariante v = catalogo.get(tabla.convertRowIndexToModel(fila));
        for (puntonorte.servicio.Ventaservicio.LineaPedido l : pedido.lineas) {
            if (l.idVariante == v.getIdVariante()) { l.cantidad++; verPedido.setText("Ver pedido (" + totalUnidades() + ")"); return; }
        }
        pedido.agregar(v.getIdVariante(), 1);
        verPedido.setText("Ver pedido (" + totalUnidades() + ")");
    }

    private int totalUnidades() { return pedido.lineas.stream().mapToInt(l -> l.cantidad).sum(); }

    private void abrirPedido() {
        if (pedido.lineas.isEmpty()) { JOptionPane.showMessageDialog(this, "Agregá al menos un producto al pedido.", "Ventas", JOptionPane.WARNING_MESSAGE); return; }
        new PedidoVentaDialog(owner, servicio, pedido).setVisible(true);
        verPedido.setText("Ver pedido (" + totalUnidades() + ")");
        recargar();
    }
}
