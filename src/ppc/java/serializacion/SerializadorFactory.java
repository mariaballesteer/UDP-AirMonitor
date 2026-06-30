package ppc.java.serializacion;

import java.util.EnumMap;
import java.util.Map;

public class SerializadorFactory {

    private final Map<Formato, ISerializador> serializadores = new EnumMap<>(Formato.class);

    public SerializadorFactory() throws SerializacionException {
        serializadores.put(Formato.XML, new SerializadorXML());
        serializadores.put(Formato.JSON, new SerializadorJSON());
    }

    public ISerializador get(Formato formato) {
        ISerializador s = serializadores.get(formato);
        if (s == null) {
            throw new IllegalArgumentException("Formato no soportado: " + formato);
        }
        return s;
    }
}
