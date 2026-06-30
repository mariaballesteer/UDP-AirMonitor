package ppc.java.red;

import ppc.java.modelo.TipoMensaje;
import ppc.java.serializacion.Formato;

/**
 * Cabecera del protocolo de aplicacion. Ocupa 2 bytes al inicio de cada
 * datagrama:
 * byte 0: tipo de mensaje ('D' distribucion, 'C' control)
 * byte 1: formato de serializacion ('X' XML, 'J' JSON)
 */
public class Cabecera {

    public static final int LONGITUD = 2;

    private final TipoMensaje tipoMensaje;
    private final Formato formato;

    public Cabecera(TipoMensaje tipoMensaje, Formato formato) {
        this.tipoMensaje = tipoMensaje;
        this.formato = formato;
    }

    public TipoMensaje getTipoMensaje() {
        return tipoMensaje;
    }

    public Formato getFormato() {
        return formato;
    }

    public byte[] aBytes() {
        return new byte[] {
                (byte) tipoMensaje.getCodigo(),
                (byte) formato.getCodigo()
        };
    }

    public static Cabecera desdeBytes(byte[] datos, int offset) {
        char tipo = (char) datos[offset];
        char fmt = (char) datos[offset + 1];
        return new Cabecera(TipoMensaje.desdeCodigo(tipo), Formato.desdeCodigo(fmt));
    }

    @Override
    public String toString() {
        return "Cabecera{tipo=" + tipoMensaje + ", formato=" + formato + '}';
    }
}
