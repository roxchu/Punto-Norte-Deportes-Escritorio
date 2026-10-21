package puntonorte.servicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import puntonorte.dao.CategoriaDAO;
import puntonorte.dao.MarcaDAO;
import puntonorte.dao.ProductoDAO;
import puntonorte.dao.ProductoVarianteDAO;
import puntonorte.dao.ProveedorDAO;
import puntonorte.dao.StockDAO;
import puntonorte.modelo.Categoria;
import puntonorte.modelo.Marca;
import puntonorte.modelo.Producto;
import puntonorte.modelo.ProductoVariante;

/**
 * Reglas de negocio del catálogo: validaciones, permisos y auditoría.
 * Las pantallas llaman acá (nunca a los DAO) y muestran el mensaje de ReglaNegocioException si algo falla.
 *
 * Lo que entra en la auditoría se guarda con tabla "productos" (módulo Productos del historial),
 * con el SKU en el detalle.
 */
public class Productoservicio {

    private static final String TABLA = "productos";

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ProductoVarianteDAO varianteDAO = new ProductoVarianteDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final MarcaDAO marcaDAO = new MarcaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final AuditoriaService auditoria = new AuditoriaService();

    // ================================================================= CONSULTAS

    /**
     * Catálogo para la tabla y los filtros del sidebar. Cualquier usuario logueado puede consultar.
     * Si la categoría tiene subcategorías, se incluyen. El costo solo lo ven Supervisor y Gerente
     * (para el resto llega en 0: la pantalla no debe mostrar esa columna).
     */
    public ArrayList<ProductoVariante> listarCatalogo(String texto, Integer idCategoria, String talle,
            String color, boolean soloStockBajo, boolean incluirInactivos) {

        exigirSesion();

        ProductoVariante.Filtro f = new ProductoVariante.Filtro();
        f.setTexto(texto);
        f.setTalle(talle);
        f.setColor(color);
        f.setSoloStockBajo(soloStockBajo);
        f.setIncluirInactivos(incluirInactivos && Permisos.puedeGestionarCatalogo());
        if (idCategoria != null) {
            f.setIdsCategoria(idsConDescendientes(idCategoria));
        }

        try {
            ArrayList<ProductoVariante> lista = varianteDAO.buscarCatalogo(f);
            if (!Permisos.puedeVerCostos()) {
                for (ProductoVariante v : lista) {
                    v.setCostoRef(0);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw errorBD("consultar el catálogo", e);
        }
    }

    public ArrayList<Categoria> listarCategorias() {
        exigirSesion();
        return categoriaDAO.listar();
    }

    public ArrayList<Marca> listarMarcas() {
        exigirSesion();
        return marcaDAO.listar();
    }

    public ArrayList<String> listarTalles() {
        exigirSesion();
        try {
            return varianteDAO.listarTalles();
        } catch (SQLException e) {
            throw errorBD("cargar los talles", e);
        }
    }

    public ArrayList<String> listarColores() {
        exigirSesion();
        try {
            return varianteDAO.listarColores();
        } catch (SQLException e) {
            throw errorBD("cargar los colores", e);
        }
    }

    // ========================================================== MARCAS / CATEGORÍAS

    /** Crea una categoría principal o una subcategoría. Un padre null significa categoría principal. */
    public int crearCategoria(String nombre, Integer idCategoriaPadre) {
        exigirCatalogo();
        nombre = limpio(nombre);
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio.");
        }
        try {
            if (idCategoriaPadre != null) {
                boolean padreExiste = false;
                for (Categoria existente : categoriaDAO.listar()) {
                    if (existente.getIdCategoria() == idCategoriaPadre) {
                        padreExiste = true;
                        break;
                    }
                }
                if (!padreExiste) {
                    throw new ReglaNegocioException("La categoría padre elegida ya no existe.");
                }
            }
            if (categoriaDAO.existeNombre(nombre)) {
                throw new ReglaNegocioException("Ya existe una categoría llamada '" + nombre + "'.");
            }
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoria.setIdCategoriaPadre(idCategoriaPadre);
            int id = categoriaDAO.guardarSeguro(categoria);
            String detalle = idCategoriaPadre == null ? nombre : nombre + " · subcategoría de " + idCategoriaPadre;
            auditoria.registrar("Alta de categoría", TABLA, id, null, nombre, null, detalle);
            return id;
        } catch (SQLException e) {
            throw errorBD("crear la categoría", e);
        }
    }

    /** Atajo para crear una categoría principal. */
    public int crearCategoria(String nombre) {
        return crearCategoria(nombre, null);
    }

    /** Crea una marca para que pueda usarse inmediatamente al dar de alta un producto. */
    public int crearMarca(String nombre) {
        exigirCatalogo();
        nombre = limpio(nombre);
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("El nombre de la marca es obligatorio.");
        }
        try {
            if (marcaDAO.existeNombre(nombre)) {
                throw new ReglaNegocioException("Ya existe una marca llamada '" + nombre + "'.");
            }
            Marca marca = new Marca();
            marca.setNombre(nombre);
            int id = marcaDAO.guardarSeguro(marca);
            auditoria.registrar("Alta de marca", TABLA, id, null, nombre, null, nombre);
            return id;
        } catch (SQLException e) {
            throw errorBD("crear la marca", e);
        }
    }

