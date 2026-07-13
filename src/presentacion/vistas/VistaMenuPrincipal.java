package presentacion.vistas;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import aplicacion.Sesion;
import dominio.modelos.Usuario;
import infraestructura.persistencia.ServicioPartidaOnline;
import infraestructura.persistencia.RepositorioUsuarioMySQL;
import shared.utils.ImageUtil;

public class VistaMenuPrincipal extends JFrame {

    private JPanel jPanel_fondo;
    private JLabel jLabel_logo;
    private JLabel jLabel_titulo;

    private Timer timerActividad;

    public VistaMenuPrincipal() {
        initComponents();
        
        String rutaLogo = "assets/images/img_logo.png";
        jLabel_logo.setSize(200, 100); 
        ImageUtil.escalarImg(jLabel_logo, rutaLogo);
        
        iniciarTimerActividad();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        jPanel_fondo = new JPanel();
        jPanel_fondo.setBackground(Color.WHITE);
        jPanel_fondo.setLayout(new BorderLayout());

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlTop.setBackground(Color.WHITE);
        
        JLabel lblCerrar = new JLabel("X");
        lblCerrar.setFont(new Font("Roboto Light", Font.BOLD, 24));
        lblCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblCerrar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
            public void mouseEntered(MouseEvent e) { lblCerrar.setForeground(Color.RED); }
            public void mouseExited(MouseEvent e) { lblCerrar.setForeground(Color.BLACK); }
        });
        pnlTop.add(lblCerrar);
        jPanel_fondo.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel();
        pnlCentro.setBackground(Color.WHITE);
        pnlCentro.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        jLabel_logo = new JLabel();
        jLabel_logo.setPreferredSize(new Dimension(200, 100));
        jLabel_logo.setHorizontalAlignment(SwingConstants.CENTER);
        pnlCentro.add(jLabel_logo, gbc);

        jLabel_titulo = new JLabel("MENÚ PRINCIPAL");
        jLabel_titulo.setFont(new Font("Roboto Medium", Font.BOLD, 36));
        jLabel_titulo.setHorizontalAlignment(SwingConstants.CENTER);
        pnlCentro.add(jLabel_titulo, gbc);

        gbc.insets = new Insets(30, 0, 15, 0);

        pnlCentro.add(crearBoton("JUGAR 1vs1", true, e -> {
            new VistaJuego().mostrar();
            dispose();
        }), gbc);
        
        gbc.insets = new Insets(15, 0, 15, 0);
        pnlCentro.add(crearBoton("JUGAR ONLINE", false, e -> {
            if (timerActividad != null) timerActividad.stop();
            new DialogJugadoresActivos(this, (gameId, oponente, esCreador) -> {
                new VistaJuego(gameId, oponente, esCreador).mostrar();
                dispose();
            }).setVisible(true);
            if (timerActividad != null && this.isVisible()) {
                timerActividad.start();
            }
        }), gbc);
        
        pnlCentro.add(crearBoton("MI PERFIL", false, e -> {
            new VistaEditarPerfil(Sesion.usuario()).mostrar();
            dispose();
        }), gbc);
        pnlCentro.add(crearBoton("RANKING", false, e -> {
            new VistaRanking().mostrar();
            dispose();
        }), gbc);

        if (Sesion.usuario().getEsAdmin()) {
            pnlCentro.add(crearBoton("CREAR USUARIO", false, e -> {
                new VistaCrearUsuario().mostrar();
                dispose();
            }), gbc);
        }
        pnlCentro.add(crearBoton("CERRAR SESIÓN", false, e -> {
            new VistaLogin().mostrar();
            dispose();
        }), gbc);

        jPanel_fondo.add(pnlCentro, BorderLayout.CENTER);

        this.setContentPane(jPanel_fondo);
    }

    private JPanel crearBoton(String texto, boolean esPrincipal, ActionListener accion) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 60));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (esPrincipal) {
            panel.setBackground(Color.BLACK);
        } else {
            panel.setBackground(Color.WHITE);
            panel.setBorder(new LineBorder(Color.BLACK, 2));
        }

        JLabel label = new JLabel(texto);
        label.setFont(new Font("Roboto", Font.BOLD, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (esPrincipal) {
            label.setForeground(Color.WHITE);
        } else {
            label.setForeground(Color.BLACK);
        }

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (accion != null) accion.actionPerformed(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (esPrincipal) {
                    panel.setBackground(new Color(50, 50, 50));
                } else {
                    panel.setBackground(Color.BLACK);
                    label.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (esPrincipal) {
                    panel.setBackground(Color.BLACK);
                } else {
                    panel.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                }
            }
        });

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void iniciarTimerActividad() {
        if (Sesion.usuario() == null) return;
        
        ServicioPartidaOnline.actualizarAcceso(Sesion.usuario().getNombreUsuario());
        
        timerActividad = new Timer(3000, e -> {
            if (Sesion.usuario() == null) return;
            ServicioPartidaOnline.actualizarAcceso(Sesion.usuario().getNombreUsuario());
            
            java.util.List<String[]> invs = ServicioPartidaOnline.buscarInvitacionesRecibidas(Sesion.usuario().getNombreUsuario());
            if (!invs.isEmpty()) {
                timerActividad.stop();
                String[] inv = invs.get(0);
                String idPartida = inv[0];
                String retador = inv[1];
                
                int rpta = JOptionPane.showConfirmDialog(
                    this,
                    "El jugador '" + retador + "' te invita a jugar online. ¿Aceptas?",
                    "Invitación Online Recibida",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (rpta == JOptionPane.YES_OPTION) {
                    ServicioPartidaOnline.responderInvitacion(idPartida, true);
                    Usuario retadorUsuario = new RepositorioUsuarioMySQL().buscarPorNombreUsuario(retador);
                    new VistaJuego(idPartida, retadorUsuario, false).mostrar();
                    dispose();
                } else {
                    ServicioPartidaOnline.responderInvitacion(idPartida, false);
                    timerActividad.start();
                }
            }
        });
        timerActividad.start();
    }

    @Override
    public void dispose() {
        if (timerActividad != null) {
            timerActividad.stop();
        }
        super.dispose();
    }

    public void mostrar() {
        this.setVisible(true);
    }
    
    public void cerrar() {
        this.dispose();
    }
}
