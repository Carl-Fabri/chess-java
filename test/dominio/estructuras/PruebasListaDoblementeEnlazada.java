package dominio.estructuras;

import dominio.enums.Color;
import dominio.enums.EstadoMovimiento;
import dominio.modelos.HistorialMovimientos;
import dominio.modelos.Movimiento;
import dominio.modelos.Partida;
import dominio.modelos.Posicion;
import dominio.modelos.Usuario;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Pruebas del TDA Lista y del historial de movimientos, sin dependencias externas.
 * Está en el paquete dominio.estructuras para poder llamar a verificarInvariantes().
 *
 * Ejecutar (desde la raíz, después de compilar src en out/):
 *   javac -encoding UTF-8 -cp out -d out-test test/dominio/estructuras/PruebasListaDoblementeEnlazada.java
 *   java -cp "out;out-test" dominio.estructuras.PruebasListaDoblementeEnlazada
 */
public class PruebasListaDoblementeEnlazada {
    private static int superadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        prueba("lista nueva está vacía", PruebasListaDoblementeEnlazada::listaVacia);
        prueba("crear: insertarAlInicio / insertarAlFinal", PruebasListaDoblementeEnlazada::insertarExtremos);
        prueba("crear: insertarEn al medio enlaza 4 punteros", PruebasListaDoblementeEnlazada::insertarEnMedio);
        prueba("consultar: obtener desde el extremo más cercano", PruebasListaDoblementeEnlazada::obtenerPorIndice);
        prueba("consultar: indiceDe y contiene", PruebasListaDoblementeEnlazada::buscar);
        prueba("editar: modificar y modificarUltimo", PruebasListaDoblementeEnlazada::editar);
        prueba("eliminar: extremos hasta vaciar", PruebasListaDoblementeEnlazada::eliminarExtremos);
        prueba("eliminar: eliminarEn y eliminar(dato) en el medio", PruebasListaDoblementeEnlazada::eliminarMedio);
        prueba("eliminar: vaciar", PruebasListaDoblementeEnlazada::vaciar);
        prueba("precondiciones: vacía, índices y null", PruebasListaDoblementeEnlazada::precondiciones);
        prueba("recorrer: iterador e iterador inverso", PruebasListaDoblementeEnlazada::iteradores);
        prueba("recorrer: iterador fail-fast", PruebasListaDoblementeEnlazada::failFast);
        prueba("historial: registrar, marcar jaque y deshacer", PruebasListaDoblementeEnlazada::historial);
        prueba("partida: captura, regresar movimiento y repone pieza", PruebasListaDoblementeEnlazada::partidaConCaptura);
        prueba("partida: jaque mate del pastor queda anotado con #", PruebasListaDoblementeEnlazada::partidaJaqueMate);