    // =================================================================== ALTA

    /**
     * Crea un producto con sus variantes (al menos una). Todo o nada.
     * El stock arranca en 0: la mercadería entra por recepción de compra o por un ajuste auditado.
     */
    public int crearProducto(String nombre, String descripcion, int idCategoria, int idMarca, int idProveedor,
            List<ProductoVariante> variantes) {

        exigirCatalogo();

        nombre = limpio(nombre);
        descripcion = limpio(descripcion);
        validarDatosProducto(nombre);

        if (variantes == null || variantes.isEmpty()) {
            throw new ReglaNegocioException("El producto necesita al menos una variante.");
        }
        Set<String> skus = new HashSet<>();
        Set<String> barras = new HashSet<>();
        for (int i = 0; i < variantes.size(); i++) {
            ProductoVariante v = variantes.get(i);
            normalizar(v);
            validarVariante(v, "Variante " + (i + 1));
            if (!skus.add(v.getSKU())) {
                throw new ReglaNegocioException("El SKU " + v.getSKU() + " está repetido en el mismo producto.");
            }
            if (v.getCodigoBarras() != null && !barras.add(v.getCodigoBarras())) {
                throw new ReglaNegocioException("El código de barras " + v.getCodigoBarras()
                        + " está repetido en el mismo producto.");
            }
        }

        exigirCategoriaYMarca(idCategoria, idMarca);
        if (idProveedor <= 0) {
            throw new ReglaNegocioException("Primero creá un proveedor y después volvé a cargar el producto.");
        }

        try {
            if (!proveedorDAO.existe(idProveedor)) {
                throw new ReglaNegocioException("El proveedor seleccionado ya no existe. Primero creá un proveedor válido.");
            }
            if (productoDAO.existeProducto(nombre, idMarca, 0)) {
                throw new ReglaNegocioException("Ya existe un producto con ese nombre y esa marca.");
            }
            for (ProductoVariante v : variantes) {
                if (varianteDAO.existeSku(v.getSKU(), 0)) {
                    throw new ReglaNegocioException("Ya existe una variante con el SKU " + v.getSKU() + ".");
                }
                if (varianteDAO.existeCodigoBarras(v.getCodigoBarras(), 0)) {
                    throw new ReglaNegocioException("Ya existe una variante con el código de barras "
                            + v.getCodigoBarras() + ".");
                }
            }

            Producto p = new Producto();
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setIdCategoria(idCategoria);
            p.setIdMarca(idMarca);
            p.setActivo(true);
            for (ProductoVariante v : variantes) {
                v.setActivo(true);
            }

            int idProducto = productoDAO.guardarConVariantes(p, variantes, idProveedor);

            StringBuilder lista = new StringBuilder();
            for (ProductoVariante v : variantes) {
                lista.append(lista.length() == 0 ? "" : ", ").append(v.getSKU());
            }
            auditoria.registrar("Alta de producto", TABLA, idProducto, null,
                    variantes.size() + (variantes.size() == 1 ? " variante" : " variantes"),
                    null, nombre + " (" + lista + ") · proveedor " + idProveedor);
            return idProducto;

        } catch (SQLException e) {
            throw errorBD("guardar el producto", e);
        }
    }

