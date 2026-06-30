package ppc.java.modelo;

/**
 * Mensaje de control: lo envia un cliente a un servidor concreto (unicast).
 */
public class MensajeControl extends Mensaje {

    private String idCliente;
    private String idServidor;     // servidor destino
    private TipoControl tipo;
    private String variable;       // para CAMBIO_UNIDADES
    private String valor;          // nuevo valor (XML/JSON, Hz, unidad, ON/OFF)
    private String respuesta;      // lo rellena el servidor en la contestacion

    /** Constructor sin argumentos (necesario para GSON). */
    public MensajeControl() {
    }

    public MensajeControl(String idCliente, String idServidor, TipoControl tipo) {
        this.idCliente = idCliente;
        this.idServidor = idServidor;
        this.tipo = tipo;
    }

    @Override
    public TipoMensaje getTipoMensaje() {
        return TipoMensaje.CONTROL;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdServidor() {
        return idServidor;
    }

    public void setIdServidor(String idServidor) {
        this.idServidor = idServidor;
    }

    public TipoControl getTipo() {
        return tipo;
    }

    public void setTipo(TipoControl tipo) {
        this.tipo = tipo;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[CONTROL] cliente=").append(idCliente)
          .append(" servidor=").append(idServidor)
          .append(" tipo=").append(tipo);
        if (variable != null) {
            sb.append(" variable=").append(variable);
        }
        if (valor != null) {
            sb.append(" valor=").append(valor);
        }
        if (respuesta != null) {
            sb.append(" respuesta=\"").append(respuesta).append('"');
        }
        return sb.toString();
    }
}
