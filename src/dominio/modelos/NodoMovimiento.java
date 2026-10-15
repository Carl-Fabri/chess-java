package dominio.modelos;

import dominio.enums.Color;
import dominio.modelos.piezas.Pieza;

public class NodoMovimiento {
    private final Posicion origen;
    private final Posicion destino;
    private final Pieza piezaMovida;
    private final Pieza piezaCapturada;
    private final Color color;

    private NodoMovimiento anterior;
    private NodoMovimiento siguiente;

    public NodoMovimiento(Posicion origen, Posicion destino, Pieza piezaMovida, Pieza piezaCapturada) {
        this.origen = origen;
        this.destino = destino;
        this.piezaMovida = piezaMovida;
        this.piezaCapturada = piezaCapturada;
        this.color = piezaMovida.getColor();
    }

    public String toNotacion() {
        return origen.toNotacion() + "-" + destino.toNotacion();
    }

    public Posicion getOrigen() {
        return origen;
    }
    public Posicion getDestino() {
        return destino;
    }
    public Pieza getPiezaMovida() {
        return piezaMovida;
    }
    public Pieza getPiezaCapturada() {
        return piezaCapturada;
    }
    public Color getColor() {
        return color;
    }
    public NodoMovimiento getAnterior() {
        return anterior;
    }
    public void setAnterior(NodoMovimiento anterior) {
        this.anterior = anterior;
    }
    public NodoMovimiento getSiguiente() {
        return siguiente;
    }
    public void setSiguiente(NodoMovimiento siguiente) {
        this.siguiente = siguiente;
    }
}