    // ================================================================= EDICIÓN

    public void editarProducto(int idProducto, String nombre, String descripcion, int idCategoria, int idMarca) {

        exigirCatalogo();

        nombre = limpio(nombre);
        descripcion = limpio(descripcion);
        validarDatosProducto(nombre);
        exigirCategoriaYMarca(idCategoria, idMarca);

        try {
            Producto anterior = productoDAO.buscarPorId(idProducto);
            if (anterior == null) {
                throw new ReglaNegocioException("El producto ya no existe.");
            }
            if (productoDAO.existeProducto(nombre, idMarca, idProducto)) {
                throw new ReglaNegocioException("Ya existe otro producto con ese nombre y esa marca.");
            }

            String antes = resumen(anterior);

            Producto nuevo = new Producto();
            nuevo.setIdProducto(idProducto);
            nuevo.setNombre(nombre);
            nuevo.setDescripcion(descripcion);
            nuevo.setIdCategoria(idCategoria);
            nuevo.setIdMarca(idMarca);
            nuevo.setActivo(anterior.isActivo());

            String despues = resumen(nuevo);
            if (antes.equals(despues) && igual(anterior.getDescripcion(), descripcion)) {
                throw new ReglaNegocioException("No hay cambios para guardar.");
            }

            productoDAO.modificar(nuevo);

            // El DAO básico no avisa si falla: releemos para confirmar.
            Producto guardado = productoDAO.buscarPorId(idProducto);
            if (guardado == null || !resumen(guardado).equals(despues)) {
                throw new ReglaNegocioException("No se pudo guardar el cambio. Revisá la consola.");
            }

            auditoria.registrar("Edición de producto", TABLA, idProducto, antes, despues, null, nombre);

        } catch (SQLException e) {
            throw errorBD("editar el producto", e);
        }
    }

    /**
     * Edita una variante (v.getIdVariante() identifica cuál). Si cambia el precio queda un registro
     * aparte en la auditoría, con el valor anterior y el nuevo.
     */
    public void editarVariante(ProductoVariante v) {

        exigirCatalogo();

        normalizar(v);
        validarVariante(v, "La variante");

        try {
            ProductoVariante anterior = varianteDAO.buscarPorId(v.getIdVariante());
            if (anterior == null) {
                throw new ReglaNegocioException("La variante ya no existe.");
            }
            if (varianteDAO.existeSku(v.getSKU(), v.getIdVariante())) {
                throw new ReglaNegocioException("Ya existe otra variante con el SKU " + v.getSKU() + ".");
            }
            if (varianteDAO.existeCodigoBarras(v.getCodigoBarras(), v.getIdVariante())) {
                throw new ReglaNegocioException("Ya existe otra variante con el código de barras "
                        + v.getCodigoBarras() + ".");
            }

            boolean cambioPrecio = Double.compare(anterior.getPrecioVenta(), v.getPrecioVenta()) != 0;
            String antes = resumen(anterior);
            v.setIdProducto(anterior.getIdProducto());   // una variante no cambia de producto
            String despues = resumen(v);

            if (!cambioPrecio && antes.equals(despues)) {
                throw new ReglaNegocioException("No hay cambios para guardar.");
            }

            varianteDAO.modificar(v);

            ProductoVariante guardada = varianteDAO.buscarPorId(v.getIdVariante());
            if (guardada == null || !resumen(guardada).equals(despues)) {
                throw new ReglaNegocioException("No se pudo guardar el cambio. Revisá la consola.");
            }

            if (cambioPrecio) {
                auditoria.registrar("Precio modificado", TABLA, anterior.getIdProducto(),
                        dinero(anterior.getPrecioVenta()), dinero(v.getPrecioVenta()), null, v.getSKU());
            }
            if (!antes.equals(despues)) {
                auditoria.registrar("Edición de variante", TABLA, anterior.getIdProducto(),
                        antes, despues, null, v.getSKU());
            }

        } catch (SQLException e) {
            throw errorBD("editar la variante", e);
        }
    }

