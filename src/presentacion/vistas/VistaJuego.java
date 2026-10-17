package presentacion.vistas;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import shared.utils.ImageUtil;
import dominio.modelos.piezas.Pieza;
import dominio.modelos.Movimiento;
import dominio.modelos.Posicion;
import dominio.modelos.Tablero;
import dominio.modelos.Usuario;
import dominio.puertos.IGestionarJuego;
import aplicacion.Sesion;
import aplicacion.casosuso.GestionarJuego;
import dominio.puertos.RepositorioUsuario;
import infraestructura.persistencia.RepositorioUsuarioMySQL;
import infraestructura.persistencia.ServicioPartidaOnline;

public class VistaJuego extends JFrame {

    private JPanel pnlTablero;
    private JPanel pnlInfo;
    private JPanel[][] casillas;
    private JPanel pnlJugador1;
    private JPanel pnlJugador2;
    
    private IGestionarJuego gestionarJuego;
    private Posicion seleccionOrigen;
    private javax.swing.table.DefaultTableModel modeloTablaMovimientos;
    private JTable tablaMovimientos;
    private JButton btnRetrocederBlancas;
    private JButton btnRetrocederNegras;

    private boolean esOnline;
    private String idPartidaOnline;
    private boolean esCreador;
    private Timer timerSincronizacion;

    private final Color colorClaro = new Color(240, 217, 181);
    private final Color colorOscuro = new Color(181, 136, 99);

    public VistaJuego() {
        this.esOnline = false;
        Usuario u1 = Sesion.usuario();
        Usuario u2 = new Usuario("jugador2", "Jugador", "Dos", "pass2");
        gestionarJuego = new GestionarJuego();
        gestionarJuego.crearPartida(u1, u2);
        
        initComponents();
        inicializarTablero();
        actualizarTablero();
        actualizarTurno();
        actualizarNombresJugadores();
    }

