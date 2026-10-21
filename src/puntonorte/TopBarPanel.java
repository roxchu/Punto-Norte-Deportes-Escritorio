package puntonorte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;
import puntonorte.servicio.Permisos;

public class TopBarPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Map<String, JButton> botones = new LinkedHashMap<>();
    private final JLabel moduloActual;

    public TopBarPanel(Consumer<String> onNavigate) {

        setLayout(new BorderLayout());
        setBackground(Estilos.AZUL);
        setBorder(BorderFactory.createEmptyBorder());
        setPreferredSize(new java.awt.Dimension(100, Estilos.ALTO_TOPBAR));

        JPanel navegacion = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navegacion.setOpaque(false);

        if (Permisos.puedeVerModulo("INICIO")) agregarBoton(navegacion, "INICIO", "⌂ Inicio", onNavigate);
        if (Permisos.puedeVerModulo("PRODUCTOS")) agregarBoton(navegacion, "PRODUCTOS", "▦ Productos", onNavigate);
        if (Permisos.puedeVerModulo("PROVEEDORES")) agregarBoton(navegacion, "PROVEEDORES", "⇄ Proveedores", onNavigate);
        if (Permisos.puedeVerModulo("VENTAS") && Permisos.puedeVender()) agregarBoton(navegacion, "VENTAS", "▤ Ventas", onNavigate);
        if (Permisos.puedeGestionarDescuentos() && Permisos.puedeVerModulo("DESCUENTOS")) {
            agregarBoton(navegacion, "DESCUENTOS", "％ Descuentos", onNavigate);
        }
        if (Permisos.puedeVerModulo("HISTORIAL")) agregarBoton(navegacion, "HISTORIAL", "☷ Historial", onNavigate);
        if (Permisos.puedeVerModulo("USUARIOS")) agregarBoton(navegacion, "USUARIOS", "◎ Usuarios", onNavigate);

        moduloActual = new JLabel("Módulo: Inicio");
        moduloActual.setForeground(new Color(0xDC, 0xE7, 0xF0));
        moduloActual.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
        moduloActual.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 18));

        add(navegacion, BorderLayout.CENTER);
        add(moduloActual, BorderLayout.EAST);
    }

    private void agregarBoton(
            JPanel panel,
            String modulo,
            String texto,
            Consumer<String> onNavigate) {

        JButton boton = new JButton(texto);
        boton.setFont(Estilos.FUENTE_NEGRITA);
        boton.setForeground(Color.WHITE);
        boton.setBackground(Estilos.AZUL);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setUI(new BasicButtonUI());
        boton.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        boton.setPreferredSize(new java.awt.Dimension(
                Math.max(100, texto.length() * 9),
                Estilos.ALTO_TOPBAR
        ));

        ActionListener accion = e -> onNavigate.accept(modulo);
        boton.addActionListener(accion);

        botones.put(modulo, boton);
        panel.add(boton);
    }

    public void marcarActivo(String modulo) {

        for (Map.Entry<String, JButton> entrada : botones.entrySet()) {

            boolean activo = entrada.getKey().equals(modulo);
            JButton boton = entrada.getValue();

            boton.setForeground(activo ? Estilos.CORAL : Color.WHITE);
            boton.setBackground(activo ? Estilos.AZUL_OSCURO : Estilos.AZUL);
        }

        moduloActual.setText("Módulo: " + nombreModulo(modulo));
    }

    private String nombreModulo(String modulo) {

        return switch (modulo) {
            case "INICIO" -> "Inicio";
            case "PRODUCTOS" -> "Productos";
            case "PROVEEDORES" -> "Proveedores";
            case "VENTAS" -> "Ventas";
            case "DESCUENTOS" -> "Descuentos";
            case "HISTORIAL" -> "Historial";
            case "USUARIOS" -> "Usuarios";
            case "CUENTA" -> "Mi cuenta";
            default -> "Sistema";
        };
    }
}
