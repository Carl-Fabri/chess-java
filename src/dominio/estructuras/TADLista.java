package dominio.estructuras;

import java.util.Iterator;

/**
 * TDA Lista: especifica QUÉ operaciones ofrece una lista y bajo qué condiciones,
 * sin decir CÓMO se implementan. Quien la usa no conoce nodos ni punteros.
 *
 * <p>Convenciones del TDA:</p>
 * <ul>
 *   <li>Las posiciones van de {@code 0} (primero) a {@code tamanio() - 1} (último).</li>
 *   <li>No se admiten datos {@code null}: lanzan {@link NullPointerException}.</li>
 *   <li>Consultar, editar o eliminar en una lista vacía lanza {@link java.util.NoSuchElementException}.</li>
 *   <li>Un índice fuera de rango lanza {@link IndexOutOfBoundsException}.</li>
 * </ul>
 *
 * @param <T> tipo del dato que guarda la lista
 */
public interface TADLista<T> extends Iterable<T> {

    // ------------------------------------------------------------------ Crear

    /** Inserta el dato como nuevo primer elemento. O(1) */
    void insertarAlInicio(T dato);

    /** Inserta el dato como nuevo último elemento. O(1) */
    void insertarAlFinal(T dato);

    /**
     * Inserta el dato en la posición indicada; los elementos desde esa posición se corren una a la derecha.
     * Precondición: {@code 0 <= indice <= tamanio()}. O(n)
     */
    void insertarEn(int indice, T dato);

    // -------------------------------------------------------------- Consultar

    /** Devuelve el primer elemento. Precondición: lista no vacía. O(1) */
    T obtenerPrimero();

    /** Devuelve el último elemento. Precondición: lista no vacía. O(1) */
    T obtenerUltimo();

    /** Devuelve el elemento de la posición indicada. Precondición: {@code 0 <= indice < tamanio()}. O(n) */
    T obtener(int indice);

    /** Posición de la primera aparición del dato (según {@code equals}), o -1 si no está. O(n) */
    int indiceDe(T dato);

    /** Indica si el dato está en la lista. O(n) */
    boolean contiene(T dato);

    /** Cantidad de elementos. O(1) */
    int tamanio();

    /** Indica si la lista no tiene elementos. O(1) */
    boolean estaVacia();

    // ----------------------------------------------------------------- Editar

    /**
     * Reemplaza el dato de la posición indicada y devuelve el anterior. No cambia la estructura.
     * Precondición: {@code 0 <= indice < tamanio()}. O(n)
     */
    T modificar(int indice, T dato);

    /** Reemplaza el último dato y devuelve el anterior. Precondición: lista no vacía. O(1) */
    T modificarUltimo(T dato);

    // --------------------------------------------------------------- Eliminar

    /** Quita y devuelve el primer elemento. Precondición: lista no vacía. O(1) */
    T eliminarPrimero();

    /** Quita y devuelve el último elemento. Precondición: lista no vacía. O(1) */
    T eliminarUltimo();

    /** Quita y devuelve el elemento de la posición indicada. Precondición: {@code 0 <= indice < tamanio()}. O(n) */
    T eliminarEn(int indice);

    /** Quita la primera aparición del dato. Devuelve si se encontró. O(n) */
    boolean eliminar(T dato);

    /** Elimina todos los elementos. O(n) */
    void vaciar();

    // ---------------------------------------------------------------- Recorrer

    /** Recorre del primero al último. Lanza ConcurrentModificationException si la lista cambia durante el recorrido. */
    @Override
    Iterator<T> iterator();

    /** Recorre del último al primero. Mismas garantías que {@link #iterator()}. */
    Iterator<T> iteradorInverso();
}
