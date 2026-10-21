package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import puntonorte.modelo.Cliente;
import puntonorte.modelo.CondicionIva;
import puntonorte.modelo.MedioPago;
import puntonorte.servicio.FacturacionServicio;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.Ventaservicio;
import puntonorte.servicio.Ventaservicio.LineaPedido;
import puntonorte.servicio.Ventaservicio.LineaResumen;
import puntonorte.servicio.Ventaservicio.PedidoVenta;
import puntonorte.servicio.Ventaservicio.ResumenVenta;

/**
 * "Pedido actual": muestra las líneas, deja cambiar cantidades, elegir medio de pago y cliente.
 * Cada cambio recalcula los importes llamando a Ventaservicio.calcular (la pantalla no calcula nada).
 */
public class PedidoVentaDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNAS = {"Producto", "Cant.", "Desc.", "Subtotal"};

    private final Ventaservicio servicio;
    private final PedidoVenta pedido;
    private final MedioPago[] medios = MedioPago.values();
    // El orden coincide con el del combo "Tipo de cliente".
    private final CondicionIva[] condiciones = {CondicionIva.CONSUMIDOR_FINAL, CondicionIva.MONOTRIBUTISTA,
            CondicionIva.RESPONSABLE_INSCRIPTO};

    private ResumenVenta ultimo;     // null si el último cálculo falló

    private JTable tabla;
    private JLabel lblSubtotal;
    private JLabel lblDescProductos;
    private JLabel lblDescGeneral;
    private JLabel lblAjusteNombre;
    private JLabel lblAjuste;
    private JLabel lblTotal;
    private JLabel lblVuelto;
    private JLabel lblError;

    private JComboBox<String> condicion;
    private JTextField comprador;
    private JTextField cuit;
    private JTextField razonSocial;
    private JTextField domicilio;
    private JPanel panelFiscalA;       // CUIT, razón social y domicilio: solo para Factura A
    private JPanel panelComprador;
    private JPanel panelCliente;
    private JCheckBox registrar;
    private JTextField dni;
    private JTextField correo;
    private JTextField telefono;
    private JComboBox<String> medio;
    private JTextField monto;
    private JTextArea obs;

    public PedidoVentaDialog(Window owner, Ventaservicio servicio, PedidoVenta pedido) {
        super(owner, "Pedido actual", ModalityType.APPLICATION_MODAL);
        this.servicio = servicio;
        this.pedido = pedido;

        setSize(930, 750);
        setMinimumSize(new Dimension(850, 710));
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(Estilos.FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel titulo = new JLabel("Pedido actual");
        titulo.setFont(Estilos.TITULO_DIALOGO);
        titulo.setForeground(Estilos.TEXTO);

        lblError = new JLabel(" ");
        lblError.setFont(Estilos.FUENTE_PEQUENA);
        lblError.setForeground(Estilos.ROJO);

        JPanel sur = new JPanel(new BorderLayout(0, 6));
        sur.setOpaque(false);
        sur.add(lblError, BorderLayout.NORTH);
        sur.add(acciones(), BorderLayout.SOUTH);

        p.add(titulo, BorderLayout.NORTH);
        p.add(centro(), BorderLayout.CENTER);
        p.add(sur, BorderLayout.SOUTH);
        add(p);

        conectarEventos();
        recalcular();
    }

    // ================================================================ ARMADO

    private JPanel centro() {
        JPanel p = new JPanel(new GridLayout(1, 2, 18, 0));
        p.setOpaque(false);
        p.add(pedidoPanel());
        p.add(formulario());
        return p;
    }

    private JPanel pedidoPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Estilos.BEIGE);
        p.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, Estilos.AZUL));

        JButton menos = ComponentesUI.botonBorde("−");
        JButton mas = ComponentesUI.botonBorde("+");
        JButton quitar = ComponentesUI.botonBorde("Quitar");
        menos.addActionListener(e -> cambiarCantidad(-1));
        mas.addActionListener(e -> cambiarCantidad(+1));
        quitar.addActionListener(e -> quitarLinea());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        botones.setOpaque(false);
        botones.add(menos);
        botones.add(mas);
        botones.add(quitar);

        tabla = ComponentesUI.tabla(COLUMNAS, new Object[0][COLUMNAS.length]);

        JPanel resumen = new JPanel();
        resumen.setOpaque(false);
        resumen.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        resumen.setLayout(new BoxLayout(resumen, BoxLayout.Y_AXIS));

        lblSubtotal = new JLabel();
        lblDescProductos = new JLabel();
        lblDescGeneral = new JLabel();
        lblAjusteNombre = new JLabel("Medio de pago");
        lblAjuste = new JLabel();
        lblTotal = new JLabel();

        resumen.add(filaTotal(new JLabel("Subtotal lista"), lblSubtotal, false));
        resumen.add(filaTotal(new JLabel("Descuentos productos"), lblDescProductos, false));
        resumen.add(filaTotal(new JLabel("Descuento general"), lblDescGeneral, false));
        resumen.add(filaTotal(lblAjusteNombre, lblAjuste, false));
        resumen.add(Box.createVerticalStrut(6));
        resumen.add(filaTotal(new JLabel("Total"), lblTotal, true));

        p.add(botones, BorderLayout.NORTH);
        p.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER);
        p.add(resumen, BorderLayout.SOUTH);
        return p;
    }

    private JPanel filaTotal(JLabel nombre, JLabel valor, boolean total) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        nombre.setFont(total ? new java.awt.Font("Tahoma", java.awt.Font.BOLD, 15) : Estilos.FUENTE);
        valor.setFont(total ? new java.awt.Font("Tahoma", java.awt.Font.BOLD, 17) : Estilos.FUENTE_NEGRITA);

        if (total) {
            nombre.setForeground(Estilos.AZUL);
            valor.setForeground(Estilos.AZUL);
        }

        p.add(nombre, BorderLayout.WEST);
        p.add(valor, BorderLayout.EAST);
        return p;
    }

    private JPanel formulario() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        condicion = ComponentesUI.combo(new String[] {"Consumidor final (Factura B)", "Monotributista (Factura B)",
                "Responsable inscripto (Factura A)"});
        cuit = ComponentesUI.campo();
        razonSocial = ComponentesUI.campo();
        domicilio = ComponentesUI.campo();
        comprador = ComponentesUI.campo();

        registrar = new JCheckBox("Registrar cliente");
        registrar.setOpaque(false);
        registrar.setFont(Estilos.FUENTE);
        registrar.setForeground(Estilos.TEXTO);

        dni = ComponentesUI.campo();
        correo = ComponentesUI.campo();
        telefono = ComponentesUI.campo();
        dni.setEnabled(false);
        correo.setEnabled(false);
        telefono.setEnabled(false);

        JPanel cliente = new JPanel(new GridLayout(1, 3, 8, 0));
        cliente.setOpaque(false);
        cliente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        cliente.add(ComponentesUI.filaFormulario("DNI", dni));
        cliente.add(ComponentesUI.filaFormulario("Correo", correo));
        cliente.add(ComponentesUI.filaFormulario("Teléfono", telefono));

        panelCliente = cliente;

        // Factura A: el receptor es una empresa, se identifica por CUIT (queda oculto para Factura B).
        JPanel filaA = new JPanel(new GridLayout(1, 3, 8, 0));
        filaA.setOpaque(false);
        filaA.add(ComponentesUI.filaFormulario("CUIT", cuit));
        filaA.add(ComponentesUI.filaFormulario("Razón social", razonSocial));
        filaA.add(ComponentesUI.filaFormulario("Domicilio", domicilio));
        panelFiscalA = new JPanel();
        panelFiscalA.setOpaque(false);
        panelFiscalA.setLayout(new BoxLayout(panelFiscalA, BoxLayout.Y_AXIS));
        panelFiscalA.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        panelFiscalA.add(filaA);
        panelFiscalA.add(Box.createVerticalStrut(8));
        panelFiscalA.setVisible(false);

        // Las etiquetas ("Efectivo (10% de descuento)") salen de la BD, no están escritas a mano.
        String[] etiquetas = new String[medios.length];
        for (int i = 0; i < medios.length; i++) {
            try {
                etiquetas[i] = servicio.etiquetaMedioPago(medios[i]);
            } catch (ReglaNegocioException e) {
                etiquetas[i] = medios[i].name();
            }
        }
        medio = ComponentesUI.combo(etiquetas);
        monto = ComponentesUI.campo();
        obs = ComponentesUI.areaTexto();

        p.add(campo("Tipo de cliente", condicion));
        p.add(Box.createVerticalStrut(8));
        p.add(panelFiscalA);
        panelComprador = campo("Nombre y apellido del comprador", comprador);
        p.add(panelComprador);
        p.add(Box.createVerticalStrut(8));
        p.add(registrar);
        p.add(Box.createVerticalStrut(8));
        p.add(cliente);
        p.add(Box.createVerticalStrut(10));
        p.add(campo("Método de pago", medio));
        p.add(Box.createVerticalStrut(10));
        p.add(campo("Monto recibido", monto));

        lblVuelto = new JLabel("Vuelto: $ 0");
        lblVuelto.setFont(Estilos.FUENTE_PEQUENA);
        lblVuelto.setForeground(Estilos.TEXTO_SECUNDARIO);
        p.add(lblVuelto);
        p.add(Box.createVerticalStrut(10));

        JPanel obsPanel = ComponentesUI.filaFormulario("Observaciones", obs);
        obsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        p.add(obsPanel);
        return p;
    }

    private JPanel campo(String titulo, java.awt.Component c) {
        JPanel p = ComponentesUI.filaFormulario(titulo, c);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        return p;
    }

    private JPanel acciones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        JButton cancelar = ComponentesUI.boton("Cancelar pedido", Estilos.ROJO, java.awt.Color.WHITE);
        JButton revisar = ComponentesUI.boton("Revisar venta", Estilos.CORAL, Estilos.AZUL);

        cancelar.addActionListener(e -> cancelarPedido());
        revisar.addActionListener(e -> revisarVenta());

        p.add(cancelar);
        p.add(revisar);
        return p;
    }

    /** Se conecta al final, para que armar la pantalla no dispare cálculos a medias. */
    private void conectarEventos() {
        condicion.addActionListener(e -> {
            actualizarSegunCondicion();
            recalcular();
        });
        cuit.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { autocompletarPorCuit(); }
        });
        medio.addActionListener(e -> {
            monto.setEnabled(medios[medio.getSelectedIndex()] == MedioPago.EFECTIVO);
            recalcular();
        });
        registrar.addActionListener(e -> {
            boolean on = registrar.isSelected();
            dni.setEnabled(on);
            correo.setEnabled(on);
            telefono.setEnabled(on);
        });
        monto.getDocument().addDocumentListener(new CambioTexto());
        monto.setEnabled(medios[medio.getSelectedIndex()] == MedioPago.EFECTIVO);
    }

    /** Factura A muestra CUIT / razón social / domicilio; Factura B muestra el comprador y "Registrar cliente". */
    private void actualizarSegunCondicion() {
        boolean esA = condiciones[condicion.getSelectedIndex()] == CondicionIva.RESPONSABLE_INSCRIPTO;
        panelFiscalA.setVisible(esA);
        panelComprador.setVisible(!esA);
        registrar.setVisible(!esA);
        panelCliente.setVisible(!esA);
        if (esA) {
            registrar.setSelected(false);
            dni.setEnabled(false);
            correo.setEnabled(false);
            telefono.setEnabled(false);
        }
        validate();
        repaint();
    }

    /** Si ya se le facturó a ese CUIT, completa razón social y domicilio (solo si están vacíos). */
    private void autocompletarPorCuit() {
        if (!razonSocial.getText().trim().isEmpty() || !domicilio.getText().trim().isEmpty()) {
            return;
        }
        FacturacionServicio.Receptor previo = servicio.buscarReceptorPorCuit(cuit.getText());
        if (previo != null) {
            razonSocial.setText(previo.nombre == null ? "" : previo.nombre);
            domicilio.setText(previo.domicilio == null ? "" : previo.domicilio);
        }
    }

    private class CambioTexto implements DocumentListener {
        @Override public void insertUpdate(DocumentEvent e) { recalcular(); }
        @Override public void removeUpdate(DocumentEvent e) { recalcular(); }
        @Override public void changedUpdate(DocumentEvent e) { recalcular(); }
    }

    // ================================================================ LÓGICA DE PANTALLA

    /** Pasa lo que hay en el formulario al PedidoVenta (la pantalla solo junta datos, no decide nada). */
    private void volcarFormulario() {
        pedido.condicionIva = condiciones[condicion.getSelectedIndex()];
        boolean esA = pedido.condicionIva == CondicionIva.RESPONSABLE_INSCRIPTO;
        pedido.receptorCuit = esA ? cuit.getText() : null;
        pedido.receptorRazonSocial = esA ? razonSocial.getText() : null;
        pedido.receptorDomicilio = esA ? domicilio.getText() : null;
        MedioPago elegido = medios[medio.getSelectedIndex()];
        pedido.medioPago = elegido;
        pedido.montoRecibido = elegido == MedioPago.EFECTIVO ? leerMonto(monto.getText()) : null;

        String nombreCompleto = comprador.getText().trim();
        String observaciones = obs.getText().trim();

        pedido.idCliente = null;
        pedido.clienteNuevo = null;

        if (esA) {
            // Factura A: el receptor es la empresa (campos fiscales); no se registra como persona.
        } else if (registrar.isSelected()) {
            Cliente c = new Cliente();
            int corte = nombreCompleto.lastIndexOf(' ');      // "Juan Carlos Pérez" -> "Juan Carlos" / "Pérez"
            c.setNombre(corte > 0 ? nombreCompleto.substring(0, corte).trim() : nombreCompleto);
            c.setApellido(corte > 0 ? nombreCompleto.substring(corte + 1).trim() : "");
            c.setDni(dni.getText());
            c.setCorreo(correo.getText());
            c.setTelefono(telefono.getText());
            pedido.clienteNuevo = c;
        } else if (!nombreCompleto.isEmpty()) {
            // Comprador sin registrar: el nombre queda anotado en las observaciones de la venta.
            observaciones = "Comprador: " + nombreCompleto + (observaciones.isEmpty() ? "" : " · " + observaciones);
        }
        pedido.observaciones = observaciones.isEmpty() ? null : observaciones;
    }

    /** Pide los importes al servicio y los muestra. Si algo no cierra, muestra el motivo en rojo. */
    private void recalcular() {
        volcarFormulario();
        try {
            ultimo = servicio.calcular(pedido);
            lblError.setText(" ");
            mostrar(ultimo);
        } catch (ReglaNegocioException e) {
            ultimo = null;
            lblError.setText(e.getMessage());
        }
    }

    private void mostrar(ResumenVenta r) {
        int seleccionada = tabla.getSelectedRow();

        Object[][] datos = new Object[r.lineas.size()][COLUMNAS.length];
        for (int i = 0; i < r.lineas.size(); i++) {
            LineaResumen l = r.lineas.get(i);
            datos[i] = new Object[] {
                    l.descripcion,
                    String.valueOf(l.cantidad),
                    l.descuento > 0 ? "- " + FormatoDinero.pesos(l.descuento) : "—",
                    FormatoDinero.pesos(l.subtotal)
            };
        }
        tabla.setModel(new DefaultTableModel(datos, COLUMNAS) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        });
        if (seleccionada >= 0 && seleccionada < datos.length) {
            tabla.setRowSelectionInterval(seleccionada, seleccionada);
        }

        lblSubtotal.setText(FormatoDinero.pesos(r.subtotalLista));
        lblDescProductos.setText("- " + FormatoDinero.pesos(r.descuentoProductos));
        lblDescGeneral.setText(r.descuentoGeneral > 0 ? "- " + FormatoDinero.pesos(r.descuentoGeneral)
                : FormatoDinero.pesos(0));

        if (r.ajustePago < 0) {
            lblAjusteNombre.setText("Descuento medio de pago");
            lblAjuste.setText("- " + FormatoDinero.pesos(-r.ajustePago));
        } else if (r.ajustePago > 0) {
            lblAjusteNombre.setText("Recargo medio de pago");
            lblAjuste.setText("+ " + FormatoDinero.pesos(r.ajustePago));
        } else {
            lblAjusteNombre.setText("Medio de pago");
            lblAjuste.setText(FormatoDinero.pesos(0));
        }
        lblTotal.setText(FormatoDinero.pesos(r.total));

        if (r.montoRecibido == null) {
            lblVuelto.setText("Vuelto: $ 0");
        } else if (r.vuelto == null) {
            lblVuelto.setText("Falta: " + FormatoDinero.pesos(r.total - r.montoRecibido));
        } else {
            lblVuelto.setText("Vuelto: " + FormatoDinero.pesos(r.vuelto));
        }

        // Desde el tope, la factura exige identificar al comprador: se fuerza el registro con DNI.
        if (r.requiereDocumento) {
            registrar.setSelected(true);
            dni.setEnabled(true);
            correo.setEnabled(true);
            telefono.setEnabled(true);
            lblError.setText("El total llega a " + FormatoDinero.pesos(FacturacionServicio.TOPE_IDENTIFICACION)
                    + " o más: cargá nombre y apellido, y el DNI del comprador.");
        }
    }

    private void cambiarCantidad(int delta) {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= pedido.lineas.size()) {
            JOptionPane.showMessageDialog(this, "Seleccioná una línea del pedido.");
            return;
        }
        LineaPedido l = pedido.lineas.get(fila);
        if (l.cantidad + delta <= 0) {
            quitarLinea();
            return;
        }

        l.cantidad += delta;
        recalcular();
        if (ultimo == null && delta > 0) {          // no alcanza el stock: se deshace el cambio
            String motivo = lblError.getText();
            l.cantidad -= delta;
            recalcular();
            lblError.setText(motivo);
        }
    }

    private void quitarLinea() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= pedido.lineas.size()) {
            JOptionPane.showMessageDialog(this, "Seleccioná una línea del pedido.");
            return;
        }
        pedido.lineas.remove(fila);
        if (pedido.lineas.isEmpty()) {
            dispose();                              // sin productos no hay pedido
            return;
        }
        recalcular();
    }

    private void cancelarPedido() {
        int r = JOptionPane.showConfirmDialog(this, "¿Cancelar el pedido y vaciar los productos?",
                "Cancelar pedido", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            pedido.limpiar();
            dispose();
        }
    }

    private void revisarVenta() {
        recalcular();
        if (ultimo == null) {
            JOptionPane.showMessageDialog(this, lblError.getText(), "No se puede continuar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            servicio.validarReceptor(pedido);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos para la Factura A", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pedido.medioPago == MedioPago.EFECTIVO && ultimo.vuelto == null) {
            JOptionPane.showMessageDialog(this, "Ingresá el monto recibido: tiene que cubrir el total ("
                    + FormatoDinero.pesos(ultimo.total) + ").", "Monto recibido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new RevisionVentaDialog(this, servicio, pedido).setVisible(true);

        if (pedido.lineas.isEmpty()) {              // se cobró: este diálogo ya no tiene nada que mostrar
            dispose();
        }
    }

    /** Acepta "80000", "80.000" y "80.000,50". Devuelve null si está vacío o no es un número. */
    private Double leerMonto(String texto) {
        String t = texto == null ? "" : texto.replace("$", "").replace(" ", "").trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.contains(",")) {
            t = t.replace(".", "").replace(',', '.');
        } else if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            t = t.replace(".", "");
        }
        try {
            return Double.valueOf(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
