package puntonorte.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import puntonorte.conexion.ConexionBD;
import puntonorte.dao.StockDAO;

/** Solicitudes operativas persistentes de anulación y recepción de mercadería. */
public final class SolicitudesOperativasService {
    public static final String ANULACION = "ANULACION_VENTA";
    public static final String RECEPCION = "RECEPCION_COMPRA";

    public static final class Solicitud {
        public final int id, referencia;
        public final String tipo, solicitante, fecha, descripcion, motivo;
        Solicitud(int id, int referencia, String tipo, String solicitante, String fecha, String descripcion, String motivo) {
            this.id=id; this.referencia=referencia; this.tipo=tipo; this.solicitante=solicitante;
            this.fecha=fecha; this.descripcion=descripcion; this.motivo=motivo;
        }
        public String titulo() { return ANULACION.equals(tipo) ? "Anulación de venta #"+referencia : "Recepción de orden #"+referencia; }
    }

    private void sesion() { if (!Sesion.activa()) throw new ReglaNegocioException("No hay una sesión iniciada."); }
    private void puedeResolver() { sesion(); if (!Permisos.puedeResolverSolicitudesOperativas(Sesion.getNombreRol())) throw new ReglaNegocioException("Solo Encargado, Supervisor o Gerente puede resolver estas solicitudes."); }

    public static final class AvisoStock {
        public final int id;
        public final String solicitante, fecha, detalle;
        AvisoStock(int id,String solicitante,String fecha,String detalle){this.id=id;this.solicitante=solicitante;this.fecha=fecha;this.detalle=detalle;}
    }

