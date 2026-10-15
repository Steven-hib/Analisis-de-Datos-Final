package utilidades;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/** Mantiene una copia CSV de los accesos a salas, además del registro SQLite. */
public final class RegistroPlano {
    private static final Path ARCHIVO = Paths.get("datos", "accesos_salas.csv");
    private RegistroPlano() { }

    public static void anexarAcceso(int id, LocalDateTime fecha, String documento, String usuario, String programa,
                                    String sala, String equipo, String curso, String facultad) throws IOException {
        Files.createDirectories(ARCHIVO.getParent());
        boolean encabezado = !Files.exists(ARCHIVO) || Files.size(ARCHIVO) == 0;
        try (BufferedWriter escritor = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (encabezado) escritor.write("Id,FechaHoraIngreso,Documento,Usuario,Programa,Sala,Equipo,Curso,Facultad\n");
            escritor.write(celda(String.valueOf(id)) + "," + celda(fecha.toString()) + "," + celda(documento) + ","
                    + celda(usuario) + "," + celda(programa) + "," + celda(sala) + "," + celda(equipo) + ","
                    + celda(curso) + "," + celda(facultad) + System.lineSeparator());
        }
    }

    private static String celda(String valor) {
        String seguro = valor == null ? "" : valor.replace("\"", "\"\"");
        return "\"" + seguro + "\"";
    }
}
