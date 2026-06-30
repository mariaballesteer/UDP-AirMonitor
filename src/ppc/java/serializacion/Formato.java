package ppc.java.serializacion;

/**
 * Formato de serializacion. Su codigo de un caracter viaja en la
 * cabecera del protocolo para indicar como van codificados los datos.
 */
public enum Formato {
    XML('X', "xml"),
    JSON('J', "json");

    private final char codigo;
    private final String extension;

    Formato(char codigo, String extension) {
        this.codigo = codigo;
        this.extension = extension;
    }

    public char getCodigo() {
        return codigo;
    }

    public String getExtension() {
        return extension;
    }

    public static Formato desdeCodigo(char c) {
        for (Formato f : values()) {
            if (f.codigo == c) {
                return f;
            }
        }
        throw new IllegalArgumentException("Codigo de formato desconocido: " + c);
    }
}
