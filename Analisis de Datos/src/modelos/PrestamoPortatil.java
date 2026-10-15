package modelos;

import java.time.LocalDateTime;

public class PrestamoPortatil {
    private int id;
    private Usuario usuario;
    private Ubicacion ubicacion;
    private Portatil portatil;
    private String celular;
    private String auxiliar;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaDevolucion;
    private String estado;

    public PrestamoPortatil() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }
    public Portatil getPortatil() { return portatil; }
    public void setPortatil(Portatil portatil) { this.portatil = portatil; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getAuxiliar() { return auxiliar; }
    public void setAuxiliar(String auxiliar) { this.auxiliar = auxiliar; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
