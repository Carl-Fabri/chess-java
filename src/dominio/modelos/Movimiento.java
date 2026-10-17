package dominio.modelos;

import dominio.enums.Color;
import dominio.enums.EstadoMovimiento;
import dominio.modelos.piezas.Pieza;

import java.util.Objects;

/**
 * Dato que guarda cada nodo del historial: un solo movimiento (de blancas o de negras).
 * Es inmutable y no conoce a sus vecinos: los punteros anterior/siguiente viven en el nodo de la lista.
 * Guarda la pieza capturada para poder revertir el movimiento.
 */
public final class Movimiento {
    private final Posicion origen;
    private final Posicion destino;
    private final Pieza piezaMovida;
    private final Pieza piezaCapturada;
    private final Color color;
    private final EstadoMovimiento estado;

    public Movimiento(Posicion origen, Posicion destino, Pieza piezaMovida, Pieza piezaCapturada) {
        this(origen, destino, piezaMovida, piezaCapturada, EstadoMovimiento.NORMAL);
    }

    private Movimiento(Posicion origen, Posicion destino, Pieza piezaMovida, Pieza piezaCapturada, EstadoMovimiento estado) {
        this.origen = Objects.requireNonNull(origen);
        this.destino = Objects.requireNonNull(destino);
        this.piezaMovida = Objects.requireNonNull(piezaMovida);
        this.piezaCapturada = piezaCapturada;
        this.color = piezaMovida.getColor();
        this.estado = Objects.requireNonNull(estado);
    }

    /** Copia del movimiento con otro estado; se usa para editar el nodo del historial. */
    public Movimiento conEstado(EstadoMovimiento nuevoEstado) {
        return new Movimiento(origen, destino, piezaMovida, piezaCapturada, nuevoEstado);
    }

    public boolean esCaptura() {
        return piezaCapturada != null;
    }

    /** Formato de sincronización online ("e2-e4"); otras partes del sistema lo parsean. */
    public String toNotacion() {
        return origen.toNotacion() + "-" + destino.toNotacion();
    }

    /** Formato para mostrar: "e2-e4", "e4xd5" (captura), "d1h5+" (jaque), "h5f7#" (mate). */
    public String toNotacionCompleta() {
        return origen.toNotacion() + (esCaptura() ? "x" : "-") + destino.toNotacion() + estado.getSufijo();
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
    public EstadoMovimiento getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return toNotacionCompleta();
    }
}
