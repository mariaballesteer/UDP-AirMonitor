package ppc.java.red;

import java.util.Arrays;

import ppc.java.modelo.Mensaje;
import ppc.java.serializacion.Formato;
import ppc.java.serializacion.ISerializador;
import ppc.java.serializacion.SerializacionException;
import ppc.java.serializacion.SerializadorFactory;

/**
 * Convierte mensajes del modelo en datagramas (cabecera + carga util) y
 * viceversa, eligiendo el serializador adecuado segun el formato.
 */
public class Empaquetador {

	private final SerializadorFactory factory;

	public Empaquetador(SerializadorFactory factory) {
		this.factory = factory;
	}

	/* Resultado de desempaquetar: el mensaje y la cabecera que lo acompanaba. */
	public static class Paquete {
		public final Cabecera cabecera;
		public final Mensaje mensaje;
		public final byte[] cargaUtil; // bytes serializados (para almacenar)

		public Paquete(Cabecera cabecera, Mensaje mensaje, byte[] cargaUtil) {
			this.cabecera = cabecera;
			this.mensaje = mensaje;
			this.cargaUtil = cargaUtil;
		}
	}

	/*
	 * Resultado de empaquetar: el datagrama completo y la carga util (para trazas).
	 */
	public static class Salida {
		public final byte[] datagrama; // cabecera + carga util (lo que se envia)
		public final byte[] cargaUtil; // solo la carga serializada (para almacenar)

		public Salida(byte[] datagrama, byte[] cargaUtil) {
			this.datagrama = datagrama;
			this.cargaUtil = cargaUtil;
		}
	}

	/* Empaqueta un mensaje con el formato indicado: [cabecera 2 bytes][carga util]. */
	public Salida empaquetar(Mensaje mensaje, Formato formato) throws SerializacionException {
		ISerializador s = factory.get(formato);
		byte[] carga = s.serializar(mensaje);
		Cabecera cab = new Cabecera(mensaje.getTipoMensaje(), formato);
		byte[] cabBytes = cab.aBytes();

		byte[] datagrama = new byte[cabBytes.length + carga.length];
		System.arraycopy(cabBytes, 0, datagrama, 0, cabBytes.length);
		System.arraycopy(carga, 0, datagrama, cabBytes.length, carga.length);
		return new Salida(datagrama, carga);
	}

	/**
	 * Desempaqueta un datagrama: lee la cabecera, selecciona el serializador y
	 * deserializa (validando, en el caso de XML).
	 */
	public Paquete desempaquetar(byte[] datos, int longitud) throws SerializacionException {
		if (longitud < Cabecera.LONGITUD) {
			throw new SerializacionException("Datagrama demasiado corto: " + longitud + " bytes");
		}
		Cabecera cab = Cabecera.desdeBytes(datos, 0);
		byte[] carga = Arrays.copyOfRange(datos, Cabecera.LONGITUD, longitud);

		ISerializador s = factory.get(cab.getFormato());
		Mensaje mensaje = s.deserializar(carga, cab.getTipoMensaje());
		return new Paquete(cab, mensaje, carga);
	}
}