    // ============================================================ HABILITAR / INHABILITAR

    /**
     * Habilita o inhabilita un producto (y con él todas sus variantes, que dejan de verse).
     * Para inhabilitar exige un motivo y que no quede mercadería en stock.
     */
    public void cambiarEstadoProducto(int idProducto, boolean activo, String motivo) {

        exigirCatalogo();
        if (!activo && limpio(motivo).isEmpty()) {
            throw new ReglaNegocioException("Indicá el motivo de la baja.");
        }

        try {
            Producto p = productoDAO.buscarPorId(idProducto);
            if (p == null) {
                throw new ReglaNegocioException("El producto ya no existe.");
            }
            if (p.isActivo() == activo) {
                throw new ReglaNegocioException("El producto ya está " + (activo ? "habilitado." : "inhabilitado."));
            }

            if (!activo) {
                for (ProductoVariante v : varianteDAO.listarPorProducto(idProducto, false)) {
                    exigirSinStock(v);
                }
            }

            if (!productoDAO.cambiarActivo(idProducto, activo)) {
                throw new ReglaNegocioException("No se pudo cambiar el estado del producto.");
            }

            auditoria.registrar(activo ? "Producto habilitado" : "Producto inhabilitado", TABLA, idProducto,
                    activo ? "INACTIVO" : "ACTIVO", activo ? "ACTIVO" : "INACTIVO",
                    activo ? null : limpio(motivo), p.getNombre());

        } catch (SQLException e) {
            throw errorBD("cambiar el estado del producto", e);
        }
    }

    public void cambiarEstadoVariante(int idVariante, boolean activo, String motivo) {

        exigirCatalogo();
        if (!activo && limpio(motivo).isEmpty()) {
            throw new ReglaNegocioException("Indicá el motivo de la baja.");
        }

        try {
            ProductoVariante v = varianteDAO.buscarPorId(idVariante);
            if (v == null) {
                throw new ReglaNegocioException("La variante ya no existe.");
            }
            if (v.isActivo() == activo) {
                throw new ReglaNegocioException("La variante ya está " + (activo ? "habilitada." : "inhabilitada."));
            }

            if (!activo) {
                exigirSinStock(v);
            }

            if (!varianteDAO.cambiarActivo(idVariante, activo)) {
                throw new ReglaNegocioException("No se pudo cambiar el estado de la variante.");
            }

            auditoria.registrar(activo ? "Variante habilitada" : "Variante inhabilitada", TABLA, v.getIdProducto(),
                    activo ? "INACTIVA" : "ACTIVA", activo ? "ACTIVA" : "INACTIVA",
                    activo ? null : limpio(motivo), v.getSKU());

        } catch (SQLException e) {
            throw errorBD("cambiar el estado de la variante", e);
        }
    }

    // ================================================================== PRIVADOS

    private void exigirSesion() {
        if (!Sesion.activa()) {
            throw new ReglaNegocioException("No hay una sesión iniciada.");
        }
    }

    private void exigirCatalogo() {
        exigirSesion();
        if (!Permisos.puedeGestionarCatalogo()) {
            throw new ReglaNegocioException("No tenés permiso para modificar el catálogo de productos.");
        }
    }

