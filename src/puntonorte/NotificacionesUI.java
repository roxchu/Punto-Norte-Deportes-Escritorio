package puntonorte;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import puntonorte.servicio.InicioService;
import puntonorte.servicio.Permisos;
import puntonorte.servicio.Sesion;
import puntonorte.servicio.SolicitudesOperativasService;

/** Avisos del sistema construidos desde las tareas pendientes reales. */
public final class NotificacionesUI {

    private NotificacionesUI() { }
    private static JButton botonAvisos;

    private static final class Aviso {
        final String titulo;
        final String detalle;
        final SolicitudesOperativasService.Solicitud solicitud;
        final SolicitudesOperativasService.AvisoStock avisoStock;
        Aviso(String titulo, String detalle) { this(titulo, detalle, null, null); }
        Aviso(String titulo, String detalle, SolicitudesOperativasService.Solicitud solicitud) { this(titulo,detalle,solicitud,null); }
        Aviso(String titulo,String detalle,SolicitudesOperativasService.Solicitud solicitud,SolicitudesOperativasService.AvisoStock avisoStock){this.titulo=titulo;this.detalle=detalle;this.solicitud=solicitud;this.avisoStock=avisoStock;}
    }

    private static List<Aviso> cargarAvisos() {
        InicioService.Datos datos = new InicioService().cargar();
        List<Aviso> avisos = new ArrayList<>();
        String rol = Sesion.getNombreRol();
        if (Permisos.puedeResolverSolicitudesOperativas(rol)) {
            for (SolicitudesOperativasService.AvisoStock aviso : new SolicitudesOperativasService().avisosStockPendientes()) {
                avisos.add(new Aviso("Aviso de stock bajo para Encargado", aviso.solicitante + " · " + aviso.fecha + "\n" + aviso.detalle, null, aviso));
            }
        }
        if (Permisos.esSupervisorOGerente(rol) && datos.solicitudesUsuarios > 0) {
            avisos.add(new Aviso("Solicitudes de acceso pendientes",
                    datos.solicitudesUsuarios + " cuenta(s) esperan aprobación. Podés revisarlas en Usuarios."));
        }
        if (datos.stockBajo > 0) {
            avisos.add(new Aviso("Productos con stock bajo",
                    datos.stockBajo + " variante(s) están en el mínimo o por debajo. Revisá Productos."));
        }
        if (datos.pedidosAbiertos > 0) {
            StringBuilder detalle = new StringBuilder();
            for (InicioService.Pedido pedido : datos.pedidos) {
                if (detalle.length() > 0) detalle.append(" · ");
                detalle.append("#").append(pedido.id).append(" ").append(pedido.proveedor)
                        .append(" ( ").append(pedido.estado).append(" )");
            }
            String tarea = Permisos.normalizar(rol).contains("deposito")
                    ? "Mercadería pendiente de recepción" : "Órdenes de compra en curso";
            avisos.add(new Aviso(tarea, datos.pedidosAbiertos + " orden(es) abiertas. " + detalle));
        }
        return avisos;
    }

    public static JButton crearBoton(Component owner) {
        JButton boton = ComponentesUI.boton("⚠ Avisos", Estilos.CORAL, Estilos.AZUL);
        botonAvisos=boton;
        boton.setToolTipText("Ver tareas pendientes del sistema");
        boton.setPreferredSize(new Dimension(115, 32));
        actualizarTexto(boton);
        boton.addActionListener(e -> { abrir(owner); actualizarTexto(boton); actualizarTarjetasInicio(); });
        return boton;
    }

    public static JPanel crearTarjetaInicio() {
        return PeticionesUI.crearTarjetaInicio();
    }

    private static String contarAvisos() {
        try { return String.valueOf(cargarAvisos().size()); }
        catch (RuntimeException ex) { return "—"; }
    }

    public static void actualizarTarjetasInicio() {
        PeticionesUI.actualizarContador();
    }

    public static void actualizarContadorAvisos() {
        // El botón superior resume avisos generales y peticiones pendientes,
        // aunque cada grupo se consulta en su bandeja propia.
        if(botonAvisos!=null)actualizarTexto(botonAvisos);
    }

