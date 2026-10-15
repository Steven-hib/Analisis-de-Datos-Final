package modelos;

public class Portatil {
    private String numEquipo;
    private String estado; // "Disponible", "En Préstamo", "Mantenimiento"

    public Portatil() {}

    public Portatil(String numEquipo, String estado) {
        this.numEquipo = numEquipo;
        this.estado = estado;
    }

    public String getNumEquipo() { return numEquipo; }
    public void setNumEquipo(String numEquipo) { this.numEquipo = numEquipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() { return numEquipo + " (" + estado + ")"; }
}
