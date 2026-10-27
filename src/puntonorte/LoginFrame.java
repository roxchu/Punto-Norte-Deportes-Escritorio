package puntonorte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import puntonorte.servicio.ReglaNegocioException;
import puntonorte.servicio.UsuarioService;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoCorreo;
    private JPasswordField campoCodigo;
    private JButton botonIngresar;

    private final UsuarioService usuarioService = new UsuarioService();

    public LoginFrame() {

        setTitle("Punto Norte Deportes - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 520));
        setSize(920, 560);
        setLocationRelativeTo(null);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilos.FONDO);
        fondo.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setContentPane(fondo);

        JPanel caja = new JPanel(new BorderLayout());
        caja.setPreferredSize(new Dimension(850, 470));
        caja.setBackground(Estilos.BLANCO);
        caja.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xCF, 0xC8, 0xBD)));

        caja.add(crearMarca(), BorderLayout.WEST);
        caja.add(crearFormulario(), BorderLayout.CENTER);

        fondo.add(caja);
    }

    private JPanel crearMarca() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 470));
        panel.setBackground(Estilos.AZUL);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));

        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(JLabel.CENTER);
        java.net.URL recursoLogo = LoginFrame.class.getResource("/puntonorte/logoPuntoNorte.jpeg");
        if (recursoLogo != null) {
            Image original = new ImageIcon(recursoLogo).getImage();
            double escala = Math.min(210.0 / original.getWidth(null), 100.0 / original.getHeight(null));
            Image imagen = original.getScaledInstance(
                    Math.max(1, (int) (original.getWidth(null) * escala)),
                    Math.max(1, (int) (original.getHeight(null) * escala)), Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(imagen));
        } else {
            logo.setText("PUNTO NORTE DEPORTES");
            logo.setForeground(Estilos.AZUL);
            logo.setFont(Estilos.FUENTE_NEGRITA);
        }

        JPanel cajaLogo = new JPanel(new java.awt.BorderLayout());
        cajaLogo.setBackground(Estilos.FONDO);
        cajaLogo.setPreferredSize(new Dimension(250, 120));
        cajaLogo.setMaximumSize(new Dimension(250, 120));
        cajaLogo.add(logo, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);
        GridBagConstraints marca = new GridBagConstraints();
        marca.gridx = 0;
        marca.gridy = 0;
        marca.weightx = 1;
        marca.fill = GridBagConstraints.HORIZONTAL;
        marca.anchor = GridBagConstraints.NORTHWEST;
        marca.insets = new Insets(0, 0, 28, 0);
        contenido.add(cajaLogo, marca);

        JLabel titulo = new JLabel("Sistema de gestión");
        titulo.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16));
        titulo.setForeground(Estilos.CORAL);
        titulo.setHorizontalAlignment(JLabel.LEFT);
        titulo.setPreferredSize(new Dimension(250, 28));
        titulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel texto = new JLabel("<html>Ventas, productos, stock y administración.</html>");
        texto.setForeground(new java.awt.Color(0xD9, 0xE3, 0xEE));
        texto.setFont(Estilos.FUENTE);
        texto.setHorizontalAlignment(JLabel.LEFT);
        texto.setPreferredSize(new Dimension(250, 44));
        texto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel textoMarca = new JPanel(new java.awt.GridLayout(2, 1, 0, 8));
        textoMarca.setOpaque(false);
        textoMarca.setPreferredSize(new Dimension(250, 80));
        textoMarca.setMinimumSize(new Dimension(250, 80));
        textoMarca.setMaximumSize(new Dimension(250, 80));
        textoMarca.add(titulo);
        textoMarca.add(texto);

        marca.gridy = 1;
        marca.insets = new Insets(0, 0, 0, 0);
        contenido.add(textoMarca, marca);
        panel.add(contenido, BorderLayout.NORTH);

        JLabel pie = new JLabel("Punto Norte Deportes");
        pie.setForeground(Estilos.BLANCO);
        pie.setFont(Estilos.FUENTE_NEGRITA);
        pie.setHorizontalAlignment(JLabel.CENTER);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    private void iniciarSesion() {

        String correo = campoCorreo.getText();
        String codigo = new String(campoCodigo.getPassword());

        // La consulta a la base va en segundo plano: así la ventana no se congela mientras espera.
        botonIngresar.setEnabled(false);
        botonIngresar.setText("Ingresando...");
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                usuarioService.autenticar(correo, codigo);
                return null;
            }

            @Override
            protected void done() {
                setCursor(java.awt.Cursor.getDefaultCursor());
                botonIngresar.setEnabled(true);
                botonIngresar.setText("\u2192 Ingresar al sistema");
                try {
                    get();
                    new MainFrame().setVisible(true);
                    dispose();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    mostrarError(causa instanceof ReglaNegocioException
                            ? causa.getMessage() : "Error inesperado: " + causa);
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                    mostrarError("No se pudo abrir el sistema: " + ex);
                }
            }
        }.execute();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "No se pudo ingresar",
                JOptionPane.WARNING_MESSAGE
        );
        campoCodigo.setText("");
        campoCodigo.requestFocus();
    }

    private JPanel crearFormulario() {

        JPanel contenedor = new JPanel(new GridBagLayout());
        contenedor.setBackground(Estilos.BLANCO);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(420, 350));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel titulo = ComponentesUI.titulo("Iniciar sesión");
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descripcion = ComponentesUI.subtitulo(
                "Ingresá con el correo registrado y el código de acceso.");
        descripcion.setAlignmentX(LEFT_ALIGNMENT);

        campoCorreo = ComponentesUI.campo();
        campoCodigo = new JPasswordField();
        campoCodigo.setFont(Estilos.FUENTE);
        campoCodigo.setPreferredSize(new Dimension(180, 34));
        campoCodigo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campoCodigo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.AZUL_CLARO),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel correo = ComponentesUI.filaFormulario("Correo electrónico", campoCorreo);
        correo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JPanel codigo = ComponentesUI.filaFormulario("Código de acceso", campoCodigo);
        codigo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JButton ingresar = ComponentesUI.boton("→ Ingresar al sistema", Estilos.CORAL, Estilos.AZUL);
        botonIngresar = ingresar;

        JButton registro = ComponentesUI.boton("Solicitar una cuenta", Estilos.BLANCO, Estilos.AZUL);
        registro.setBorder(BorderFactory.createLineBorder(Estilos.AZUL_CLARO));

        ingresar.addActionListener(e -> iniciarSesion());

        // Enter en cualquiera de los dos campos = clic en "Ingresar"
        getRootPane().setDefaultButton(ingresar);

        registro.addActionListener(e -> {
            new RegistroFrame().setVisible(true);
            dispose();
        });

        form.add(titulo);
        form.add(Box.createVerticalStrut(6));
        form.add(descripcion);
        form.add(Box.createVerticalStrut(24));
        form.add(correo);
        form.add(Box.createVerticalStrut(12));
        form.add(codigo);
        form.add(Box.createVerticalStrut(18));
        form.add(ComponentesUI.filaBotonCentrada(ingresar, 300, 38));
        form.add(Box.createVerticalStrut(10));
        form.add(ComponentesUI.filaBotonCentrada(registro, 300, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 35, 20, 35);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        contenedor.add(form, gbc);

        return contenedor;
    }
}
