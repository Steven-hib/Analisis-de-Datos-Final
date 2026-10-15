package modelos;
import java.time.LocalDateTime;

public class RegistroAcceso {
    private int idRegistro;
    private Usuario usuario; // Objeto de tipo Usuario
    private Sala sala;       // Objeto de tipo Sala
    private String numEquipo; // Opcional 
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraSalida; // Para los docentes
    private String cursoDocente;
    private String facultad;

    public RegistroAcceso() {}

    public RegistroAcceso(int idRegistro, Usuario usuario, Sala sala, String numEquipo, 
                          LocalDateTime fechaHoraIngreso, LocalDateTime fechaHoraSalida, String cursoDocente) {
        this.idRegistro = idRegistro;
        this.usuario = usuario;
        this.sala = sala;
        this.numEquipo = numEquipo;
        this.fechaHoraIngreso = fechaHoraIngreso;
        this.fechaHoraSalida = fechaHoraSalida;
        this.cursoDocente = cursoDocente;
    }

    public RegistroAcceso(int idRegistro, Usuario usuario, Sala sala, String numEquipo,
                          LocalDateTime fechaHoraIngreso, LocalDateTime fechaHoraSalida, String cursoDocente, String facultad) {
        this(idRegistro, usuario, sala, numEquipo, fechaHoraIngreso, fechaHoraSalida, cursoDocente);
        this.facultad = facultad;
    }

    // Getters y Setters
    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Sala getSala() { return sala; }
    public void setSala(Sala sala) { this.sala = sala; }

    public String getNumEquipo() { return numEquipo; }
    public void setNumEquipo(String numEquipo) { this.numEquipo = numEquipo; }

    public LocalDateTime getFechaHoraIngreso() { return fechaHoraIngreso; }
    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) { this.fechaHoraIngreso = fechaHoraIngreso; }

    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    public String getCursoDocente() { return cursoDocente; }
    public void setCursoDocente(String cursoDocente) { this.cursoDocente = cursoDocente; }

    public String getFacultad() { return facultad; }
    public void setFacultad(String facultad) { this.facultad = facultad; }
}
