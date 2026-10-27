package puntonorte;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import puntonorte.servicio.Permisos;
import puntonorte.servicio.Sesion;
import puntonorte.servicio.SolicitudesOperativasService;

/** Bandeja separada para solicitudes que requieren una decisión operativa. */
public final class PeticionesUI {
    private static JLabel contador;
    private PeticionesUI() { }

    public static int contarPendientes() {
        if (!Permisos.puedeResolverSolicitudesOperativas(Sesion.getNombreRol())) return 0;
        try { return new SolicitudesOperativasService().cantidadPendiente(); }
        catch (RuntimeException ex) { return -1; }
    }

    public static JPanel crearTarjetaInicio() {
        JPanel contenido=new JPanel(new BorderLayout(12,6));contenido.setBackground(Estilos.BEIGE);contenido.setBorder(BorderFactory.createEmptyBorder(10,12,9,12));
        contador=new JLabel(textoContador());contador.setFont(new java.awt.Font("Tahoma",java.awt.Font.BOLD,26));contador.setForeground(Estilos.AZUL);
        JLabel descripcion=new JLabel("solicitudes por resolver");descripcion.setForeground(Estilos.TEXTO_SECUNDARIO);descripcion.setFont(Estilos.FUENTE_PEQUENA);
        JPanel numero=new JPanel();numero.setOpaque(false);numero.setLayout(new BoxLayout(numero,BoxLayout.Y_AXIS));numero.add(contador);numero.add(descripcion);
        JButton ver=ComponentesUI.boton("Ver peticiones",Estilos.CORAL,Estilos.AZUL);ver.addActionListener(e->{abrir(contenido);actualizarContador();NotificacionesUI.actualizarContadorAvisos();});
        contenido.add(numero,BorderLayout.CENTER);contenido.add(ver,BorderLayout.EAST);
        return ComponentesUI.tarjeta("Peticiones",contenido);
    }

    public static void actualizarContador() { if(contador!=null)contador.setText(textoContador()); }
    private static String textoContador(){int cantidad=contarPendientes();return cantidad<0?"—":String.valueOf(cantidad);}

    private static void abrir(Component owner) {
        Window ventana=SwingUtilities.getWindowAncestor(owner);JDialog dialogo=new JDialog(ventana,"Peticiones",JDialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(650,460);dialogo.setMinimumSize(new Dimension(510,360));dialogo.setLocationRelativeTo(ventana);
        JPanel fondo=new JPanel(new BorderLayout(0,12));fondo.setBackground(Estilos.FONDO);fondo.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));fondo.add(ComponentesUI.titulo("Peticiones pendientes"),BorderLayout.NORTH);
        JPanel listado=new JPanel();listado.setOpaque(false);listado.setLayout(new BoxLayout(listado,BoxLayout.Y_AXIS));
        try {
            if(!Permisos.puedeResolverSolicitudesOperativas(Sesion.getNombreRol())) {
                JLabel mensaje=new JLabel("Las peticiones de anulación y recepción las resuelve Encargado, Supervisor o Gerente.");mensaje.setForeground(Estilos.TEXTO_SECUNDARIO);listado.add(mensaje);
            } else {
                List<SolicitudesOperativasService.Solicitud> solicitudes=new SolicitudesOperativasService().pendientes();
                if(solicitudes.isEmpty()){JLabel vacio=new JLabel("No hay peticiones pendientes para resolver.");vacio.setForeground(Estilos.TEXTO_SECUNDARIO);listado.add(vacio);}
                for(SolicitudesOperativasService.Solicitud s:solicitudes){
                    JPanel item=new JPanel(new BorderLayout(0,5));item.setBackground(Estilos.BEIGE);item.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,4,0,0,Estilos.CORAL),BorderFactory.createEmptyBorder(9,12,9,12)));
                    JLabel titulo=new JLabel(s.titulo());titulo.setForeground(Estilos.AZUL);titulo.setFont(Estilos.FUENTE_NEGRITA);
                    JLabel detalle=new JLabel("<html>"+escapar(s.solicitante+" · "+s.fecha+" · "+s.descripcion+" · Motivo: "+s.motivo)+"</html>");detalle.setForeground(Estilos.TEXTO_SECUNDARIO);detalle.setFont(Estilos.FUENTE_PEQUENA);item.add(titulo,BorderLayout.NORTH);item.add(detalle,BorderLayout.CENTER);
                    JPanel acciones=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));acciones.setOpaque(false);JButton rechazar=ComponentesUI.botonBorde("Rechazar"),aprobar=ComponentesUI.boton("Aprobar",Estilos.CORAL,Estilos.AZUL);
                    rechazar.addActionListener(e->resolver(dialogo,s,false));aprobar.addActionListener(e->resolver(dialogo,s,true));acciones.add(rechazar);acciones.add(aprobar);item.add(acciones,BorderLayout.SOUTH);item.setMaximumSize(new Dimension(Integer.MAX_VALUE,125));listado.add(item);listado.add(Box.createVerticalStrut(9));
                }
            }
        } catch(RuntimeException ex){listado.add(new JLabel("No se pudieron cargar las peticiones. Revisá la conexión y la migración de base."));}
        fondo.add(new JScrollPane(listado),BorderLayout.CENTER);JPanel sur=new JPanel(new FlowLayout(FlowLayout.RIGHT));sur.setOpaque(false);JButton cerrar=ComponentesUI.botonBorde("Cerrar");cerrar.addActionListener(e->dialogo.dispose());sur.add(cerrar);fondo.add(sur,BorderLayout.SOUTH);dialogo.setContentPane(fondo);dialogo.setVisible(true);
    }

    private static void resolver(JDialog dialogo,SolicitudesOperativasService.Solicitud s,boolean aprobar){
        String motivo=aprobar?"Aprobada por "+Sesion.getNombreRol():JOptionPane.showInputDialog(dialogo,"Motivo del rechazo para "+s.titulo()+":","Rechazar petición",JOptionPane.QUESTION_MESSAGE);
        if(motivo==null)return;if(!aprobar&&motivo.isBlank()){JOptionPane.showMessageDialog(dialogo,"Ingresá el motivo del rechazo.","Peticiones",JOptionPane.WARNING_MESSAGE);return;}
        if(aprobar&&JOptionPane.showConfirmDialog(dialogo,"¿Querés aprobar "+s.titulo()+"?","Confirmar aprobación",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{new SolicitudesOperativasService().resolver(s.id,aprobar,motivo);dialogo.dispose();}catch(RuntimeException ex){JOptionPane.showMessageDialog(dialogo,ex.getMessage(),"Peticiones",JOptionPane.WARNING_MESSAGE);}
    }
    private static String escapar(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>");}
}
