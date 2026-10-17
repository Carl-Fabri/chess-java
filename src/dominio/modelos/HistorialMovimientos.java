package dominio.modelos;

import dominio.enums.EstadoMovimiento;
import dominio.estructuras.ListaDoblementeEnlazada;
import dominio.estructuras.TADLista;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TDA Historial de movimientos de una partida.
 *
 * <p>Encapsula un {@link TADLista} de {@link Movimiento} y solo expone las operaciones con sentido en ajedrez.
 * Cada acción del juego corresponde a una acción de la lista:</p>
 * <ul>
 *   <li><b>Crear</b>: {@link #registrar} → insertarAlFinal (cada jugada válida).</li>
 *   <li><b>Editar</b>: {@link #marcarUltimo} → modificarUltimo (anotar jaque o jaque mate).</li>
 *   <li><b>Eliminar</b>: {@link #deshacerUltimo} → eliminarUltimo (regresar movimiento).</li>
 *   <li><b>Recorrer</b>: for-each (tabla de la vista y sincronización online).</li>
 * </ul>
 */
public class HistorialMovimientos implements Iterable<Movimiento> {
    private final TADLista<Movimiento> movimientos;

    public HistorialMovimientos() {
        this.movimientos = new ListaDoblementeEnlazada<>();
    }

    /** Crear: agrega la jugada al final del historial. O(1) */
    public void registrar(Movimiento movimiento) {
        movimientos.insertarAlFinal(movimiento);
    }

    /**
     * Editar: reemplaza la última jugada por una copia con el estado indicado. O(1)
     * @throws IllegalStateException si el historial está vacío
     */
    public void marcarUltimo(EstadoMovimiento estado) {
        if (movimientos.estaVacia()) throw new IllegalStateException("No hay movimientos que marcar");
        movimientos.modificarUltimo(movimientos.obtenerUltimo().conEstado(estado));
    }

    /** Eliminar: quita y devuelve la última jugada, o null si el historial está vacío. O(1) */
    public Movimiento deshacerUltimo() {
        return movimientos.estaVacia() ? null : movimientos.eliminarUltimo();
    }

    /** Última jugada, o null si el historial está vacío. O(1) */
    public Movimiento getUltimo() {
        return movimientos.estaVacia() ? null : movimientos.obtenerUltimo();
    }

    /** Jugada por número (1 = primera). Recorre desde el extremo más cercano. O(n) */
    public Movimiento getMovimiento(int numero) {
        return movimientos.obtener(numero - 1);
    }

    /** Notaciones en orden ("e2-e4", ...) para guardar la partida online. O(n) */
    public List<String> aNotaciones() {
        List<String> notaciones = new ArrayList<>(movimientos.tamanio());
        for (Movimiento movimiento : movimientos) {
            notaciones.add(movimiento.toNotacion());
        }
        return notaciones;
    }

    public boolean estaVacio() {
        return movimientos.estaVacia();
    }
    public int getTamanio() {
        return movimientos.tamanio();
    }

    @Override
    public Iterator<Movimiento> iterator() {
        return movimientos.iterator();
    }

    @Override
    public String toString() {
        return movimientos.toString();
    }
}
