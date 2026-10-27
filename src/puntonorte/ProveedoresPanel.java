package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Proveedor;
import puntonorte.modelo.Ubicacion;
import puntonorte.servicio.CompraService;
import puntonorte.servicio.Permisos;
import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.Stockservicio;
import puntonorte.servicio.SolicitudesOperativasService;

/** Gestión de proveedores, órdenes de compra y recepciones parciales. */
public class ProveedoresPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final java.awt.Frame owner;
    private final CompraService servicio = new CompraService();
    private final Stockservicio stockService = new Stockservicio();
    private final DefaultTableModel proveedoresModel = modelo("Razón social", "CUIT", "Teléfono", "Correo", "Condición de pago");
    private final DefaultTableModel pedidosModel = modelo("Orden", "Proveedor", "Fecha", "Estado", "Detalle");
    private final DefaultTableModel productosModel = modelo("Producto", "Razón social", "Costo Cotización");
    private final DefaultTableModel recepcionModel = modelo("Variante", "Cantidad aceptada", "Observación");
    private final DefaultTableModel borradorModel = modelo("Variante", "Cantidad", "Cotización del proveedor");
    private final List<CompraService.Linea> lineasPedido = new ArrayList<>();
    private final List<String> descripcionesPedido = new ArrayList<>();
    private final List<CompraService.Linea> lineasRecepcion = new ArrayList<>();
    private final List<String> observaciones = new ArrayList<>();
    private JComboBox<Proveedor> proveedorPedido;
    private JComboBox<ProductoVariante> variantePedido, varianteRecepcion;
    private JComboBox<CompraService.PedidoFila> pedidoRecepcion;
    private JComboBox<Ubicacion> ubicacionRecepcion;
    private JTextField cantidadPedido, barras, comprobante;
    private JLabel precioProveedorPedido;

    public ProveedoresPanel(java.awt.Frame owner) {
        this.owner=owner;setLayout(new BorderLayout());setBackground(Estilos.FONDO);setBorder(BorderFactory.createEmptyBorder(24,26,24,26));
        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new javax.swing.BoxLayout(encabezado, javax.swing.BoxLayout.Y_AXIS));
        encabezado.add(ComponentesUI.titulo("Proveedores y compras"));
        add(encabezado, BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();ComponentesUI.estilizarPestanas(tabs);tabs.addTab("Proveedores",panelProveedores());tabs.addTab("Órdenes de compra",panelPedidos());tabs.addTab("Recepción",panelRecepcion());tabs.addTab("Productos",panelProductos());add(ComponentesUI.tarjeta("Gestión de proveedores", tabs),BorderLayout.CENTER);
        recargar();
    }
    private JPanel panelProveedores(){JPanel p=new JPanel(new BorderLayout(8,8));p.setBackground(Estilos.FONDO);JTable t=new JTable(proveedoresModel);ComponentesUI.estilizarTablaExistente(t);p.add(new JScrollPane(t),BorderLayout.CENTER);JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));acciones.setOpaque(false);if(Permisos.puedeGestionarCompras()){acciones.add(boton("＋ Nuevo proveedor",this::nuevoProveedor));acciones.add(boton("Editar seleccionado",()->editarProveedor(t.getSelectedRow())));}p.add(acciones,BorderLayout.SOUTH);return p;}
    private JPanel panelPedidos(){JPanel p=new JPanel(new BorderLayout(8,8));p.setBackground(Estilos.FONDO);JTable t=new JTable(pedidosModel);ComponentesUI.estilizarTablaExistente(t);p.add(new JScrollPane(t),BorderLayout.CENTER);
        if(Permisos.puedeGestionarCompras()){
            JPanel form=new JPanel();form.setLayout(new javax.swing.BoxLayout(form,javax.swing.BoxLayout.Y_AXIS));form.setBackground(Estilos.FONDO);form.setBorder(BorderFactory.createTitledBorder("Crear orden de compra manual"));
            proveedorPedido=new JComboBox<>();variantePedido=new JComboBox<>();cantidadPedido=new JTextField("1",5);precioProveedorPedido=new JLabel("Precio unitario del proveedor: sin informar");
            proveedorPedido.addActionListener(e->cargarVariantesPedido());variantePedido.addActionListener(e->actualizarPrecioProveedor());
            ajustarAncho(proveedorPedido,300);ajustarAncho(variantePedido,650);ajustarAncho(cantidadPedido,90);
            JPanel filaProveedor=new JPanel(new FlowLayout(FlowLayout.LEFT,6,3));filaProveedor.setOpaque(false);filaProveedor.add(new JLabel("Proveedor"));filaProveedor.add(proveedorPedido);
            JPanel filaVariante=new JPanel(new FlowLayout(FlowLayout.LEFT,6,3));filaVariante.setOpaque(false);filaVariante.add(new JLabel("Variante"));filaVariante.add(variantePedido);
            JPanel filaCarga=new JPanel(new FlowLayout(FlowLayout.LEFT,8,3));filaCarga.setOpaque(false);filaCarga.add(new JLabel("Cantidad"));filaCarga.add(cantidadPedido);filaCarga.add(boton("Agregar ítem",this::agregarLineaPedido));filaCarga.add(precioProveedorPedido);
            JTable tablaBorrador = new JTable(borradorModel);ComponentesUI.estilizarTablaExistente(tablaBorrador);JScrollPane borrador=new JScrollPane(tablaBorrador);borrador.setPreferredSize(new Dimension(500,88));borrador.setMinimumSize(new Dimension(100,65));
            JPanel accionesForm=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,3));accionesForm.setOpaque(false);accionesForm.add(boton("Detalle orden de compra",()->detalleOrdenSeleccionada(t)));if(Permisos.esSupervisorOGerente(puntonorte.servicio.Sesion.getNombreRol()))accionesForm.add(boton("Cancelar orden seleccionada",()->cancelarPedido(t.getSelectedRow())));accionesForm.add(boton("Quitar último",this::quitarUltimaLinea));accionesForm.add(boton("Crear orden de compra",this::crearPedido));
            form.add(filaProveedor);form.add(filaVariante);form.add(filaCarga);form.add(borrador);form.add(accionesForm);p.add(form,BorderLayout.SOUTH);
        }
        return p;}
    private JPanel panelRecepcion(){JPanel p=new JPanel(new BorderLayout(8,8));p.setBackground(Estilos.FONDO);p.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));pedidoRecepcion=new JComboBox<>();pedidoRecepcion.addActionListener(e->cargarVariantesPendientes());ubicacionRecepcion=new JComboBox<>();comprobante=new JTextField(12);barras=new JTextField(18);barras.addActionListener(e->escanear());varianteRecepcion=new JComboBox<>();
        ajustarAncho(pedidoRecepcion,370);ajustarAncho(ubicacionRecepcion,190);ajustarAncho(comprobante,250);ajustarAncho(barras,260);ajustarAncho(varianteRecepcion,650);
        JPanel arriba=new JPanel();arriba.setLayout(new javax.swing.BoxLayout(arriba,javax.swing.BoxLayout.Y_AXIS));arriba.setOpaque(false);
        JPanel filaPedido=new JPanel(new FlowLayout(FlowLayout.LEFT,8,3));filaPedido.setOpaque(false);filaPedido.add(new JLabel("Orden pendiente"));filaPedido.add(pedidoRecepcion);filaPedido.add(new JLabel("Ubicación"));filaPedido.add(ubicacionRecepcion);
        JPanel filaComprobante=new JPanel(new FlowLayout(FlowLayout.LEFT,8,3));filaComprobante.setOpaque(false);filaComprobante.add(new JLabel("Factura o remito"));filaComprobante.add(comprobante);arriba.add(filaPedido);arriba.add(filaComprobante);p.add(arriba,BorderLayout.NORTH);
        JPanel centro=new JPanel(new BorderLayout(8,8));centro.setOpaque(false);JPanel scan=new JPanel();scan.setLayout(new javax.swing.BoxLayout(scan,javax.swing.BoxLayout.Y_AXIS));scan.setOpaque(false);scan.setBorder(BorderFactory.createTitledBorder("Recepción: escanear o probar manualmente"));
        JPanel filaCodigo=new JPanel(new FlowLayout(FlowLayout.LEFT,8,3));filaCodigo.setOpaque(false);filaCodigo.add(new JLabel("Código de barras (también podés escribirlo)"));filaCodigo.add(barras);filaCodigo.add(boton("Buscar código",this::escanear));
        JPanel filaManual=new JPanel(new FlowLayout(FlowLayout.LEFT,8,3));filaManual.setOpaque(false);filaManual.add(new JLabel("Sin lector: elegí una variante pendiente"));filaManual.add(varianteRecepcion);
        JPanel filaManualAccion=new JPanel(new FlowLayout(FlowLayout.LEFT,8,3));filaManualAccion.setOpaque(false);filaManualAccion.add(boton("Probar ingreso manual",this::agregarRecepcion));scan.add(filaCodigo);scan.add(filaManual);scan.add(filaManualAccion);
        scan.setPreferredSize(new Dimension(850,150));scan.setMinimumSize(new Dimension(200,150));
        JTable tablaRecepcion = new JTable(recepcionModel);ComponentesUI.estilizarTablaExistente(tablaRecepcion);JScrollPane listaRecepcion=new JScrollPane(tablaRecepcion);
        listaRecepcion.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(12,0,0,0),listaRecepcion.getBorder()));
        centro.add(scan,BorderLayout.NORTH);centro.add(listaRecepcion,BorderLayout.CENTER);p.add(centro,BorderLayout.CENTER);
        JPanel abajo=new JPanel(new FlowLayout(FlowLayout.RIGHT));abajo.setOpaque(false);abajo.add(boton("Limpiar",this::limpiarRecepcion));abajo.add(boton(Permisos.normalizar(puntonorte.servicio.Sesion.getNombreRol()).contains("deposito")?"Solicitar aprobación":"Registrar recepción aceptada",this::registrarRecepcion));p.add(abajo,BorderLayout.SOUTH);return p;}
    private JPanel panelProductos(){JPanel p=new JPanel(new BorderLayout(8,8));p.setBackground(Estilos.FONDO);JTable tablaProductos = new JTable(productosModel);ComponentesUI.estilizarTablaExistente(tablaProductos);p.add(new JScrollPane(tablaProductos),BorderLayout.CENTER);return p;}
    public final void recargar(){try{
        List<Proveedor> proveedores = servicio.proveedores();
        List<CompraService.PedidoFila> pedidos = servicio.pedidos();
        List<CompraService.PedidoFila> pendientes = servicio.pendientes();
        proveedoresModel.setRowCount(0);for(Proveedor v:proveedores)proveedoresModel.addRow(new Object[]{v.getRazonSocial(),v.getCUIT(),v.getTelefono(),v.getCorreo(),v.getCondicionPago()});
        pedidosModel.setRowCount(0);for(CompraService.PedidoFila f:pedidos)pedidosModel.addRow(new Object[]{f.id,f.proveedor,f.fecha,f.estado,f.resumen});
        productosModel.setRowCount(0);for(CompraService.ProductoProveedorFila f:servicio.productosProveedores())productosModel.addRow(new Object[]{f.producto,f.proveedor==null?"Sin proveedor asociado":f.proveedor,f.proveedor==null?"—":(f.cotizacion==null?"Sin informar":FormatoDinero.pesos(f.cotizacion))});
        if(proveedorPedido!=null){proveedorPedido.removeAllItems();for(Proveedor v:proveedores)proveedorPedido.addItem(v);cargarVariantesPedido();}
        if(pedidoRecepcion!=null){pedidoRecepcion.removeAllItems();for(CompraService.PedidoFila f:pendientes)pedidoRecepcion.addItem(f);cargarVariantesPendientes();}
        if(ubicacionRecepcion!=null){ubicacionRecepcion.removeAllItems();for(Ubicacion u:stockService.listarUbicaciones()){ubicacionRecepcion.addItem(u);if(puntonorte.servicio.Permisos.normalizar(u.getNombre()).contains("deposito"))ubicacionRecepcion.setSelectedItem(u);}}
    }catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void cargarVariantesPendientes(){if(pedidoRecepcion==null||varianteRecepcion==null)return;varianteRecepcion.removeAllItems();CompraService.PedidoFila pedido=(CompraService.PedidoFila)pedidoRecepcion.getSelectedItem();if(pedido==null)return;try{for(ProductoVariante v:servicio.variantesPendientesDePedido(pedido.id))varianteRecepcion.addItem(v);}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void cargarVariantesPedido(){if(proveedorPedido==null||variantePedido==null)return;Proveedor proveedor=(Proveedor)proveedorPedido.getSelectedItem();variantePedido.removeAllItems();if(proveedor==null){actualizarPrecioProveedor();return;}try{for(ProductoVariante v:servicio.variantesDeProveedor(proveedor.getIdProveedor()))variantePedido.addItem(v);actualizarPrecioProveedor();}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void actualizarPrecioProveedor(){if(precioProveedorPedido==null)return;ProductoVariante v=(ProductoVariante)variantePedido.getSelectedItem();Double precio=v==null?null:v.getCostoProveedor();precioProveedorPedido.setText("Precio unitario del proveedor: "+(precio==null?"sin informar":FormatoDinero.pesos(precio)));}
    private void nuevoProveedor(){editarProveedor(-1);}
    private void editarProveedor(int fila){try{Proveedor existente=null;if(fila>=0){List<Proveedor> vs=servicio.proveedores();if(fila>=vs.size())return;existente=vs.get(fila);}JTextField razon=new JTextField(existente==null?"":existente.getRazonSocial()),cuit=new JTextField(existente==null?"":n(existente.getCUIT())),tel=new JTextField(existente==null?"":n(existente.getTelefono())),correo=new JTextField(existente==null?"":n(existente.getCorreo())),condicion=new JTextField(existente==null?"Contado":n(existente.getCondicionPago()));JPanel f=form("Razón social",razon,"CUIT",cuit,"Teléfono",tel,"Correo",correo,"Condición de pago (ej.: 30 días)",condicion);if(JOptionPane.showConfirmDialog(owner,f,existente==null?"Nuevo proveedor":"Editar proveedor",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){Proveedor v=existente==null?new Proveedor():existente;v.setNombre(razon.getText());v.setRazonSocial(razon.getText());v.setCUIT(cuit.getText());v.setTelefono(tel.getText());v.setCorreo(correo.getText());v.setCondicionPago(condicion.getText());servicio.guardarProveedor(v);recargar();}}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void agregarLineaPedido(){try{ProductoVariante v=(ProductoVariante)variantePedido.getSelectedItem();if(v==null)throw new ReglaNegocioException("El proveedor seleccionado no tiene productos asociados.");int qty=entero(cantidadPedido.getText());if(qty<=0)throw new ReglaNegocioException("Ingrese una cantidad mayor que cero.");lineasPedido.add(new CompraService.Linea(v.getIdVariante(),qty,0));descripcionesPedido.add(etiqueta(v));Object precio=v.getCostoProveedor()==null?"Sin informar":FormatoDinero.pesos(v.getCostoProveedor());borradorModel.addRow(new Object[]{etiqueta(v),qty,precio});cantidadPedido.setText("1");}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void quitarUltimaLinea(){if(!lineasPedido.isEmpty()){lineasPedido.remove(lineasPedido.size()-1);descripcionesPedido.remove(descripcionesPedido.size()-1);borradorModel.removeRow(borradorModel.getRowCount()-1);}}
    private void crearPedido(){try{Proveedor v=(Proveedor)proveedorPedido.getSelectedItem();if(v==null)throw new ReglaNegocioException("Seleccione un proveedor.");servicio.crearPedido(v.getIdProveedor(),new ArrayList<>(lineasPedido));lineasPedido.clear();descripcionesPedido.clear();borradorModel.setRowCount(0);recargar();mensaje("La orden de compra se creó correctamente. El stock se actualizará al registrar la recepción.");}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void escanear(){try{ProductoVariante v=servicio.buscarBarras(barras.getText());if(v==null)throw new ReglaNegocioException("No se encontró una variante activa asociada a ese código de barras.");ProductoVariante pendiente=buscarVariante(v.getIdVariante());if(pendiente==null)throw new ReglaNegocioException("La variante seleccionada no tiene unidades pendientes en esta orden de compra.");varianteRecepcion.setSelectedItem(pendiente);agregarRecepcion();}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private ProductoVariante buscarVariante(int id){for(int i=0;i<varianteRecepcion.getItemCount();i++)if(varianteRecepcion.getItemAt(i).getIdVariante()==id)return varianteRecepcion.getItemAt(i);return null;}
    private void agregarRecepcion(){try{CompraService.PedidoFila pedido=(CompraService.PedidoFila)pedidoRecepcion.getSelectedItem();ProductoVariante v=(ProductoVariante)varianteRecepcion.getSelectedItem();if(pedido==null||v==null)throw new ReglaNegocioException("Seleccione una orden de compra y una variante.");String valor=JOptionPane.showInputDialog(owner,"Ingrese la cantidad aceptada de "+etiqueta(v)+":","Cantidad recibida",JOptionPane.QUESTION_MESSAGE);if(valor==null)return;int q=entero(valor);if(q<=0)throw new ReglaNegocioException("Ingrese una cantidad mayor que cero.");String obs=JOptionPane.showInputDialog(owner,"Observación (opcional): faltante, sobrante, producto incorrecto o producto dañado.");lineasRecepcion.add(new CompraService.Linea(v.getIdVariante(),q,0));observaciones.add(obs);recepcionModel.addRow(new Object[]{etiqueta(v),q,obs});barras.setText("");}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void registrarRecepcion(){try{CompraService.PedidoFila pedido=(CompraService.PedidoFila)pedidoRecepcion.getSelectedItem();Ubicacion ubi=(Ubicacion)ubicacionRecepcion.getSelectedItem();if(pedido==null||ubi==null)throw new ReglaNegocioException("Seleccione una orden de compra pendiente y una ubicación.");if(Permisos.normalizar(puntonorte.servicio.Sesion.getNombreRol()).contains("deposito")){new SolicitudesOperativasService().solicitarRecepcion(pedido.id,ubi.getIdUbicacion(),comprobante.getText(),new ArrayList<>(lineasRecepcion),new ArrayList<>(observaciones));limpiarRecepcion();recargar();mensaje("La recepción quedó pendiente de aprobación. El stock se actualizará cuando Supervisor o Gerente la apruebe.");}else{servicio.recibir(pedido.id,ubi.getIdUbicacion(),comprobante.getText(),new ArrayList<>(lineasRecepcion),new ArrayList<>(observaciones));limpiarRecepcion();recargar();mensaje("La recepción se registró correctamente. Solo las unidades aceptadas ingresaron al stock; las cantidades restantes continúan pendientes.");}}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void cancelarPedido(int fila){if(fila<0){aviso("Seleccione una orden de compra.");return;}try{int id=(int)pedidosModel.getValueAt(fila,0);String motivo=JOptionPane.showInputDialog(owner,"Ingrese el motivo de cancelación de la orden de compra #"+id+":");if(motivo==null)return;servicio.cancelarPedido(id,motivo);recargar();mensaje("La orden de compra se canceló correctamente. Las recepciones registradas y el stock ingresado se conservarán.");}catch(ReglaNegocioException e){aviso(e.getMessage());}}
    private void detalleOrdenSeleccionada(JTable tabla){int fila=tabla.getSelectedRow();if(fila<0){aviso("Seleccione una orden de compra para consultar su detalle.");return;}int modelo=tabla.convertRowIndexToModel(fila);int id=((Number)pedidosModel.getValueAt(modelo,0)).intValue();OrdenCompraDialog.mostrar(owner,servicio,id);}
    private void limpiarRecepcion(){lineasRecepcion.clear();observaciones.clear();recepcionModel.setRowCount(0);comprobante.setText("");barras.setText("");}
    private JButton boton(String s,Runnable r){JButton b=ComponentesUI.boton(s,Estilos.CORAL,Estilos.AZUL);b.addActionListener(e->r.run());return b;}
    private void ajustarAncho(java.awt.Component c,int ancho){c.setPreferredSize(new Dimension(ancho,32));c.setMaximumSize(new Dimension(ancho,34));}
    private JPanel form(Object... a){JPanel p=new JPanel(new GridLayout(a.length/2,2,8,8));for(int i=0;i<a.length;i+=2){p.add(new JLabel((String)a[i]));p.add((java.awt.Component)a[i+1]);}return p;}
    private static DefaultTableModel modelo(String... c){return new DefaultTableModel(c,0){private static final long serialVersionUID=1L;@Override public boolean isCellEditable(int r,int c){return false;}};}
    private int entero(String s){try{return Integer.parseInt(s.trim());}catch(Exception e){throw new ReglaNegocioException("Ingrese una cantidad entera válida.");}}
    private String etiqueta(ProductoVariante v){return v.getNombreProducto()+" · "+v.getSKU()+" · Talle "+n(v.getTalle())+" · "+n(v.getColor());}
    private String n(String s){return s==null?"":s;}
    private void aviso(String s){JOptionPane.showMessageDialog(owner,s,"Atención",JOptionPane.WARNING_MESSAGE);}
    private void mensaje(String s){JOptionPane.showMessageDialog(owner,s,"Proveedores y compras",JOptionPane.INFORMATION_MESSAGE);}
}
