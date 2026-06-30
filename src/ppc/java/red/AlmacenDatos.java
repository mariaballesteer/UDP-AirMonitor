package ppc.java.red;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ppc.java.serializacion.Formato;

/**
 * Almacena en archivos las trazas de la informacion transmitida y recibida
 * (XML o JSON) para su posterior verificacion.
 */
public class AlmacenDatos {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Path directorio;

    public AlmacenDatos(String subdirectorio) {
        this.directorio = Paths.get("logs", subdirectorio);
        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            System.err.println("No se pudo crear el directorio de logs: " + e.getMessage());
        }
    }

    /**
     * Anade la carga a su fichero de historial correspondiente. La
     * extension del fichero (.xml / .json) refleja el formato.
     *
     * @return la ruta del fichero de historial, o null si falla.
     */
    public synchronized Path registrar(byte[] cargaUtil, String etiqueta, Formato formato) {
        Path destino = directorio.resolve("historial_" + etiqueta + "." + formato.getExtension());
        String cabecera = "===== " + etiqueta + " [" + formato + "] " + LocalDateTime.now().format(FMT)
                + " =====" + System.lineSeparator();
        try (OutputStream os = Files.newOutputStream(destino,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            os.write(cabecera.getBytes(StandardCharsets.UTF_8));
            os.write(cargaUtil);
            os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            return destino;
        } catch (IOException e) {
            System.err.println("Error guardando traza en " + destino + ": " + e.getMessage());
            return null;
        }
    }
}