    public boolean hayAvisoStockActivo() {
        sesion();
        try(Connection c=ConexionBD.conectar()) {
            int bajo=contarStockBajo(c);
            if(bajo==0) try(PreparedStatement p=c.prepareStatement("UPDATE avisos_stock_bajo SET estado='Restablecido' WHERE estado IN ('Pendiente','Atendido')")){p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM avisos_stock_bajo WHERE estado IN ('Pendiente','Atendido')");ResultSet r=p.executeQuery()){r.next();return r.getInt(1)>0;}
        } catch(SQLException e){throw error("consultar avisos de stock",e);}
    }

    public void enviarAvisoStockBajo() {
        sesion();String rol=Permisos.normalizar(Sesion.getNombreRol());
        if(!rol.equals("vendedor")&&!rol.contains("deposito"))throw new ReglaNegocioException("Este aviso está disponible para Vendedor y Depósito.");
        try(Connection c=ConexionBD.conectar()) {c.setAutoCommit(false);try {
            int bajo=contarStockBajo(c);
            if(bajo==0)throw new ReglaNegocioException("Ya no hay variantes con stock bajo para informar.");
            try(PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM avisos_stock_bajo WHERE estado IN ('Pendiente','Atendido')");ResultSet r=q.executeQuery()){r.next();if(r.getInt(1)>0)throw new ReglaNegocioException("Ya se avisó al Encargado por este nivel de stock. El botón volverá cuando se normalice el stock.");}
            StringBuilder detalle=new StringBuilder();
            String sql="SELECT p.nombre,v.sku,v.talle,v.color,COALESCE(SUM(s.cantidad),0) cantidad,v.stockMinimo minimo FROM producto_variantes v JOIN productos p ON p.idProducto=v.idProducto LEFT JOIN stock s ON s.idVariante=v.idVariante WHERE p.activo=1 AND v.activo=1 GROUP BY v.idVariante,p.nombre,v.sku,v.talle,v.color,v.stockMinimo HAVING COALESCE(SUM(s.cantidad),0)<=v.stockMinimo ORDER BY cantidad,p.nombre";
            try(PreparedStatement q=c.prepareStatement(sql);ResultSet r=q.executeQuery()){while(r.next()){if(detalle.length()>0)detalle.append("\n");detalle.append(r.getString("nombre")).append(" · ").append(r.getString("sku")).append(" · talle ").append(r.getString("talle")==null?"—":r.getString("talle")).append(" · ").append(r.getString("color")==null?"—":r.getString("color")).append(" · stock ").append(r.getInt("cantidad")).append(" / mínimo ").append(r.getInt("minimo"));}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO avisos_stock_bajo (idUsuarioSolicita,detalle,estado) VALUES (?,?,'Pendiente')")){p.setInt(1,Sesion.getIdUsuario());p.setString(2,detalle.toString());p.executeUpdate();}
            auditoria(c,"Aviso de stock bajo enviado",0,"avisos_stock_bajo",detalle.toString());c.commit();
        }catch(Exception e){c.rollback();if(e instanceof RuntimeException)throw(RuntimeException)e;throw new SQLException(e);}}catch(SQLException e){throw error("enviar aviso al Encargado",e);}
    }

    public List<AvisoStock> avisosStockPendientes() {
        sesion();if(!Permisos.puedeResolverSolicitudesOperativas(Sesion.getNombreRol()))return new ArrayList<>();
        List<AvisoStock> out=new ArrayList<>();String sql="SELECT a.idAviso,CONCAT(u.nombre,' ',u.apellido) solicitante,a.fechaAviso,a.detalle FROM avisos_stock_bajo a JOIN usuarios u ON u.idUsuario=a.idUsuarioSolicita WHERE a.estado='Pendiente' ORDER BY a.fechaAviso";
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(new AvisoStock(r.getInt("idAviso"),r.getString("solicitante"),String.valueOf(r.getTimestamp("fechaAviso")),r.getString("detalle")));return out;}catch(SQLException e){throw error("consultar avisos de stock bajo",e);}
    }

    public void marcarAvisoStockAtendido(int id) {
        puedeResolver();try(Connection c=ConexionBD.conectar()){c.setAutoCommit(false);try(PreparedStatement p=c.prepareStatement("UPDATE avisos_stock_bajo SET estado='Atendido',idUsuarioAtiende=?,fechaAtencion=CURRENT_TIMESTAMP WHERE idAviso=? AND estado='Pendiente'")){p.setInt(1,Sesion.getIdUsuario());p.setInt(2,id);if(p.executeUpdate()!=1)throw new ReglaNegocioException("El aviso ya fue atendido.");auditoria(c,"Aviso de stock bajo atendido",id,"avisos_stock_bajo","Atendido por "+Sesion.getNombreCompleto());c.commit();}catch(Exception e){c.rollback();if(e instanceof RuntimeException)throw(RuntimeException)e;throw new SQLException(e);}}catch(SQLException e){throw error("marcar el aviso como atendido",e);}
    }

    private int contarStockBajo(Connection c)throws SQLException {String sql="SELECT COUNT(*) FROM (SELECT v.idVariante FROM producto_variantes v JOIN productos p ON p.idProducto=v.idProducto LEFT JOIN stock s ON s.idVariante=v.idVariante WHERE p.activo=1 AND v.activo=1 GROUP BY v.idVariante,v.stockMinimo HAVING COALESCE(SUM(s.cantidad),0)<=v.stockMinimo) x";try(PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}}

    public void solicitarAnulacion(int venta, String motivo) {
        sesion(); if (!Permisos.normalizar(Sesion.getNombreRol()).equals("vendedor")) throw new ReglaNegocioException("Solo un Vendedor puede solicitar la anulación de una venta.");
        if (motivo == null || motivo.isBlank()) throw new ReglaNegocioException("Ingresá el motivo de la anulación.");
        guardarSolicitud(ANULACION, venta, 0, 0, null, motivo.trim(), null, null);
    }

    public void solicitarRecepcion(int pedido, int ubicacion, String comprobante, List<CompraService.Linea> lineas, List<String> observaciones) {
        sesion(); if (!Permisos.normalizar(Sesion.getNombreRol()).contains("deposito")) throw new ReglaNegocioException("Solo Depósito puede registrar una recepción para aprobación.");
        if (lineas == null || lineas.isEmpty()) throw new ReglaNegocioException("Agregá al menos una variante recibida.");
        if (ubicacion <= 0) throw new ReglaNegocioException("Seleccioná la ubicación de ingreso.");
        guardarSolicitud(RECEPCION, 0, pedido, ubicacion, comprobante, "Recepción informada por Depósito", lineas, observaciones);
    }

    private void guardarSolicitud(String tipo, int venta, int pedido, int ubicacion, String comprobante, String motivo,
            List<CompraService.Linea> lineas, List<String> observaciones) {
        try (Connection c=ConexionBD.conectar()) {
            c.setAutoCommit(false);
            try {
                validarReferencia(c,tipo,venta,pedido,lineas);
                int id;
                String sql="INSERT INTO solicitudes_operativas (tipo,idVenta,idPedido,idUbicacion,numComprobante,motivo,idUsuarioSolicita,estado) VALUES (?,?,?,?,?,?,?,'Pendiente')";
                try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                    if (ANULACION.equals(tipo)) { p.setString(1,tipo);p.setInt(2,venta);p.setNull(3,java.sql.Types.INTEGER);p.setNull(4,java.sql.Types.INTEGER);p.setNull(5,java.sql.Types.VARCHAR); }
                    else { p.setString(1,tipo);p.setNull(2,java.sql.Types.INTEGER);p.setInt(3,pedido);p.setInt(4,ubicacion);p.setString(5,comprobante); }
                    p.setString(6,motivo);p.setInt(7,Sesion.getIdUsuario());p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("No se generó el número de solicitud.");id=r.getInt(1);}
                }
                if (lineas != null) for(int i=0;i<lineas.size();i++) { CompraService.Linea l=lineas.get(i); if(l.cantidad<=0)throw new ReglaNegocioException("Las cantidades deben ser mayores que cero."); try(PreparedStatement p=c.prepareStatement("INSERT INTO solicitud_operativa_detalle (idSolicitud,idVariante,cantidad,observacion) VALUES (?,?,?,?)")){p.setInt(1,id);p.setInt(2,l.varianteId);p.setInt(3,l.cantidad);p.setString(4,observaciones!=null&&i<observaciones.size()?observaciones.get(i):null);p.executeUpdate();} }
                auditoria(c,"Solicitud operativa creada",id,tipo+" #"+(venta>0?venta:pedido),motivo);c.commit();
            } catch(Exception e) { c.rollback(); if(e instanceof RuntimeException)throw (RuntimeException)e;throw new SQLException(e); }
        } catch(SQLException e) { throw error("guardar la solicitud",e); }
    }

