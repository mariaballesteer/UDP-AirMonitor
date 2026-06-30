package ppc.java.modelo;

/**
 * Representa una variable (temperatura, humedad, PM10, etc.) con su
 * valor actual y la unidad en que se expresa.
 */
public class Variable {

    private String nombre;
    private double valor;
    private String unidad;

    /** Constructor sin argumentos (necesario para GSON). */
    public Variable() {
    }

    public Variable(String nombre, double valor, String unidad) {
        this.nombre = nombre;
        this.valor = valor;
        this.unidad = unidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    @Override
    public String toString() {
        return String.format("%s = %.2f %s", nombre, valor, unidad);
    }
}
