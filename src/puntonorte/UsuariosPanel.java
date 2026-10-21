package puntonorte;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import puntonorte.modelo.EstadoUsuario;
import puntonorte.modelo.Usuario;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.UsuarioService;

/** Listados reales de usuarios y operaciones del módulo. */
public class UsuariosPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JFrame owner;
    private final UsuarioService usuarioService = new UsuarioService();
    private final JTabbedPane tabs = new JTabbedPane();
    private String filtroBusqueda = "";

    public UsuariosPanel(JFrame owner) {
        this.owner = owner;
        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.add(ComponentesUI.titulo("Gestión de Usuarios"));
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(ComponentesUI.subtitulo("Solicitudes y estados de las cuentas."));
        contenido.add(Box.createVerticalStrut(18));
        tabs.setFont(Estilos.FUENTE_NEGRITA);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        contenido.add(tabs);
        add(contenido, BorderLayout.CENTER);
        recargar();
    }

    public final void recargar() {
        try {
            tabs.removeAll();
            agregarTab("Solicitudes", EstadoUsuario.PENDIENTE);
            agregarTab("Activos", EstadoUsuario.ACTIVO);
            agregarTab("Pausados", EstadoUsuario.PAUSADO);
            agregarTab("Desactivados", EstadoUsuario.DESACTIVADO);
            revalidate(); repaint();
        } catch (ReglaNegocioException ex) {
            tabs.removeAll();
            tabs.addTab("Usuarios", new JPanel());
        }
    }

    private void agregarTab(String titulo, EstadoUsuario estado) {
        ArrayList<Usuario> usuarios = usuarioService.listarPorEstado(estado);
        if (!filtroBusqueda.isBlank()) {
            String consulta = filtroBusqueda.trim().toLowerCase(java.util.Locale.ROOT);
            usuarios.removeIf(u -> !textoUsuario(u).contains(consulta));
        }
        tabs.addTab(titulo + "  " + usuarios.size(), crearTabla(usuarios, estado));
    }

    public void aplicarFiltroBusqueda(String texto) {
        filtroBusqueda = texto == null ? "" : texto;
        recargar();
    }

    private String textoUsuario(Usuario usuario) {
        return (usuario.getDni() + " " + usuario.getNombre() + " " + usuario.getApellido()
                + " " + valor(usuario.getTelefono()) + " " + usuario.getCorreo() + " "
                + usuarioService.nombreRol(usuario.getIdRol())).toLowerCase(java.util.Locale.ROOT);
    }

    private JPanel crearTabla(ArrayList<Usuario> usuarios, EstadoUsuario estado) {
        String[] columnas = {"DNI", "Nombre y apellido", "Teléfono", "Correo", "Rol", "Estado"};
        Object[][] datos = new Object[usuarios.size()][columnas.length];
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            datos[i] = new Object[] {u.getDni(), u.getNombre() + " " + u.getApellido(),
                    valor(u.getTelefono()), u.getCorreo(), usuarioService.nombreRol(u.getIdRol()),
                    nombreEstado(u.getActivo())};
        }
        JTable tabla = ComponentesUI.tabla(columnas, datos);
        ComponentesUI.atenuarFilasInactivas(tabla, fila -> {
            EstadoUsuario estadoUsuario = usuarios.get(fila).getActivo();
            return estadoUsuario == EstadoUsuario.PAUSADO || estadoUsuario == EstadoUsuario.DESACTIVADO;
        });
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        if (estado == EstadoUsuario.PENDIENTE) {
            agregarBoton(acciones, "✕ Rechazar", Estilos.ROJO, java.awt.Color.WHITE,
                    () -> rechazar(seleccion(tabla, usuarios)));
            agregarBoton(acciones, "✓ Aceptar solicitud", Estilos.CORAL, Estilos.AZUL,
                    () -> aprobar(seleccion(tabla, usuarios)));
        } else if (estado == EstadoUsuario.ACTIVO) {
            agregarBoton(acciones, "Reenviar código", Estilos.BLANCO, Estilos.AZUL,
                    () -> reenviar(seleccion(tabla, usuarios)));
            agregarBoton(acciones, "Ⅱ Pausar", Estilos.CORAL, Estilos.AZUL,
                    () -> pausar(seleccion(tabla, usuarios)));
            agregarBoton(acciones, "× Desactivar", Estilos.ROJO, java.awt.Color.WHITE,
                    () -> desactivar(seleccion(tabla, usuarios)));
        } else if (estado == EstadoUsuario.PAUSADO) {
            agregarBoton(acciones, "↺ Reactivar", Estilos.CORAL, Estilos.AZUL,
                    () -> reactivar(seleccion(tabla, usuarios)));
            agregarBoton(acciones, "× Desactivar", Estilos.ROJO, java.awt.Color.WHITE,
                    () -> desactivar(seleccion(tabla, usuarios)));
        } else {
            agregarBoton(acciones, "↺ Reactivar", Estilos.CORAL, Estilos.AZUL,
                    () -> reactivar(seleccion(tabla, usuarios)));
        }
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Estilos.FONDO);
        panel.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void agregarBoton(JPanel panel, String texto, java.awt.Color fondo, java.awt.Color letra, Runnable accion) {
        JButton boton = ComponentesUI.boton(texto, fondo, letra);
        boton.addActionListener(e -> accion.run());
        panel.add(boton);
    }

    private Usuario seleccion(JTable tabla, ArrayList<Usuario> usuarios) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(owner, "Seleccioná un usuario de la tabla.", "Falta seleccionar",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return usuarios.get(tabla.convertRowIndexToModel(fila));
    }

    private void aprobar(Usuario u) {
        if (u != null) new AprobarUsuarioDialog(owner, u, usuarioService.rolesAsignables(), this::recargar).setVisible(true);
    }
    private void rechazar(Usuario u) { ejecutarConMotivo(u, "Rechazar solicitud", usuarioService::rechazar); }
    private void pausar(Usuario u) { ejecutarConMotivo(u, "Pausar usuario", usuarioService::pausar); }
    private void desactivar(Usuario u) { ejecutarConMotivo(u, "Desactivar usuario", usuarioService::desactivar); }

    private void ejecutarConMotivo(Usuario u, String accion, OperacionConMotivo operacion) {
        if (u == null) return;
        String motivo = JOptionPane.showInputDialog(owner, "Motivo de la acción:", accion,
                JOptionPane.QUESTION_MESSAGE);
        if (motivo == null) return;
        try { operacion.ejecutar(u.getIdUsuario(), motivo); recargar(); }
        catch (ReglaNegocioException ex) { mostrarError(ex); }
    }
    private void reactivar(Usuario u) {
        if (u == null) return;
        try { usuarioService.reactivar(u.getIdUsuario()); recargar(); }
        catch (ReglaNegocioException ex) { mostrarError(ex); }
    }
    private void reenviar(Usuario u) {
        if (u == null) return;
        try { usuarioService.reenviarCodigo(u.getIdUsuario());
            JOptionPane.showMessageDialog(owner, "Se envió un código nuevo a " + u.getCorreo() + "."); }
        catch (ReglaNegocioException ex) { mostrarError(ex); }
    }
    private void mostrarError(ReglaNegocioException ex) {
        JOptionPane.showMessageDialog(owner, ex.getMessage(), "No se pudo realizar la acción", JOptionPane.WARNING_MESSAGE);
    }
    private String valor(String texto) { return texto == null || texto.isBlank() ? "-" : texto; }
    private String nombreEstado(EstadoUsuario estado) {
        return estado.name().substring(0, 1) + estado.name().substring(1).toLowerCase();
    }
    @FunctionalInterface private interface OperacionConMotivo { void ejecutar(int id, String motivo); }
}
