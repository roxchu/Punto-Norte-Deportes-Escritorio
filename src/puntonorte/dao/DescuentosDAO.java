package puntonorte.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import puntonorte.conexion.ConexionBD;
import puntonorte.modelo.Descuentos;
import puntonorte.modelo.MedioPago;
import puntonorte.modelo.Naturaleza;

public class DescuentosDAO {

    /** Lista todo el catálogo, incluidos los descuentos inactivos. */
    public ArrayList<Descuentos> listarTodosSeguro() throws SQLException {
        ArrayList<Descuentos> lista = new ArrayList<>();
        String sql = "SELECT d.*, GROUP_CONCAT(p.nombre ORDER BY p.nombre SEPARATOR ', ') AS productosAsociados "
                + "FROM descuentos d LEFT JOIN descuento_producto dp ON dp.idDescuento = d.idDescuento "
                + "LEFT JOIN productos p ON p.idProducto = dp.idProducto "
                + "GROUP BY d.idDescuento ORDER BY d.activo DESC, d.nombre";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Descuentos d = mapear(rs);
                d.setProductoAsociado(rs.getString("productosAsociados"));
                lista.add(d);
            }
        }
        return lista;
    }

    /** Alta controlada por servicio; propaga errores SQL. */
    public int guardarSeguro(Descuentos descuento) throws SQLException {
        String sql = "INSERT INTO descuentos (nombre, naturaleza, tipo, medioPago, valor, fechaInicio, fechaFin, activo, cantidadCompra, cantidadPaga) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            cargar(ps, descuento);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("La BD no devolvió el id del descuento.");
                return rs.getInt(1);
            }
        }
    }

    /** Guarda la regla y su producto asociado de forma atómica. */
    public int guardarSeguro(Descuentos descuento, int idProducto) throws SQLException {
        String insertDescuento = "INSERT INTO descuentos (nombre, naturaleza, tipo, medioPago, valor, fechaInicio, fechaFin, activo, cantidadCompra, cantidadPaga) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(insertDescuento, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    cargar(ps, descuento);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("La BD no devolvió el id del descuento.");
                        id = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO descuento_producto (idDescuento, idProducto) VALUES (?, ?)")) {
                    ps.setInt(1, id);
                    ps.setInt(2, idProducto);
                    ps.executeUpdate();
                }
                con.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Descuentos de producto vigentes y ligados al producto de la variante indicada. */
    public ArrayList<Descuentos> listarVigentesPorVariante(java.time.LocalDate hoy, int idVariante) throws SQLException {
        String sql = "SELECT d.* FROM descuentos d "
                + "JOIN descuento_producto dp ON dp.idDescuento = d.idDescuento "
                + "JOIN producto_variantes v ON v.idProducto = dp.idProducto "
                + "WHERE d.activo = 1 AND d.tipo IN ('por producto', 'especiales') AND d.naturaleza = 'descuento' "
                + "AND d.medioPago IS NULL AND v.idVariante = ? "
                + "AND (d.fechaInicio IS NULL OR d.fechaInicio <= ?) "
                + "AND (d.fechaFin IS NULL OR d.fechaFin >= ?) ORDER BY d.nombre";
        ArrayList<Descuentos> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVariante);
            ps.setDate(2, Date.valueOf(hoy));
            ps.setDate(3, Date.valueOf(hoy));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Descuentos generales seleccionables en el pedido: porcentaje global o monto fijo general. */
    public ArrayList<Descuentos> listarDescuentosGenerales(java.time.LocalDate hoy) throws SQLException {
        String sql = "SELECT * FROM descuentos WHERE activo = 1 AND naturaleza = 'descuento' AND medioPago IS NULL "
                + "AND (tipo = 'generales' OR tipo = 'porcentuales') "
                + "AND (fechaInicio IS NULL OR fechaInicio <= ?) AND (fechaFin IS NULL OR fechaFin >= ?) ORDER BY nombre";
        ArrayList<Descuentos> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(hoy));
            ps.setDate(2, Date.valueOf(hoy));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        }
        return lista;
    }

    public boolean estaAsociadoAProducto(int idDescuento, int idProducto) throws SQLException {
        String sql = "SELECT 1 FROM descuento_producto WHERE idDescuento = ? AND idProducto = ?";
        try (Connection con = ConexionBD.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDescuento);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /** Activar/desactivar sin borrar, para conservar ventas históricas. */
    public boolean cambiarActivoSeguro(int idDescuento, boolean activo) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement("UPDATE descuentos SET activo = ? WHERE idDescuento = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idDescuento);
            return ps.executeUpdate() > 0;
        }
    }

    private void cargar(PreparedStatement ps, Descuentos d) throws SQLException {
        ps.setString(1, d.getNombre());
        ps.setString(2, d.getNaturaleza().name().toLowerCase());
        ps.setString(3, d.getTipo());
        ps.setString(4, d.getMedioPago() == null ? null : d.getMedioPago().name());
        ps.setDouble(5, d.getValor());
        ps.setDate(6, d.getFechaInicio() == null ? null : Date.valueOf(d.getFechaInicio()));
        ps.setDate(7, d.getFechaFin() == null ? null : Date.valueOf(d.getFechaFin()));
        ps.setBoolean(8, d.isActivo());
        if (d.getCantidadCompra() == null) ps.setNull(9, java.sql.Types.INTEGER); else ps.setInt(9, d.getCantidadCompra());
        if (d.getCantidadPaga() == null) ps.setNull(10, java.sql.Types.INTEGER); else ps.setInt(10, d.getCantidadPaga());
    }

    // INSERT
    public void guardar(Descuentos descuento) {
        String sql = "INSERT INTO descuentos "
                + "(nombre, naturaleza, tipo, medioPago, valor, fechaInicio, fechaFin, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, descuento.getNombre());

            // naturaleza: NOT NULL, siempre tiene valor. Ojo: la BD guarda 'descuento'/'recargo'
            // en minúscula, por eso .toLowerCase() al escribir.
            ps.setString(2, descuento.getNaturaleza().name().toLowerCase());

            ps.setString(3, descuento.getTipo()); // tipo quedó como String, no enum

            // medioPago: nullable -> setString admite null directo, no hace falta setNull
            ps.setString(4, descuento.getMedioPago() != null ? descuento.getMedioPago().name() : null);

            ps.setDouble(5, descuento.getValor());

            // fechaInicio / fechaFin: nullable -> Date.valueOf admite null si lo evaluamos antes
            ps.setDate(6, descuento.getFechaInicio() != null ? Date.valueOf(descuento.getFechaInicio()) : null);
            ps.setDate(7, descuento.getFechaFin() != null ? Date.valueOf(descuento.getFechaFin()) : null);

            ps.setBoolean(8, descuento.isActivo());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // SELECT
    public ArrayList<Descuentos> listar() {
        ArrayList<Descuentos> lista = new ArrayList<>();
        String sql = "SELECT * FROM descuentos";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Descuentos descuento = new Descuentos();
                descuento.setIdDescuento(rs.getInt("idDescuento"));
                descuento.setNombre(rs.getString("nombre"));
                descuento.setNaturaleza(Naturaleza.valueOf(rs.getString("naturaleza").toUpperCase()));
                descuento.setTipo(rs.getString("tipo"));

                // medioPago: nullable -> el String ya viene null si la columna es NULL
                String medioPagoTexto = rs.getString("medioPago");
                descuento.setMedioPago(medioPagoTexto != null ? MedioPago.valueOf(medioPagoTexto.toUpperCase()) : null);

                descuento.setValor(rs.getDouble("valor"));

                descuento.setFechaInicio(rs.getDate("fechaInicio") != null ? rs.getDate("fechaInicio").toLocalDate() : null);
                descuento.setFechaFin(rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null);

                descuento.setActivo(rs.getBoolean("activo"));

                lista.add(descuento);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    // UPDATE
    public void modificar(Descuentos descuento) {
        String sql = "UPDATE descuentos SET "
                + "nombre = ?, naturaleza = ?, tipo = ?, medioPago = ?, valor = ?, "
                + "fechaInicio = ?, fechaFin = ?, activo = ? "
                + "WHERE idDescuento = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, descuento.getNombre());
            ps.setString(2, descuento.getNaturaleza().name().toLowerCase());
            ps.setString(3, descuento.getTipo());
            ps.setString(4, descuento.getMedioPago() != null ? descuento.getMedioPago().name() : null);
            ps.setDouble(5, descuento.getValor());
            ps.setDate(6, descuento.getFechaInicio() != null ? Date.valueOf(descuento.getFechaInicio()) : null);
            ps.setDate(7, descuento.getFechaFin() != null ? Date.valueOf(descuento.getFechaFin()) : null);
            ps.setBoolean(8, descuento.isActivo());
            ps.setInt(9, descuento.getIdDescuento());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // DELETE
    public void eliminar(int idDescuento) {
        String sql = "DELETE FROM descuentos WHERE idDescuento = ?";

        try {
            Connection conexion = ConexionBD.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idDescuento);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }

    // ================================================================ ESPECIALES
    // Estos métodos LANZAN SQLException para que el servicio sepa si algo falló.

    /** Devuelve el descuento o null si no existe. */
    public Descuentos buscarPorId(int idDescuento) throws SQLException {
        String sql = "SELECT * FROM descuentos WHERE idDescuento = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDescuento);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Descuentos/recargos que se pueden usar HOY: activos y dentro de su rango de fechas
     * (una fecha vacía significa "sin límite"). El servicio los separa en los de medio de pago
     * (medioPago != null) y los de producto (medioPago == null).
     */
    public ArrayList<Descuentos> listarVigentes(java.time.LocalDate hoy) throws SQLException {
        String sql = "SELECT * FROM descuentos WHERE activo = 1 "
                + "AND (fechaInicio IS NULL OR fechaInicio <= ?) "
                + "AND (fechaFin IS NULL OR fechaFin >= ?) "
                + "ORDER BY nombre";

        ArrayList<Descuentos> lista = new ArrayList<>();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(hoy));
            ps.setDate(2, Date.valueOf(hoy));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // ================================================================== PRIVADOS

    private Descuentos mapear(ResultSet rs) throws SQLException {
        Descuentos descuento = new Descuentos();
        descuento.setIdDescuento(rs.getInt("idDescuento"));
        descuento.setNombre(rs.getString("nombre"));
        descuento.setNaturaleza(Naturaleza.valueOf(rs.getString("naturaleza").toUpperCase()));
        descuento.setTipo(rs.getString("tipo"));

        String medioPagoTexto = rs.getString("medioPago");
        descuento.setMedioPago(medioPagoTexto != null ? MedioPago.valueOf(medioPagoTexto.toUpperCase()) : null);

        descuento.setValor(rs.getDouble("valor"));
        descuento.setFechaInicio(rs.getDate("fechaInicio") != null ? rs.getDate("fechaInicio").toLocalDate() : null);
        descuento.setFechaFin(rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null);
        descuento.setActivo(rs.getBoolean("activo"));
        descuento.setCantidadCompra((Integer) rs.getObject("cantidadCompra"));
        descuento.setCantidadPaga((Integer) rs.getObject("cantidadPaga"));
        return descuento;
    }
}