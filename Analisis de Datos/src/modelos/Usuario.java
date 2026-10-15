package modelos;

public class Usuario {
    
    // Atributos privados 
    private String identificacion;
    private String nombres;
    private String apellidos;
    private String tipoUsuario; // "Estudiante", "Docente", "Funcionario", "Invitado", "Auxiliar_TI"
    private String programa;
    private Integer semestre; // Usamos Integer para ver si es invitado/docente
    private String jornada; // "Diurna", "Nocturna"
    private String facultadDependencia;
    private boolean estadoBloqueo;
    private boolean habilitadoPeriodo;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor con todos los parámetros
    public Usuario(String identificacion, String nombres, String apellidos, String tipoUsuario, 
                   String programa, Integer semestre, String jornada, String facultadDependencia, 
                   boolean estadoBloqueo, boolean habilitadoPeriodo) {
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.tipoUsuario = tipoUsuario;
        this.programa = programa;
        this.semestre = semestre;
        this.jornada = jornada;
        this.facultadDependencia = facultadDependencia;
        this.estadoBloqueo = estadoBloqueo;
        this.habilitadoPeriodo = habilitadoPeriodo;
    }

    // Getters y Setters
    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public String getFacultadDependencia() {
        return facultadDependencia;
    }

    public void setFacultadDependencia(String facultadDependencia) {
        this.facultadDependencia = facultadDependencia;
    }

    public boolean isEstadoBloqueo() {
        return estadoBloqueo;
    }

    public void setEstadoBloqueo(boolean estadoBloqueo) {
        this.estadoBloqueo = estadoBloqueo;
    }

    public boolean isHabilitadoPeriodo() {
        return habilitadoPeriodo;
    }

    public void setHabilitadoPeriodo(boolean habilitadoPeriodo) {
        this.habilitadoPeriodo = habilitadoPeriodo;
    }

    // Método toString para depuración y pruebas en consola
    @Override
    public String toString() {
        return "Usuario{" +
                "identificacion='" + identificacion + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", tipoUsuario='" + tipoUsuario + '\'' +
                ", estadoBloqueo=" + estadoBloqueo +
                '}';
    }
}
