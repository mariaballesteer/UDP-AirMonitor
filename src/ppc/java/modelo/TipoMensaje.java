package ppc.java.modelo;

/**
 * Tipos de mensaje del protocolo de aplicacion.
 */
public enum TipoMensaje {
    /** Mensaje de distribucion (broadcast, servidor -> clientes). */
    DISTRIBUCION('D'),
    /** Mensaje de control (unicast, cliente -> servidor y su respuesta servidor -> cliente). */
    CONTROL('C');

    private final char codigo;

    TipoMensaje(char codigo) {
        this.codigo = codigo;
    }

    public char getCodigo() {
        return codigo;
    }

    public static TipoMensaje desdeCodigo(char c) {
        for (TipoMensaje t : values()) {
            if (t.codigo == c) {
                return t;
            }
        }
        throw new IllegalArgumentException("Codigo de tipo de mensaje desconocido: " + c);
    }
}
