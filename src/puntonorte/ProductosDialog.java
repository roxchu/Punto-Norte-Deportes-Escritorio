package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import puntonorte.modelo.Categoria;
import puntonorte.modelo.Marca;
import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Proveedor;
import puntonorte.servicio.CompraService;
import puntonorte.servicio.Productoservicio;
import puntonorte.servicio.ReglaNegocioException;

/** Alta de producto (con una primera variante) y edición de la variante seleccionada. */
public class ProductosDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final Productoservicio servicio = new Productoservicio();
    private final CompraService compras = new CompraService();
    private final ProductoVariante variante;
    private final Runnable alGuardar;

    private JTextField nombre, descripcion, sku, talle, color, precio, costo, minimo, maximo;
    private JComboBox<Opcion> categoria, marca;
    private JComboBox<Proveedor> proveedor;

    public ProductosDialog(Window owner, ProductoVariante variante, Runnable alGuardar) {
        super(owner, variante == null ? "Agregar Producto" : "Editar producto", ModalityType.APPLICATION_MODAL);
        this.variante = variante;
        this.alGuardar = alGuardar;
        setSize(760, 560);
        setMinimumSize(new Dimension(680, 520));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Estilos.FONDO);
        add(crearContenido(), BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                nombre.requestFocusInWindow();
                if (variante == null && proveedor.getItemCount() == 0) {
                    JOptionPane.showMessageDialog(ProductosDialog.this,
                            "Todavía no hay proveedores registrados. Primero creá un proveedor y después cargá el producto.",
                            "Falta crear un proveedor", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    private JPanel crearContenido() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(Estilos.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        panel.add(ComponentesUI.titulo(variante == null ? "Agregar Producto" : "Editar producto y variante"),
                BorderLayout.NORTH);

        nombre = ComponentesUI.campo(); descripcion = ComponentesUI.campo(); sku = ComponentesUI.campo();
        talle = ComponentesUI.campo(); color = ComponentesUI.campo(); precio = ComponentesUI.campo();
        costo = ComponentesUI.campo(); minimo = ComponentesUI.campo(); maximo = ComponentesUI.campo();
        categoria = new JComboBox<>(opcionesCategorias());
        marca = new JComboBox<>(opcionesMarcas());
        proveedor = new JComboBox<>();
        for (Proveedor p : compras.proveedores()) proveedor.addItem(p);
        prepararEdicion();

        JPanel formulario = new JPanel(new GridLayout(variante == null ? 6 : 5, 1, 0, 12));
        formulario.setOpaque(false);
        formulario.add(fila(ComponentesUI.filaFormulario("Producto", nombre),
                ComponentesUI.filaFormulario("Descripción", descripcion)));
        formulario.add(fila(ComponentesUI.filaFormulario("Categoría", categoria),
                ComponentesUI.filaFormulario("Marca", marca)));
        if (variante == null) {
            formulario.add(fila(ComponentesUI.filaFormulario("Proveedor", proveedor),
                    ComponentesUI.filaFormulario("", new JLabel("El producto quedará asociado a este proveedor."))));
        }
        formulario.add(fila(ComponentesUI.filaFormulario("SKU", sku),
                ComponentesUI.filaFormulario("Precio de venta", precio)));
        formulario.add(filaTriple(ComponentesUI.filaFormulario("Talle", talle),
                ComponentesUI.filaFormulario("Color", color), ComponentesUI.filaFormulario("Costo ref.", costo)));
        formulario.add(fila(ComponentesUI.filaFormulario("Stock mínimo", minimo),
                ComponentesUI.filaFormulario("Stock máximo (0 = sin máximo)", maximo)));

        JPanel acciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        JButton cancelar = ComponentesUI.boton("Cancelar", Estilos.BLANCO, Estilos.AZUL);
        cancelar.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        JButton guardar = ComponentesUI.boton("✓ Guardar", Estilos.CORAL, Estilos.AZUL);
        JButton nuevaCategoria = ComponentesUI.boton("＋ Categoría", Estilos.BLANCO, Estilos.AZUL);
        nuevaCategoria.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        JButton nuevaMarca = ComponentesUI.boton("＋ Marca", Estilos.BLANCO, Estilos.AZUL);
        nuevaMarca.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardar());
        nuevaCategoria.addActionListener(e -> nuevaCategoria());
        nuevaMarca.addActionListener(e -> nuevaMarca());
        acciones.add(nuevaCategoria); acciones.add(nuevaMarca); acciones.add(cancelar); acciones.add(guardar);
        panel.add(formulario, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void prepararEdicion() {
        if (variante == null) return;
        nombre.setText(variante.getNombreProducto());
        descripcion.setText(variante.getDescripcionProducto());
        sku.setText(variante.getSKU()); talle.setText(variante.getTalle()); color.setText(variante.getColor());
        precio.setText(String.valueOf(variante.getPrecioVenta())); costo.setText(String.valueOf(variante.getCostoRef()));
        minimo.setText(String.valueOf(variante.getStockMinimo())); maximo.setText(String.valueOf(variante.getStockMaximo()));
        seleccionar(categoria, variante.getCategoria());
        seleccionar(marca, variante.getMarca());
    }

    private void guardar() {
        try {
            Opcion cat = (Opcion) categoria.getSelectedItem();
            Opcion mar = (Opcion) marca.getSelectedItem();
            if (cat == null || mar == null) throw new ReglaNegocioException("Cargá al menos una categoría y una marca.");
            ProductoVariante v = new ProductoVariante();
            if (variante != null) v.setIdVariante(variante.getIdVariante());
            v.setSKU(sku.getText()); v.setTalle(talle.getText()); v.setColor(color.getText());
            v.setPrecioVenta(numero(precio, "precio de venta"));
            v.setCostoRef(numero(costo, "costo de referencia"));
            v.setStockMinimo(entero(minimo, "stock mínimo")); v.setStockMaximo(entero(maximo, "stock máximo"));
            if (variante == null) {
                Proveedor seleccionado = (Proveedor) proveedor.getSelectedItem();
                if (seleccionado == null) {
                    throw new ReglaNegocioException("Primero creá un proveedor y después volvé a cargar el producto.");
                }
                List<ProductoVariante> variantes = new ArrayList<>(); variantes.add(v);
                servicio.crearProducto(nombre.getText(), descripcion.getText(), cat.id, mar.id,
                        seleccionado.getIdProveedor(), variantes);
            } else {
                boolean cambioProducto = !igual(nombre.getText(), variante.getNombreProducto())
                        || !igual(descripcion.getText(), variante.getDescripcionProducto())
                        || !cat.texto.equalsIgnoreCase(variante.getCategoria())
                        || !mar.texto.equalsIgnoreCase(variante.getMarca());
                boolean cambioVariante = !igual(sku.getText(), variante.getSKU())
                        || !igual(talle.getText(), variante.getTalle()) || !igual(color.getText(), variante.getColor())
                        || Double.compare(v.getPrecioVenta(), variante.getPrecioVenta()) != 0
                        || Double.compare(v.getCostoRef(), variante.getCostoRef()) != 0
                        || v.getStockMinimo() != variante.getStockMinimo() || v.getStockMaximo() != variante.getStockMaximo();
                if (!cambioProducto && !cambioVariante) throw new ReglaNegocioException("No hay cambios para guardar.");
                if (cambioProducto) servicio.editarProducto(variante.getIdProducto(), nombre.getText(), descripcion.getText(), cat.id, mar.id);
                if (cambioVariante) servicio.editarVariante(v);
            }
            alGuardar.run();
            dispose();
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void nuevaCategoria() {
        JTextField nombreNueva = ComponentesUI.campo();
        JComboBox<PadreOpcion> padre = new JComboBox<>(opcionesPadre());
        JPanel formulario = new JPanel(new GridLayout(2, 2, 8, 8));
        formulario.add(new JLabel("Nombre:")); formulario.add(nombreNueva);
        formulario.add(new JLabel("Categoría padre:")); formulario.add(padre);
        if (JOptionPane.showConfirmDialog(this, formulario, "Nueva categoría", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            PadreOpcion opcionPadre = (PadreOpcion) padre.getSelectedItem();
            int id = servicio.crearCategoria(nombreNueva.getText(), opcionPadre == null ? null : opcionPadre.id);
            categoria.setModel(new DefaultComboBoxModel<>(opcionesCategorias()));
            seleccionarPorId(categoria, id);
            alGuardar.run();
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo crear", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void nuevaMarca() {
        String nombreNueva = JOptionPane.showInputDialog(this, "Nombre de la marca:", "Nueva marca",
                JOptionPane.QUESTION_MESSAGE);
        if (nombreNueva == null) return;
        try {
            int id = servicio.crearMarca(nombreNueva);
            marca.setModel(new DefaultComboBoxModel<>(opcionesMarcas()));
            seleccionarPorId(marca, id);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo crear", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double numero(JTextField campo, String etiqueta) {
        try { return Double.parseDouble(campo.getText().trim().replace(',', '.')); }
        catch (NumberFormatException e) { throw new ReglaNegocioException("Ingresá un número válido para " + etiqueta + "."); }
    }
    private int entero(JTextField campo, String etiqueta) {
        try { return Integer.parseInt(campo.getText().trim()); }
        catch (NumberFormatException e) { throw new ReglaNegocioException("Ingresá un número entero válido para " + etiqueta + "."); }
    }
    private boolean igual(String a, String b) { return (a == null ? "" : a.trim()).equals(b == null ? "" : b.trim()); }
    private Opcion[] opcionesCategorias() {
        List<Categoria> lista = servicio.listarCategorias(); Opcion[] r = new Opcion[lista.size()];
        for (int i = 0; i < r.length; i++) r[i] = new Opcion(lista.get(i).getIdCategoria(), lista.get(i).getNombre()); return r;
    }
    private PadreOpcion[] opcionesPadre() {
        List<Categoria> lista = servicio.listarCategorias();
        PadreOpcion[] r = new PadreOpcion[lista.size() + 1];
        r[0] = new PadreOpcion(null, "Sin padre (categoría principal)");
        for (int i = 0; i < lista.size(); i++) {
            Categoria c = lista.get(i);
            r[i + 1] = new PadreOpcion(c.getIdCategoria(), nombreConRuta(c, lista));
        }
        return r;
    }
    private String nombreConRuta(Categoria categoriaActual, List<Categoria> todas) {
        String resultado = categoriaActual.getNombre();
        Integer padre = categoriaActual.getIdCategoriaPadre();
        while (padre != null) {
            Categoria encontrada = null;
            for (Categoria c : todas) if (c.getIdCategoria() == padre) { encontrada = c; break; }
            if (encontrada == null) break;
            resultado = encontrada.getNombre() + " > " + resultado;
            padre = encontrada.getIdCategoriaPadre();
        }
        return resultado;
    }
    private Opcion[] opcionesMarcas() {
        List<Marca> lista = servicio.listarMarcas(); Opcion[] r = new Opcion[lista.size()];
        for (int i = 0; i < r.length; i++) r[i] = new Opcion(lista.get(i).getIdMarca(), lista.get(i).getNombre()); return r;
    }
    private void seleccionar(JComboBox<Opcion> combo, String texto) {
        for (int i = 0; i < combo.getItemCount(); i++) if (combo.getItemAt(i).texto.equalsIgnoreCase(texto)) combo.setSelectedIndex(i);
    }
    private void seleccionarPorId(JComboBox<Opcion> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) if (combo.getItemAt(i).id == id) combo.setSelectedIndex(i);
    }
    private JPanel fila(JPanel a, JPanel b) { JPanel p = new JPanel(new GridLayout(1, 2, 12, 0)); p.setOpaque(false); p.add(a); p.add(b); return p; }
    private JPanel filaTriple(JPanel a, JPanel b, JPanel c) { JPanel p = new JPanel(new GridLayout(1, 3, 12, 0)); p.setOpaque(false); p.add(a); p.add(b); p.add(c); return p; }
    private static class Opcion { final int id; final String texto; Opcion(int id, String texto) { this.id = id; this.texto = texto; } @Override public String toString() { return texto; } }
    private static class PadreOpcion { final Integer id; final String texto; PadreOpcion(Integer id, String texto) { this.id = id; this.texto = texto; } @Override public String toString() { return texto; } }
}
