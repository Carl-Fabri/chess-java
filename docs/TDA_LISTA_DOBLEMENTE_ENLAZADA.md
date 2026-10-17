# TDA Lista doblemente enlazada e Historial de movimientos

Documentación técnica de cómo Chess Java guarda el historial de cada partida con un **Tipo de Dato Abstracto (TDA)** implementado como **lista doblemente enlazada**, sin `ArrayList` ni `LinkedList` de Java.

**Contenido**

1. [Conceptos: TDA, nodo y puntero](#1-conceptos-tda-nodo-y-puntero)
2. [Arquitectura](#2-arquitectura)
3. [Especificación del TDA `TADLista<T>`](#3-especificación-del-tda-tadlistat)
4. [Implementación `ListaDoblementeEnlazada<T>`](#4-implementación-listadoblementeenlazadat)
5. [Crear: inserción](#5-crear-inserción)
6. [Consultar: búsqueda bidireccional](#6-consultar-búsqueda-bidireccional)
7. [Editar: modificación](#7-editar-modificación)
8. [Eliminar: `desenlazar`](#8-eliminar-desenlazar)
9. [Recorrer: iteradores fail-fast](#9-recorrer-iteradores-fail-fast)
10. [El historial de movimientos](#10-el-historial-de-movimientos)
11. [Flujo dentro del juego](#11-flujo-dentro-del-juego)
12. [Complejidad](#12-complejidad)
13. [Pruebas](#13-pruebas)
14. [Decisiones de diseño](#14-decisiones-de-diseño)

---

## 1. Conceptos: TDA, nodo y puntero

**TDA.** Un Tipo de Dato Abstracto define un conjunto de valores y las operaciones permitidas sobre ellos, separando:

| | Pregunta | En Java |
|---|---|---|
| **Especificación** | ¿QUÉ hace y bajo qué condiciones? | `interface TADLista<T>` |
| **Implementación** | ¿CÓMO lo hace? | `class ListaDoblementeEnlazada<T>` |

El usuario del TDA solo conoce la especificación. Las **variables** (cabeza, cola, tamaño, nodos) y los **métodos auxiliares** (`nodoEn`, `desenlazar`, validaciones) son `private`: quedan encapsulados.

**Nodo y puntero.** Una lista enlazada es una colección de nodos; cada nodo guarda un dato y punteros a otros nodos. Java no tiene la sintaxis de punteros de C, pero toda variable de tipo objeto es una **referencia**, que cumple el papel de un puntero:

| C | Java |
|---|---|
| `struct Nodo *siguiente;` | `Nodo<T> siguiente;` |
| `p = p->siguiente;` | `actual = actual.siguiente;` |
| `NULL` | `null` |
| `malloc(sizeof(Nodo))` | `new Nodo<>(dato, anterior, siguiente)` |
| `free(nodo)` | cortar todas sus referencias → el *Garbage Collector* lo libera |

En una lista **doblemente** enlazada cada nodo tiene dos punteros: `anterior` y `siguiente`.

```
null ◄── [ anterior | dato | siguiente ] ──► null
```

---

## 2. Arquitectura

```
src/dominio/
├── estructuras/                        Estructuras de datos genéricas
│   ├── TADLista.java                   Especificación del TDA (interfaz)
│   └── ListaDoblementeEnlazada.java    Implementación (Nodo privado interno)
├── enums/
│   └── EstadoMovimiento.java           NORMAL, JAQUE (+), JAQUE_MATE (#)
└── modelos/
    ├── Movimiento.java                 El DATO: inmutable, sin punteros
    ├── HistorialMovimientos.java       TDA del dominio sobre TADLista<Movimiento>
    ├── Partida.java                    Crea / edita / elimina jugadas del historial
    ├── Tablero.java                    deshacerMovimiento(Movimiento)
    └── Usuario.java                    TADLista<Partida> para las partidas jugadas
test/dominio/estructuras/
    └── PruebasListaDoblementeEnlazada.java
```

```
 VistaJuego ──for-each──►  HistorialMovimientos  ──usa──►  TADLista<Movimiento>
 Partida ──registrar/marcar/deshacer──┘                         ▲ implements
                                                   ListaDoblementeEnlazada<T>
                                                        └── private static Nodo<T>
```

Tres capas separadas: **dato** (`Movimiento`), **nodo** (`Nodo<T>`, invisible fuera de la lista) y **TDA** (`TADLista<T>`).

---

## 3. Especificación del TDA `TADLista<T>`

Convenciones: posiciones de `0` a `tamanio()-1`; no se admiten datos `null`; las precondiciones violadas lanzan excepción y **no modifican la lista**.

| Grupo | Operación | Precondición | Resultado | Excepción si falla | Costo |
|---|---|---|---|---|---|
| Crear | `insertarAlInicio(d)` | `d != null` | `d` es el primero | `NullPointerException` | O(1) |
| Crear | `insertarAlFinal(d)` | `d != null` | `d` es el último | `NullPointerException` | O(1) |
| Crear | `insertarEn(i, d)` | `0 ≤ i ≤ n` | `d` queda en la posición `i` | `IndexOutOfBoundsException` | O(n) |
| Consultar | `obtenerPrimero()` / `obtenerUltimo()` | no vacía | dato del extremo | `NoSuchElementException` | O(1) |
| Consultar | `obtener(i)` | `0 ≤ i < n` | dato en `i` | `IndexOutOfBoundsException` | O(n) |
| Consultar | `indiceDe(d)` / `contiene(d)` | — | posición o `-1` / booleano | — | O(n) |
| Consultar | `tamanio()` / `estaVacia()` | — | — | — | O(1) |
| Editar | `modificar(i, d)` | `0 ≤ i < n`, `d != null` | reemplaza y devuelve el anterior | `IndexOutOfBounds` / `NullPointer` | O(n) |
| Editar | `modificarUltimo(d)` | no vacía, `d != null` | reemplaza el último | `NoSuchElement` / `NullPointer` | O(1) |
| Eliminar | `eliminarPrimero()` / `eliminarUltimo()` | no vacía | quita y devuelve | `NoSuchElementException` | O(1) |
| Eliminar | `eliminarEn(i)` | `0 ≤ i < n` | quita y devuelve | `IndexOutOfBoundsException` | O(n) |
| Eliminar | `eliminar(d)` | — | quita la 1.ª aparición; `true` si existía | — | O(n) |
| Eliminar | `vaciar()` | — | lista vacía | — | O(n) |
| Recorrer | `iterator()` / `iteradorInverso()` | — | de cabeza a cola / de cola a cabeza | `ConcurrentModificationException` si la lista cambia | O(1) por paso |

¿Por qué excepciones y no devolver `null`? Porque una precondición rota es un error de programación: fallar en el punto exacto hace el error visible, en vez de propagar un `null` que explota más tarde en otro lugar.

---

## 4. Implementación `ListaDoblementeEnlazada<T>`

```java
public class ListaDoblementeEnlazada<T> implements TADLista<T> {

    private static final class Nodo<T> {
        private T dato;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;
        private Nodo(T dato, Nodo<T> anterior, Nodo<T> siguiente) { ... }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamanio;
    private int modificaciones;   // para los iteradores fail-fast
    ...
}
```

**Encapsulamiento del nodo.** `Nodo` es una clase **privada** anidada: ninguna clase fuera de `ListaDoblementeEnlazada` puede nombrarla, crearla ni tocar sus punteros (el compilador lo impide). Es **`static`** para que cada nodo no guarde una referencia oculta a la lista (ahorra memoria y evita fugas).

**Atributos:**

| Atributo | Función |
|---|---|
| `cabeza` | Puntero al primer nodo |
| `cola` | Puntero al último nodo: inserción y eliminación al final en O(1) |
| `tamanio` | Contador: `tamanio()` en O(1) sin recorrer |
| `modificaciones` | Cuenta cambios estructurales; los iteradores lo usan para detectar modificaciones concurrentes |

**Invariantes de representación** (se cumplen al terminar cada operación pública; `verificarInvariantes()` los comprueba en las pruebas):

1. Vacía ⇔ `cabeza == null` ⇔ `cola == null` ⇔ `tamanio == 0`.
2. `cabeza.anterior == null` y `cola.siguiente == null`.
3. Para todo nodo `n` con siguiente: `n.siguiente.anterior == n` (el doble enlace es simétrico).
4. Recorrer desde `cabeza` por `siguiente` visita exactamente `tamanio` nodos y el último es `cola` (sin ciclos ni nodos perdidos).

---

## 5. Crear: inserción

### `insertarAlFinal(dato)`: O(1)

```java
Nodo<T> nuevo = new Nodo<>(dato, cola, null);  // 1. nuevo.anterior = cola actual
if (cola == null) {
    cabeza = nuevo;                             //    lista vacía: también es la cabeza
} else {
    cola.siguiente = nuevo;                     // 2. la cola actual apunta al nuevo
}
cola = nuevo;                                   // 3. el nuevo es la cola
tamanio++; modificaciones++;
```

```
Antes:   cabeza ─► [e2-e4] ◄─► [d7-d5] ◄─ cola
Después: cabeza ─► [e2-e4] ◄─► [d7-d5] ◄─► [e4xd5] ◄─ cola
```

El constructor del nodo recibe sus dos punteros, así el nodo nace ya enlazado hacia la lista y solo falta el enlace de vuelta.

### `insertarAlInicio(dato)`: O(1)

Simétrica: `new Nodo<>(dato, null, cabeza)`, luego `cabeza.anterior = nuevo` y `cabeza = nuevo`.

### `insertarEn(indice, dato)`: O(n)

Los extremos se delegan a las operaciones O(1). En el medio se ubica el **sucesor** con `nodoEn(indice)` y se actualizan **4 punteros**:

```java
Nodo<T> sucesor = nodoEn(indice);
Nodo<T> predecesor = sucesor.anterior;
Nodo<T> nuevo = new Nodo<>(dato, predecesor, sucesor);  // punteros 1 y 2
predecesor.siguiente = nuevo;                           // puntero 3
sucesor.anterior = nuevo;                               // puntero 4
```

```
[A] ◄─► [C]          →          [A] ◄─► [B] ◄─► [C]
pred     suc                    pred    nuevo    suc
```

---

## 6. Consultar: búsqueda bidireccional

`nodoEn(indice)` aprovecha el doble enlace: si el índice está en la primera mitad avanza desde la `cabeza` por `siguiente`; si no, retrocede desde la `cola` por `anterior`. Así hace **a lo sumo n/2 pasos**.

```java
if (indice < tamanio / 2) {
    actual = cabeza;  for (i = 0; i < indice; i++)           actual = actual.siguiente;
} else {
    actual = cola;    for (i = tamanio - 1; i > indice; i--) actual = actual.anterior;
}
```

`obtener`, `modificar`, `eliminarEn` e `insertarEn` reutilizan este método.

---

## 7. Editar: modificación

Editar cambia el **dato** de un nodo existente, sin tocar ningún puntero. Por eso no incrementa `modificaciones` (un recorrido en curso sigue siendo válido).

```java
public T modificarUltimo(T dato) {
    Objects.requireNonNull(dato);
    verificarNoVacia();
    return reemplazarDato(cola, dato);   // O(1) gracias al puntero cola
}
```

En el juego se usa para **anotar el jaque o el jaque mate** en la última jugada (ver sección 10).

---

## 8. Eliminar: `desenlazar`

Todas las eliminaciones (`eliminarPrimero`, `eliminarUltimo`, `eliminarEn`, `eliminar(dato)`) terminan en **un único método privado**. Concentrar la lógica de punteros en un solo lugar evita duplicarla y reduce los errores:

```java
private T desenlazar(Nodo<T> nodo) {
    Nodo<T> predecesor = nodo.anterior;
    Nodo<T> sucesor = nodo.siguiente;

    if (predecesor == null) cabeza = sucesor;           // era la cabeza
    else predecesor.siguiente = sucesor;                 // el predecesor salta al sucesor

    if (sucesor == null) cola = predecesor;              // era la cola
    else sucesor.anterior = predecesor;                  // el sucesor apunta al predecesor

    T dato = nodo.dato;
    nodo.dato = null; nodo.anterior = null; nodo.siguiente = null;  // liberar para el GC
    tamanio--; modificaciones++;
    return dato;
}
```

```
[A] ◄─► [B] ◄─► [C]      →      [A] ◄─► [C]          [B] queda aislado → GC
```

Los cuatro casos (único nodo, cabeza, cola, medio) se resuelven con los dos `if`, sin ramas especiales.

**¿Por qué doble enlace?** Para `eliminarUltimo`: con una lista simple, encontrar el penúltimo nodo exige recorrer toda la lista (O(n)). Con el puntero `anterior`, la nueva cola es `cola.anterior`: O(1).

---

## 9. Recorrer: iteradores fail-fast

`TADLista<T>` extiende `Iterable<T>`, así que se puede escribir `for (Movimiento m : historial)`. El iterador entrega **datos, nunca nodos**.

```java
private final class Iterador implements Iterator<T> {
    private Nodo<T> actual;
    private final boolean haciaAdelante;
    private final int modificacionesEsperadas = modificaciones;

    public T next() {
        if (modificaciones != modificacionesEsperadas)
            throw new ConcurrentModificationException();
        T dato = actual.dato;
        actual = haciaAdelante ? actual.siguiente : actual.anterior;
        return dato;
    }
}
```

- **Bidireccional**: la misma clase recorre hacia adelante (`iterator()`) o hacia atrás (`iteradorInverso()`), según el puntero que siga.
- **Fail-fast**: si alguien inserta o elimina mientras se recorre, el iterador lo detecta y falla de inmediato en lugar de seguir un puntero que ya no es válido.

---

## 10. El historial de movimientos

### El dato: `Movimiento`

Inmutable (`final` en todos sus campos). Guarda `origen`, `destino`, `piezaMovida`, `piezaCapturada`, `color` y `estado` (`NORMAL`, `JAQUE`, `JAQUE_MATE`).

| Método | Uso |
|---|---|
| `toNotacion()` → `"e4-d5"` | Formato que se guarda en MySQL para las partidas online (no cambia, para no romper la sincronización) |
| `toNotacionCompleta()` → `"e4xd5+"` | Lo que muestra la tabla: `x` si hubo captura, `+` jaque, `#` mate |
| `conEstado(estado)` | Devuelve una copia con otro estado; así se edita sin romper la inmutabilidad |
| `getPiezaCapturada()` | Permite reponer la pieza al deshacer |

### El TDA del dominio: `HistorialMovimientos`

Encapsula un `TADLista<Movimiento>` (declarado con el tipo de la **interfaz**) y traduce cada acción del juego a una acción de la lista:

| Acción en el juego | Método del historial | Operación del TDA | Costo |
|---|---|---|---|
| Jugar | `registrar(m)` | **Crear**: `insertarAlFinal` | O(1) |
| Anotar jaque / mate | `marcarUltimo(estado)` | **Editar**: `modificarUltimo` | O(1) |
| Regresar movimiento | `deshacerUltimo()` | **Eliminar**: `eliminarUltimo` | O(1) |
| Consultar última jugada | `getUltimo()` | `obtenerUltimo` | O(1) |
| Ir a la jugada N | `getMovimiento(n)` | `obtener(n-1)` (bidireccional) | O(n/2) |
| Mostrar la tabla / sincronizar | `for-each`, `aNotaciones()` | iterador | O(n) |

Hacia afuera no ofrece `insertarEn` ni `eliminarEn`: en ajedrez no se insertan jugadas en medio de la partida. **El TDA del dominio restringe el TDA genérico a lo que tiene sentido.**

---

## 11. Flujo dentro del juego

### Jugar (crear + editar)

```java
// Partida.realizarMovimiento
if (!tablero.ejecutarMovimiento(origen, destino)) return false;
historial.registrar(new Movimiento(origen, destino, piezaMovida, piezaCapturada));   // CREAR
turnoActual = rival;
if (tablero.estaEnJaque(turnoActual)) {                                              // EDITAR
    historial.marcarUltimo(tablero.estaEnJaqueMate(turnoActual) ? JAQUE_MATE : JAQUE);
}
```

La jugada se registra apenas el tablero la acepta. Su anotación (`+`/`#`) depende de la posición **resultante**, así que se completa después editando el último nodo en O(1).

### Regresar movimiento (eliminar)

```java
// Partida.retrocederMovimiento
Movimiento ultimo = historial.deshacerUltimo();   // ELIMINAR la cola
tablero.deshacerMovimiento(ultimo);                // pieza a origen, capturada a destino
turnoActual = ultimo.getColor();
```

`puedeRetroceder(color)` exige que `historial.getUltimo()` sea del mismo color (solo se regresa la jugada propia antes de que el rival responda) y que queden retrocesos (`MAX_RETROCESOS = 2`). Al eliminar el nodo, su anotación de jaque desaparece con él.

### Mostrar (recorrer)

```java
// VistaJuego.actualizarHistorialMovimientos
for (Movimiento m : partida.getHistorial()) {
    modelo.addRow(new Object[]{n++, jugador, pieza, m.toNotacionCompleta()});
}
```

### Partidas online

`aNotaciones()` recorre la lista y produce `"e2-e4,d7-d5,e4-d5"`, que se guarda en MySQL. El rival compara la cantidad de jugadas guardadas con `getTamanio()` (O(1)): si hay más, reproduce las nuevas; si hay menos, el oponente regresó un movimiento y se llama a `retrocederMovimiento`.

---

## 12. Complejidad

| Operación | Arreglo / `ArrayList` | Lista simple (solo cabeza) | **Lista doble (cabeza + cola)** |
|---|---|---|---|
| Insertar al final | O(1) amortizado (a veces copia todo) | O(n) | **O(1)** |
| Insertar al inicio | O(n) | O(1) | **O(1)** |
| Eliminar último | O(1) | O(n) | **O(1)** |
| Eliminar primero | O(n) | O(1) | **O(1)** |
| Editar último | O(1) | O(n) | **O(1)** |
| Acceso por índice | **O(1)** | O(n) | O(n/2) |
| Memoria extra por elemento | ninguna | 1 puntero | 2 punteros |

El historial solo inserta al final, edita el final, elimina el final y recorre en orden: todas sus operaciones frecuentes son O(1) en la lista doble. A cambio, cada nodo usa un puntero más que en una lista simple. El `Tablero` sigue siendo una matriz `Pieza[8][8]` porque necesita acceso directo por fila y columna.

---

## 13. Pruebas

[`test/dominio/estructuras/PruebasListaDoblementeEnlazada.java`](../test/dominio/estructuras/PruebasListaDoblementeEnlazada.java), sin dependencias externas. Está en el mismo paquete que la lista para poder llamar a `verificarInvariantes()` (visibilidad de paquete) después de cada operación.

```powershell
# compilar el proyecto en out/ (ver README) y luego:
javac -encoding UTF-8 -cp out -d out-test test\dominio\estructuras\PruebasListaDoblementeEnlazada.java
java -cp "out;out-test" dominio.estructuras.PruebasListaDoblementeEnlazada
```

| Grupo | Casos |
|---|---|
| Crear | lista vacía, un nodo (cabeza == cola), inicio/final, `insertarEn` en medio y extremos |
| Consultar | `obtener` en cada posición (ambas mitades), `indiceDe`, `contiene` |
| Editar | `modificar`, `modificarUltimo` sin cambiar estructura |
| Eliminar | extremos hasta vaciar, `eliminarEn`, `eliminar(dato)`, `vaciar` y reutilización |
| Precondiciones | lista vacía, índice fuera de rango, `null`; una operación rechazada no corrompe la lista |
| Recorrer | iterador hacia adelante y hacia atrás, fail-fast |
| Integración | historial (registrar/marcar/deshacer), partida con captura y regreso, mate del pastor anotado `h5xf7#` |

Resultado actual: **15 superadas, 0 fallidas**.

---

## 14. Decisiones de diseño

| Decisión | Motivo |
|---|---|
| Interfaz `TADLista<T>` separada | Programar contra la especificación; la implementación se puede cambiar sin tocar a los clientes |
| `Nodo` privado, estático y anidado | Encapsulamiento total de los punteros; sin referencia oculta a la lista |
| Constructor `Nodo(dato, anterior, siguiente)` | El nodo nace enlazado; menos asignaciones sueltas |
| Un solo `desenlazar` | Toda la lógica de eliminación en un lugar: 4 casos con 2 `if` |
| `nodoEn` bidireccional | Usa el doble enlace para recorrer a lo sumo n/2 nodos |
| Precondiciones con excepciones | Fallar rápido y en el lugar correcto; la lista nunca queda inconsistente |
| No se admiten `null` | `indiceDe`/`eliminar` usan `equals` sin casos especiales; `null` nunca es un dato válido del historial |
| Contador `modificaciones` | Iteradores fail-fast como los de `java.util` |
| `Movimiento` inmutable + `conEstado` | Editar reemplaza el dato completo; nadie puede alterar una jugada guardada por otra vía |
| `HistorialMovimientos` restringe el TDA | Solo expone operaciones válidas en ajedrez (no se insertan jugadas en medio) |
| `toNotacion()` sin cambios | Compatibilidad con las partidas online ya guardadas en MySQL |