    private static void actualizarTexto(JButton boton) {
        String cantidad = contarTotalPendientes();
        boton.setText("⚠ Avisos" + ("—".equals(cantidad) ? "" : " (" + cantidad + ")"));
    }

    private static String contarTotalPendientes() {
        try {
            int avisos=cargarAvisos().size();int peticiones=PeticionesUI.contarPendientes();
            return peticiones<0?"—":String.valueOf(avisos+peticiones);
        } catch(RuntimeException ex){return "—";}
    }

    private static void abrir(Component owner) {
        Window ventana = SwingUtilities.getWindowAncestor(owner);
        JDialog dialogo = new JDialog(ventana, "Avisos", JDialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(560, 410);
        dialogo.setMinimumSize(new Dimension(465, 350));
        dialogo.setLocationRelativeTo(ventana);
        JPanel fondo = new JPanel(new BorderLayout(0, 12));
        fondo.setBackground(Estilos.FONDO);
        fondo.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        fondo.add(ComponentesUI.titulo("Avisos del sistema"), BorderLayout.NORTH);
        JPanel listado = new JPanel();
        listado.setOpaque(false);
        listado.setLayout(new BoxLayout(listado, BoxLayout.Y_AXIS));
        try {
            List<Aviso> avisos = cargarAvisos();
            if (avisos.isEmpty()) {
                JLabel vacio = new JLabel("No hay tareas pendientes para tu rol.");
                vacio.setForeground(Estilos.TEXTO_SECUNDARIO);
                listado.add(vacio);
            } else {
                for (Aviso aviso : avisos) {
                    JPanel item = new JPanel(new BorderLayout(0, 5));
                    item.setBackground(Estilos.BEIGE);
                    item.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, Estilos.CORAL),
                            BorderFactory.createEmptyBorder(9, 12, 9, 12)));
                    JLabel titulo = new JLabel(aviso.titulo);
                    titulo.setForeground(Estilos.AZUL);
                    titulo.setFont(Estilos.FUENTE_NEGRITA);
                    JLabel detalle = new JLabel("<html>" + escapar(aviso.detalle) + "</html>");
                    detalle.setFont(Estilos.FUENTE_PEQUENA);
                    detalle.setForeground(Estilos.TEXTO_SECUNDARIO);
                    item.add(titulo, BorderLayout.NORTH);
                    item.add(detalle, BorderLayout.CENTER);
                    if (aviso.avisoStock != null) {
                        JButton atendido = ComponentesUI.boton("Marcar atendido", Estilos.CORAL, Estilos.AZUL);
                        atendido.addActionListener(e -> atenderAviso(dialogo, aviso.avisoStock));
                        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
                        botones.setOpaque(false); botones.add(atendido); item.add(botones, BorderLayout.SOUTH);
                    }
                    item.setMaximumSize(new Dimension(Integer.MAX_VALUE, aviso.avisoStock == null ? 90 : 145));
                    listado.add(item);
                    listado.add(Box.createVerticalStrut(9));
                }
            }
        } catch (RuntimeException ex) {
            listado.add(new JLabel("No se pudieron consultar las tareas. Revisá la conexión con la base."));
        }
        fondo.add(new JScrollPane(listado), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        JButton cerrar = ComponentesUI.botonBorde("Cerrar");
        cerrar.addActionListener(e -> dialogo.dispose());
        acciones.add(cerrar);
        fondo.add(acciones, BorderLayout.SOUTH);
        dialogo.setContentPane(fondo);
        dialogo.setVisible(true);
    }

    private static void atenderAviso(JDialog dialogo,SolicitudesOperativasService.AvisoStock aviso) {
        try { new SolicitudesOperativasService().marcarAvisoStockAtendido(aviso.id); dialogo.dispose(); }
        catch(RuntimeException ex){JOptionPane.showMessageDialog(dialogo,ex.getMessage(),"Aviso de stock",JOptionPane.WARNING_MESSAGE);}
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }
}
