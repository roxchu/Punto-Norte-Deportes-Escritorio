package puntonorte;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;

import puntonorte.servicio.UsuarioService;
import puntonorte.servicio.Permisos;
import puntonorte.servicio.Productoservicio;
import puntonorte.modelo.Categoria;

/**
 * Ventana principal. Los módulos se crean la PRIMERA VEZ que se abren (no todos juntos al iniciar
 * sesión): cada panel consulta la base al construirse, y con una base remota eso sumaba muchos
 * segundos de pantalla congelada antes de poder entrar.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout;
    private final JPanel contenido;
    private final TopBarPanel topBar;
    private final SidebarPanel sidebar;

    private final Map<String, Supplier<Component>> fabricas = new HashMap<>();
    private final Set<String> creados = new HashSet<>();
    private HistorialPanel historialPanel;      // null hasta que se abre por primera vez
    private DescuentosPanel descuentosPanel;    // null hasta que se abre (o si no tiene permiso)
    private ProductosPanel productosPanel;
    private VentasPanel ventasPanel;
    private UsuariosPanel usuariosPanel;
    private ProveedoresPanel proveedoresPanel;

    public MainFrame() {

        setTitle("Punto Norte Deportes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1280, 760);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contenido = new JPanel(cardLayout);
        contenido.setBackground(Estilos.FONDO);

        sidebar = new SidebarPanel(
                () -> mostrarModulo("CUENTA"),
                this::salir
        );
        sidebar.setAplicarFiltrosProductos(filtros -> {
            if (productosPanel != null) productosPanel.aplicarFiltros(filtros);
        });
        sidebar.setAplicarFiltrosVentas(filtros -> {
            if (ventasPanel != null) ventasPanel.aplicarFiltros(filtros);
        });
        sidebar.setAplicarFiltroUsuarios(filtro -> {
            if (usuariosPanel != null) usuariosPanel.aplicarFiltroBusqueda(filtro);
        });
        sidebar.setAplicarFiltrosHistorial(filtros -> {
            if (historialPanel != null) historialPanel.aplicarFiltros(filtros);
        });

        topBar = new TopBarPanel(this::mostrarModulo);

        // Cómo se arma cada módulo (se ejecuta recién cuando se abre).
        fabricas.put("INICIO", () -> new InicioPanel());
        fabricas.put("PRODUCTOS", () -> {
            productosPanel = new ProductosPanel(this, () -> sidebar.setOpcionesCategorias(new Productoservicio().listarCategorias()));
            return productosPanel;
        });
        fabricas.put("VENTAS", () -> {
            ventasPanel = new VentasPanel(this);
            sidebar.setOpcionesCategorias(new Productoservicio().listarCategorias());
            return ventasPanel;
        });
        if (Permisos.puedeGestionarDescuentos()) {
            fabricas.put("DESCUENTOS", () -> {
                descuentosPanel = new DescuentosPanel(this);
                return descuentosPanel;
            });
        }
        fabricas.put("PROVEEDORES", () -> {
            proveedoresPanel = new ProveedoresPanel(this);
            return proveedoresPanel;
        });
        fabricas.put("HISTORIAL", () -> {
            historialPanel = new HistorialPanel();
            return historialPanel;
        });
        fabricas.put("USUARIOS", () -> {
            usuariosPanel = new UsuariosPanel(this);
            return usuariosPanel;
        });
        fabricas.put("CUENTA", () -> new CuentaPanel());

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(topBar, BorderLayout.NORTH);
        centro.add(contenido, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);

        mostrarModulo("INICIO");
    }

    public void mostrarModulo(String modulo) {

        if (!Permisos.puedeVerModulo(modulo)) return;
        if ("DESCUENTOS".equals(modulo) && !fabricas.containsKey("DESCUENTOS")) return;

        boolean recienCreado = false;
        Supplier<Component> fabrica = fabricas.get(modulo);
        if (fabrica != null && !creados.contains(modulo)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                contenido.add(fabrica.get(), modulo);
                creados.add(modulo);
                recienCreado = true;
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        // Al volver a un módulo ya creado se refrescan sus datos (igual que antes).
        if (!recienCreado) {
            if ("HISTORIAL".equals(modulo) && historialPanel != null) historialPanel.recargar();
            if ("DESCUENTOS".equals(modulo) && descuentosPanel != null) descuentosPanel.recargar();
            if ("PROVEEDORES".equals(modulo) && proveedoresPanel != null) proveedoresPanel.recargar();
        }

        cardLayout.show(contenido, modulo);
        topBar.marcarActivo(modulo);
        sidebar.mostrarSeccion(modulo);
    }

    private void salir() {
        new UsuarioService().cerrarSesion();
        new LoginFrame().setVisible(true);
        dispose();
    }

    private java.util.ArrayList<String> listarSubcategorias() {
        java.util.ArrayList<String> nombres = new java.util.ArrayList<>();
        try {
            for (Categoria categoria : new Productoservicio().listarCategorias())
                if (categoria.getIdCategoriaPadre() != null) nombres.add(categoria.getNombre());
        } catch (RuntimeException ignored) { }
        return nombres;
    }
}
