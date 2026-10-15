package aplicacion.casosuso;

import dominio.enums.Color;
import dominio.modelos.Partida;
import dominio.modelos.Posicion;
import dominio.modelos.Tablero;
import dominio.modelos.Usuario;
import dominio.puertos.IGestionarJuego;

public class GestionarJuego implements IGestionarJuego {
    private Partida partidaActual;

    @Override
    public Partida crearPartida(Usuario jugadorBlancas, Usuario jugadorNegras) {
        this.partidaActual = new Partida(jugadorBlancas, jugadorNegras);
        return this.partidaActual;
    }

    @Override
    public boolean realizarMovimiento(Posicion origen, Posicion destino) {
        if (partidaActual == null) return false;
        return partidaActual.realizarMovimiento(origen, destino);
    }

    @Override
    public Tablero getTablero() {
        if (partidaActual == null) return null;
        return partidaActual.getTablero();
    }

    @Override
    public Color getTurnoActual() {
        if (partidaActual == null) return null;
        return partidaActual.getTurnoActual();
    }

    @Override
    public Partida getPartidaActual() {
        return partidaActual;
    }

    @Override
    public boolean estaEnJaque(Color color) {
        if (partidaActual == null) return false;
        return partidaActual.getTablero().estaEnJaque(color);
    }

    @Override
    public boolean estaEnJaqueMate(Color color) {
        if (partidaActual == null) return false;
        return partidaActual.getTablero().estaEnJaqueMate(color);
    }

    @Override
    public boolean puedeRetroceder(Color color) {
        if (partidaActual == null) return false;
        return partidaActual.puedeRetroceder(color);
    }

    @Override
    public boolean retrocederMovimiento(Color color) {
        if (partidaActual == null) return false;
        return partidaActual.retrocederMovimiento(color);
    }

    @Override
    public int getRetrocesosRestantes(Color color) {
        if (partidaActual == null) return 0;
        return partidaActual.getRetrocesosRestantes(color);
    }
}