    private void validarReferencia(Connection c,String tipo,int venta,int pedido,List<CompraService.Linea> lineas)throws SQLException {
        if(ANULACION.equals(tipo)) {
            try(PreparedStatement p=c.prepareStatement("SELECT estado FROM ventas WHERE idVenta=? FOR UPDATE")){p.setInt(1,venta);try(ResultSet r=p.executeQuery()){if(!r.next())throw new ReglaNegocioException("La venta no existe.");String estado=r.getString(1);if(!"Pagada".equalsIgnoreCase(estado)&&!"Entregada".equalsIgnoreCase(estado))throw new ReglaNegocioException("Solo se puede solicitar la anulación de una venta pagada o entregada.");}}
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM solicitudes_operativas WHERE tipo=? AND idVenta=? AND estado='Pendiente'")){p.setString(1,tipo);p.setInt(2,venta);try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>0)throw new ReglaNegocioException("Esta venta ya tiene una solicitud pendiente.");}}
        } else {
            try(PreparedStatement p=c.prepareStatement("SELECT estado FROM pedidos_compra WHERE idPedido=? FOR UPDATE")){p.setInt(1,pedido);try(ResultSet r=p.executeQuery()){if(!r.next()||!("Pendiente".equalsIgnoreCase(r.getString(1))||"Parcial".equalsIgnoreCase(r.getString(1))))throw new ReglaNegocioException("La orden no existe o ya está cerrada.");}}
            if(lineas==null||lineas.isEmpty())throw new ReglaNegocioException("La recepción está vacía.");
        }
    }

