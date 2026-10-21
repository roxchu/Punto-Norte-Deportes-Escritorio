package puntonorte.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import puntonorte.conexion.ConexionBD;
import puntonorte.dao.ProductoVarianteDAO;
import puntonorte.modelo.ProductoVariante;
import puntonorte.modelo.Proveedor;

/** Operaciones transaccionales del circuito de pedidos y recepción de compras. */
public class CompraService {
    public static final class PedidoFila {
        public final int id; public final String proveedor; public final LocalDate fecha; public final String estado; public final String resumen;
        PedidoFila(int id, String proveedor, LocalDate fecha, String estado, String resumen) { this.id=id; this.proveedor=proveedor; this.fecha=fecha; this.estado=estado; this.resumen=resumen; }
        @Override public String toString() { return "Pedido #"+id+" · "+proveedor+" · "+estado; }
    }
    public static final class Linea {
        public final int varianteId, cantidad; public final double costo;
        public Linea(int varianteId,int cantidad,double costo){this.varianteId=varianteId;this.cantidad=cantidad;this.costo=costo;}
    }
    private void sesion() { if (!Sesion.activa()) throw new ReglaNegocioException("No hay una sesión iniciada."); }
    private boolean deposito() { return Permisos.normalizar(Sesion.getNombreRol()).contains("deposito"); }
    private void puedeGestionar() { sesion(); if (deposito() || Permisos.normalizar(Sesion.getNombreRol()).equals("vendedor")) throw new ReglaNegocioException("No tenés permiso para gestionar proveedores o pedidos."); }

