package utilidades;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/** Inicializa y abre la base local SQLite del sistema. */
public final class BaseDatos {
    private static final Path CARPETA_DATOS = Paths.get("datos");
    private static final Path ARCHIVO_BD = CARPETA_DATOS.resolve("gestion_tecnologica.db");
    private static final String URL = "jdbc:sqlite:" + ARCHIVO_BD.toString().replace('\\', '/');

    private BaseDatos() { }

    public static Connection conectar() throws SQLException {
        Connection conexion = DriverManager.getConnection(URL);
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        }
        return conexion;
    }

    public static void inicializar() throws SQLException, IOException {
        Files.createDirectories(CARPETA_DATOS);
        try (Connection conexion = conectar(); Statement s = conexion.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS usuarios ("
                    + "identificacion TEXT PRIMARY KEY, nombres TEXT NOT NULL, apellidos TEXT DEFAULT '', "
                    + "tipo_usuario TEXT NOT NULL, programa TEXT DEFAULT '', semestre INTEGER, jornada TEXT DEFAULT '', "
                    + "facultad_dependencia TEXT DEFAULT '', bloqueado INTEGER NOT NULL DEFAULT 0)");
            s.execute("CREATE TABLE IF NOT EXISTS salas (num_sala TEXT PRIMARY KEY, capacidad INTEGER NOT NULL DEFAULT 30, ubicacion TEXT DEFAULT '')");
            s.execute("CREATE TABLE IF NOT EXISTS portatiles (num_equipo TEXT PRIMARY KEY, estado TEXT NOT NULL DEFAULT 'DISPONIBLE')");
            s.execute("CREATE TABLE IF NOT EXISTS ubicaciones (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL UNIQUE)");
            s.execute("CREATE TABLE IF NOT EXISTS periodos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL UNIQUE, activo INTEGER NOT NULL DEFAULT 0)");
            s.execute("CREATE UNIQUE INDEX IF NOT EXISTS un_periodo_activo ON periodos(activo) WHERE activo = 1");
            s.execute("CREATE TABLE IF NOT EXISTS habilitaciones (identificacion TEXT NOT NULL, periodo_id INTEGER NOT NULL, estado INTEGER NOT NULL DEFAULT 1, "
                    + "PRIMARY KEY(identificacion, periodo_id), FOREIGN KEY(identificacion) REFERENCES usuarios(identificacion), FOREIGN KEY(periodo_id) REFERENCES periodos(id))");
            s.execute("CREATE TABLE IF NOT EXISTS accesos (id INTEGER PRIMARY KEY AUTOINCREMENT, identificacion TEXT NOT NULL, num_sala TEXT NOT NULL, "
                    + "num_equipo TEXT, fecha_ingreso TEXT NOT NULL, fecha_salida TEXT, curso TEXT DEFAULT '', facultad TEXT DEFAULT '', "
                    + "FOREIGN KEY(identificacion) REFERENCES usuarios(identificacion), FOREIGN KEY(num_sala) REFERENCES salas(num_sala))");
            s.execute("CREATE TABLE IF NOT EXISTS prestamos (id INTEGER PRIMARY KEY AUTOINCREMENT, identificacion TEXT NOT NULL, ubicacion_id INTEGER NOT NULL, "
                    + "celular TEXT NOT NULL, num_equipo TEXT, auxiliar TEXT, fecha_solicitud TEXT NOT NULL, fecha_entrega TEXT, fecha_devolucion TEXT, "
                    + "estado TEXT NOT NULL, FOREIGN KEY(identificacion) REFERENCES usuarios(identificacion), FOREIGN KEY(ubicacion_id) REFERENCES ubicaciones(id), "
                    + "FOREIGN KEY(num_equipo) REFERENCES portatiles(num_equipo))");
            s.execute("CREATE INDEX IF NOT EXISTS idx_accesos_fecha ON accesos(fecha_ingreso)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_prestamos_fecha ON prestamos(fecha_solicitud)");
            s.execute("CREATE UNIQUE INDEX IF NOT EXISTS un_equipo_prestado ON prestamos(num_equipo) WHERE num_equipo IS NOT NULL AND estado = 'PRESTADO'");
            s.execute("INSERT OR IGNORE INTO salas(num_sala, capacidad, ubicacion) VALUES ('A101',30,'Bloque principal'),('A102',30,'Bloque principal'),('LAB-1',25,'Laboratorio')");
            s.execute("INSERT OR IGNORE INTO portatiles(num_equipo, estado) VALUES ('PORT-001','DISPONIBLE'),('PORT-002','DISPONIBLE'),('PORT-003','DISPONIBLE')");
            s.execute("INSERT OR IGNORE INTO ubicaciones(nombre) VALUES ('Biblioteca'),('Cafetería'),('Aula de clase'),('Laboratorio'),('Otro')");
        }
        asegurarPeriodoActual();
    }

    private static void asegurarPeriodoActual() throws SQLException {
        LocalDate hoy = LocalDate.now();
        String nombrePeriodo = hoy.getYear() + (hoy.getMonthValue() <= 6 ? "-1" : "-2");
        try (Connection c = conectar(); Statement s = c.createStatement()) {
            s.executeUpdate("UPDATE periodos SET activo=0 WHERE activo=1");
            try (java.sql.PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO periodos(nombre, activo) VALUES (?,1)")) {
                p.setString(1, nombrePeriodo);
                p.executeUpdate();
            }
            try (java.sql.PreparedStatement p = c.prepareStatement("UPDATE periodos SET activo=1 WHERE nombre=?")) {
                p.setString(1, nombrePeriodo);
                p.executeUpdate();
            }
        }
    }
}
