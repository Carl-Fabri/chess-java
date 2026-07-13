package dominio.modelos;

import dominio.enums.Color;
import dominio.modelos.piezas.*;

import java.util.List;

public class Tablero {
    private Pieza[][] tablero;
    
    public Tablero() {
        this.tablero = new Pieza[8][8];
        inicializarTablero();
    }
    
    private void inicializarTablero() {
        for (int fila = 0; fila < 8; fila++) {
            for (int columna = 0; columna < 8; columna++) {
                tablero[fila][columna] = null;
            }
        }
        
        tablero[0][0] = new Torre(Color.NEGRO, new Posicion(0, 0));
        tablero[0][1] = new Caballo(Color.NEGRO, new Posicion(0, 1));
        tablero[0][2] = new Alfil(Color.NEGRO, new Posicion(0, 2));
        tablero[0][3] = new Reina(Color.NEGRO, new Posicion(0, 3));
        tablero[0][4] = new Rey(Color.NEGRO, new Posicion(0, 4));
        tablero[0][5] = new Alfil(Color.NEGRO, new Posicion(0, 5));
        tablero[0][6] = new Caballo(Color.NEGRO, new Posicion(0, 6));
        tablero[0][7] = new Torre(Color.NEGRO, new Posicion(0, 7));
        
        for (int columna = 0; columna < 8; columna++) {
            tablero[1][columna] = new Peon(Color.NEGRO, new Posicion(1, columna));
        }
        
        tablero[7][0] = new Torre(Color.BLANCO, new Posicion(7, 0));
        tablero[7][1] = new Caballo(Color.BLANCO, new Posicion(7, 1));
        tablero[7][2] = new Alfil(Color.BLANCO, new Posicion(7, 2));
        tablero[7][3] = new Reina(Color.BLANCO, new Posicion(7, 3));
        tablero[7][4] = new Rey(Color.BLANCO, new Posicion(7, 4));
        tablero[7][5] = new Alfil(Color.BLANCO, new Posicion(7, 5));
        tablero[7][6] = new Caballo(Color.BLANCO, new Posicion(7, 6));
        tablero[7][7] = new Torre(Color.BLANCO, new Posicion(7, 7));
        
        for (int columna = 0; columna < 8; columna++) {
            tablero[6][columna] = new Peon(Color.BLANCO, new Posicion(6, columna));
        }
    }
    
    public Pieza getPieza(Posicion posicion) {
        if (!posicion.esValida()) return null;
        return tablero[posicion.getFila()][posicion.getColumna()];
    }
    
    public void setPieza(Posicion posicion, Pieza pieza) {
        if (posicion.esValida()) {
            tablero[posicion.getFila()][posicion.getColumna()] = pieza;
            if (pieza != null) {
                pieza.mover(posicion);
            }
        }
    }
    
    public void removerPieza(Posicion posicion) {
        if (posicion.esValida()) {
            tablero[posicion.getFila()][posicion.getColumna()] = null;
        }
    }
    
    public boolean ejecutarMovimiento(Posicion origen, Posicion destino) {
        Pieza pieza = getPieza(origen);
        if (pieza == null) return false;
        if (!pieza.esMovimientoValido(destino, this)) return false;
        
        Pieza piezaCapturada = getPieza(destino);
        
        tablero[destino.getFila()][destino.getColumna()] = pieza;
        tablero[origen.getFila()][origen.getColumna()] = null;
        pieza.mover(destino);
        
        if (estaEnJaque(pieza.getColor())) {
            pieza.mover(origen);
            tablero[origen.getFila()][origen.getColumna()] = pieza;
            tablero[destino.getFila()][destino.getColumna()] = piezaCapturada;
            return false;
        }
        
        return true;
    }

