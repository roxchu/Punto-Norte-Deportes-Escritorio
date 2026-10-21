package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import puntonorte.modelo.Descuentos;
import puntonorte.modelo.MedioPago;
import puntonorte.modelo.Naturaleza;
import puntonorte.modelo.Producto;
import puntonorte.servicio.DescuentosServicio;
import puntonorte.servicio.ReglaNegocioException;

/** Alta de descuentos según los cuatro tipos permitidos por descuentos.tipo en MySQL. */
public class DescuentoDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final DescuentosServicio servicio = new DescuentosServicio();
    private final Runnable alGuardar;
    private final JTextField nombre = ComponentesUI.campo();
    private final JTextField valor = ComponentesUI.campo();
    private final JTextField inicio = ComponentesUI.campo();
    private final JTextField fin = ComponentesUI.campo();
    private final JTextField cantidadCompra = ComponentesUI.campo();
    private final JTextField cantidadPaga = ComponentesUI.campo();
    private final JComboBox<String> tipo = new JComboBox<>(new String[] {"porcentuales", "por producto", "especiales", "generales"});
    private final JComboBox<Naturaleza> naturaleza = new JComboBox<>(Naturaleza.values());
    private final JComboBox<String> medio = new JComboBox<>(new String[] {"Sin medio de pago", "EFECTIVO", "DEBITO", "CREDITO", "TRANSFERENCIA"});
    private final JComboBox<ProductoOpcion> producto = new JComboBox<>();

    public DescuentoDialog(JFrame owner, Runnable alGuardar) {
        super(owner, "Nuevo descuento", true);
        this.alGuardar = alGuardar;
        cargarProductos();
        tipo.addActionListener(e -> actualizarCamposTipo());
        cantidadCompra.setText("2");
        cantidadPaga.setText("1");
        setSize(800, 640);
        setMinimumSize(new Dimension(740, 600));
        setLocationRelativeTo(owner);
        JPanel base = new JPanel(new BorderLayout(0, 14));
        base.setBackground(Estilos.FONDO);
        base.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        base.add(ComponentesUI.titulo("Nuevo descuento"), BorderLayout.NORTH);

        JPanel formulario = new JPanel();
        formulario.setOpaque(false);
        formulario.setLayout(new javax.swing.BoxLayout(formulario, javax.swing.BoxLayout.Y_AXIS));
        formulario.add(filaDoble(
                ComponentesUI.filaFormulario("Nombre", nombre),
                ComponentesUI.filaFormulario("Tipo", tipo)));
        formulario.add(javax.swing.Box.createVerticalStrut(16));
        formulario.add(filaDoble(
                ComponentesUI.filaFormulario("Producto (para tipo por producto)", producto),
                ComponentesUI.filaFormulario("Naturaleza", naturaleza)));
        formulario.add(javax.swing.Box.createVerticalStrut(16));
        formulario.add(filaDoble(
                ComponentesUI.filaFormulario("Valor (10 o 10% si es porcentual; si no, pesos)", valor),
                ComponentesUI.filaFormulario("Medio de pago (opcional)", medio)));
        formulario.add(javax.swing.Box.createVerticalStrut(16));
        formulario.add(filaDoble(
                ComponentesUI.filaFormulario("Unidades para la promoción (N)", cantidadCompra),
                ComponentesUI.filaFormulario("Unidades que se pagan (M)", cantidadPaga)));
        formulario.add(javax.swing.Box.createVerticalStrut(16));
        formulario.add(filaDoble(
                ComponentesUI.filaFormulario("Fecha inicio (AAAA-MM-DD)", inicio),
                ComponentesUI.filaFormulario("Fecha fin (AAAA-MM-DD)", fin)));
        formulario.add(javax.swing.Box.createVerticalStrut(12));
        JLabel ayuda = new JLabel("Dejá las fechas vacías para que no tengan límite.");
        ayuda.setFont(Estilos.FUENTE_PEQUENA);
        ayuda.setForeground(Estilos.TEXTO_SECUNDARIO);
        ayuda.setAlignmentX(LEFT_ALIGNMENT);
        formulario.add(ayuda);
        actualizarCamposTipo();
        base.add(formulario, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        JButton cancelar = ComponentesUI.botonBorde("Cancelar");
        JButton guardar = ComponentesUI.boton("Guardar", Estilos.CORAL, Estilos.AZUL);
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardar());
        acciones.add(cancelar); acciones.add(guardar);
        base.add(acciones, BorderLayout.SOUTH);
        setContentPane(base);
    }

    private JPanel filaDoble(java.awt.Component izquierda, java.awt.Component derecha) {
        JPanel fila = new JPanel(new GridLayout(1, 2, 18, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        fila.add(izquierda);
        fila.add(derecha);
        return fila;
    }

    private void guardar() {
        try {
            Descuentos d = new Descuentos();
            d.setNombre(nombre.getText());
            d.setTipo((String) tipo.getSelectedItem());
            d.setNaturaleza((Naturaleza) naturaleza.getSelectedItem());
            String medioElegido = (String) medio.getSelectedItem();
            d.setMedioPago("Sin medio de pago".equals(medioElegido) ? null : MedioPago.valueOf(medioElegido));
            boolean especial = "especiales".equals(d.getTipo());
            d.setValor(especial ? 0 : leerValor());
            d.setCantidadCompra(especial ? Integer.valueOf(cantidadCompra.getText().trim()) : null);
            d.setCantidadPaga(especial ? Integer.valueOf(cantidadPaga.getText().trim()) : null);
            d.setFechaInicio(parsearFecha(inicio.getText()));
            d.setFechaFin(parsearFecha(fin.getText()));
            ProductoOpcion seleccionado = (ProductoOpcion) producto.getSelectedItem();
            Integer idProducto = ("por producto".equals(d.getTipo()) || especial) && seleccionado != null
                    ? seleccionado.id : null;
            servicio.crear(d, idProducto);
            alGuardar.run();
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresá un valor numérico válido.", "Descuento", JOptionPane.WARNING_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Las fechas deben tener formato AAAA-MM-DD.", "Descuento", JOptionPane.WARNING_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Descuento", JOptionPane.WARNING_MESSAGE);
        }
    }

    private LocalDate parsearFecha(String texto) {
        String t = texto.trim();
        return t.isEmpty() ? null : LocalDate.parse(t);
    }

    private double leerValor() {
        String texto = valor.getText().trim().replace(',', '.');
        boolean porcentual = "porcentuales".equals(tipo.getSelectedItem());
        if (texto.endsWith("%")) {
            if (!porcentual) {
                throw new NumberFormatException("El símbolo % solo se acepta para descuentos porcentuales.");
            }
            texto = texto.substring(0, texto.length() - 1).trim();
        }
        return Double.parseDouble(texto);
    }

    private void actualizarCamposTipo() {
        String seleccionado = (String) tipo.getSelectedItem();
        boolean productoVinculado = "por producto".equals(seleccionado) || "especiales".equals(seleccionado);
        boolean especial = "especiales".equals(seleccionado);
        boolean general = "generales".equals(seleccionado);
        producto.setEnabled(productoVinculado);
        medio.setEnabled(!productoVinculado && !general);
        valor.setEnabled(!especial);
        cantidadCompra.setEnabled(especial);
        cantidadPaga.setEnabled(especial);
        if (productoVinculado || general) medio.setSelectedIndex(0);
        if (especial) naturaleza.setSelectedItem(Naturaleza.DESCUENTO);
        naturaleza.setEnabled(!especial);
    }

    private void cargarProductos() {
        try {
            for (Producto p : servicio.listarProductosActivos()) {
                producto.addItem(new ProductoOpcion(p.getIdProducto(), p.getNombre()));
            }
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Descuento", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static class ProductoOpcion {
        private final int id;
        private final String nombre;
        private ProductoOpcion(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}