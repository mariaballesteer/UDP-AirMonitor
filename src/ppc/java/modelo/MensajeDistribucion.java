package ppc.java.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Mensaje de distribucion: lo emiten los servidores en modo broadcast con un
 * conjunto de variables (minimo tres) y sus valores actuales.
 */
public class MensajeDistribucion extends Mensaje {

    private String idServidor;
    private long marcaTiempo;            // milisegundos desde epoch
    private List<Variable> variables = new ArrayList<>();

    /** Constructor sin argumentos (necesario para GSON). */
    public MensajeDistribucion() {
    }

    public MensajeDistribucion(String idServidor, long marcaTiempo) {
        this.idServidor = idServidor;
        this.marcaTiempo = marcaTiempo;
    }

    @Override
    public TipoMensaje getTipoMensaje() {
        return TipoMensaje.DISTRIBUCION;
    }

    public String getIdServidor() {
        return idServidor;
    }

    public void setIdServidor(String idServidor) {
        this.idServidor = idServidor;
    }

    public long getMarcaTiempo() {
        return marcaTiempo;
    }

    public void setMarcaTiempo(long marcaTiempo) {
        this.marcaTiempo = marcaTiempo;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public void addVariable(Variable v) {
        this.variables.add(v);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[DISTRIBUCION] servidor=").append(idServidor)
          .append(" t=").append(marcaTiempo).append('\n');
        for (Variable v : variables) {
            sb.append("    ").append(v).append('\n');
        }
        return sb.toString();
    }
}
