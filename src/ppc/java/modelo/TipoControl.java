package ppc.java.modelo;

/**
 * Operaciones de control que un cliente puede solicitar a un servidor.
 */
public enum TipoControl {
    /** Cambiar la codificacion (XML/JSON) usada por el servidor. */
    CAMBIO_CODIFICACION,
    /** Cambiar la frecuencia de transmision (en Hz). */
    CAMBIO_FRECUENCIA,
    /** Cambiar la unidad de una variable concreta. */
    CAMBIO_UNIDADES,
    /** Activar o desactivar el envio de datos. */
    ACTIVAR_DESACTIVAR
}
