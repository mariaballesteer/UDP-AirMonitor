package ppc.java.red;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ppc.java.modelo.MensajeControl;
import ppc.java.modelo.MensajeDistribucion;
import ppc.java.modelo.Variable;
import ppc.java.serializacion.Formato;
import ppc.java.serializacion.SerializacionException;
import ppc.java.serializacion.SerializadorFactory;

/**
 * Servidor del servicio de distribucion. Difunde periodicamente (broadcast)
 * mensajes con valores de sus variables y atiende mensajes de control
 * unicast dirigidos a el, respondiendo unicamente al cliente que los envia.
 */
public class Servidor {

    /* Sensor simulado: genera valores aleatorios dentro de un rango. */
    static class Sensor {
        String nombre;
        String unidad;
        double min;
        double max;

        Sensor(String nombre, String unidad, double min, double max) {
            this.nombre = nombre;
            this.unidad = unidad;
            this.min = min;
            this.max = max;
        }

        double generar(Random r) {
            return min + r.nextDouble() * (max - min);
        }
    }

    private final String id;
    private final int puertoControl;
    private final InetAddress dirBroadcast;
    private final List<Sensor> sensores = new ArrayList<>();
    private final Random random = new Random();

    private final Empaquetador empaquetador;
    private final AlmacenDatos almacen;

    private volatile Formato formato = Formato.XML;
    private volatile long periodoMs = 2000;   // 0.5 Hz por defecto
    private volatile boolean activo = true;
    private volatile boolean ejecutando = true;

    private DatagramSocket socketDifusion;
    private DatagramSocket socketControl;

    public Servidor(String id, int puertoControl, String dirBroadcast,
                    SerializadorFactory factory) throws Exception {
        this.id = id;
        this.puertoControl = puertoControl;
        this.dirBroadcast = InetAddress.getByName(dirBroadcast);
        this.empaquetador = new Empaquetador(factory);
        this.almacen = new AlmacenDatos("servidor_" + id);
    }

    public void anadirSensor(String nombre, String unidad, double min, double max) {
        sensores.add(new Sensor(nombre, unidad, min, max));
    }

    public void arrancar() throws Exception {
        socketDifusion = new DatagramSocket();
        socketDifusion.setBroadcast(true);

        socketControl = new DatagramSocket(puertoControl);

        System.out.printf("[%s] Servidor arrancado. Control en puerto %d. Difusion a %s:%d (%s, %.1f Hz)%n",
                id, puertoControl, dirBroadcast.getHostAddress(), Protocolo.PUERTO_DIFUSION,
                formato, 1000.0 / periodoMs);

        Thread hiloDifusion = new Thread(this::bucleDifusion, "difusion-" + id);
        Thread hiloControl = new Thread(this::bucleControl, "control-" + id);
        hiloDifusion.start();
        hiloControl.start();
    }