    public List<Solicitud> pendientes() {
        puedeResolver(); List<Solicitud> out=new ArrayList<>();
        String sql="SELECT s.idSolicitud,s.tipo,COALESCE(s.idVenta,s.idPedido) referencia,CONCAT(u.nombre,' ',u.apellido) solicitante,s.fechaSolicitud,s.motivo,CASE WHEN s.tipo='ANULACION_VENTA' THEN CONCAT('Venta ',s.idVenta,' · ',v.estado) ELSE CONCAT('Proveedor ',pr.razonSocial,' · ',COALESCE(s.numComprobante,'sin comprobante'),' · Artículos: ',COALESCE((SELECT GROUP_CONCAT(CONCAT(p2.nombre,' ',COALESCE(v2.talle,''),' ',COALESCE(v2.color,''),' ×',d.cantidad) ORDER BY d.idDetalle SEPARATOR ', ') FROM solicitud_operativa_detalle d JOIN producto_variantes v2 ON v2.idVariante=d.idVariante JOIN productos p2 ON p2.idProducto=v2.idProducto WHERE d.idSolicitud=s.idSolicitud),'—')) END descripcion FROM solicitudes_operativas s JOIN usuarios u ON u.idUsuario=s.idUsuarioSolicita LEFT JOIN ventas v ON v.idVenta=s.idVenta LEFT JOIN pedidos_compra pc ON pc.idPedido=s.idPedido LEFT JOIN proveedor pr ON pr.idProveedor=pc.idProveedor WHERE s.estado='Pendiente' ORDER BY s.fechaSolicitud,s.idSolicitud";
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(new Solicitud(r.getInt("idSolicitud"),r.getInt("referencia"),r.getString("tipo"),r.getString("solicitante"),String.valueOf(r.getTimestamp("fechaSolicitud")),r.getString("descripcion"),r.getString("motivo")));return out;}catch(SQLException e){throw error("consultar solicitudes pendientes",e);}
    }

    public int cantidadPendiente() {
        puedeResolver();
        try(Connection c=ConexionBD.conectar();PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM solicitudes_operativas WHERE estado='Pendiente'");ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}catch(SQLException e){throw error("contar peticiones pendientes",e);}
    }

    public void resolver(int id, boolean aprobar, String motivoRechazo) {
        puedeResolver(); if(!aprobar&&(motivoRechazo==null||motivoRechazo.isBlank()))throw new ReglaNegocioException("Ingresá el motivo del rechazo.");
        try(Connection c=ConexionBD.conectar()) {c.setAutoCommit(false);try {
            SolicitudDatos s=leerPendiente(c,id); if(ANULACION.equals(s.tipo)){if(aprobar)aprobarAnulacion(c,s);}
            else if(aprobar)aprobarRecepcion(c,s);
            try(PreparedStatement p=c.prepareStatement("UPDATE solicitudes_operativas SET estado=?,idUsuarioResuelve=?,fechaResolucion=CURRENT_TIMESTAMP,motivoResolucion=? WHERE idSolicitud=? AND estado='Pendiente'")){p.setString(1,aprobar?"Aprobada":"Rechazada");p.setInt(2,Sesion.getIdUsuario());p.setString(3,aprobar?null:motivoRechazo.trim());p.setInt(4,id);if(p.executeUpdate()!=1)throw new ReglaNegocioException("La solicitud ya fue resuelta por otra persona.");}
            auditoria(c,aprobar?"Solicitud operativa aprobada":"Solicitud operativa rechazada",id,s.tipo+" #"+s.referencia,aprobar?s.motivo:motivoRechazo.trim());c.commit();
        } catch(Exception e){c.rollback();if(e instanceof RuntimeException)throw(RuntimeException)e;throw new SQLException(e);}}catch(SQLException e){throw error("resolver la solicitud",e);}
    }

    private static final class SolicitudDatos {int id,referencia,ubicacion,solicitante;String tipo,motivo,comprobante;}
    private SolicitudDatos leerPendiente(Connection c,int id)throws SQLException {try(PreparedStatement p=c.prepareStatement("SELECT * FROM solicitudes_operativas WHERE idSolicitud=? AND estado='Pendiente' FOR UPDATE")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new ReglaNegocioException("La solicitud ya no está pendiente.");SolicitudDatos s=new SolicitudDatos();s.id=id;s.tipo=r.getString("tipo");s.referencia=ANULACION.equals(s.tipo)?r.getInt("idVenta"):r.getInt("idPedido");s.ubicacion=r.getInt("idUbicacion");s.solicitante=r.getInt("idUsuarioSolicita");s.motivo=r.getString("motivo");s.comprobante=r.getString("numComprobante");return s;}}}
    private void aprobarAnulacion(Connection c,SolicitudDatos s)throws SQLException {
        int ubicacion;String estado;try(PreparedStatement p=c.prepareStatement("SELECT estado,idUbicacion FROM ventas WHERE idVenta=? FOR UPDATE")){p.setInt(1,s.referencia);try(ResultSet r=p.executeQuery()){if(!r.next())throw new ReglaNegocioException("La venta ya no existe.");estado=r.getString(1);ubicacion=r.getInt(2);}}
        if(!"Pagada".equalsIgnoreCase(estado)&&!"Entregada".equalsIgnoreCase(estado))throw new ReglaNegocioException("La venta ya no puede anularse porque cambió de estado.");
        StockDAO stock=new StockDAO();try(PreparedStatement p=c.prepareStatement("SELECT idVariante,cantidad FROM detalle_ventas WHERE idVenta=?")){p.setInt(1,s.referencia);try(ResultSet r=p.executeQuery()){while(r.next())if(stock.ajustarStock(c,r.getInt(1),ubicacion,r.getInt(2))<0)throw new ReglaNegocioException("No se pudo restituir el stock de la venta.");}}
        try(PreparedStatement p=c.prepareStatement("UPDATE ventas SET estado='Anulada',estadoFactura=CASE WHEN estadoFactura IS NULL THEN NULL ELSE 'ANULADA' END WHERE idVenta=?")){p.setInt(1,s.referencia);p.executeUpdate();}
        try(PreparedStatement p=c.prepareStatement("INSERT INTO anulaciones (idVenta,usrSolicita,usrAutoriza,fechaHora,motivo) VALUES (?,?,?,CURRENT_TIMESTAMP,?)")){p.setInt(1,s.referencia);p.setInt(2,s.solicitante);p.setInt(3,Sesion.getIdUsuario());p.setString(4,s.motivo);p.executeUpdate();}
    }
    private void aprobarRecepcion(Connection c,SolicitudDatos s)throws SQLException {
        int idRecepcion;String estado;
        try(PreparedStatement p=c.prepareStatement("SELECT estado FROM pedidos_compra WHERE idPedido=? FOR UPDATE")){p.setInt(1,s.referencia);try(ResultSet r=p.executeQuery()){if(!r.next())throw new ReglaNegocioException("La orden ya no existe.");estado=r.getString(1);if(!"Pendiente".equalsIgnoreCase(estado)&&!"Parcial".equalsIgnoreCase(estado))throw new ReglaNegocioException("La orden ya está cerrada.");}}
        try(PreparedStatement p=c.prepareStatement("INSERT INTO recepciones_compra (idPedido,idUbicacion,idUsuarioRecepciona,numComprobante,fechaRecepcion) VALUES (?,?,?,?,CURRENT_TIMESTAMP)",Statement.RETURN_GENERATED_KEYS)){p.setInt(1,s.referencia);p.setInt(2,s.ubicacion);p.setInt(3,s.solicitante);p.setString(4,s.comprobante);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("No se generó la recepción.");idRecepcion=r.getInt(1);}}
        StockDAO stock=new StockDAO();try(PreparedStatement p=c.prepareStatement("SELECT idVariante,cantidad,observacion FROM solicitud_operativa_detalle WHERE idSolicitud=?")){p.setInt(1,s.id);try(ResultSet r=p.executeQuery()){if(!r.isBeforeFirst())throw new ReglaNegocioException("La recepción no tiene artículos.");while(r.next()){int variante=r.getInt(1),cantidad=r.getInt(2);int pendiente;try(PreparedStatement q=c.prepareStatement("SELECT d.cantidadPedida-COALESCE((SELECT SUM(dr.cantidadRecibida) FROM detalle_recepcion dr JOIN recepciones_compra rc ON rc.idRecepcion=dr.idRecepcion WHERE rc.idPedido=d.idPedido AND dr.idVariante=d.idVariante),0) FROM detalle_ped_compra d WHERE d.idPedido=? AND d.idVariante=? FOR UPDATE")){q.setInt(1,s.referencia);q.setInt(2,variante);try(ResultSet x=q.executeQuery()){if(!x.next())throw new ReglaNegocioException("La variante no pertenece a la orden.");pendiente=x.getInt(1);}}
            if(cantidad>pendiente)throw new ReglaNegocioException("La cantidad solicitada excede lo pendiente para una variante.");try(PreparedStatement d=c.prepareStatement("INSERT INTO detalle_recepcion (idRecepcion,idVariante,cantidadRecibida,observacion) VALUES (?,?,?,?)")){d.setInt(1,idRecepcion);d.setInt(2,variante);d.setInt(3,cantidad);d.setString(4,r.getString(3));d.executeUpdate();}stock.ajustarStock(c,variante,s.ubicacion,cantidad);
        }}}
        try(PreparedStatement p=c.prepareStatement("UPDATE pedidos_compra SET estado=CASE WHEN EXISTS (SELECT 1 FROM detalle_ped_compra d WHERE d.idPedido=? AND d.cantidadPedida>COALESCE((SELECT SUM(dr.cantidadRecibida) FROM detalle_recepcion dr JOIN recepciones_compra rc ON rc.idRecepcion=dr.idRecepcion WHERE rc.idPedido=d.idPedido AND dr.idVariante=d.idVariante),0)) THEN 'Parcial' ELSE 'Completado' END WHERE idPedido=?")){p.setInt(1,s.referencia);p.setInt(2,s.referencia);p.executeUpdate();}
    }
    private void auditoria(Connection c,String accion,int ref,String tabla,String detalle)throws SQLException {try(PreparedStatement p=c.prepareStatement("INSERT INTO auditoria (idUsuario,usrAutoriza,accionRealizada,tablaAfectada,idRegistroAfectado,motivo,detalleCambio) VALUES (?,?,?,?,?,?,?)")){p.setInt(1,Sesion.getIdUsuario());p.setInt(2,Sesion.getIdUsuario());p.setString(3,accion);p.setString(4,tabla);p.setInt(5,ref);p.setString(6,detalle);p.setString(7,detalle);p.executeUpdate();}}
    private ReglaNegocioException error(String accion,SQLException e){return new ReglaNegocioException("No se pudo "+accion+": "+e.getMessage());}
}
