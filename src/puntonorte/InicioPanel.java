package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import puntonorte.servicio.InicioService;
import puntonorte.servicio.Sesion;

public class InicioPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private InicioService.Datos datos;

    public InicioPanel() {
        recargar();
    }

    public final void recargar() {
        InicioService.Datos cargados;
        try {
            cargados = new InicioService().cargar();
        } catch (RuntimeException ex) {
            cargados = new InicioService.Datos();
            cargados.disponible = false;
        }
        datos = cargados;

        removeAll();
        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);
        GridBagConstraints fila = new GridBagConstraints();
        fila.gridx = 0;
        fila.gridwidth = GridBagConstraints.REMAINDER;
        fila.weightx = 1;
        fila.fill = GridBagConstraints.HORIZONTAL;
        fila.anchor = GridBagConstraints.NORTHWEST;
        fila.insets = new Insets(0, 0, 18, 0);
        contenido.add(crearEncabezado(), fila);
        fila.gridy = 1;
        contenido.add(crearResumen(), fila);
        int siguienteFila = 2;
        fila.gridy = siguienteFila++;
        contenido.add(crearDashboard(), fila);
        fila.gridy = siguienteFila;
        fila.weighty = 1;
        fila.fill = GridBagConstraints.BOTH;
        fila.insets = new Insets(4, 0, 0, 0);
        contenido.add(crearHistorial(), fila);

        // El contenido ocupa todo el espacio central y cada fila se expande hasta
        // los márgenes del panel para mantener alineados resumen y tablas.
        add(contenido, BorderLayout.CENTER);
        revalidate();
        repaint();
        NotificacionesUI.actualizarTarjetasInicio();
    }

    private JPanel crearEncabezado() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        panel.add(ComponentesUI.titulo("Panel del empleado"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(ComponentesUI.subtitulo(
                "Usuario activo: " + Sesion.getNombreCompleto() + " · " + Sesion.getNombreRol()
        ));
        for (java.awt.Component componente : panel.getComponents()) {
            if (componente instanceof JLabel) {
                ((JLabel) componente).setAlignmentX(LEFT_ALIGNMENT);
                ((JLabel) componente).setHorizontalAlignment(JLabel.LEFT);
            }
        }

        return panel;
    }

    private JPanel crearResumen() {

        JPanel panel = new JPanel(new GridLayout(1, 5, 0, 0));
        panel.setBackground(Estilos.BEIGE);
        panel.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, Estilos.AZUL));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(resumen("Productos registrados", valor(datos.productos)));
        boolean vendedor = puntonorte.servicio.Permisos.normalizar(Sesion.getNombreRol()).equals("vendedor");
        panel.add(resumen(vendedor ? "Variantes con menos de 3 en Salón" : "Productos con stock bajo",
                valor(vendedor ? datos.stockSalonBajo : datos.stockBajo)));
        panel.add(resumen("Ventas registradas hoy", valor(datos.ventasHoy)));
        panel.add(resumen("Pedidos en curso", valor(datos.pedidosAbiertos)));
        panel.add(resumen("Usuarios pendientes", valor(datos.solicitudesUsuarios)));

        return panel;
    }

    private JPanel resumen(String texto, String valor) {

        JPanel panel = new JPanel();
        panel.setBackground(Estilos.BEIGE);
        panel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1,
                new java.awt.Color(0xC9, 0xC1, 0xB6)
        ));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel nombre = new JLabel(texto);
        nombre.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 10));
        nombre.setForeground(Estilos.TEXTO_SECUNDARIO);

        JLabel cantidad = new JLabel(valor);
        cantidad.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 20));
        cantidad.setForeground(Estilos.TEXTO);

        panel.setBorder(BorderFactory.createCompoundBorder(
                panel.getBorder(),
                BorderFactory.createEmptyBorder(12, 15, 10, 15)
        ));

        panel.add(nombre);
        panel.add(Box.createVerticalStrut(4));
        panel.add(cantidad);

        return panel;
    }

    private String valor(int numero) {
        return datos.disponible ? String.valueOf(numero) : "—";
    }

    private JPanel crearDashboard() {

        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(900, 170));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(crearPedidos());
        panel.add(PeticionesUI.crearTarjetaInicio(datos.stockBajo, datos.disponible,
                datos.stockSalonBajo, datos.variantesStockSalonBajo));

        return panel;
    }

    private JPanel crearPedidos() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Estilos.BEIGE);
        panel.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, Estilos.AZUL));

        JLabel titulo = new JLabel("Pedidos a proveedores");
        titulo.setFont(Estilos.FUENTE_NEGRITA);
        titulo.setBorder(BorderFactory.createEmptyBorder(12, 13, 10, 13));

        String[] columnas = {
                "Pedido",
                "Proveedor",
                "Estado"
        };

        Object[][] filas = new Object[datos.pedidos.size()][columnas.length];
        for (int i = 0; i < datos.pedidos.size(); i++) {
            InicioService.Pedido pedido = datos.pedidos.get(i);
            filas[i] = new Object[] {"PC" + String.format("%05d", pedido.id), pedido.proveedor, pedido.estado};
        }

        JTable tabla = ComponentesUI.tabla(columnas, filas);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearHistorial() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Últimas operaciones");
        titulo.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16));

        String[] columnas = {
                "Fecha",
                "Usuario",
                "Rol",
                "Operación",
                "Módulo",
                "Detalle"
        };

        List<InicioService.Operacion> actividad = datos.operaciones;
        Object[][] filas = new Object[actividad.size()][columnas.length];
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (int i = 0; i < actividad.size(); i++) {
            InicioService.Operacion operacion = actividad.get(i);
            filas[i] = new Object[] {operacion.fecha.format(formato), operacion.usuario, operacion.rol,
                    operacion.operacion, operacion.modulo, operacion.detalle};
        }

        JTable tabla = ComponentesUI.tabla(columnas, filas);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(ComponentesUI.scrollTabla(tabla), BorderLayout.CENTER);

        return panel;
    }
}
