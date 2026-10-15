package dominio.modelos;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista doblemente enlazada de movimientos: cada nodo apunta a su anterior y a su siguiente.
 */
public class HistorialMovimientos {
    private NodoMovimiento cabeza;
    private NodoMovimiento cola;
    private int tamanio;

    public void agregar(NodoMovimiento nodo) {
        if (cabeza == null) {
            cabeza = nodo;
        } else {
            cola.setSiguiente(nodo);
            nodo.setAnterior(cola);
        }
        cola = nodo;
        tamanio++;
    }

    public NodoMovimiento eliminarUltimo() {
        if (cola == null) return null;

        NodoMovimiento eliminado = cola;
        cola = eliminado.getAnterior();
        if (cola == null) {
            cabeza = null;
        } else {
            cola.setSiguiente(null);
        }
        eliminado.setAnterior(null);
        tamanio--;
        return eliminado;
    }

    public List<String> aNotaciones() {
        List<String> notaciones = new ArrayList<>();
        NodoMovimiento actual = cabeza;
        while (actual != null) {
            notaciones.add(actual.toNotacion());
            actual = actual.getSiguiente();
        }
        return notaciones;
    }

    public boolean estaVacio() {
        return tamanio == 0;
    }
    public NodoMovimiento getCabeza() {
        return cabeza;
    }
    public NodoMovimiento getCola() {
        return cola;
    }
    public int getTamanio() {
        return tamanio;
    }
}
