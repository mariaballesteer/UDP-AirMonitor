package ppc.java.red;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import ppc.java.modelo.MensajeControl;
import ppc.java.modelo.MensajeDistribucion;
import ppc.java.serializacion.Formato;
import ppc.java.serializacion.SerializacionException;
import ppc.java.serializacion.SerializadorFactory;

/**
 * Cliente del servicio. Recibe las tramas de distribucion (broadcast) y las
 * muestra por consola, y permite enviar mensajes de control unicast a un
 * servidor concreto, esperando su respuesta (con reintentos).
 */
public class Cliente {

    private final String id;
    private final Empaquetador empaquetador;
    private final AlmacenDatos almacen;

    private DatagramSocket socketDifusion;
    private DatagramSocket socketControl;
    private volatile boolean ejecutando = true;
    public volatile boolean menuActivo = false;

    public Cliente(String id, SerializadorFactory factory) throws Exception {
        this.id = id;
        this.empaquetador = new Empaquetador(factory);
        this.almacen = new AlmacenDatos("cliente_" + id);
    }

    public void arrancar() throws Exception {
        // Socket de difusion: reuseAddress permite varios clientes en el mismo equipo
        socketDifusion = new DatagramSocket(null);
        socketDifusion.setReuseAddress(true);
        socketDifusion.bind(new InetSocketAddress(Protocolo.PUERTO_DIFUSION));

        // Socket de control: puerto por el se reciben las respuestas
        socketControl = new DatagramSocket();
        socketControl.setSoTimeout(Protocolo.TIMEOUT_CONTROL_MS);

        System.out.printf("[%s] Cliente arrancado. Escuchando difusion en puerto %d%n",
                id, Protocolo.PUERTO_DIFUSION);

        Thread hilo = new Thread(this::bucleDifusion, "difusion-cliente-" + id);
        hilo.setDaemon(true);
        hilo.start();
    }

    // Recepcion de tramas de distribucion
    private void bucleDifusion() {
        byte[] buffer = new byte[Protocolo.TAM_BUFFER];
        while (ejecutando) {
            try {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                socketDifusion.receive(p);

                Empaquetador.Paquete paq = empaquetador.desempaquetar(p.getData(), p.getLength());
                almacen.registrar(paq.cargaUtil, "difusion_rx", paq.cabecera.getFormato());

                if (paq.mensaje instanceof MensajeDistribucion && !menuActivo) {
                    boolean esXml = paq.cabecera.getFormato() == ppc.java.serializacion.Formato.XML;
                    String validacion = esXml ? " - Mensaje XML validado correctamente" : "";
                    System.out.printf("%n--- Trama recibida (%s)%s ---%n%s",
                            paq.cabecera.getFormato(), validacion, paq.mensaje);
                    System.out.print("> ");
                }
            } catch (SerializacionException e) {
                System.err.printf("[%s] Trama descartada (invalida): %s%n", id, e.getMessage());
            } catch (Exception e) {
                if (ejecutando) {
                    System.err.printf("[%s] Error recibiendo difusion: %s%n", id, e.getMessage());
                }
            }
        }
    }

    /*
     * Envia un mensaje de control a un servidor y espera su respuesta.
     * Reintenta hasta 3 veces si no llega la respuesta dentro del tiempo.
     */
    public MensajeControl enviarControl(MensajeControl peticion, String host, int puerto,
                                        Formato formato) {
        try {
            Empaquetador.Salida sal = empaquetador.empaquetar(peticion, formato);
            byte[] datagrama = sal.datagrama;
            InetAddress destino = InetAddress.getByName(host);
            byte[] buffer = new byte[Protocolo.TAM_BUFFER];
            almacen.registrar(sal.cargaUtil, "control_tx", formato);

            for (int intento = 1; intento <= Protocolo.REINTENTOS_CONTROL; intento++) {
                DatagramPacket envio = new DatagramPacket(datagrama, datagrama.length,
                        destino, puerto);
                socketControl.send(envio);
                System.out.printf("[%s] Control enviado a %s:%d (%s) [intento %d/%d]%n",
                        id, host, puerto, formato, intento, Protocolo.REINTENTOS_CONTROL);

                try {
                    DatagramPacket resp = new DatagramPacket(buffer, buffer.length);
                    socketControl.receive(resp);
                    Empaquetador.Paquete paq = empaquetador.desempaquetar(
                            resp.getData(), resp.getLength());
                    almacen.registrar(paq.cargaUtil, "respuesta_rx", paq.cabecera.getFormato());
                    if (paq.mensaje instanceof MensajeControl) {
                        return (MensajeControl) paq.mensaje;
                    }
                } catch (SocketTimeoutException te) {
                    System.out.printf("[%s] Sin respuesta, reintentando...%n", id);
                }
            }
            System.out.printf("[%s] No se obtuvo respuesta tras %d intentos.%n",
                    id, Protocolo.REINTENTOS_CONTROL);
            return null;
        } catch (Exception e) {
            System.err.printf("[%s] Error enviando control: %s%n", id, e.getMessage());
            return null;
        }
    }

    public void parar() {
        ejecutando = false;
        if (socketDifusion != null) socketDifusion.close();
        if (socketControl != null) socketControl.close();
    }

    public String getId() {
        return id;
    }
}
