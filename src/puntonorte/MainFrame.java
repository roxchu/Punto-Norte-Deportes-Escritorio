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
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.SwingWorker;

import puntonorte.servicio.UsuarioService;
import puntonorte.servicio.Permisos;
import puntonorte.servicio.Productoservicio;
import puntonorte.servicio.InicioService;
import puntonorte.servicio.Sesion;
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
    private String moduloActual = "INICIO";
    private InicioPanel inicioPanel;
    private boolean cargandoAvisoStock;
    private final JPanel avisoStockGlobal = new JPanel(new BorderLayout(10, 0));
    private final JLabel textoAvisoStockGlobal = new JLabel();

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
        sidebar.setRefrescarPanel(this::refrescarModuloActual);
        sidebar.setAplicarFiltrosProductos(filtros -> {
            if (productosPanel != null) productosPanel.aplicarFiltros(filtros);
        });
        sidebar.setAplicarFiltrosProveedores(filtros -> {
            if (proveedoresPanel != null) proveedoresPanel.aplicarFiltrosPedidos(filtros);
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

        avisoStockGlobal.setBackground(new java.awt.Color(0xFF, 0xF0, 0xE8));
        avisoStockGlobal.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Estilos.CORAL),
                javax.swing.BorderFactory.createEmptyBorder(7, 18, 7, 14)));
        textoAvisoStockGlobal.setForeground(Estilos.AZUL);
        textoAvisoStockGlobal.setFont(Estilos.FUENTE_NEGRITA);
        JButton revisarStock = ComponentesUI.botonBorde("Revisar productos");
        revisarStock.addActionListener(e -> {
            mostrarModulo("PRODUCTOS");
            if (productosPanel != null) {
                if (puntonorte.servicio.Permisos.normalizar(Sesion.getNombreRol()).equals("vendedor"))
                    sidebar.prepararFiltroStockSalonBajo();
                else sidebar.prepararFiltroStockBajo();
                productosPanel.seleccionarPrimerProductoStockBajo();
            }
        });
        avisoStockGlobal.add(textoAvisoStockGlobal, BorderLayout.CENTER);
        avisoStockGlobal.add(revisarStock, BorderLayout.EAST);
        avisoStockGlobal.setVisible(false);

        // Cómo se arma cada módulo (se ejecuta recién cuando se abre).
        fabricas.put("INICIO", () -> {
            inicioPanel = new InicioPanel();
            return inicioPanel;
        });
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
        JPanel cabecera = new JPanel();
        cabecera.setLayout(new javax.swing.BoxLayout(cabecera, javax.swing.BoxLayout.Y_AXIS));
        cabecera.add(topBar);
        cabecera.add(avisoStockGlobal);
        centro.add(cabecera, BorderLayout.NORTH);
        centro.add(contenido, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);

        mostrarModulo("INICIO");
        Timer refrescoAvisoStock = new Timer(60000, e -> refrescarAvisoStock());
        refrescoAvisoStock.start();
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
            if ("INICIO".equals(modulo) && inicioPanel != null) inicioPanel.recargar();
            if ("HISTORIAL".equals(modulo) && historialPanel != null) historialPanel.recargar();
            if ("DESCUENTOS".equals(modulo) && descuentosPanel != null) descuentosPanel.recargar();
            if ("PROVEEDORES".equals(modulo) && proveedoresPanel != null) proveedoresPanel.recargar();
        }

        cardLayout.show(contenido, modulo);
        moduloActual = modulo;
        topBar.marcarActivo(modulo);
        sidebar.mostrarSeccion(modulo);
        refrescarAvisoStock();
    }

    private void refrescarAvisoStock() {
        if (cargandoAvisoStock) return;
        cargandoAvisoStock = true;
        new SwingWorker<InicioService.Datos, Void>() {
            @Override protected InicioService.Datos doInBackground() { return new InicioService().cargar(); }
            @Override protected void done() {
                try {
                    InicioService.Datos datos = get();
                    boolean vendedor = Permisos.normalizar(Sesion.getNombreRol()).equals("vendedor");
                    int cantidadAvisada = vendedor ? datos.stockSalonBajo : datos.stockBajo;
                    boolean visible = datos.disponible && cantidadAvisada > 0;
                    textoAvisoStockGlobal.setText(vendedor
                            ? "Atención: hay " + cantidadAvisada + " variante(s) con menos de 3 unidades en Salón."
                            : "Atención: hay " + cantidadAvisada + " variante(s) con stock bajo en el sistema.");
                    avisoStockGlobal.setVisible(visible);
                    if (!visible) {
                        try { new puntonorte.servicio.SolicitudesOperativasService().hayAvisoStockActivo(); }
                        catch (RuntimeException ignored) { }
                    }
                } catch (Exception ex) {
                    avisoStockGlobal.setVisible(false);
                } finally {
                    cargandoAvisoStock = false;
                    avisoStockGlobal.revalidate();
                    avisoStockGlobal.repaint();
                }
            }
        }.execute();
    }

    private void refrescarModuloActual() {
        switch (moduloActual) {
            case "INICIO":
                if (inicioPanel != null) inicioPanel.recargar();
                break;
            case "PRODUCTOS":
                if (productosPanel != null) productosPanel.recargar();
                break;
            case "VENTAS":
                if (ventasPanel != null) ventasPanel.recargar();
                break;
            case "PROVEEDORES":
                if (proveedoresPanel != null) proveedoresPanel.recargar();
                break;
            case "HISTORIAL":
                if (historialPanel != null) historialPanel.recargar();
                break;
            case "DESCUENTOS":
                if (descuentosPanel != null) descuentosPanel.recargar();
                break;
            case "USUARIOS":
                if (usuariosPanel != null) usuariosPanel.recargar();
                break;
            default:
                break;
        }
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
