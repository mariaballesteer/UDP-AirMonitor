package ppc.java.app;

import ppc.java.red.Protocolo;
import ppc.java.red.Servidor;
import ppc.java.serializacion.SerializadorFactory;

/**
 * Lanza los tres servidores a la vez (cada uno en sus propios hilos).
 */
public class MainServidores {

	public static void main(String[] args) throws Exception {
		String dirBroadcast = (args.length >= 1) ? args[0] : Protocolo.DIR_BROADCAST;
		SerializadorFactory factory = new SerializadorFactory();

		System.out.println("Iniciando 3 servidores...");
		for (int i = 1; i <= 3; i++) {
			Servidor s = MainServidor.construir(i, dirBroadcast, factory);
			Runtime.getRuntime().addShutdownHook(new Thread(s::parar));
			s.arrancar();
		}
		System.out.println("Servidores 1, 2 y 3 en ejecucion.");
	}
}
