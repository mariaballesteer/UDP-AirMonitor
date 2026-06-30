package ppc.java.serializacion;

import ppc.java.modelo.Mensaje;
import ppc.java.modelo.TipoMensaje;

/**
 * Interfaz comun para serializar/deserializar mensajes. 
 * Permite intercambiar la implementacion XML (DOM) o JSON (GSON).
 */
public interface ISerializador {

    /**
     * Serializa un mensaje a su representacion en bytes (UTF-8).
     */
    byte[] serializar(Mensaje mensaje) throws SerializacionException;

    /**
     * Deserializa unos bytes al mensaje concreto indicado por el tipo.
     * En el caso de XML se realiza ademas la validacion explicita contra el
     * esquema correspondiente.
     */
    Mensaje deserializar(byte[] datos, TipoMensaje tipo) throws SerializacionException;

    /*
     * Devuelve el formato implementado por este serializador.
     */
    Formato getFormato();
}
