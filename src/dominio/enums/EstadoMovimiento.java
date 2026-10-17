package dominio.enums;

/**
 * Efecto de un movimiento sobre el rey rival. Se anota con el sufijo de la notación algebraica.
 */
public enum EstadoMovimiento {
    NORMAL(""),
    JAQUE("+"),
    JAQUE_MATE("#");

    private final String sufijo;

    EstadoMovimiento(String sufijo) {
        this.sufijo = sufijo;
    }

    public String getSufijo() {
        return sufijo;
    }
}
