package dominio.estructuras;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Implementación del {@link TADLista} con nodos doblemente enlazados.
 *
 * <pre>
 *            cabeza                             cola
 *              │                                  │
 *              ▼                                  ▼
 *   null ◄── [Dato 1] ◄──► [Dato 2] ◄──► [Dato 3] ──► null
 * </pre>
 *
 * <p>Invariantes (se cumplen al terminar cada operación pública):</p>
 * <ol>
 *   <li>Lista vacía ⇔ {@code cabeza == null} ⇔ {@code cola == null} ⇔ {@code tamanio == 0}.</li>
 *   <li>{@code cabeza.anterior == null} y {@code cola.siguiente == null}.</li>
 *   <li>Para todo nodo {@code n} con siguiente: {@code n.siguiente.anterior == n}.</li>
 *   <li>Recorrer desde la cabeza por {@code siguiente} visita exactamente {@code tamanio} nodos y termina en la cola.</li>
 * </ol>
 */
public class ListaDoblementeEnlazada<T> implements TADLista<T> {

    /**
     * Nodo: un dato y dos punteros. Es privado y estático: solo esta clase puede crear nodos
     * o mover sus enlaces, y el nodo no guarda una referencia oculta a la lista.
     */
    private static final class Nodo<T> {
        private T dato;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;

        private Nodo(T dato, Nodo<T> anterior, Nodo<T> siguiente) {
            this.dato = dato;
            this.anterior = anterior;
            this.siguiente = siguiente;
        }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamanio;
    /** Cuenta los cambios estructurales (insertar/eliminar) para que los iteradores detecten modificaciones. */
    private int modificaciones;

    public ListaDoblementeEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamanio = 0;
        this.modificaciones = 0;
    }

    // ------------------------------------------------------------------ Crear

    @Override
    public void insertarAlInicio(T dato) {
        Objects.requireNonNull(dato, "La lista no admite datos null");
        Nodo<T> nuevo = new Nodo<>(dato, null, cabeza);  // 1. nuevo.siguiente apunta a la cabeza actual
        if (cabeza == null) {
            cola = nuevo;                                 // lista vacía: también es la cola
        } else {
            cabeza.anterior = nuevo;                      // 2. la cabeza actual apunta hacia atrás al nuevo
        }
        cabeza = nuevo;                                   // 3. el nuevo pasa a ser la cabeza
        tamanio++;
        modificaciones++;
    }

    @Override
    public void insertarAlFinal(T dato) {
        Objects.requireNonNull(dato, "La lista no admite datos null");
        Nodo<T> nuevo = new Nodo<>(dato, cola, null);    // 1. nuevo.anterior apunta a la cola actual
        if (cola == null) {
            cabeza = nuevo;                               // lista vacía: también es la cabeza
        } else {
            cola.siguiente = nuevo;                       // 2. la cola actual apunta hacia adelante al nuevo
        }
        cola = nuevo;                                     // 3. el nuevo pasa a ser la cola
        tamanio++;
        modificaciones++;
    }