        System.out.println();
        System.out.println(superadas + " superadas, " + fallidas + " fallidas");
        if (fallidas > 0) System.exit(1);
    }

    // ------------------------------------------------------------------ Pruebas

    private static void listaVacia() {
        ListaDoblementeEnlazada<String> l = new ListaDoblementeEnlazada<>();
        verificar(l.estaVacia() && l.tamanio() == 0, "debe estar vacía");
        verificar(l.verificarInvariantes(), "invariantes");
        verificar(l.toString().equals("[]"), "toString vacío");
    }

    private static void insertarExtremos() {
        ListaDoblementeEnlazada<String> l = new ListaDoblementeEnlazada<>();
        l.insertarAlFinal("B");
        verificar(l.verificarInvariantes() && l.obtenerPrimero().equals("B") && l.obtenerUltimo().equals("B"), "un nodo: cabeza == cola");
        l.insertarAlInicio("A");
        l.insertarAlFinal("C");
        verificar(l.verificarInvariantes(), "invariantes");
        igual("[A <-> B <-> C]", l.toString());
    }

    private static void insertarEnMedio() {
        ListaDoblementeEnlazada<String> l = lista("A", "C", "E");
        l.insertarEn(1, "B");
        l.insertarEn(3, "D");
        l.insertarEn(0, "inicio");
        l.insertarEn(l.tamanio(), "fin");
        verificar(l.verificarInvariantes(), "invariantes");
        igual("[inicio <-> A <-> B <-> C <-> D <-> E <-> fin]", l.toString());
    }

    private static void obtenerPorIndice() {
        ListaDoblementeEnlazada<Integer> l = new ListaDoblementeEnlazada<>();
        for (int i = 0; i < 9; i++) l.insertarAlFinal(i * 10);
        for (int i = 0; i < 9; i++) verificar(l.obtener(i) == i * 10, "obtener(" + i + ")");
    }

    private static void buscar() {
        ListaDoblementeEnlazada<String> l = lista("A", "B", "C", "B");
        verificar(l.indiceDe("B") == 1, "primera aparición de B");
        verificar(l.indiceDe("Z") == -1, "Z no está");
        verificar(l.contiene("C") && !l.contiene("Z"), "contiene");
    }

    private static void editar() {
        ListaDoblementeEnlazada<String> l = lista("A", "B", "C");
        igual("B", l.modificar(1, "b"));
        igual("C", l.modificarUltimo("c"));
        verificar(l.verificarInvariantes() && l.tamanio() == 3, "editar no cambia la estructura");
        igual("[A <-> b <-> c]", l.toString());
    }

    private static void eliminarExtremos() {
        ListaDoblementeEnlazada<String> l = lista("A", "B", "C");
        igual("A", l.eliminarPrimero());
        verificar(l.verificarInvariantes(), "invariantes tras eliminarPrimero");
        igual("C", l.eliminarUltimo());
        verificar(l.verificarInvariantes(), "invariantes tras eliminarUltimo");
        igual("B", l.eliminarUltimo());
        verificar(l.estaVacia() && l.verificarInvariantes(), "queda vacía y consistente");
    }

    private static void eliminarMedio() {
        ListaDoblementeEnlazada<String> l = lista("A", "B", "C", "D", "E");
        igual("C", l.eliminarEn(2));
        verificar(l.eliminar("D"), "elimina D");
        verificar(!l.eliminar("Z"), "Z no existe");
        verificar(l.verificarInvariantes(), "invariantes");
        igual("[A <-> B <-> E]", l.toString());
    }

    private static void vaciar() {
        ListaDoblementeEnlazada<String> l = lista("A", "B", "C");
        l.vaciar();
        verificar(l.estaVacia() && l.verificarInvariantes() && !l.iterator().hasNext(), "vacía tras vaciar");
        l.insertarAlFinal("X");
        verificar(l.verificarInvariantes() && l.tamanio() == 1, "reutilizable tras vaciar");
    }

    private static void precondiciones() {
        ListaDoblementeEnlazada<String> l = new ListaDoblementeEnlazada<>();
        lanza(NoSuchElementException.class, l::eliminarUltimo);
        lanza(NoSuchElementException.class, l::obtenerPrimero);
        lanza(NoSuchElementException.class, () -> l.modificarUltimo("X"));
        lanza(IndexOutOfBoundsException.class, () -> l.obtener(0));
        lanza(IndexOutOfBoundsException.class, () -> l.insertarEn(1, "X"));
        lanza(NullPointerException.class, () -> l.insertarAlFinal(null));
        verificar(l.verificarInvariantes(), "una operación rechazada no corrompe la lista");
    }

    private static void iteradores() {
        ListaDoblementeEnlazada<String> l = lista("A", "B", "C");
        StringBuilder adelante = new StringBuilder();
        for (String s : l) adelante.append(s);
        StringBuilder atras = new StringBuilder();
        for (Iterator<String> it = l.iteradorInverso(); it.hasNext(); ) atras.append(it.next());
        igual("ABC", adelante.toString());
        igual("CBA", atras.toString());
    }

    private static void failFast() {
        ListaDoblementeEnlazada<String> l = lista("A", "B", "C");
        lanza(ConcurrentModificationException.class, () -> {
            for (String s : l) {
                if (s.equals("A")) l.insertarAlFinal("D");
            }
        });
    }

    private static void historial() {
        Partida p = partida();
        HistorialMovimientos h = new HistorialMovimientos();
        Movimiento m = new Movimiento(new Posicion("e2"), new Posicion("e4"), p.getTablero().getPieza(new Posicion("e2")), null);
        h.registrar(m);
        h.marcarUltimo(EstadoMovimiento.JAQUE);
        igual("e2-e4+", h.getUltimo().toNotacionCompleta());
        igual("e2-e4", h.getUltimo().toNotacion());
        verificar(h.deshacerUltimo() != null && h.estaVacio(), "deshacer deja el historial vacío");
        verificar(h.deshacerUltimo() == null, "deshacer en vacío devuelve null");
    }

    private static void partidaConCaptura() {
        Partida p = partida();
        mover(p, "e2", "e4");
        mover(p, "d7", "d5");
        mover(p, "e4", "d5");
        igual("e2-e4,d7-d5,e4-d5", String.join(",", p.getMovimientos()));
        igual("e4xd5", p.getHistorial().getUltimo().toNotacionCompleta());
        verificar(p.retrocederMovimiento(Color.BLANCO), "blancas regresan su jugada");
        verificar(p.getTablero().getPieza(new Posicion("d5")).getColor() == Color.NEGRO, "peón negro repuesto en d5");
        verificar(p.getTablero().getPieza(new Posicion("e4")).getColor() == Color.BLANCO, "peón blanco vuelve a e4");
        verificar(p.getHistorial().getTamanio() == 2 && p.getTurnoActual() == Color.BLANCO, "tamaño y turno");
    }

    private static void partidaJaqueMate() {
        Partida p = partida();
        mover(p, "e2", "e4");
        mover(p, "e7", "e5");
        mover(p, "f1", "c4");
        mover(p, "b8", "c6");
        mover(p, "d1", "h5");
        mover(p, "g8", "f6");
        mover(p, "h5", "f7");
        igual("h5xf7#", p.getHistorial().getUltimo().toNotacionCompleta());
        igual("d1-h5", p.getHistorial().getMovimiento(5).toNotacionCompleta());
    }

    // ------------------------------------------------------------------ Utilidades

    private static Partida partida() {
        return new Partida(new Usuario("a", "A", "A", "x"), new Usuario("b", "B", "B", "x"));
    }

    private static void mover(Partida p, String origen, String destino) {
        verificar(p.realizarMovimiento(new Posicion(origen), new Posicion(destino)), "movimiento " + origen + "-" + destino);
    }

    @SafeVarargs
    private static <T> ListaDoblementeEnlazada<T> lista(T... datos) {
        ListaDoblementeEnlazada<T> l = new ListaDoblementeEnlazada<>();
        for (T d : datos) l.insertarAlFinal(d);
        return l;
    }

    private static void prueba(String nombre, Runnable cuerpo) {
        try {
            cuerpo.run();
            superadas++;
            System.out.println("OK    " + nombre);
        } catch (Throwable e) {
            fallidas++;
            System.out.println("FALLA " + nombre + " -> " + e);
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static void igual(Object esperado, Object obtenido) {
        if (!esperado.equals(obtenido)) throw new AssertionError("esperado " + esperado + " pero fue " + obtenido);
    }

    private static void lanza(Class<? extends Throwable> tipo, Runnable accion) {
        try {
            accion.run();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) return;
            throw new AssertionError("esperaba " + tipo.getSimpleName() + " pero lanzó " + e);
        }
        throw new AssertionError("esperaba " + tipo.getSimpleName());
    }
}
