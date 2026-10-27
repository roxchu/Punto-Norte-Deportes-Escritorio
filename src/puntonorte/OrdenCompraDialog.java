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

import puntonorte.servicio.CompraService;
import puntonorte.servicio.ReglaNegocioException;

/** Vista imprimible del detalle de una orden de compra. */
public class OrdenCompraDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    public static void mostrar(Window owner, CompraService servicio, int idOrden) {
        try {
            new OrdenCompraDialog(owner, servicio.detalleOrdenCompra(idOrden)).setVisible(true);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Orden de compra", JOptionPane.WARNING_MESSAGE);
        }
    }

    private OrdenCompraDialog(Window owner, CompraService.DetalleOrdenCompra orden) {
        super(owner, "Detalle de orden de compra · OC-" + orden.id, ModalityType.APPLICATION_MODAL);
        setSize(940, 700);
        setMinimumSize(new java.awt.Dimension(760, 580));
        setLocationRelativeTo(owner);

        JEditorPane documento = new JEditorPane();
        documento.setEditorKit(new HTMLEditorKit());
        documento.setContentType("text/html");
        documento.setEditable(false);
        documento.setBackground(java.awt.Color.WHITE);
        documento.setText(html(orden));
        documento.setCaretPosition(0);

        JPanel base = new JPanel(new BorderLayout(0, 12));
        base.setBackground(Estilos.FONDO);
        base.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        base.add(ComponentesUI.titulo("Detalle de orden de compra"), BorderLayout.NORTH);
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

    private String html(CompraService.DetalleOrdenCompra o) {
        StringBuilder b = new StringBuilder();
        b.append("<html><head><style>")
         .append("body{font-family:Arial,sans-serif;color:#173b60;margin:24px;font-size:12px;}")
         .append("h1{font-size:20px;margin:0 0 5px;} .muted{color:#64748b;} .rule{border-top:2px solid #173b60;margin:14px 0;}")
         .append("table{width:100%;border-collapse:collapse;} th{background:#e9eef3;text-align:left;}")
         .append("td,th{padding:8px;border-bottom:1px solid #ccd5df;} .right{text-align:right;white-space:nowrap;}")
         .append(".datos td{border:0;padding:4px 8px 4px 0;} .totales{width:48%;margin:18px 0 0 auto;}")
         .append(".total{font-size:15px;font-weight:bold;border-top:2px solid #173b60;}")
         .append("</style></head><body>");
        b.append("<table class='datos'><tr><td style='width:72%'><h1>").append(esc(o.emisorRazonSocial)).append("</h1>")
         .append("<div>Domicilio comercial: ").append(esc(o.domicilioComercial)).append("</div>")
         .append("<div>Localidad: ").append(esc(o.localidad)).append("</div></td><td class='right'>")
         .append("<img src='").append(esc(logoUri())).append("' width='145' height='75'></td></tr></table>")
         .append("<div class='rule'></div>")
         .append("<table class='datos'><tr><td><b>Razón social:</b> ").append(esc(o.razonSocialProveedor))
         .append("</td><td><b>CUIT:</b> ").append(esc(vacio(o.cuitProveedor)))
         .append("</td><td><b>Cond. pago:</b> ").append(esc(vacio(o.condicionPago))).append("</td></tr></table>")
         .append("<div class='rule'></div><h1>Orden de compra OC-").append(String.format("%06d", o.id)).append("</h1>")
         .append("<div class='muted'>Fecha: ").append(o.fecha.format(FECHA)).append(" &nbsp; Estado: ").append(esc(o.estado)).append("</div>")
         .append("<br><table><thead><tr><th>Código</th><th>Descripción</th><th class='right'>Cantidad</th>")
         .append("<th class='right'>Precio unitario</th><th class='right'>Precio total</th></tr></thead><tbody>");
        for (CompraService.LineaOrdenCompra linea : o.lineas) {
            b.append("<tr><td>").append(esc(linea.codigo)).append("</td><td>").append(esc(linea.descripcion))
             .append("</td><td class='right'>").append(linea.cantidad).append("</td><td class='right'>")
             .append(dinero(linea.precioUnitario)).append("</td><td class='right'>").append(dinero(linea.total())).append("</td></tr>");
        }
        b.append("</tbody></table><table class='totales'>");
        if (o.cotizacionCompleta) {
            b.append("<tr><td>Total sin IVA</td><td class='right'>").append(dinero(o.neto)).append("</td></tr>")
             .append("<tr><td>IVA (").append(String.format(Locale.ROOT,"%.0f",o.porcentajeIva)).append("%)</td><td class='right'>")
             .append(dinero(o.iva)).append("</td></tr><tr class='total'><td>Total</td><td class='right'>")
             .append(dinero(o.total)).append("</td></tr>");
        } else {
            b.append("<tr class='total'><td colspan='2'>No se pueden calcular los totales: falta la cotización de una o más líneas.</td></tr>");
        }
        return b.append("</table></body></html>").toString();
    }

    private void imprimir(JEditorPane documento) {
        try {
            documento.print(null, null, true, null, null, true);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo imprimir la orden: " + ex.getMessage(),
                    "Impresión", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String logoUri() {
        URL recurso = getClass().getResource("/puntonorte/logoPuntoNorte.jpeg");
        if (recurso != null) return recurso.toExternalForm();
        File archivo = new File("src/puntonorte/logoPuntoNorte.jpeg");
        return archivo.exists() ? archivo.toURI().toString() : "";
    }

    private String dinero(Double n) { return n == null ? "Sin cotización" : DINERO.format(n); }
    private String dinero(double n) { return DINERO.format(n); }
    private String vacio(String s) { return s == null || s.isBlank() ? "—" : s; }
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