    @Override
    public void insertarEn(int indice, T dato) {
        Objects.requireNonNull(dato, "La lista no admite datos null");
        if (indice < 0 || indice > tamanio) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fuera de rango [0, " + tamanio + "]");
        }
        if (indice == 0) {
            insertarAlInicio(dato);
        } else if (indice == tamanio) {
            insertarAlFinal(dato);
        } else {
            // Insertar entre predecesor y sucesor: se actualizan 4 punteros
            Nodo<T> sucesor = nodoEn(indice);
            Nodo<T> predecesor = sucesor.anterior;
            Nodo<T> nuevo = new Nodo<>(dato, predecesor, sucesor);
            predecesor.siguiente = nuevo;
            sucesor.anterior = nuevo;
            tamanio++;
            modificaciones++;
        }
    }

    // -------------------------------------------------------------- Consultar

    @Override
    public T obtenerPrimero() {
        verificarNoVacia();
        return cabeza.dato;
    }

    @Override
    public T obtenerUltimo() {
        verificarNoVacia();
        return cola.dato;
    }

    @Override
    public T obtener(int indice) {
        verificarIndice(indice);
        return nodoEn(indice).dato;
    }

    @Override
    public int indiceDe(T dato) {
        int indice = 0;
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (actual.dato.equals(dato)) return indice;
            indice++;
        }
        return -1;
    }

    @Override
    public boolean contiene(T dato) {
        return indiceDe(dato) != -1;
    }

    @Override
    public int tamanio() {
        return tamanio;
    }

    @Override
    public boolean estaVacia() {
        return tamanio == 0;
    }

    // ----------------------------------------------------------------- Editar

    @Override
    public T modificar(int indice, T dato) {
        Objects.requireNonNull(dato, "La lista no admite datos null");
        verificarIndice(indice);
        return reemplazarDato(nodoEn(indice), dato);
    }

    @Override
    public T modificarUltimo(T dato) {
        Objects.requireNonNull(dato, "La lista no admite datos null");
        verificarNoVacia();
        return reemplazarDato(cola, dato);
    }

    // --------------------------------------------------------------- Eliminar

    @Override
    public T eliminarPrimero() {
        verificarNoVacia();
        return desenlazar(cabeza);
    }

    @Override
    public T eliminarUltimo() {
        verificarNoVacia();
        return desenlazar(cola);
    }

    @Override
    public T eliminarEn(int indice) {
        verificarIndice(indice);
        return desenlazar(nodoEn(indice));
    }

    @Override
    public boolean eliminar(T dato) {
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (actual.dato.equals(dato)) {
                desenlazar(actual);
                return true;
            }
        }
        return false;
    }

    @Override
    public void vaciar() {
        // Se cortan los enlaces de cada nodo para que el recolector de basura los libere
        Nodo<T> actual = cabeza;
        while (actual != null) {
            Nodo<T> siguiente = actual.siguiente;
            actual.dato = null;
            actual.anterior = null;
            actual.siguiente = null;
            actual = siguiente;
        }
        cabeza = null;
        cola = null;
        tamanio = 0;
        modificaciones++;
    }

    // ---------------------------------------------------------------- Recorrer

    @Override
    public Iterator<T> iterator() {
        return new Iterador(cabeza, true);
    }

    @Override
    public Iterator<T> iteradorInverso() {
        return new Iterador(cola, false);
    }

    /** Avanza por "siguiente" o retrocede por "anterior"; entrega datos, nunca nodos. */
    private final class Iterador implements Iterator<T> {
        private Nodo<T> actual;
        private final boolean haciaAdelante;
        private final int modificacionesEsperadas;

        private Iterador(Nodo<T> inicio, boolean haciaAdelante) {
            this.actual = inicio;
            this.haciaAdelante = haciaAdelante;
            this.modificacionesEsperadas = modificaciones;
        }

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if (modificaciones != modificacionesEsperadas) {
                throw new ConcurrentModificationException("La lista cambió durante el recorrido");
            }
            if (actual == null) throw new NoSuchElementException();
            T dato = actual.dato;
            actual = haciaAdelante ? actual.siguiente : actual.anterior;
            return dato;
        }
    }

    // ------------------------------------------------------- Lógica de punteros

    /**
     * Único punto donde se quita un nodo: une a su predecesor con su sucesor y actualiza
     * cabeza/cola si el nodo estaba en un extremo. O(1) gracias al doble enlace.
     */
    private T desenlazar(Nodo<T> nodo) {
        Nodo<T> predecesor = nodo.anterior;
        Nodo<T> sucesor = nodo.siguiente;

        if (predecesor == null) {
            cabeza = sucesor;                 // era la cabeza
        } else {
            predecesor.siguiente = sucesor;   // el predecesor salta al sucesor
        }

        if (sucesor == null) {
            cola = predecesor;                // era la cola
        } else {
            sucesor.anterior = predecesor;    // el sucesor apunta atrás al predecesor
        }

        T dato = nodo.dato;
        nodo.dato = null;                     // se desconecta el nodo para que el GC lo libere
        nodo.anterior = null;
        nodo.siguiente = null;
        tamanio--;
        modificaciones++;
        return dato;
    }

    /** Localiza el nodo de una posición recorriendo desde el extremo más cercano: a lo sumo n/2 pasos. */
    private Nodo<T> nodoEn(int indice) {
        Nodo<T> actual;
        if (indice < tamanio / 2) {
            actual = cabeza;
            for (int i = 0; i < indice; i++) {
                actual = actual.siguiente;
            }
        } else {
            actual = cola;
            for (int i = tamanio - 1; i > indice; i--) {
                actual = actual.anterior;
            }
        }
        return actual;
    }

    private T reemplazarDato(Nodo<T> nodo, T dato) {
        T anterior = nodo.dato;
        nodo.dato = dato;
        return anterior;
    }

    private void verificarNoVacia() {
        if (tamanio == 0) throw new NoSuchElementException("La lista está vacía");
    }

    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fuera de rango [0, " + tamanio + ")");
        }
    }

    /** Comprueba los 4 invariantes de la clase. Lo usan las pruebas tras cada operación. */
    boolean verificarInvariantes() {
        if (tamanio == 0) return cabeza == null && cola == null;
        if (cabeza == null || cola == null) return false;
        if (cabeza.anterior != null || cola.siguiente != null) return false;

        int contados = 0;
        Nodo<T> previo = null;
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (actual.anterior != previo) return false;
            if (actual.dato == null) return false;
            previo = actual;
            contados++;
            if (contados > tamanio) return false; // evita ciclos
        }
        return contados == tamanio && previo == cola;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" <-> ");
        }
        return sb.append("]").toString();
    }
}
