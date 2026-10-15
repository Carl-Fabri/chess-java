package dominio.modelos;

import dominio.enums.Color;
import dominio.enums.EstadoJuego;
import dominio.modelos.piezas.Pieza;

import java.time.LocalDateTime;
import java.util.List;

public class Partida {
    public static final int MAX_RETROCESOS = 2;

    private String id;
    private Usuario jugadorBlancas;
    private Usuario jugadorNegras;
    private Tablero tablero;
    private Color turnoActual;
    private EstadoJuego estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private HistorialMovimientos historial;
    private int retrocesosBlancas;
    private int retrocesosNegras;
    
    public Partida(Usuario jugadorBlancas, Usuario jugadorNegras) {
        this.id = generarId();
        this.jugadorBlancas = jugadorBlancas;
        this.jugadorNegras = jugadorNegras;
        this.tablero = new Tablero();
        this.turnoActual = Color.BLANCO;
        this.estado = EstadoJuego.EN_PROGRESO;
        this.fechaInicio = LocalDateTime.now();
        this.historial = new HistorialMovimientos();
        this.retrocesosBlancas = MAX_RETROCESOS;
        this.retrocesosNegras = MAX_RETROCESOS;
    }
    
    public boolean realizarMovimiento(Posicion origen, Posicion destino) {
        if (estado != EstadoJuego.EN_PROGRESO) return false;
        
        Pieza piezaMovida = tablero.getPieza(origen);
        Pieza piezaCapturada = tablero.getPieza(destino);
        if (!tablero.ejecutarMovimiento(origen, destino)) return false;

        historial.agregar(new NodoMovimiento(origen, destino, piezaMovida, piezaCapturada));

        turnoActual = (turnoActual == Color.BLANCO) ? Color.NEGRO : Color.BLANCO;

        return true;
    }

    /**
     * El jugador que hizo el último movimiento puede regresarlo, hasta MAX_RETROCESOS veces por partida.
     */
    public boolean puedeRetroceder(Color color) {
        if (estado != EstadoJuego.EN_PROGRESO) return false;
        NodoMovimiento ultimo = historial.getCola();
        return ultimo != null && ultimo.getColor() == color && getRetrocesosRestantes(color) > 0;
    }

    public boolean retrocederMovimiento(Color color) {
        if (!puedeRetroceder(color)) return false;

        NodoMovimiento ultimo = historial.eliminarUltimo();
        tablero.deshacerMovimiento(ultimo);
        turnoActual = ultimo.getColor();

        if (color == Color.BLANCO) {
            retrocesosBlancas--;
        } else {
            retrocesosNegras--;
        }
        return true;
    }

    public int getRetrocesosRestantes(Color color) {
        return color == Color.BLANCO ? retrocesosBlancas : retrocesosNegras;
    }
    
    public void finalizarPartida(Usuario ganador) {
        this.estado = EstadoJuego.FINALIZADO;
        this.fechaFin = LocalDateTime.now();
        
        if (ganador != null) {
            ganador.incrementarVictorias();
            Usuario perdedor = ganador.equals(jugadorBlancas) ? jugadorNegras : jugadorBlancas;
            perdedor.incrementarDerrotas();
        } else {
            jugadorBlancas.incrementarEmpates();
            jugadorNegras.incrementarEmpates();
        }
        
        jugadorBlancas.agregarPartida(this);
        jugadorNegras.agregarPartida(this);
    }
    
    public Usuario getJugadorActual() {
        return turnoActual == Color.BLANCO ? jugadorBlancas : jugadorNegras;
    }
    
    private String generarId() {
        return "GAME-" + System.currentTimeMillis();
    }
    
    public String getId() {
        return id;
    }
    public Usuario getJugadorBlancas() {
        return jugadorBlancas;
    }
    public Usuario getJugadorNegras() {
        return jugadorNegras;
    }
    public Tablero getTablero() {
        return tablero;
    }
    public Color getTurnoActual() {
        return turnoActual;
    }
    public EstadoJuego getEstado() {
        return estado;
    }
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }
    public HistorialMovimientos getHistorial() {
        return historial;
    }
    public List<String> getMovimientos() {
        return historial.aNotaciones();
    }
    
    @Override
    public String toString() {
        return "Partida{" +
                "blancas=" + jugadorBlancas.getNombreUsuario() +
                ", negras=" + jugadorNegras.getNombreUsuario() +
                ", estado=" + estado +
                ", turno=" + turnoActual +
                '}';
    }
}
