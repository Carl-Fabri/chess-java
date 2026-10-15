package dominio.puertos;

import dominio.modelos.Partida;
import dominio.modelos.Posicion;
import dominio.modelos.Tablero;
import dominio.modelos.Usuario;
import dominio.enums.Color;

public interface IGestionarJuego {
    Partida crearPartida(Usuario jugadorBlancas, Usuario jugadorNegras);
    boolean realizarMovimiento(Posicion origen, Posicion destino);
    Tablero getTablero();
    Color getTurnoActual();
    Partida getPartidaActual();
    boolean estaEnJaque(Color color);
    boolean estaEnJaqueMate(Color color);
    boolean puedeRetroceder(Color color);
    boolean retrocederMovimiento(Color color);
    int getRetrocesosRestantes(Color color);
}
