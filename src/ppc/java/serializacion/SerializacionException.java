package ppc.java.serializacion;

/* Excepcion lanzada cuando falla la serializacion o la deserializacion.*/
public class SerializacionException extends Exception {

    public SerializacionException(String mensaje) {
        super(mensaje);
    }

    public SerializacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
