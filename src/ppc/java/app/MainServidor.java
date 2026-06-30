package ppc.java.app;

import ppc.java.red.Protocolo;
import ppc.java.red.Servidor;
import ppc.java.serializacion.SerializadorFactory;

/**
 * Lanzador de un unico servidor. Para desplegar servidores en equipos
 * distintos.
 */
public class MainServidor {

	/**
	 * Construye y configura el servidor con su conjunto de sensores. Entre los tres
	 * se cubren todas las variables y cada uno tiene al menos tres.
	 */
	public static Servidor construir(int num, String dirBroadcast, SerializadorFactory factory) throws Exception {
		String id = "srv" + num;
		int puerto = Protocolo.PUERTO_CONTROL_BASE + num;
		Servidor servidor = new Servidor(id, puerto, dirBroadcast, factory);

		switch (num) {
		case 1:
			servidor.anadirSensor("temperatura", "C", 5, 40);
			servidor.anadirSensor("humedad", "%", 20, 95);
			servidor.anadirSensor("PM10", "ug/m3", 0, 150);
			break;
		case 2:
			servidor.anadirSensor("temperatura", "C", 5, 40);
			servidor.anadirSensor("SO2", "ug/m3", 0, 350);
			servidor.anadirSensor("NO2", "ug/m3", 0, 200);
			break;
		case 3:
			servidor.anadirSensor("O3", "ug/m3", 0, 240);
			servidor.anadirSensor("humedad", "%", 20, 95);
			servidor.anadirSensor("PM10", "ug/m3", 0, 150);
			break;
		default:
			throw new IllegalArgumentException("numServidor debe estar entre 1 y 3");
		}
		return servidor;
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Uso: MainServidor <numServidor 1..3> [dirBroadcast]");
			return;
		}
		int num = Integer.parseInt(args[0]);
		String dirBroadcast = (args.length >= 2) ? args[1] : Protocolo.DIR_BROADCAST;

		SerializadorFactory factory = new SerializadorFactory();
		Servidor servidor = construir(num, dirBroadcast, factory);

		Runtime.getRuntime().addShutdownHook(new Thread(servidor::parar));
		servidor.arrancar();
		Thread.currentThread().join();
	}
}
