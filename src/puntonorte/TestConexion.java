package puntonorte;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import puntonorte.conexion.ConexionBD;

/**
 * Prueba aislada de la base de datos (no abre ninguna pantalla).
 * Clic derecho -> Run As -> Java Application, y mirar la consola de Eclipse.
 */
public class TestConexion {

    public static void main(String[] args) {
        System.out.println("=== Prueba de conexión ===");

        for (int i = 1; i <= 4; i++) {
            long inicio = System.currentTimeMillis();
            try (Connection con = ConexionBD.conectar()) {
                System.out.println("Conexión " + i + ": " + (System.currentTimeMillis() - inicio) + " ms");
            } catch (Exception e) {
                System.out.println("Conexión " + i + " FALLÓ tras " + (System.currentTimeMillis() - inicio) + " ms");
                e.printStackTrace(System.out);
                return;
            }
        }

        long inicio = System.currentTimeMillis();
        try (Connection con = ConexionBD.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            rs.next();
            System.out.println("Consulta a 'usuarios': " + rs.getInt(1) + " filas, "
                    + (System.currentTimeMillis() - inicio) + " ms");
        } catch (Exception e) {
            System.out.println("La consulta FALLÓ:");
            e.printStackTrace(System.out);
        }
        System.out.println("=== Fin ===");
    }
}