    private void validarDatosProducto(String nombre) {
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio.");
        }
    }

    private void exigirCategoriaYMarca(int idCategoria, int idMarca) {
        boolean categoriaOk = false;
        for (Categoria c : categoriaDAO.listar()) {
            if (c.getIdCategoria() == idCategoria) {
                categoriaOk = true;
                break;
            }
        }
        if (!categoriaOk) {
            throw new ReglaNegocioException("Elegí una categoría válida.");
        }

        boolean marcaOk = false;
        for (Marca m : marcaDAO.listar()) {
            if (m.getIdMarca() == idMarca) {
                marcaOk = true;
                break;
            }
        }
        if (!marcaOk) {
            throw new ReglaNegocioException("Elegí una marca válida.");
        }
    }

    /** Deja los textos prolijos: sin espacios de más, SKU en mayúsculas, código de barras vacío = null. */
    private void normalizar(ProductoVariante v) {
        v.setSKU(limpio(v.getSKU()).toUpperCase());
        v.setTalle(limpio(v.getTalle()));
        v.setColor(limpio(v.getColor()));
        String barras = limpio(v.getCodigoBarras());
        v.setCodigoBarras(barras.isEmpty() ? null : barras);
    }

    private void validarVariante(ProductoVariante v, String quien) {
        if (v.getSKU().isEmpty()) {
            throw new ReglaNegocioException(quien + ": el SKU es obligatorio.");
        }
        if (v.getTalle().isEmpty() || v.getColor().isEmpty()) {
            throw new ReglaNegocioException(quien + " (" + v.getSKU()
                    + "): talle y color son obligatorios (usá \"Único\" si no aplica).");
        }
        if (!(v.getPrecioVenta() > 0) || Double.isInfinite(v.getPrecioVenta())) {
            throw new ReglaNegocioException(quien + " (" + v.getSKU() + "): el precio debe ser mayor a 0.");
        }
        if (!(v.getCostoRef() >= 0) || Double.isInfinite(v.getCostoRef())) {
            throw new ReglaNegocioException(quien + " (" + v.getSKU() + "): el costo no puede ser negativo.");
        }
        if (v.getStockMinimo() < 0 || v.getStockMaximo() < 0) {
            throw new ReglaNegocioException(quien + " (" + v.getSKU() + "): el stock mínimo y máximo no pueden ser negativos.");
        }
        if (v.getStockMaximo() > 0 && v.getStockMaximo() < v.getStockMinimo()) {
            throw new ReglaNegocioException(quien + " (" + v.getSKU() + "): el stock máximo no puede ser menor al mínimo.");
        }
    }

    /** No se inhabilita mercadería que sigue físicamente en el local: primero hay que ajustar el stock a 0. */
    private void exigirSinStock(ProductoVariante v) throws SQLException {
        int stock = stockDAO.obtenerStockTotal(v.getIdVariante());
        if (stock > 0) {
            throw new ReglaNegocioException("No se puede inhabilitar: la variante " + v.getSKU()
                    + " todavía tiene " + stock + " unidades en stock. Primero ajustá el stock a 0.");
        }
    }

    /** La categoría elegida y todas sus subcategorías. */
    private List<Integer> idsConDescendientes(int idCategoria) {
        ArrayList<Categoria> todas = categoriaDAO.listar();
        Set<Integer> ids = new LinkedHashSet<>();
        ids.add(idCategoria);

        boolean creció = true;
        while (creció) {
            creció = false;
            for (Categoria c : todas) {
                if (c.getIdCategoriaPadre() != null && ids.contains(c.getIdCategoriaPadre())
                        && ids.add(c.getIdCategoria())) {
                    creció = true;
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private String resumen(Producto p) {
        return "nombre=" + p.getNombre() + "; categoría=" + p.getIdCategoria() + "; marca=" + p.getIdMarca();
    }

    private String resumen(ProductoVariante v) {
        return "sku=" + v.getSKU() + "; talle=" + v.getTalle() + "; color=" + v.getColor()
                + "; barras=" + (v.getCodigoBarras() == null ? "-" : v.getCodigoBarras())
                + "; precio=" + dinero(v.getPrecioVenta()) + "; costo=" + dinero(v.getCostoRef())
                + "; mín=" + v.getStockMinimo() + "; máx=" + v.getStockMaximo();
    }

    private String dinero(double valor) {
        return "$ " + String.format(Locale.ROOT, "%.2f", valor);
    }

    private boolean igual(String a, String b) {
        return limpio(a).equals(limpio(b));
    }

    private String limpio(String texto) {
        return texto == null ? "" : texto.trim();
    }

    /** 1062 = clave duplicada en MySQL (por ejemplo un SKU que ya existe). */
    private ReglaNegocioException errorBD(String accion, SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new ReglaNegocioException("Ya existe un registro con ese SKU o código de barras.");
        }
        return new ReglaNegocioException("No se pudo " + accion + ". Detalle: " + e.getMessage());
    }
}
