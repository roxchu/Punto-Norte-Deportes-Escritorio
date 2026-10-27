package puntonorte;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import puntonorte.modelo.DatosFiscalesVenta;
import puntonorte.modelo.Emisor;
import puntonorte.modelo.Ventas;
import puntonorte.servicio.FacturacionServicio;
import puntonorte.servicio.FacturacionServicio.Comprobante;
import puntonorte.servicio.FacturacionServicio.LineaComprobante;
import puntonorte.servicio.ReglaNegocioException;

/** Vista imprimible de una venta, con el formato visual del detalle de orden de compra. */
public class DetalleVentaDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    public static void mostrar(Window owner, int idVenta) {
        try {
            Comprobante comprobante = new FacturacionServicio().armarComprobante(idVenta);
            new DetalleVentaDialog(owner, comprobante).setVisible(true);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Detalle de venta", JOptionPane.WARNING_MESSAGE);
        }
    }

    private DetalleVentaDialog(Window owner, Comprobante comprobante) {
        super(owner, "Detalle de venta · #" + comprobante.venta.getIdVenta(), ModalityType.APPLICATION_MODAL);
        setSize(940, 700);
        setMinimumSize(new java.awt.Dimension(760, 580));
        setLocationRelativeTo(owner);

        JEditorPane documento = new JEditorPane();
        documento.setEditorKit(new HTMLEditorKit());
        documento.setContentType("text/html");
        documento.setEditable(false);
        documento.setBackground(java.awt.Color.WHITE);
        documento.setText(html(comprobante));
        documento.setCaretPosition(0);

        JPanel base = new JPanel(new BorderLayout(0, 12));
        base.setBackground(Estilos.FONDO);
        base.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        base.add(ComponentesUI.titulo("Detalle de venta"), BorderLayout.NORTH);
        base.add(new JScrollPane(documento), BorderLayout.CENTER);

        javax.swing.JButton imprimir = ComponentesUI.boton("Imprimir", Estilos.CORAL, Estilos.AZUL);
        javax.swing.JButton cerrar = ComponentesUI.botonBorde("Cerrar");
        imprimir.addActionListener(e -> imprimir(documento));
        cerrar.addActionListener(e -> dispose());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(imprimir);
        acciones.add(cerrar);
        base.add(acciones, BorderLayout.SOUTH);
        setContentPane(base);
    }

    private String html(Comprobante c) {
        Emisor e = c.emisor;
        DatosFiscalesVenta f = c.fiscal;
        Ventas v = c.venta;
        StringBuilder b = new StringBuilder();
        b.append("<html><head><style>")
         .append("body{font-family:Arial,sans-serif;color:#173b60;margin:24px;font-size:12px;}")
         .append("h1{font-size:20px;margin:0 0 5px;} .muted{color:#64748b;} .rule{border-top:2px solid #173b60;margin:14px 0;}")
         .append("table{width:100%;border-collapse:collapse;} th{background:#e9eef3;text-align:left;}")
         .append("td,th{padding:8px;border-bottom:1px solid #ccd5df;} .right{text-align:right;white-space:nowrap;}")
         .append(".datos td{border:0;padding:4px 8px 4px 0;} .totales{width:48%;margin:18px 0 0 auto;}")
         .append(".total{font-size:15px;font-weight:bold;border-top:2px solid #173b60;}")
         .append("</style></head><body>")
         .append("<table class='datos'><tr><td style='width:72%'><h1>").append(esc(e.getRazonSocial())).append("</h1>")
         .append("<div>Domicilio comercial: ").append(esc(e.getDomicilioComercial())).append("</div>")
         .append("<div>Localidad: ").append(esc(e.getLocalidad())).append("</div></td><td class='right'>")
         .append("<img src='").append(esc(logoUri())).append("' width='145' height='75'></td></tr></table>")
         .append("<div class='rule'></div><table class='datos'><tr><td><b>Cliente:</b> ").append(esc(f.getReceptorNombre()))
         .append("</td><td><b>Documento:");
        if (f.getReceptorNumeroDoc() != null && !f.getReceptorNumeroDoc().isBlank()) {
            b.append("</b> ").append(esc(f.getReceptorTipoDoc())).append(" ").append(esc(f.getReceptorNumeroDoc()));
        } else {
            b.append("</b> Consumidor final");
        }
        b.append("</td><td><b>Cond. IVA:</b> ").append(esc(f.getCondicionIva() == null ? "—" : f.getCondicionIva().getTexto()))
         .append("</td></tr></table><div class='rule'></div><h1>Venta #")
         .append(String.format("%06d", v.getIdVenta())).append("</h1><div class='muted'>Fecha: ")
         .append(v.getFechaHora().format(FECHA)).append(" &nbsp; Comprobante: Factura ").append(esc(f.getTipoComprobante()))
         .append(" &nbsp; Estado: ").append(esc(v.getEstado() == null ? "—" : v.getEstado().toString()))
         .append("</div><br><table><thead><tr><th>Descripción</th><th class='right'>Cantidad</th>")
         .append("<th class='right'>Precio unitario</th><th class='right'>Precio total</th></tr></thead><tbody>");
        for (LineaComprobante l : c.lineas) {
            b.append("<tr><td>").append(esc(l.descripcion)).append("</td><td class='right'>").append(l.cantidad)
             .append("</td><td class='right'>").append(dinero(l.precioUnitario)).append("</td><td class='right'>")
             .append(dinero(l.importe)).append("</td></tr>");
        }
        b.append("</tbody></table><table class='totales'>")
         .append("<tr><td>Subtotal</td><td class='right'>").append(dinero(v.getSubtotal())).append("</td></tr>");
        if (v.getDescuentoTotal() > 0) {
            b.append("<tr><td>Descuentos</td><td class='right'>− ").append(dinero(v.getDescuentoTotal())).append("</td></tr>");
        }
        if (Math.abs(v.getMontoAjustePago()) >= 0.005) {
            b.append("<tr><td>").append(v.getMontoAjustePago() < 0 ? "Descuento por medio de pago" : "Recargo por medio de pago")
             .append("</td><td class='right'>").append(v.getMontoAjustePago() < 0 ? "− " : "+ ")
             .append(dinero(Math.abs(v.getMontoAjustePago()))).append("</td></tr>");
        }
        if (f.getMontoNeto() != null) {
            b.append("<tr><td>Total sin IVA</td><td class='right'>").append(dinero(f.getMontoNeto())).append("</td></tr>");
        }
        if (f.getMontoIva() != null) {
            b.append("<tr><td>IVA (contenido)</td><td class='right'>").append(dinero(f.getMontoIva())).append("</td></tr>");
        }
        b.append("<tr class='total'><td>Total</td><td class='right'>").append(dinero(v.getMontoTotal()))
         .append("</td></tr><tr><td>Medio de pago</td><td class='right'>").append(esc(String.valueOf(v.getMedioPago())))
         .append("</td></tr></table></body></html>");
        return b.toString();
    }

    private void imprimir(JEditorPane documento) {
        try {
            documento.print(null, null, true, null, null, true);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo imprimir el detalle: " + ex.getMessage(),
                    "Impresión", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String logoUri() {
        URL recurso = getClass().getResource("/puntonorte/logoPuntoNorte.jpeg");
        if (recurso != null) return recurso.toExternalForm();
        File archivo = new File("src/puntonorte/logoPuntoNorte.jpeg");
        return archivo.exists() ? archivo.toURI().toString() : "";
    }

    private String dinero(double n) { return DINERO.format(n); }
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