    public Posicion getPosicionRey(Color color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Pieza p = tablero[i][j];
                if (p != null && p instanceof Rey && p.getColor() == color) {
                    return p.getPosicion();
                }
            }
        }
        return null;
    }

    public boolean esCasillaAtacada(Posicion pos, Color colorAtacante) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Pieza p = tablero[i][j];
                if (p != null && p.getColor() == colorAtacante) {
                    if (p instanceof Peon) {
                        int direccion = (p.getColor() == Color.BLANCO) ? -1 : 1;
                        int filaAtaque = p.getPosicion().getFila() + direccion;
                        if (filaAtaque == pos.getFila()) {
                            if (Math.abs(p.getPosicion().getColumna() - pos.getColumna()) == 1) {
                                return true;
                            }
                        }
                    } else if (p instanceof Rey) {
                        if (Math.abs(p.getPosicion().getFila() - pos.getFila()) <= 1 &&
                            Math.abs(p.getPosicion().getColumna() - pos.getColumna()) <= 1) {
                            return true;
                        }
                    } else if (p instanceof Caballo) {
                        int diffFila = Math.abs(p.getPosicion().getFila() - pos.getFila());
                        int diffCol = Math.abs(p.getPosicion().getColumna() - pos.getColumna());
                        if ((diffFila == 2 && diffCol == 1) || (diffFila == 1 && diffCol == 2)) {
                            return true;
                        }
                    } else if (p instanceof Alfil) {
                        if (Math.abs(p.getPosicion().getFila() - pos.getFila()) == Math.abs(p.getPosicion().getColumna() - pos.getColumna())) {
                            if (esCaminoLibre(p.getPosicion(), pos)) {
                                return true;
                            }
                        }
                    } else if (p instanceof Torre) {
                        if (p.getPosicion().getFila() == pos.getFila() || p.getPosicion().getColumna() == pos.getColumna()) {
                            if (esCaminoLibre(p.getPosicion(), pos)) {
                                return true;
                            }
                        }
                    } else if (p instanceof Reina) {
                        boolean esDiagonal = Math.abs(p.getPosicion().getFila() - pos.getFila()) == Math.abs(p.getPosicion().getColumna() - pos.getColumna());
                        boolean esOrtogonal = p.getPosicion().getFila() == pos.getFila() || p.getPosicion().getColumna() == pos.getColumna();
                        if (esDiagonal || esOrtogonal) {
                            if (esCaminoLibre(p.getPosicion(), pos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean esCaminoLibre(Posicion origen, Posicion destino) {
        int stepFila = Integer.compare(destino.getFila(), origen.getFila());
        int stepCol = Integer.compare(destino.getColumna(), origen.getColumna());
        
        int currFila = origen.getFila() + stepFila;
        int currCol = origen.getColumna() + stepCol;
        
        while (currFila != destino.getFila() || currCol != destino.getColumna()) {
            if (tablero[currFila][currCol] != null) {
                return false;
            }
            currFila += stepFila;
            currCol += stepCol;
        }
        return true;
    }

    public boolean estaEnJaque(Color colorRey) {
        Posicion posRey = getPosicionRey(colorRey);
        if (posRey == null) return false;
        
        Color colorEnemigo = (colorRey == Color.BLANCO) ? Color.NEGRO : Color.BLANCO;
        return esCasillaAtacada(posRey, colorEnemigo);
    }

    public boolean estaEnJaqueMate(Color colorRey) {
        if (!estaEnJaque(colorRey)) return false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Pieza p = tablero[i][j];
                if (p != null && p.getColor() == colorRey) {
                    List<Posicion> movimientos = p.getMovimientosPosibles(this);
                    Posicion origen = p.getPosicion();

                    for (Posicion destino : movimientos) {
                        Pieza piezaCapturada = getPieza(destino);
                        
                        tablero[destino.getFila()][destino.getColumna()] = p;
                        tablero[origen.getFila()][origen.getColumna()] = null;
                        p.mover(destino);

                        boolean sigueEnJaque = estaEnJaque(colorRey);

                        p.mover(origen);
                        tablero[origen.getFila()][origen.getColumna()] = p;
                        tablero[destino.getFila()][destino.getColumna()] = piezaCapturada;

                        if (!sigueEnJaque) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