    public List<Proveedor> proveedores() {
        sesion(); List<Proveedor> out=new ArrayList<>();
        try(Connection c=ConexionBD.conectar(); PreparedStatement p=c.prepareStatement("SELECT * FROM proveedor ORDER BY nombre"); ResultSet r=p.executeQuery()) {
            while(r.next()) out.add(new Proveedor(r.getInt("idProveedor"),r.getString("nombre"),r.getString("razonSocial"),r.getString("cuit"),r.getString("telefono"),r.getString("correo")));
            return out;
        } catch(SQLException e){throw error("consultar proveedores",e);}
    }
    public void guardarProveedor(Proveedor v) {
        puedeGestionar();
        if(v.getNombre()==null||v.getNombre().isBlank()||v.getRazonSocial()==null||v.getRazonSocial().isBlank()) throw new ReglaNegocioException("Completá el nombre comercial y la razón social.");
        String sql=v.getIdProveedor()==0?"INSERT INTO proveedor (nombre,razonSocial,cuit,telefono,correo) VALUES (?,?,?,?,?)":"UPDATE proveedor SET nombre=?,razonSocial=?,cuit=?,telefono=?,correo=? WHERE idProveedor=?";
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,v.getNombre().trim());p.setString(2,v.getRazonSocial().trim());p.setString(3,v.getCUIT());p.setString(4,v.getTelefono());p.setString(5,v.getCorreo());if(v.getIdProveedor()!=0)p.setInt(6,v.getIdProveedor());p.executeUpdate();
        }catch(SQLException e){throw error("guardar el proveedor",e);}
    }
    public List<ProductoVariante> variantes() {
        sesion(); try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement("SELECT v.*,p.nombre nombreProducto FROM producto_variantes v JOIN productos p ON p.idProducto=v.idProducto WHERE v.activo=1 AND p.activo=1 ORDER BY p.nombre,v.talle,v.color");ResultSet r=p.executeQuery()){
            List<ProductoVariante> out=new ArrayList<>();while(r.next()){ProductoVariante v=new ProductoVariante();v.setIdVariante(r.getInt("idVariante"));v.setIdProducto(r.getInt("idProducto"));v.setSKU(r.getString("sku"));v.setCodigoBarras(r.getString("codigoBarras"));v.setTalle(r.getString("talle"));v.setColor(r.getString("color"));v.setCostoRef(r.getDouble("costoRef"));v.setNombreProducto(r.getString("nombreProducto"));out.add(v);}return out;
        }catch(SQLException e){throw error("consultar variantes",e);}
    }
    /** Variantes de productos asociados al proveedor seleccionado para armar un pedido coherente. */
    public List<ProductoVariante> variantesDeProveedor(int idProveedor) {
        sesion();List<ProductoVariante> out=new ArrayList<>();
        String sql="SELECT v.*,p.nombre nombreProducto,pp.costoUnitario FROM producto_variantes v JOIN productos p ON p.idProducto=v.idProducto JOIN producto_proveedor pp ON pp.idProducto=p.idProducto WHERE pp.idProveedor=? AND v.activo=1 AND p.activo=1 ORDER BY p.nombre,v.talle,v.color";
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,idProveedor);try(ResultSet r=p.executeQuery()){while(r.next()){ProductoVariante v=new ProductoVariante();v.setIdVariante(r.getInt("idVariante"));v.setIdProducto(r.getInt("idProducto"));v.setSKU(r.getString("sku"));v.setCodigoBarras(r.getString("codigoBarras"));v.setTalle(r.getString("talle"));v.setColor(r.getString("color"));v.setCostoRef(r.getDouble("costoRef"));double costo=r.getDouble("costoUnitario");v.setCostoProveedor(r.wasNull()?null:costo);v.setNombreProducto(r.getString("nombreProducto"));out.add(v);}}return out;}catch(SQLException e){throw error("consultar los productos de ese proveedor",e);}
    }
    public ProductoVariante buscarBarras(String codigo) {
        sesion(); if(codigo==null||codigo.isBlank()) return null;
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement("SELECT v.*,p.nombre nombreProducto FROM producto_variantes v JOIN productos p ON p.idProducto=v.idProducto WHERE v.codigoBarras=? AND v.activo=1 AND p.activo=1")){
            p.setString(1,codigo.trim());try(ResultSet r=p.executeQuery()){if(!r.next())return null;ProductoVariante v=new ProductoVariante();v.setIdVariante(r.getInt("idVariante"));v.setSKU(r.getString("sku"));v.setNombreProducto(r.getString("nombreProducto"));v.setTalle(r.getString("talle"));v.setColor(r.getString("color"));return v;}
        }catch(SQLException e){throw error("buscar el código de barras",e);}
    }
    public List<PedidoFila> pedidos() {
        sesion();List<PedidoFila> out=new ArrayList<>();String sql="SELECT pc.idPedido,pr.nombre,pc.fechaEmision,pc.estado,GROUP_CONCAT(CONCAT(p.nombre,' ',COALESCE(v.talle,''),' ',COALESCE(v.color,''),' ×',d.cantidadPedida) ORDER BY d.idDetPedido SEPARATOR ', ') resumen FROM pedidos_compra pc JOIN proveedor pr ON pr.idProveedor=pc.idProveedor LEFT JOIN detalle_ped_compra d ON d.idPedido=pc.idPedido LEFT JOIN producto_variantes v ON v.idVariante=d.idVariante LEFT JOIN productos p ON p.idProducto=v.idProducto GROUP BY pc.idPedido ORDER BY pc.fechaEmision DESC,pc.idPedido DESC";
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(new PedidoFila(r.getInt(1),r.getString(2),r.getDate(3).toLocalDate(),r.getString(4),r.getString(5)));return out;}catch(SQLException e){throw error("consultar pedidos",e);}
    }
    public List<PedidoFila> pendientes() { List<PedidoFila> all=pedidos();all.removeIf(p->!("Pendiente".equalsIgnoreCase(p.estado)||"Parcial".equalsIgnoreCase(p.estado)));return all; }
    /** Solo variantes del pedido seleccionado que todavía tienen unidades pendientes de recibir. */
    public List<ProductoVariante> variantesPendientesDePedido(int idPedido) {
        sesion();List<ProductoVariante> out=new ArrayList<>();
        String sql="SELECT v.idVariante,v.idProducto,v.sku,v.codigoBarras,v.talle,v.color,p.nombre nombreProducto,d.cantidadPedida-COALESCE((SELECT SUM(dr.cantidadRecibida) FROM detalle_recepcion dr JOIN recepciones_compra rc ON rc.idRecepcion=dr.idRecepcion WHERE rc.idPedido=d.idPedido AND dr.idVariante=d.idVariante),0) pendiente FROM detalle_ped_compra d JOIN producto_variantes v ON v.idVariante=d.idVariante JOIN productos p ON p.idProducto=v.idProducto WHERE d.idPedido=? AND d.cantidadPedida>COALESCE((SELECT SUM(dr.cantidadRecibida) FROM detalle_recepcion dr JOIN recepciones_compra rc ON rc.idRecepcion=dr.idRecepcion WHERE rc.idPedido=d.idPedido AND dr.idVariante=d.idVariante),0) ORDER BY p.nombre,v.talle,v.color";
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,idPedido);try(ResultSet r=p.executeQuery()){while(r.next()){ProductoVariante v=new ProductoVariante();v.setIdVariante(r.getInt("idVariante"));v.setIdProducto(r.getInt("idProducto"));v.setSKU(r.getString("sku"));v.setCodigoBarras(r.getString("codigoBarras"));v.setTalle(r.getString("talle"));v.setColor(r.getString("color"));v.setNombreProducto(r.getString("nombreProducto"));v.setCantidadPendientePedido(r.getInt("pendiente"));out.add(v);}}return out;}catch(SQLException e){throw error("consultar las variantes pendientes del pedido",e);}
    }
    public void crearPedido(int proveedor,List<Linea> lineas) {
        puedeGestionar();if(proveedor<=0||lineas==null||lineas.isEmpty())throw new ReglaNegocioException("Elegí un proveedor y agregá al menos una variante.");
        try(Connection c=ConexionBD.conectar()){c.setAutoCommit(false);try{
            int pedido;try(PreparedStatement p=c.prepareStatement("INSERT INTO pedidos_compra (idProveedor,idUsuario,fechaEmision,estado) VALUES (?,?,?, 'Pendiente')",Statement.RETURN_GENERATED_KEYS)){p.setInt(1,proveedor);p.setInt(2,Sesion.getIdUsuario());p.setDate(3,java.sql.Date.valueOf(LocalDate.now()));p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("No se obtuvo el número de pedido.");pedido=k.getInt(1);}}
            for(Linea l:lineas){if(l.cantidad<=0)throw new ReglaNegocioException("La cantidad pedida debe ser mayor a cero.");try(PreparedStatement relacion=c.prepareStatement("SELECT COUNT(*) FROM producto_variantes v JOIN producto_proveedor pp ON pp.idProducto=v.idProducto WHERE v.idVariante=? AND pp.idProveedor=?")){relacion.setInt(1,l.varianteId);relacion.setInt(2,proveedor);try(ResultSet r=relacion.executeQuery()){r.next();if(r.getInt(1)==0)throw new ReglaNegocioException("El producto de la variante no está asociado al proveedor elegido.");}}try(PreparedStatement p=c.prepareStatement("INSERT INTO detalle_ped_compra (idPedido,idVariante,cantidadPedida,costoAcordado) SELECT ?,?, ?, pp.costoUnitario FROM producto_variantes v JOIN producto_proveedor pp ON pp.idProducto=v.idProducto WHERE v.idVariante=? AND pp.idProveedor=?")){p.setInt(1,pedido);p.setInt(2,l.varianteId);p.setInt(3,l.cantidad);p.setInt(4,l.varianteId);p.setInt(5,proveedor);if(p.executeUpdate()!=1)throw new SQLException("No se pudo agregar una línea del pedido.");}}
            auditar(c,"Pedido a proveedor creado","pedidos_compra",pedido,"Pedido #"+pedido);c.commit();
        }catch(Exception e){c.rollback();if(e instanceof RuntimeException)throw (RuntimeException)e;throw new SQLException(e);}}catch(SQLException e){throw error("crear el pedido",e);}
    }
    public void cancelarPedido(int idPedido,String motivo) {
        sesion();if(!Permisos.esSupervisorOGerente(Sesion.getNombreRol()))throw new ReglaNegocioException("Solo Supervisor o Gerente puede cancelar un pedido.");
        if(motivo==null||motivo.isBlank())throw new ReglaNegocioException("Indicá el motivo de cancelación.");
        try(Connection c=ConexionBD.conectar()){c.setAutoCommit(false);try{
            String estado;try(PreparedStatement p=c.prepareStatement("SELECT estado FROM pedidos_compra WHERE idPedido=? FOR UPDATE")){p.setInt(1,idPedido);try(ResultSet r=p.executeQuery()){if(!r.next())throw new ReglaNegocioException("El pedido no existe.");estado=r.getString(1);}}
            if("Cancelado".equalsIgnoreCase(estado)||"Completado".equalsIgnoreCase(estado))throw new ReglaNegocioException("El pedido ya está cerrado y no se puede cancelar.");
            try(PreparedStatement p=c.prepareStatement("INSERT INTO anulacion_compra (idPedido,usrSolicita,usrAutoriza,motivo) VALUES (?,?,?,?)")){p.setInt(1,idPedido);p.setInt(2,Sesion.getIdUsuario());p.setInt(3,Sesion.getIdUsuario());p.setString(4,motivo.trim());p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("UPDATE pedidos_compra SET estado='Cancelado' WHERE idPedido=?")){p.setInt(1,idPedido);p.executeUpdate();}
            auditar(c,"Pedido a proveedor cancelado","pedidos_compra",idPedido,"Pedido #"+idPedido+" · Motivo: "+motivo.trim());c.commit();
        }catch(Exception e){c.rollback();if(e instanceof RuntimeException)throw (RuntimeException)e;throw new SQLException(e);}}catch(SQLException e){throw error("cancelar el pedido",e);}
    }
    public void recibir(int idPedido,int ubicacion,String comprobante,List<Linea> aceptadas,List<String> observaciones) {
        sesion();if(!deposito()&&!Permisos.esSupervisorOGerente(Sesion.getNombreRol())&&!Permisos.normalizar(Sesion.getNombreRol()).equals("encargado"))throw new ReglaNegocioException("Solo Depósito o un responsable puede registrar recepciones.");
        if(aceptadas==null||aceptadas.isEmpty())throw new ReglaNegocioException("Escaneá al menos una variante e indicá la cantidad aceptada.");
        try(Connection c=ConexionBD.conectar()){c.setAutoCommit(false);try{
            try(PreparedStatement check=c.prepareStatement("SELECT estado FROM pedidos_compra WHERE idPedido=? FOR UPDATE")){check.setInt(1,idPedido);try(ResultSet r=check.executeQuery()){if(!r.next()||"Cancelado".equalsIgnoreCase(r.getString(1))||"Completado".equalsIgnoreCase(r.getString(1)))throw new ReglaNegocioException("El pedido ya no admite recepciones.");}}
            int idRec;try(PreparedStatement p=c.prepareStatement("INSERT INTO recepciones_compra (idPedido,idUbicacion,idUsuarioRecepciona,numComprobante) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){p.setInt(1,idPedido);p.setInt(2,ubicacion);p.setInt(3,Sesion.getIdUsuario());p.setString(4,comprobante==null||comprobante.isBlank()?null:comprobante.trim());p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("No se obtuvo el número de recepción.");idRec=k.getInt(1);}}
            for(int i=0;i<aceptadas.size();i++){Linea l=aceptadas.get(i);if(l.cantidad<=0)throw new ReglaNegocioException("La cantidad aceptada debe ser mayor a cero.");int pedida;try(PreparedStatement p=c.prepareStatement("SELECT d.cantidadPedida-COALESCE((SELECT SUM(dr.cantidadRecibida) FROM detalle_recepcion dr JOIN recepciones_compra rc ON rc.idRecepcion=dr.idRecepcion WHERE rc.idPedido=d.idPedido AND dr.idVariante=d.idVariante),0) pendiente FROM detalle_ped_compra d WHERE d.idPedido=? AND d.idVariante=? FOR UPDATE")){p.setInt(1,idPedido);p.setInt(2,l.varianteId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new ReglaNegocioException("La variante escaneada no pertenece al pedido.");pedida=r.getInt(1);}}if(l.cantidad>pedida)throw new ReglaNegocioException("La cantidad aceptada supera lo pendiente para una variante.");String obs=observaciones!=null&&observaciones.size()>i?observaciones.get(i):null;try(PreparedStatement p=c.prepareStatement("INSERT INTO detalle_recepcion (idRecepcion,idVariante,cantidadRecibida,observacion) VALUES (?,?,?,?)")){p.setInt(1,idRec);p.setInt(2,l.varianteId);p.setInt(3,l.cantidad);p.setString(4,obs);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO stock (idVariante,idUbicacion,cantidad) VALUES (?,?,?) ON DUPLICATE KEY UPDATE cantidad=cantidad+VALUES(cantidad)")){p.setInt(1,l.varianteId);p.setInt(2,ubicacion);p.setInt(3,l.cantidad);p.executeUpdate();}
            }
            int pendientes;try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM detalle_ped_compra d WHERE d.idPedido=? AND d.cantidadPedida>COALESCE((SELECT SUM(dr.cantidadRecibida) FROM detalle_recepcion dr JOIN recepciones_compra rc ON rc.idRecepcion=dr.idRecepcion WHERE rc.idPedido=d.idPedido AND dr.idVariante=d.idVariante),0)")){p.setInt(1,idPedido);try(ResultSet r=p.executeQuery()){r.next();pendientes=r.getInt(1);}}
            String estado=pendientes==0?"Completado":"Parcial";try(PreparedStatement p=c.prepareStatement("UPDATE pedidos_compra SET estado=? WHERE idPedido=?")){p.setString(1,estado);p.setInt(2,idPedido);p.executeUpdate();}
            auditar(c,"Recepción de compra registrada","recepciones_compra",idRec,"Pedido #"+idPedido+" · "+aceptadas.size()+" variante(s) aceptadas; pendientes: "+pendientes);c.commit();
        }catch(Exception e){c.rollback();if(e instanceof RuntimeException)throw (RuntimeException)e;throw new SQLException(e);}}catch(SQLException e){throw error("registrar la recepción",e);}
    }
    private void auditar(Connection c,String accion,String tabla,int id,String detalle)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO auditoria (idUsuario,usrAutoriza,accionRealizada,tablaAfectada,idRegistroAfectado,detalleCambio) VALUES (?,?,?,?,?,?)")){p.setInt(1,Sesion.getIdUsuario());p.setInt(2,Sesion.getIdUsuario());p.setString(3,accion);p.setString(4,tabla);p.setInt(5,id);p.setString(6,detalle);p.executeUpdate();}}
    private ReglaNegocioException error(String accion,SQLException e){return new ReglaNegocioException("No se pudo "+accion+". Detalle: "+e.getMessage());}
}
