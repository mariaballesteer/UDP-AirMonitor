package ppc.java.red;

public final class Protocolo {

    private Protocolo() {
    }

    /** Puerto al que los servidores difunden (broadcast) los datos. */
    public static final int PUERTO_DIFUSION = 5000;

    /** Direccion de broadcast por defecto (configurable). */
    public static final String DIR_BROADCAST = "255.255.255.255";

    /** Puerto base de control. El servidor i escucha en PUERTO_CONTROL_BASE + i. */
    public static final int PUERTO_CONTROL_BASE = 6000;

    /** Tamano del buffer de recepcion de datagramas. */
    public static final int TAM_BUFFER = 16384;

    /** Tiempo de espera (ms) del cliente por la respuesta de control. */
    public static final int TIMEOUT_CONTROL_MS = 2000;

    /** Numero de reintentos de un mensaje de control (gestion de perdidas). */
    public static final int REINTENTOS_CONTROL = 3;
}
