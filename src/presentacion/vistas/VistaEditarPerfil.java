package presentacion.vistas;

import dominio.modelos.Usuario;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import shared.utils.ImageUtil;

public class VistaEditarPerfil extends JFrame {

    private final Usuario usuario;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JPasswordField txtPassword;
    private JLabel lblUsuario;

    public VistaEditarPerfil(Usuario usuario) {
        this.usuario = usuario;
        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        JLabel lblTitulo = new JLabel("Editar Perfil");
        lblTitulo.setFont(new Font("Roboto Medium", Font.BOLD, 36));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(20, 20, 30, 20);
        mainPanel.add(lblTitulo, gbc);

        JPanel pnlUsuario = new JPanel(new BorderLayout());
        pnlUsuario.setBackground(Color.WHITE);
        JLabel lblUsuarioTitle = new JLabel("Usuario:");
        lblUsuarioTitle.setFont(new Font("Roboto", Font.PLAIN, 18));
        lblUsuario = new JLabel();
        lblUsuario.setFont(new Font("Roboto", Font.BOLD, 18));
        pnlUsuario.add(lblUsuarioTitle, BorderLayout.WEST);
        pnlUsuario.add(lblUsuario, BorderLayout.CENTER);
        gbc.insets = new Insets(10, 20, 10, 20);
        mainPanel.add(pnlUsuario, gbc);

        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Roboto", Font.PLAIN, 18));
        txtNombre.setBorder(BorderFactory.createTitledBorder("Nombre"));
        mainPanel.add(txtNombre, gbc);
        txtApellido = new JTextField();
        txtApellido.setFont(new Font("Roboto", Font.PLAIN, 18));
        txtApellido.setBorder(BorderFactory.createTitledBorder("Apellido"));
        mainPanel.add(txtApellido, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Roboto", Font.PLAIN, 18));
        txtPassword.setBorder(BorderFactory.createTitledBorder("Password"));
        mainPanel.add(txtPassword, gbc);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        pnlBotones.setBackground(Color.WHITE);
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("Roboto Medium", Font.BOLD, 18));
        btnGuardar.setBackground(Color.BLACK);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarCambios());

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

    private void cargarDatos() {
        lblUsuario.setText(usuario.getNombreUsuario());
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtPassword.setText(usuario.getPassword());
    }

    private void guardarCambios() {
        usuario.setNombre(txtNombre.getText().trim());
        usuario.setApellido(txtApellido.getText().trim());
        usuario.setPassword(new String(txtPassword.getPassword()));
        JOptionPane.showMessageDialog(this, "Perfil actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        new VistaMenuPrincipal().mostrar();
        dispose();
    }

    public void mostrar() {
        this.setVisible(true);
    }
}
