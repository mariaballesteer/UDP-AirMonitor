package ppc.java.app;

import java.util.Scanner;

import ppc.java.modelo.MensajeControl;
import ppc.java.modelo.TipoControl;
import ppc.java.red.Cliente;
import ppc.java.red.Protocolo;
import ppc.java.serializacion.Formato;
import ppc.java.serializacion.SerializadorFactory;

/**
 * Lanzador del cliente.
 * Muestra por consola las tramas de distribucion recibidas y ofrece dos menus
 * para enviar mensajes de control a un servidor concreto.
 * 
 * Menu 1:
 *   1) Enviar mensaje de control
 *   2) Salir
 *   
 * Menu 2 (cuando se elige enviar mensaje de control):
 *  1) Cambiar codificacion (XML/JSON)
 *  2) Cambiar frecuencia (Hz)
 *  3) Cambiar unidades de una variable
 *  4) Activar / Desactivar envio
 * 
 */
public class MainCliente {

    private static String host = "localhost";

    public static void main(String[] args) throws Exception {
        String id = (args.length >= 1) ? args[0] : "cli1";
        if (args.length >= 2) {
            host = args[1];
        }

        SerializadorFactory factory = new SerializadorFactory();
        Cliente cliente = new Cliente(id, factory);
        cliente.arrancar();

        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENU CLIENTE (" + id + ") ===");
            System.out.println("1) Enviar mensaje de control");
            System.out.println("2) Salir");
            System.out.print("> ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    enviarControl(cliente, sc, id);
                    break;
                case "2":
                    salir = true;
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        }
        cliente.parar();
        System.out.println("Cliente finalizado.");
        System.exit(0);
    }

    private static void enviarControl(Cliente cliente, Scanner sc, String idCliente) {
        cliente.menuActivo = true;
    	// Servidor destino
        System.out.print("Servidor destino (1..3): ");
        int num;
        try {
            num = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Numero no valido.");
            return;
        }
        int puerto = Protocolo.PUERTO_CONTROL_BASE + num;
        String idServidor = "srv" + num;

        // Tipo de control
        System.out.println("Tipo de control:");
        System.out.println("  1) Cambiar codificacion (XML/JSON)");
        System.out.println("  2) Cambiar frecuencia (Hz)");
        System.out.println("  3) Cambiar unidades de una variable");
        System.out.println("  4) Activar / Desactivar envio");
        System.out.print("> ");
        String t = sc.nextLine().trim();

        MensajeControl m = new MensajeControl(idCliente, idServidor, null);
        switch (t) {
            case "1":
                m.setTipo(TipoControl.CAMBIO_CODIFICACION);
                System.out.print("Nueva codificacion (XML/JSON): ");
                m.setValor(sc.nextLine().trim().toUpperCase());
                break;
            case "2":
                m.setTipo(TipoControl.CAMBIO_FRECUENCIA);
                System.out.print("Nueva frecuencia en Hz (p.e. 2): ");
                m.setValor(sc.nextLine().trim());
                break;
            case "3":
                m.setTipo(TipoControl.CAMBIO_UNIDADES);
                System.out.print("Nombre de la variable (p.e. temperatura): ");
                m.setVariable(sc.nextLine().trim());
                System.out.print("Nueva unidad (p.e. F): ");
                m.setValor(sc.nextLine().trim());
                break;
            case "4":
                m.setTipo(TipoControl.ACTIVAR_DESACTIVAR);
                System.out.print("ON / OFF: ");
                m.setValor(sc.nextLine().trim().toUpperCase());
                break;
            default:
                System.out.println("Tipo no valido.");
                return;
        }

        // Formato en que se envia la peticion
        System.out.print("Formato de la peticion (XML/JSON) [XML]: ");
        String f = sc.nextLine().trim().toUpperCase();
        Formato formato = f.equals("JSON") ? Formato.JSON : Formato.XML;

        // Enviar y esperar respuesta
        cliente.menuActivo = false;
        MensajeControl respuesta = cliente.enviarControl(m, host, puerto, formato);
        if (respuesta != null) {
            System.out.println("RESPUESTA del servidor: " + respuesta.getRespuesta());
        } else {
            System.out.println("No se recibio respuesta del servidor.");
        }
    }
}
