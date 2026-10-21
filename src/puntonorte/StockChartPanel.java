package puntonorte;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class StockChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String[] categorias = {
            "Ropa",
            "Calzado",
            "Accesorios",
            "Equipo"
    };

    private final int[] valores = {
            75,
            19,
            81,
            28
    };

    public StockChartPanel() {

        setBackground(Estilos.BEIGE);
        setPreferredSize(new Dimension(520, 260));
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int ancho = getWidth();
        int alto = getHeight();

        int izquierda = 48;
        int derecha = 20;
        int arriba = 28;
        int abajo = 45;

        int anchoGrafico = ancho - izquierda - derecha;
        int altoGrafico = alto - arriba - abajo;

        int maximo = 100;

        g2.setFont(new Font("Tahoma", Font.PLAIN, 10));

        for (int i = 0; i <= 4; i++) {

            int valor = maximo * i / 4;
            int y = arriba + altoGrafico - (altoGrafico * i / 4);

            g2.setColor(new Color(0xB9, 0xC6, 0xD2));
            g2.drawLine(izquierda, y, ancho - derecha, y);

            g2.setColor(Estilos.TEXTO_SECUNDARIO);
            g2.drawString(String.valueOf(valor), 15, y + 4);
        }

        int paso = anchoGrafico / categorias.length;
        int anchoBarra = Math.min(65, paso / 2);

        for (int i = 0; i < categorias.length; i++) {

            int valor = valores[i];
            int altoBarra = (int) ((valor / (double) maximo) * altoGrafico);

            int x = izquierda + (paso * i) + (paso - anchoBarra) / 2;
            int y = arriba + altoGrafico - altoBarra;

            g2.setColor(Estilos.AZUL);
            g2.fillRect(x, y, anchoBarra, altoBarra);

            g2.setColor(Estilos.CORAL);
            g2.fillRect(x, y, anchoBarra, 4);

            g2.setColor(Estilos.TEXTO);
            g2.setFont(new Font("Tahoma", Font.BOLD, 11));

            String textoValor = String.valueOf(valor);
            int valorX = x + (anchoBarra - g2.getFontMetrics().stringWidth(textoValor)) / 2;

            g2.drawString(textoValor, valorX, y - 7);

            g2.setFont(new Font("Tahoma", Font.PLAIN, 10));
            g2.setColor(Estilos.TEXTO_SECUNDARIO);

            String categoria = categorias[i];
            int textoX = x + (anchoBarra - g2.getFontMetrics().stringWidth(categoria)) / 2;

            g2.drawString(categoria, textoX, arriba + altoGrafico + 22);
        }

        g2.dispose();
    }
}