    public VistaJuego(String idPartida, Usuario oponente, boolean esCreador) {
        this.esOnline = true;
        this.idPartidaOnline = idPartida;
        this.esCreador = esCreador;
        
        Usuario u1 = esCreador ? Sesion.usuario() : oponente;
        Usuario u2 = esCreador ? oponente : Sesion.usuario();
        
        gestionarJuego = new GestionarJuego();
        gestionarJuego.crearPartida(u1, u2);
        
        initComponents();
        inicializarTablero();
        actualizarTablero();
        actualizarTurno();
        actualizarNombresJugadores();
        
        iniciarSincronizacionOnline();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        pnlTablero = new JPanel(new GridLayout(8, 8));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.66; 
        int padding = 50;
        gbc.insets = new Insets(padding, padding, padding, padding);
        mainPanel.add(pnlTablero, gbc);
        gbc.insets = new Insets(0, 0, 0, 0);

        pnlInfo = new JPanel(new GridLayout(2, 1));
        pnlInfo.setBackground(Color.WHITE);
        
        pnlJugador2 = crearPanelJugador("Jugador 2 (Negras)", "assets/images/user.png");
        pnlInfo.add(pnlJugador2);
        
        pnlJugador1 = crearPanelJugador("Jugador 1 (Blancas)", "assets/images/user.png");
        pnlInfo.add(pnlJugador1);

        btnRetrocederNegras = crearBotonRetroceso(dominio.enums.Color.NEGRO);
        pnlJugador2.add(btnRetrocederNegras);
        btnRetrocederBlancas = crearBotonRetroceso(dominio.enums.Color.BLANCO);
        pnlJugador1.add(btnRetrocederBlancas);

        gbc.gridx = 1;
        gbc.weightx = 0.33;
        mainPanel.add(pnlInfo, gbc);

        JButton btnSalir = new JButton("Volver al Menú");
        btnSalir.addActionListener(e -> {
            new VistaMenuPrincipal().mostrar();
            dispose();
        });
        JPanel pnlBoton = new JPanel();
        pnlBoton.add(btnSalir);
        pnlInfo.add(pnlBoton, 0);
        pnlInfo.setLayout(new BorderLayout());
        pnlInfo.removeAll();
        
        JPanel pnlTopRight = new JPanel(new BorderLayout());
        pnlTopRight.add(pnlJugador2, BorderLayout.CENTER);
        
        JLabel lblCerrar = new JLabel("X  ");
        lblCerrar.setFont(new Font("Arial", Font.BOLD, 20));
        lblCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblCerrar.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCerrar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new VistaMenuPrincipal().mostrar();
                dispose();
            }
        });
        pnlTopRight.add(lblCerrar, BorderLayout.NORTH);

        pnlInfo.add(pnlTopRight, BorderLayout.NORTH);
        pnlInfo.add(pnlJugador1, BorderLayout.SOUTH);
        
        JPanel pnlExtra = new JPanel(new BorderLayout());
        pnlExtra.setBackground(Color.WHITE);
        pnlExtra.setBorder(BorderFactory.createTitledBorder("Historial de Movimientos"));
        
        modeloTablaMovimientos = new javax.swing.table.DefaultTableModel(new String[]{"Nº", "Jugador", "Pieza", "Movimiento"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaMovimientos = new JTable(modeloTablaMovimientos);
        tablaMovimientos.setFont(new Font("Roboto", Font.PLAIN, 12));
        tablaMovimientos.setRowHeight(20);
        tablaMovimientos.getColumnModel().getColumn(0).setPreferredWidth(35);
        tablaMovimientos.getColumnModel().getColumn(1).setPreferredWidth(90);
        tablaMovimientos.getColumnModel().getColumn(2).setPreferredWidth(90);
        tablaMovimientos.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        JScrollPane scrollMovimientos = new JScrollPane(tablaMovimientos);
        scrollMovimientos.getViewport().setBackground(Color.WHITE);
        pnlExtra.add(scrollMovimientos, BorderLayout.CENTER);
        pnlInfo.add(pnlExtra, BorderLayout.CENTER);


        mainPanel.add(pnlInfo, gbc);
        this.setContentPane(mainPanel);
    }

    private JPanel crearPanelJugador(String nombre, String imgPath) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(nombre));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(0, 150));
        
        JLabel lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(50, 50));
        lblAvatar.setBorder(new LineBorder(Color.BLACK));
        lblAvatar.setText("IMG");
        
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Roboto", Font.BOLD, 16));
        
        panel.add(lblAvatar);
        panel.add(lblNombre);
        
        return panel;
    }

    private void inicializarTablero() {
        casillas = new JPanel[8][8];
        pnlTablero.removeAll();

        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                JPanel casilla = new JPanel(new BorderLayout());
                if ((fila + col) % 2 == 0) {
                    casilla.setBackground(colorClaro);
                } else {
                    casilla.setBackground(colorOscuro);
                }
                
                final int r = fila;
                final int c = col;
                casilla.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        manejarClick(new Posicion(r, c));
                    }
                });
                
                casillas[fila][col] = casilla;
                pnlTablero.add(casilla);
            }
        }
    }

    private void actualizarTablero() {
        Tablero tablero = gestionarJuego.getTablero();
        
        java.util.List<Posicion> movimientosValidos = null;
        if (seleccionOrigen != null) {
            Pieza piezaSeleccionada = tablero.getPieza(seleccionOrigen);
            if (piezaSeleccionada != null) {
                movimientosValidos = piezaSeleccionada.getMovimientosPosibles(tablero);
            }
        }
        
        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                JPanel casilla = casillas[fila][col];
                casilla.removeAll();
                
                if ((fila + col) % 2 == 0) {
                    casilla.setBackground(colorClaro);
                } else {
                    casilla.setBackground(colorOscuro);
                }
                
                Posicion posActual = new Posicion(fila, col);
                if (seleccionOrigen != null) {
                    if (posActual.equals(seleccionOrigen)) {
                        casilla.setBackground(new Color(186, 202, 68)); 
                    } else {
                        if (movimientosValidos != null && movimientosValidos.contains(posActual)) {
                             casilla.setBackground(new Color(214, 214, 189)); 
                        }
                    }
                }
                
                Pieza pieza = tablero.getPieza(posActual);
                if (pieza != null) {
                    String tipo = pieza.getTipo().toString().toLowerCase();
                    String color = (pieza.getColor() == dominio.enums.Color.BLANCO) ? "blanco" : "negro";
                    colocarPieza(casilla, tipo, color);
                }
                
                casilla.revalidate();
                casilla.repaint();
            }
        }
        actualizarHistorialMovimientos();
        actualizarRetrocesos();
    }

    private void manejarClick(Posicion pos) {
        Tablero tablero = gestionarJuego.getTablero();
        Pieza piezaClickeada = tablero.getPieza(pos);
        
        if (esOnline) {
            dominio.enums.Color miColor = esCreador ? dominio.enums.Color.BLANCO : dominio.enums.Color.NEGRO;
            if (gestionarJuego.getTurnoActual() != miColor) {
                return;
            }
        }
        
        if (seleccionOrigen == null) {
            if (piezaClickeada != null && piezaClickeada.getColor() == gestionarJuego.getTurnoActual()) {
                seleccionOrigen = pos;
                actualizarTablero();
            }
        } else {
            if (pos.equals(seleccionOrigen)) {
                seleccionOrigen = null;
                actualizarTablero();
            } else {
                if (gestionarJuego.realizarMovimiento(seleccionOrigen, pos)) {
                    if (esOnline) {
                        sincronizarMovimientosOnline();
                    }
                    
                    seleccionOrigen = null;
                    actualizarTablero();
                    actualizarTurno();
                    verificarEstadoJuego();
                } else {
                    if (piezaClickeada != null && piezaClickeada.getColor() == gestionarJuego.getTurnoActual()) {
                        seleccionOrigen = pos;
                        actualizarTablero();
                    }
                }
            }
        }
    }

    private void sincronizarMovimientosOnline() {
        String movs = String.join(",", gestionarJuego.getPartidaActual().getMovimientos());
        ServicioPartidaOnline.realizarMovimientoOnline(idPartidaOnline, movs, gestionarJuego.getTurnoActual().toString());
    }

    private JButton crearBotonRetroceso(dominio.enums.Color color) {
        JButton boton = new JButton();
        boton.addActionListener(e -> retrocederMovimiento(color));
        boton.setVisible(false);
        return boton;
    }

    private void retrocederMovimiento(dominio.enums.Color color) {
        if (!gestionarJuego.retrocederMovimiento(color)) return;

        if (esOnline) {
            sincronizarMovimientosOnline();
        }
        seleccionOrigen = null;
        actualizarTablero();
        actualizarTurno();
    }

    /**
     * El botón solo aparece en el panel del jugador que acaba de mover (y en online, solo si es el jugador local).
     */
    private void actualizarRetrocesos() {
        if (btnRetrocederBlancas == null) return;
        actualizarBotonRetroceso(btnRetrocederBlancas, dominio.enums.Color.BLANCO);
        actualizarBotonRetroceso(btnRetrocederNegras, dominio.enums.Color.NEGRO);
    }

    private void actualizarBotonRetroceso(JButton boton, dominio.enums.Color color) {
        boolean esJugadorLocal = !esOnline || color == (esCreador ? dominio.enums.Color.BLANCO : dominio.enums.Color.NEGRO);
        boton.setText("Regresar movimiento (" + gestionarJuego.getRetrocesosRestantes(color) + ")");
        boton.setVisible(esJugadorLocal && gestionarJuego.puedeRetroceder(color));
    }

    private void colocarPieza(JPanel casilla, String tipo, String color) {
        String ruta = "assets/piezas/" + tipo + "_" + color + ".png";
        JLabel lblPieza = new JLabel();
        lblPieza.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblPieza.setSize(80, 80);
        ImageUtil.escalarImgMantenerProporcion(lblPieza, ruta);
        
        casilla.add(lblPieza, BorderLayout.CENTER);
    }
    
    public void mostrar() {
        this.setVisible(true);
    }

    private void actualizarTurno() {
        dominio.enums.Color turno = gestionarJuego.getTurnoActual();
        if (turno == dominio.enums.Color.BLANCO) {
            pnlJugador1.setBackground(new Color(220, 255, 220));
            pnlJugador2.setBackground(Color.WHITE);
        } else {
            pnlJugador1.setBackground(Color.WHITE);
            pnlJugador2.setBackground(new Color(220, 255, 220));
        }
    }

    private void verificarEstadoJuego() {
        dominio.enums.Color turnoActual = gestionarJuego.getTurnoActual();
        if (gestionarJuego.estaEnJaque(turnoActual)) {
            if (gestionarJuego.estaEnJaqueMate(turnoActual)) {
                JOptionPane.showMessageDialog(this, 
                    "¡JAQUE MATE! Ganaron las " + (turnoActual == dominio.enums.Color.BLANCO ? "Negras" : "Blancas"), 
                    "Fin del Juego", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (timerSincronizacion != null) {
                    timerSincronizacion.stop();
                }
                
                Usuario ganador = (turnoActual == dominio.enums.Color.BLANCO) ? 
                    gestionarJuego.getPartidaActual().getJugadorNegras() : 
                    gestionarJuego.getPartidaActual().getJugadorBlancas();
                
                gestionarJuego.getPartidaActual().finalizarPartida(ganador);
                
                RepositorioUsuario repo = new RepositorioUsuarioMySQL();
                repo.actualizar(gestionarJuego.getPartidaActual().getJugadorBlancas());
                repo.actualizar(gestionarJuego.getPartidaActual().getJugadorNegras());
                
                if (esOnline) {
                    String estadoFinal = (turnoActual == dominio.enums.Color.BLANCO) ? "FINALIZADA_NEGRAS" : "FINALIZADA_BLANCAS";
                    ServicioPartidaOnline.finalizarPartidaOnline(idPartidaOnline, estadoFinal);
                }
                
                new VistaMenuPrincipal().mostrar();
                dispose();
            } else {
                System.out.println("Estas en jaque");
                JOptionPane.showMessageDialog(this, 
                    "¡ESTÁS EN JAQUE!", 
                    "Alerta", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void iniciarSincronizacionOnline() {
        timerSincronizacion = new Timer(1000, e -> {
            if (!esOnline) return;
            
            ServicioPartidaOnline.actualizarAcceso(Sesion.usuario().getNombreUsuario());
            
            ServicioPartidaOnline.PartidaOnlineInfo partida = ServicioPartidaOnline.obtenerPartida(idPartidaOnline);
            if (partida == null) return;
            
            if ("ABANDONADA".equals(partida.estado)) {
                timerSincronizacion.stop();
                JOptionPane.showMessageDialog(this, "El oponente abandonó la partida.", "Partida Terminada", JOptionPane.INFORMATION_MESSAGE);
                new VistaMenuPrincipal().mostrar();
                dispose();
                return;
            }
            
            dominio.enums.Color colorLocal = esCreador ? dominio.enums.Color.BLANCO : dominio.enums.Color.NEGRO;
            dominio.enums.Color colorOponente = esCreador ? dominio.enums.Color.NEGRO : dominio.enums.Color.BLANCO;

            String movsDB = partida.movimientos;
            int movesDBSize = (movsDB == null || movsDB.trim().isEmpty()) ? 0 : movsDB.split(",").length;

            // El oponente regresó su último movimiento
            if (movesDBSize < gestionarJuego.getPartidaActual().getHistorial().getTamanio()) {
                if (gestionarJuego.retrocederMovimiento(colorOponente)) {
                    seleccionOrigen = null;
                    actualizarTablero();
                    actualizarTurno();
                }
                return;
            }

            if (gestionarJuego.getTurnoActual() != colorLocal) {
                if (movesDBSize > 0) {
                    String[] moves = movsDB.split(",");
                    int localMovesSize = gestionarJuego.getPartidaActual().getHistorial().getTamanio();

                    if (moves.length > localMovesSize) {
                        for (int i = localMovesSize; i < moves.length; i++) {
                            String nextMove = moves[i];
                            if (nextMove.contains("-")) {
                                String[] parts = nextMove.split("-");
                                Posicion origen = new Posicion(parts[0]);
                                Posicion destino = new Posicion(parts[1]);
                                
                                if (gestionarJuego.realizarMovimiento(origen, destino)) {
                                    actualizarTablero();
                                    actualizarTurno();
                                    verificarEstadoJuegoSilencioso();
                                }
                            }
                        }
                    }
                }
            }
        });
        timerSincronizacion.start();
    }

    private void verificarEstadoJuegoSilencioso() {
        dominio.enums.Color turnoActual = gestionarJuego.getTurnoActual();
        if (gestionarJuego.estaEnJaque(turnoActual)) {
            if (gestionarJuego.estaEnJaqueMate(turnoActual)) {
                JOptionPane.showMessageDialog(this, 
                    "¡JAQUE MATE! Ganaron las " + (turnoActual == dominio.enums.Color.BLANCO ? "Negras" : "Blancas"), 
                    "Fin del Juego", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (timerSincronizacion != null) {
                    timerSincronizacion.stop();
                }
                
                Usuario ganador = (turnoActual == dominio.enums.Color.BLANCO) ? 
                    gestionarJuego.getPartidaActual().getJugadorNegras() : 
                    gestionarJuego.getPartidaActual().getJugadorBlancas();
                
                gestionarJuego.getPartidaActual().finalizarPartida(ganador);
                
                RepositorioUsuario repo = new RepositorioUsuarioMySQL();
                repo.actualizar(gestionarJuego.getPartidaActual().getJugadorBlancas());
                repo.actualizar(gestionarJuego.getPartidaActual().getJugadorNegras());
                
                new VistaMenuPrincipal().mostrar();
                dispose();
            }
        }
    }

    @Override
    public void dispose() {
        if (timerSincronizacion != null) {
            timerSincronizacion.stop();
        }
        if (esOnline) {
            ServicioPartidaOnline.PartidaOnlineInfo partida = ServicioPartidaOnline.obtenerPartida(idPartidaOnline);
            if (partida != null && "EN_PROGRESO".equals(partida.estado)) {
                ServicioPartidaOnline.finalizarPartidaOnline(idPartidaOnline, "ABANDONADA");
            }
        }
        super.dispose();
    }

    private void actualizarNombresJugadores() {
        Usuario uBlancas = gestionarJuego.getPartidaActual().getJugadorBlancas();
        Usuario uNegras = gestionarJuego.getPartidaActual().getJugadorNegras();
        
        String nombreBlancas = uBlancas != null ? uBlancas.getNombreUsuario() : "Blancas";
        String nombreNegras = uNegras != null ? uNegras.getNombreUsuario() : "Negras";
        
        pnlJugador1.setBorder(BorderFactory.createTitledBorder(nombreBlancas + " (Blancas)"));
        pnlJugador2.setBorder(BorderFactory.createTitledBorder(nombreNegras + " (Negras)"));
        
        for (Component c : pnlJugador1.getComponents()) {
            if (c instanceof JLabel && !"IMG".equals(((JLabel) c).getText())) {
                ((JLabel) c).setText(nombreBlancas);
            }
        }
        for (Component c : pnlJugador2.getComponents()) {
            if (c instanceof JLabel && !"IMG".equals(((JLabel) c).getText())) {
                ((JLabel) c).setText(nombreNegras);
            }
        }
    }

    private String traducirTipoPieza(dominio.enums.TipoPieza tipo) {
        switch (tipo) {
            case PEON: return "Peón";
            case TORRE: return "Torre";
            case CABALLO: return "Caballo";
            case ALFIL: return "Alfil";
            case REINA: return "Reina";
            case REY: return "Rey";
            default: return tipo.toString();
        }
    }

    private void actualizarHistorialMovimientos() {
        if (modeloTablaMovimientos == null) return;
        modeloTablaMovimientos.setRowCount(0);

        // Una fila por nodo: el iterador del TDA recorre de la cabeza a la cola sin exponer los nodos
        int numMovimiento = 1;
        for (Movimiento movimiento : gestionarJuego.getPartidaActual().getHistorial()) {
            String jugador = movimiento.getColor() == dominio.enums.Color.BLANCO ? "Blancas" : "Negras";
            String pieza = traducirTipoPieza(movimiento.getPiezaMovida().getTipo());
            modeloTablaMovimientos.addRow(new Object[]{numMovimiento, jugador, pieza, movimiento.toNotacionCompleta()});
            numMovimiento++;
        }

        int ultimaFila = modeloTablaMovimientos.getRowCount() - 1;
        if (ultimaFila >= 0) {
            tablaMovimientos.scrollRectToVisible(tablaMovimientos.getCellRect(ultimaFila, 0, true));
        }
    }
}
