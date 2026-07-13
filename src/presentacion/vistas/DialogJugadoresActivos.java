package presentacion.vistas;

import aplicacion.Sesion;
import dominio.modelos.Usuario;
import dominio.puertos.RepositorioUsuario;
import infraestructura.persistencia.RepositorioUsuarioMySQL;
import infraestructura.persistencia.ServicioPartidaOnline;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class DialogJugadoresActivos extends JDialog {

    public interface MatchCallback {
        void onMatchStarted(String gameId, Usuario oponente, boolean esCreador);
    }

    private MatchCallback callback;
    private JTable tblJugadores;
    private DefaultTableModel modelTable;
    private JButton btnInvitar;
    private JButton btnCerrar;
    private JLabel lblStatus;
    private List<Usuario> jugadoresActivos = new ArrayList<>();
    private Timer timerRefrescarJugadores;
    private Timer timerChequearRespuesta;
    
    private String gameIdActual;
    private Usuario oponenteActual;

    public DialogJugadoresActivos(Frame parent, MatchCallback callback) {
        super(parent, "Jugadores Activos", true);
        this.callback = callback;
        
        initComponents();
        refrescarListaJugadores();
        
        // Timer to refresh active players every 3 seconds
        timerRefrescarJugadores = new Timer(3000, e -> refrescarListaJugadores());
        timerRefrescarJugadores.start();
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelarTodo();
            }
        });
        
        this.setSize(500, 400);
        this.setLocationRelativeTo(parent);
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.getContentPane().setBackground(Color.WHITE);

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setBackground(Color.WHITE);
        pnlNorth.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblTitulo = new JLabel("SELECCIONA UN JUGADOR PARA INVITAR");
        lblTitulo.setFont(new Font("Roboto Medium", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        pnlNorth.add(lblTitulo, BorderLayout.CENTER);
        this.add(pnlNorth, BorderLayout.NORTH);

        modelTable = new DefaultTableModel(new String[]{"Usuario", "Nombre Completo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblJugadores = new JTable(modelTable);
        tblJugadores.setFont(new Font("Roboto", Font.PLAIN, 14));
        tblJugadores.setRowHeight(25);
        tblJugadores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollTable = new JScrollPane(tblJugadores);
        scrollTable.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollTable.getViewport().setBackground(Color.WHITE);
        
        JPanel pnlCenter = new JPanel(new BorderLayout(5, 5));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        pnlCenter.add(scrollTable, BorderLayout.CENTER);
        
        lblStatus = new JLabel("Buscando jugadores activos...");
        lblStatus.setFont(new Font("Roboto", Font.ITALIC, 13));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        pnlCenter.add(lblStatus, BorderLayout.SOUTH);
        
        this.add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlSouth.setBackground(Color.WHITE);
        
        btnInvitar = new JButton("Invitar a Jugar");
        btnInvitar.setFont(new Font("Roboto Medium", Font.BOLD, 14));
        btnInvitar.setBackground(Color.BLACK);
        btnInvitar.setForeground(Color.WHITE);
        btnInvitar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnInvitar.addActionListener(e -> invitarSeleccionado());

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Roboto Medium", Font.BOLD, 14));
        btnCerrar.setBackground(Color.GRAY);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> {
            cancelarTodo();
            this.dispose();
        });

        pnlSouth.add(btnInvitar);
        pnlSouth.add(btnCerrar);
        this.add(pnlSouth, BorderLayout.SOUTH);
    }

    private void refrescarListaJugadores() {
        if (gameIdActual != null) {
            // If waiting for invitation response, don't update list
            return;
        }
        
        String miUsuario = Sesion.usuario().getNombreUsuario();
        List<Usuario> activos = ServicioPartidaOnline.obtenerUsuariosActivos(miUsuario);
        
        jugadoresActivos.clear();
        jugadoresActivos.addAll(activos);
        
        modelTable.setRowCount(0);
        for (Usuario u : activos) {
            modelTable.addRow(new Object[]{u.getNombreUsuario(), u.getNombre() + " " + u.getApellido()});
        }
        
        if (activos.isEmpty()) {
            lblStatus.setText("No hay otros jugadores activos en este momento.");
            btnInvitar.setEnabled(false);
        } else {
            lblStatus.setText("Jugadores disponibles: " + activos.size());
            btnInvitar.setEnabled(true);
        }
    }

    private void invitarSeleccionado() {
        int selectedRow = tblJugadores.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un jugador de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        oponenteActual = jugadoresActivos.get(selectedRow);
        gameIdActual = "GAME-" + System.currentTimeMillis();
        
        // Disable controls
        tblJugadores.setEnabled(false);
        btnInvitar.setEnabled(false);
        lblStatus.setText("Enviando invitación a " + oponenteActual.getNombreUsuario() + "...");
        
        // Create invitation in database
        ServicioPartidaOnline.crearInvitacion(gameIdActual, Sesion.usuario().getNombreUsuario(), oponenteActual.getNombreUsuario());
        
        // Start polling for acceptance
        timerChequearRespuesta = new Timer(1500, e -> chequearRespuesta());
        timerChequearRespuesta.start();
        
        btnCerrar.setText("Cancelar Invitación");
    }

    private void chequearRespuesta() {
        if (gameIdActual == null) return;
        
        ServicioPartidaOnline.PartidaOnlineInfo partida = ServicioPartidaOnline.obtenerPartida(gameIdActual);
        if (partida == null) return;
        
        if ("EN_PROGRESO".equals(partida.estado)) {
            // Aceptada!
            timerChequearRespuesta.stop();
            timerRefrescarJugadores.stop();
            this.dispose();
            if (callback != null) {
                callback.onMatchStarted(gameIdActual, oponenteActual, true);
            }
        } else if ("RECHAZADA".equals(partida.estado)) {
            // Rechazada!
            timerChequearRespuesta.stop();
            limpiarInvitacionActual();
            JOptionPane.showMessageDialog(this, oponenteActual.getNombreUsuario() + " rechazó tu invitación.", "Invitación Rechazada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarInvitacionActual() {
        gameIdActual = null;
        oponenteActual = null;
        tblJugadores.setEnabled(true);
        btnInvitar.setEnabled(true);
        btnCerrar.setText("Cerrar");
        refrescarListaJugadores();
    }

    private void cancelarTodo() {
        if (timerRefrescarJugadores != null) {
            timerRefrescarJugadores.stop();
        }
        if (timerChequearRespuesta != null) {
            timerChequearRespuesta.stop();
        }
        if (gameIdActual != null) {
            ServicioPartidaOnline.responderInvitacion(gameIdActual, false); // Mark as rejected/cancel
        }
    }
}
