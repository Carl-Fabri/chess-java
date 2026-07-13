package presentacion.vistas;

import aplicacion.casosuso.RegistrarUsuario;
import dominio.modelos.Usuario;
import dominio.modelos.UsuarioBuilder;
import infraestructura.persistencia.RepositorioUsuarioMySQL;
import shared.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VistaCrearUsuario extends JFrame {

    private JTextField txtNombreUsuario;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JPasswordField txtPassword;
    private JCheckBox chkEsAdmin;

    public VistaCrearUsuario() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        topBar.setBackground(Color.WHITE);
        JLabel lblCerrar = new JLabel("X");
        lblCerrar.setFont(new Font("Roboto Light", Font.BOLD, 24));
        lblCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblCerrar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new VistaMenuPrincipal().mostrar();
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblCerrar.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblCerrar.setForeground(Color.BLACK);
            }
        });
        topBar.add(lblCerrar);
        mainPanel.add(topBar, gbc);

        JLabel lblTitulo = new JLabel("Crear Usuario");
        lblTitulo.setFont(new Font("Roboto Medium", Font.BOLD, 36));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(20, 20, 30, 20);
        mainPanel.add(lblTitulo, gbc);

        txtNombreUsuario = new JTextField();
        txtNombreUsuario.setFont(new Font("Roboto", Font.PLAIN, 18));
        txtNombreUsuario.setBorder(BorderFactory.createTitledBorder("Nombre de Usuario"));
        gbc.insets = new Insets(10, 20, 10, 20);
        mainPanel.add(txtNombreUsuario, gbc);

        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Roboto", Font.PLAIN, 18));
        txtNombre.setBorder(BorderFactory.createTitledBorder("Nombre"));
        mainPanel.add(txtNombre, gbc);

        txtApellido = new JTextField();
        txtApellido.setFont(new Font("Roboto", Font.PLAIN, 18));
        txtApellido.setBorder(BorderFactory.createTitledBorder("Apellido"));
        mainPanel.add(txtApellido, gbc);

        txtPassword = new JPasswordField("123456");
        txtPassword.setFont(new Font("Roboto", Font.PLAIN, 18));
        txtPassword.setBorder(BorderFactory.createTitledBorder("Password"));
        mainPanel.add(txtPassword, gbc);

        chkEsAdmin = new JCheckBox("Es Administrador");
        chkEsAdmin.setFont(new Font("Roboto", Font.PLAIN, 18));
        chkEsAdmin.setBackground(Color.WHITE);
        mainPanel.add(chkEsAdmin, gbc);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        pnlBotones.setBackground(Color.WHITE);
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("Roboto Medium", Font.BOLD, 18));
        btnGuardar.setBackground(Color.BLACK);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarUsuario());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Roboto Medium", Font.BOLD, 18));
        btnCancelar.setBackground(Color.GRAY);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> {
            new VistaMenuPrincipal().mostrar();
            dispose();
        });

        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnCancelar);
        gbc.insets = new Insets(30, 20, 20, 20);
        mainPanel.add(pnlBotones, gbc);

        JLabel lblLogo = new JLabel();
        lblLogo.setPreferredSize(new Dimension(200, 100));
        ImageUtil.escalarImg(lblLogo, "assets/images/img_logo.png");

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(lblLogo, BorderLayout.SOUTH);
    }

    private void guardarUsuario() {
        try {
            Usuario nuevoUsuario = new UsuarioBuilder()
                    .nombreUsuario(txtNombreUsuario.getText().trim())
                    .nombre(txtNombre.getText().trim())
                    .apellido(txtApellido.getText().trim())
                    .password(new String(txtPassword.getPassword()))
                    .esAdmin(chkEsAdmin.isSelected())
                    .build();

            RegistrarUsuario registrarUsuarioCaso = new RegistrarUsuario(new RepositorioUsuarioMySQL());
            registrarUsuarioCaso.ejecutar(nuevoUsuario);
            JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            new VistaMenuPrincipal().mostrar();
            dispose();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Campos incompletos", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al crear usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void mostrar() {
        this.setVisible(true);
    }
}
