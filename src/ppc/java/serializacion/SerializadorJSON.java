package ppc.java.serializacion;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import ppc.java.modelo.Mensaje;
import ppc.java.modelo.MensajeControl;
import ppc.java.modelo.MensajeDistribucion;
import ppc.java.modelo.TipoMensaje;

/**
 * Serializador/deserializador JSON basado en la libreria GSON.
 */
public class SerializadorJSON implements ISerializador {

    private final Gson gson;

    public SerializadorJSON() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public Formato getFormato() {
        return Formato.JSON;
    }

    @Override
    public byte[] serializar(Mensaje mensaje) throws SerializacionException {
        try {
            String json = gson.toJson(mensaje);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SerializacionException("Error serializando a JSON", e);
        }
    }

    @Override
    public Mensaje deserializar(byte[] datos, TipoMensaje tipo) throws SerializacionException {
        String json = new String(datos, StandardCharsets.UTF_8);
        try {
            if (tipo == TipoMensaje.DISTRIBUCION) {
                return gson.fromJson(json, MensajeDistribucion.class);
            } else {
                return gson.fromJson(json, MensajeControl.class);
            }
        } catch (JsonSyntaxException e) {
            throw new SerializacionException("JSON mal formado: " + e.getMessage(), e);
        }
    }
}
