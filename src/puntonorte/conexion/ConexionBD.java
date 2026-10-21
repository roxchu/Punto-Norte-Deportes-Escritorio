package puntonorte.conexion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Centraliza la conexión a la base de datos. Todas las clases DAO llaman a ConexionBD.conectar().
 *
 *  - Usuario y contraseña van aparte de la URL.
 *  - Timeouts: si el servidor no responde, falla con un error en vez de congelar la pantalla.
 *  - Reutiliza conexiones: abrir una conexión nueva al hosting tarda varios segundos. Cuando un DAO
 *    hace con.close(), la conexión vuelve a una pequeña reserva en lugar de cerrarse.
 *    Solo se verifica que siga viva si estuvo más de 30 segundos sin usarse (así no se agrega un
 *    viaje de red extra en cada consulta).
 *  - precalentar(): abre una conexión en segundo plano al iniciar el programa, mientras el usuario
 *    escribe en el login.
 */
public class ConexionBD {

    private static final String HOST = "pma.torga.com.ar";
    private static final int PUERTO = 3306;
    private static final String BASE = "s32_puntonorte";
    private static final String USUARIO = "u32_3PZUQ41yzT";
    private static final String CONTRASENA = "DrxyfB^Fr+BEFV@cSSsTUlJa";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE
            + "?connectTimeout=20000&socketTimeout=30000";

    private static final int MAX_LIBRES = 2;
    private static final long INACTIVA_MS = 30_000;

    private static final class Libre {
        final Connection con;
        final long desde = System.currentTimeMillis();
        Libre(Connection con) { this.con = con; }
    }

    private static final ConcurrentLinkedQueue<Libre> libres = new ConcurrentLinkedQueue<>();

    public static Connection conectar() throws SQLException {
        Libre libre;
        while ((libre = libres.poll()) != null) {
            boolean reciente = System.currentTimeMillis() - libre.desde < INACTIVA_MS;
            if (reciente ? !cerradaLocalmente(libre.con) : estaViva(libre.con)) {
                return envolver(libre.con);
            }
            cerrarSilencioso(libre.con);
        }
        return envolver(DriverManager.getConnection(URL, USUARIO, CONTRASENA));
    }

    /** Abre una conexión en segundo plano para que la primera consulta real no espere. */
    public static void precalentar() {
        Thread hilo = new Thread(() -> {
            try (Connection c = conectar()) {
                // solo se abre para dejarla lista en la reserva
            } catch (SQLException ignorada) {
                // si falla, la primera consulta real mostrará el error
            }
        }, "precalentar-bd");
        hilo.setDaemon(true);
        hilo.start();
    }

    private static boolean cerradaLocalmente(Connection c) {
        try {
            return c.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    private static boolean estaViva(Connection c) {
        try {
            return !c.isClosed() && c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /** Devuelve un "doble" de la conexión: close() la manda a la reserva en lugar de cerrarla. */
    private static Connection envolver(Connection real) {
        AtomicBoolean cerrada = new AtomicBoolean(false);

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class},
                (proxy, metodo, args) -> {
                    String nombre = metodo.getName();
                    if ("close".equals(nombre)) {
                        if (cerrada.compareAndSet(false, true)) {
                            devolver(real);
                        }
                        return null;
                    }
                    if ("isClosed".equals(nombre)) {
                        return cerrada.get() || real.isClosed();
                    }
                    try {
                        return metodo.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    /** Deja la conexión limpia (sin transacción pendiente) y la guarda para la próxima consulta. */
    private static void devolver(Connection real) {
        try {
            if (real.isClosed()) {
                return;
            }
            if (!real.getAutoCommit()) {
                real.rollback();             // lo que no se confirmó, no queda a medias
                real.setAutoCommit(true);
            }
            if (libres.size() < MAX_LIBRES) {
                libres.offer(new Libre(real));
                return;
            }
        } catch (SQLException e) {
            // si algo falla, se descarta esta conexión
        }
        cerrarSilencioso(real);
    }

    private static void cerrarSilencioso(Connection c) {
        try {
            c.close();
        } catch (SQLException ignorada) {
            // nada más que hacer
        }
    }
}