    // Hilo de difusion
    private void bucleDifusion() {
        while (ejecutando) {
            try {
                if (activo) {
                    MensajeDistribucion m = construirMensaje();
                    Formato f = this.formato;
                    Empaquetador.Salida sal = empaquetador.empaquetar(m, f);
                    DatagramPacket p = new DatagramPacket(sal.datagrama, sal.datagrama.length,
                            dirBroadcast, Protocolo.PUERTO_DIFUSION);
                    socketDifusion.send(p);
                    almacen.registrar(sal.cargaUtil, "difusion_tx", f);
                }
                Thread.sleep(periodoMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.printf("[%s] Error en difusion: %s%n", id, e.getMessage());
            }
        }
    }

    private synchronized MensajeDistribucion construirMensaje() {
        MensajeDistribucion m = new MensajeDistribucion(id, System.currentTimeMillis());
        for (Sensor s : sensores) {
            m.addVariable(new Variable(s.nombre, s.generar(random), s.unidad));
        }
        return m;
    }

    // Hilo de control
    private void bucleControl() {
        byte[] buffer = new byte[Protocolo.TAM_BUFFER];
        while (ejecutando) {
            try {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                socketControl.receive(p);

                Empaquetador.Paquete paq = empaquetador.desempaquetar(p.getData(), p.getLength());
                // Guardar la traza de lo recibido para verificacion posterior
                almacen.registrar(paq.cargaUtil, "control_rx", paq.cabecera.getFormato());

                if (!(paq.mensaje instanceof MensajeControl)) {
                    continue;
                }
                MensajeControl peticion = (MensajeControl) paq.mensaje;
                System.out.printf("[%s] Control recibido de %s:%d -> %s%n",
                        id, p.getAddress().getHostAddress(), p.getPort(), peticion);

                String resultado = aplicarControl(peticion);

                // Construir y enviar respuesta UNICAST al cliente que pregunto
                MensajeControl respuesta = new MensajeControl(
                        peticion.getIdCliente(), id, peticion.getTipo());
                respuesta.setVariable(peticion.getVariable());
                respuesta.setValor(peticion.getValor());
                respuesta.setRespuesta(resultado);

                Formato f = this.formato; // formato actual (puede haber cambiado)
                Empaquetador.Salida sal = empaquetador.empaquetar(respuesta, f);
                DatagramPacket pr = new DatagramPacket(sal.datagrama, sal.datagrama.length,
                        p.getAddress(), p.getPort());
                socketControl.send(pr);
                almacen.registrar(sal.cargaUtil, "respuesta_tx", f);
            } catch (SerializacionException e) {
                System.err.printf("[%s] Mensaje de control descartado (invalido): %s%n",
                        id, e.getMessage());
            } catch (Exception e) {
                if (ejecutando) {
                    System.err.printf("[%s] Error en control: %s%n", id, e.getMessage());
                }
            }
        }
    }

    private synchronized String aplicarControl(MensajeControl c) {
        try {
            switch (c.getTipo()) {
                case CAMBIO_CODIFICACION: {
                    Formato nuevo = Formato.valueOf(c.getValor().trim().toUpperCase());
                    this.formato = nuevo;
                    return "OK: codificacion cambiada a " + nuevo;
                }
                case CAMBIO_FRECUENCIA: {
                    double hz = Double.parseDouble(c.getValor().trim());
                    if (hz <= 0) {
                        return "ERROR: la frecuencia debe ser positiva";
                    }
                    this.periodoMs = (long) (1000.0 / hz);
                    return "OK: frecuencia cambiada a " + hz + " Hz";
                }
                case CAMBIO_UNIDADES: {
                    return cambiarUnidad(c.getVariable(), c.getValor());
                }
                case ACTIVAR_DESACTIVAR: {
                    String v = c.getValor() == null ? "" : c.getValor().trim().toUpperCase();
                    boolean encender = v.equals("ON") || v.equals("TRUE") || v.equals("1");
                    this.activo = encender;
                    return "OK: envio de datos " + (encender ? "ACTIVADO" : "DESACTIVADO");
                }
                default:
                    return "ERROR: tipo de control no soportado";
            }
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /** Cambia la unidad de una variable; convierte el rango si es temperatura C/F. */
    private String cambiarUnidad(String nombreVar, String nuevaUnidad) {
        if (nombreVar == null || nuevaUnidad == null) {
            return "ERROR: faltan parametros variable/valor";
        }
        for (Sensor s : sensores) {
            if (s.nombre.equalsIgnoreCase(nombreVar)) {
                if (s.nombre.equalsIgnoreCase("temperatura")) {
                    convertirTemperatura(s, nuevaUnidad);
                }
                s.unidad = nuevaUnidad;
                return "OK: unidad de " + s.nombre + " cambiada a " + nuevaUnidad;
            }
        }
        return "ERROR: variable no encontrada en este servidor: " + nombreVar;
    }

    private void convertirTemperatura(Sensor s, String nuevaUnidad) {
        boolean aFahrenheit = nuevaUnidad.toUpperCase().contains("F");
        boolean enCelsius = !s.unidad.toUpperCase().contains("F");
        if (aFahrenheit && enCelsius) {
            s.min = s.min * 9 / 5 + 32;
            s.max = s.max * 9 / 5 + 32;
        } else if (!aFahrenheit && !enCelsius) {
            s.min = (s.min - 32) * 5 / 9;
            s.max = (s.max - 32) * 5 / 9;
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
