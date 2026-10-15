package controladores;

import servicios.SistemaService;

import java.sql.SQLException;

/** Controlador del flujo de ingreso y salida de salas. */
public class ControladorSalas {
    private final SistemaService sistema;

    public ControladorSalas(SistemaService sistema) { this.sistema = sistema; }

    public int registrarIngreso(String documento, String nombreInvitado, String programaInvitado,
                                String numeroSala, String numeroEquipo, String curso, String facultad) throws SQLException {
        return sistema.registrarAcceso(documento, nombreInvitado, programaInvitado, numeroSala, numeroEquipo, curso, facultad);
    }

    public void registrarSalida(int idRegistro) throws SQLException { sistema.registrarSalidaSala(idRegistro); }
}
