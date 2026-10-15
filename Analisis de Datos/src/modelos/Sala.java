package modelos;

public class Sala {
    private String numSala;
    private int capacidad;
    private String ubicacion;

    public Sala() {}

    public Sala(String numSala, int capacidad, String ubicacion) {
        this.numSala = numSala;
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
    }

    public String getNumSala() { return numSala; }
    public void setNumSala(String numSala) { this.numSala = numSala; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    @Override
    public String toString() { return numSala; }
}
