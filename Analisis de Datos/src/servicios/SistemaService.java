package servicios;

import modelos.Portatil;
import modelos.Sala;
import modelos.Ubicacion;
import modelos.Usuario;
import utilidades.BaseDatos;
import utilidades.LectorArchivos;
import utilidades.RegistroPlano;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Reglas de negocio y consultas del sistema. */
public class SistemaService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Usuario buscarUsuario(String identificacion) throws SQLException {
        try (Connection c = BaseDatos.conectar(); PreparedStatement p = c.prepareStatement(
                "SELECT * FROM usuarios WHERE identificacion=?")) {
            p.setString(1, limpiar(identificacion));
            try (ResultSet r = p.executeQuery()) { return r.next() ? leerUsuario(r) : null; }
        }
    }

    public List<Usuario> listarUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = BaseDatos.conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM usuarios ORDER BY nombres, apellidos")) {
            while (r.next()) lista.add(leerUsuario(r));
        }
        return lista;
    }

    public void guardarUsuario(Usuario u) throws SQLException {
        validarUsuario(u);
        try (Connection c = BaseDatos.conectar()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("INSERT INTO usuarios(identificacion,nombres,apellidos,tipo_usuario,programa,semestre,jornada,facultad_dependencia,bloqueado) "
                    + "VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT(identificacion) DO UPDATE SET nombres=excluded.nombres, apellidos=excluded.apellidos, "
                    + "tipo_usuario=excluded.tipo_usuario, programa=excluded.programa, semestre=excluded.semestre, jornada=excluded.jornada, "
                    + "facultad_dependencia=excluded.facultad_dependencia, bloqueado=excluded.bloqueado")) {
                p.setString(1, limpiar(u.getIdentificacion()));
                p.setString(2, limpiar(u.getNombres()));
                p.setString(3, vacio(u.getApellidos()));
                p.setString(4, limpiar(u.getTipoUsuario()));
                p.setString(5, vacio(u.getPrograma()));
                if (u.getSemestre() == null) p.setNull(6, java.sql.Types.INTEGER); else p.setInt(6, u.getSemestre());
                p.setString(7, vacio(u.getJornada()));
                p.setString(8, vacio(u.getFacultadDependencia()));
                p.setInt(9, u.isEstadoBloqueo() ? 1 : 0);
                p.executeUpdate();
            }
            int periodo = obtenerPeriodoActivo(c);
            try (PreparedStatement p = c.prepareStatement("INSERT INTO habilitaciones(identificacion,periodo_id,estado) VALUES(?,?,?) "
                    + "ON CONFLICT(identificacion,periodo_id) DO UPDATE SET estado=excluded.estado")) {
                p.setString(1, limpiar(u.getIdentificacion()));
                p.setInt(2, periodo);
                p.setInt(3, u.isHabilitadoPeriodo() ? 1 : 0);
                p.executeUpdate();
            }
            c.commit();
        }
    }

    public int importarUsuarios(File archivo) throws IOException, SQLException {
        List<Usuario> usuarios = LectorArchivos.cargarUsuariosDesdeArchivo(archivo);
        for (Usuario u : usuarios) guardarUsuario(u);
        return usuarios.size();
    }

    public List<Sala> listarSalas() throws SQLException {
        List<Sala> lista = new ArrayList<>();
        try (Connection c = BaseDatos.conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM salas ORDER BY num_sala")) {
            while (r.next()) lista.add(new Sala(r.getString("num_sala"), r.getInt("capacidad"), r.getString("ubicacion")));
        }
        return lista;
    }

    public void guardarSala(String numero, int capacidad, String ubicacion) throws SQLException {
        if (limpiar(numero).isEmpty()) throw new IllegalArgumentException("El número de sala es obligatorio.");
        if (capacidad < 1) throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        try (Connection c = BaseDatos.conectar(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO salas(num_sala,capacidad,ubicacion) VALUES(?,?,?) ON CONFLICT(num_sala) DO UPDATE SET capacidad=excluded.capacidad, ubicacion=excluded.ubicacion")) {
            p.setString(1, limpiar(numero)); p.setInt(2, capacidad); p.setString(3, vacio(ubicacion)); p.executeUpdate();
        }
    }

    public List<Portatil> listarPortatiles() throws SQLException {
        List<Portatil> lista = new ArrayList<>();
        try (Connection c = BaseDatos.conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM portatiles ORDER BY num_equipo")) {
            while (r.next()) lista.add(new Portatil(r.getString("num_equipo"), r.getString("estado")));
        }
        return lista;
    }

    public List<Portatil> listarPortatilesDisponibles() throws SQLException {
        List<Portatil> lista = new ArrayList<>();
        try (Connection c = BaseDatos.conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM portatiles WHERE estado='DISPONIBLE' ORDER BY num_equipo")) {
            while (r.next()) lista.add(new Portatil(r.getString("num_equipo"), r.getString("estado")));
        }
        return lista;
    }

    public void guardarPortatil(String numero) throws SQLException {
        if (limpiar(numero).isEmpty()) throw new IllegalArgumentException("El código del portátil es obligatorio.");
        try (Connection c = BaseDatos.conectar(); PreparedStatement p = c.prepareStatement(
                "INSERT OR IGNORE INTO portatiles(num_equipo,estado) VALUES(?,'DISPONIBLE')")) {
            p.setString(1, limpiar(numero)); p.executeUpdate();
        }
    }

    public List<Ubicacion> listarUbicaciones() throws SQLException {
        List<Ubicacion> lista = new ArrayList<>();
        try (Connection c = BaseDatos.conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT id,nombre FROM ubicaciones ORDER BY nombre")) {
            while (r.next()) lista.add(new Ubicacion(r.getInt("id"), r.getString("nombre")));
        }
        return lista;
    }

    public void guardarUbicacion(String nombre) throws SQLException {
        if (limpiar(nombre).isEmpty()) throw new IllegalArgumentException("El nombre de la ubicación es obligatorio.");
        try (Connection c = BaseDatos.conectar(); PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO ubicaciones(nombre) VALUES(?)")) {
            p.setString(1, limpiar(nombre)); p.executeUpdate();
        }
    }

    public int registrarAcceso(String identificacion, String nombreInvitado, String programaInvitado,
                               String numeroSala, String numeroEquipo, String curso, String facultad) throws SQLException {
        String doc = limpiar(identificacion);
        if (doc.isEmpty()) throw new IllegalArgumentException("La identificación es obligatoria.");
        String sala = limpiar(numeroSala);
        if (sala.isEmpty()) throw new IllegalArgumentException("Debes seleccionar una sala.");
        try (Connection c = BaseDatos.conectar()) {
            c.setAutoCommit(false);
            Usuario usuario = buscarUsuario(c, doc);
            if (usuario == null) {
                if (limpiar(nombreInvitado).isEmpty() || limpiar(programaInvitado).isEmpty())
                    throw new IllegalArgumentException("El usuario no existe. Para registrarlo como invitado, indica nombre y programa.");
                usuario = new Usuario(doc, limpiar(nombreInvitado), "", "Invitado", limpiar(programaInvitado), null, "", "", false, true);
                insertarUsuario(c, usuario);
                try (PreparedStatement h = c.prepareStatement("INSERT INTO habilitaciones(identificacion,periodo_id,estado) VALUES(?,?,1)")) {
                    h.setString(1, doc); h.setInt(2, obtenerPeriodoActivo(c)); h.executeUpdate();
                }
            }
            if ("Docente".equalsIgnoreCase(usuario.getTipoUsuario())) {
                if (limpiar(curso).isEmpty()) throw new IllegalArgumentException("Para el ingreso docente debes registrar el curso.");
                if (limpiar(facultad).isEmpty()) facultad = usuario.getFacultadDependencia();
                if (limpiar(facultad).isEmpty()) throw new IllegalArgumentException("Para el ingreso docente debes registrar la facultad.");
            }
            try (PreparedStatement habilitar = c.prepareStatement("INSERT INTO habilitaciones(identificacion,periodo_id,estado) VALUES(?,?,1) "
                    + "ON CONFLICT(identificacion,periodo_id) DO UPDATE SET estado=1")) {
                habilitar.setString(1, doc);
                habilitar.setInt(2, obtenerPeriodoActivo(c));
                habilitar.executeUpdate();
            }
            LocalDateTime fechaIngreso = LocalDateTime.now();
            int id;
            try (PreparedStatement p = c.prepareStatement("INSERT INTO accesos(identificacion,num_sala,num_equipo,fecha_ingreso,curso,facultad) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, doc); p.setString(2, sala); p.setString(3, limpiar(numeroEquipo).isEmpty() ? null : limpiar(numeroEquipo));
                p.setString(4, fechaIngreso.format(FECHA)); p.setString(5, vacio(curso)); p.setString(6, vacio(facultad));
                p.executeUpdate();
                try (ResultSet keys = p.getGeneratedKeys()) { id = keys.next() ? keys.getInt(1) : ultimoId(c); }
            }
            c.commit();
            try {
                RegistroPlano.anexarAcceso(id, fechaIngreso, doc, usuario.getNombres() + " " + usuario.getApellidos(),
                        usuario.getPrograma(), sala, numeroEquipo, curso, facultad);
            } catch (IOException e) {
                System.err.println("El acceso quedó en la base de datos, pero no se pudo anexar al CSV: " + e.getMessage());
            }
            return id;
        }
    }

    public void registrarSalidaSala(int idAcceso) throws SQLException {
        try (Connection c = BaseDatos.conectar(); PreparedStatement p = c.prepareStatement("UPDATE accesos SET fecha_salida=? WHERE id=? AND fecha_salida IS NULL")) {
            p.setString(1, LocalDateTime.now().format(FECHA)); p.setInt(2, idAcceso);
            if (p.executeUpdate() == 0) throw new IllegalArgumentException("No hay un ingreso pendiente con ese número.");
        }
    }

    public int solicitarPrestamo(String identificacion, int ubicacionId, String celular) throws SQLException {
        if (limpiar(celular).isEmpty()) throw new IllegalArgumentException("El celular es obligatorio.");
        try (Connection c = BaseDatos.conectar()) {
            Usuario usuario = buscarUsuario(c, limpiar(identificacion));
            if (usuario == null) throw new IllegalArgumentException("El usuario no está registrado. Importa o crea primero su ficha.");
            try (PreparedStatement p = c.prepareStatement("INSERT INTO prestamos(identificacion,ubicacion_id,celular,fecha_solicitud,estado) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, usuario.getIdentificacion()); p.setInt(2, ubicacionId); p.setString(3, limpiar(celular));
                p.setString(4, LocalDateTime.now().format(FECHA));
                p.setString(5, usuario.isEstadoBloqueo() ? "ALERTA_BLOQUEO" : "SOLICITADO");
                p.executeUpdate();
                try (ResultSet keys = p.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : ultimoId(c); }
            }
        }
    }

    public void asignarPortatil(int idPrestamo, String numeroEquipo, String nombreAuxiliar) throws SQLException {
        if (limpiar(nombreAuxiliar).isEmpty()) throw new IllegalArgumentException("Registra el nombre del auxiliar de TI.");
        try (Connection c = BaseDatos.conectar()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("SELECT p.identificacion,u.bloqueado,p.estado FROM prestamos p JOIN usuarios u ON u.identificacion=p.identificacion WHERE p.id=?")) {
                p.setInt(1, idPrestamo);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("No existe esa solicitud.");
                    if (r.getInt("bloqueado") != 0) throw new IllegalArgumentException("El usuario está bloqueado. No se puede asignar el equipo.");
                    if (!"SOLICITADO".equals(r.getString("estado"))) throw new IllegalArgumentException("La solicitud no está pendiente de asignación.");
                }
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE portatiles SET estado='PRESTADO' WHERE num_equipo=? AND estado='DISPONIBLE'")) {
                p.setString(1, limpiar(numeroEquipo));
                if (p.executeUpdate() == 0) throw new IllegalArgumentException("El portátil no existe o no está disponible.");
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE prestamos SET num_equipo=?, auxiliar=?, fecha_entrega=?, estado='PRESTADO' WHERE id=?")) {
                p.setString(1, limpiar(numeroEquipo)); p.setString(2, limpiar(nombreAuxiliar));
                p.setString(3, LocalDateTime.now().format(FECHA)); p.setInt(4, idPrestamo); p.executeUpdate();
            }
            c.commit();
        }
    }

    public void registrarDevolucion(int idPrestamo) throws SQLException {
        try (Connection c = BaseDatos.conectar()) {
            c.setAutoCommit(false);
            String equipo = null;
            try (PreparedStatement p = c.prepareStatement("SELECT num_equipo,estado FROM prestamos WHERE id=?")) {
                p.setInt(1, idPrestamo);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next() || !"PRESTADO".equals(r.getString("estado"))) throw new IllegalArgumentException("Ese préstamo no está activo.");
                    equipo = r.getString("num_equipo");
                }
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE prestamos SET fecha_devolucion=?,estado='DEVUELTO' WHERE id=?")) {
                p.setString(1, LocalDateTime.now().format(FECHA)); p.setInt(2, idPrestamo); p.executeUpdate();
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE portatiles SET estado='DISPONIBLE' WHERE num_equipo=?")) {
                p.setString(1, equipo); p.executeUpdate();
            }
            c.commit();
        }
    }

    public void cambiarBloqueo(String identificacion, boolean bloqueado) throws SQLException {
        try (Connection c = BaseDatos.conectar()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("UPDATE usuarios SET bloqueado=? WHERE identificacion=?")) {
                p.setInt(1, bloqueado ? 1 : 0); p.setString(2, limpiar(identificacion));
                if (p.executeUpdate() == 0) throw new IllegalArgumentException("No existe un usuario con esa identificación.");
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE prestamos SET estado=? WHERE identificacion=? AND estado=?")) {
                p.setString(1, bloqueado ? "ALERTA_BLOQUEO" : "SOLICITADO");
                p.setString(2, limpiar(identificacion));
                p.setString(3, bloqueado ? "SOLICITADO" : "ALERTA_BLOQUEO");
                p.executeUpdate();
            }
            c.commit();
        }
    }

    public List<String[]> consultarAccesos(String desde, String hasta, String programa, String identificacion) throws SQLException {
        String sql = "SELECT a.id,a.fecha_ingreso,a.fecha_salida,u.identificacion,u.nombres||' '||u.apellidos AS usuario,u.tipo_usuario,u.programa,a.num_sala,a.num_equipo,a.curso,a.facultad "
                + "FROM accesos a JOIN usuarios u ON u.identificacion=a.identificacion WHERE (?='' OR DATE(a.fecha_ingreso)>=DATE(?)) AND (?='' OR DATE(a.fecha_ingreso)<=DATE(?)) "
                + "AND (?='' OR u.programa LIKE ?) AND (?='' OR u.identificacion=?) ORDER BY a.fecha_ingreso DESC";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = BaseDatos.conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, vacio(desde)); p.setString(2, vacio(desde)); p.setString(3, vacio(hasta)); p.setString(4, vacio(hasta));
            p.setString(5, vacio(programa)); p.setString(6, "%" + vacio(programa) + "%"); p.setString(7, vacio(identificacion)); p.setString(8, vacio(identificacion));
            try (ResultSet r = p.executeQuery()) { while (r.next()) rows.add(new String[]{r.getString("id"),r.getString("fecha_ingreso"),vacio(r.getString("fecha_salida")),r.getString("identificacion"),r.getString("usuario"),r.getString("tipo_usuario"),r.getString("programa"),r.getString("num_sala"),vacio(r.getString("num_equipo")),vacio(r.getString("curso")),vacio(r.getString("facultad"))}); }
        }
        return rows;
    }

    public List<String[]> consultarPrestamos(String desde, String hasta, String programa, String identificacion) throws SQLException {
        String sql = "SELECT p.id,p.fecha_solicitud,p.fecha_entrega,p.fecha_devolucion,u.identificacion,u.nombres||' '||u.apellidos AS usuario,u.tipo_usuario,u.programa,l.nombre AS ubicacion,p.celular,p.num_equipo,p.auxiliar,p.estado "
                + "FROM prestamos p JOIN usuarios u ON u.identificacion=p.identificacion JOIN ubicaciones l ON l.id=p.ubicacion_id WHERE (?='' OR DATE(p.fecha_solicitud)>=DATE(?)) AND (?='' OR DATE(p.fecha_solicitud)<=DATE(?)) "
                + "AND (?='' OR u.programa LIKE ?) AND (?='' OR u.identificacion=?) ORDER BY p.fecha_solicitud DESC";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = BaseDatos.conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, vacio(desde)); p.setString(2, vacio(desde)); p.setString(3, vacio(hasta)); p.setString(4, vacio(hasta));
            p.setString(5, vacio(programa)); p.setString(6, "%" + vacio(programa) + "%"); p.setString(7, vacio(identificacion)); p.setString(8, vacio(identificacion));
            try (ResultSet r = p.executeQuery()) { while (r.next()) rows.add(new String[]{r.getString("id"),r.getString("fecha_solicitud"),vacio(r.getString("fecha_entrega")),vacio(r.getString("fecha_devolucion")),r.getString("identificacion"),r.getString("usuario"),r.getString("tipo_usuario"),r.getString("programa"),r.getString("ubicacion"),r.getString("celular"),vacio(r.getString("num_equipo")),vacio(r.getString("auxiliar")),r.getString("estado")}); }
        }
        return rows;
    }

    public List<String[]> resumirAccesos(boolean porIdentificacion, String desde, String hasta, String programa, String identificacion) throws SQLException {
        String select = porIdentificacion
                ? "SELECT u.identificacion,u.nombres||' '||u.apellidos AS usuario,u.programa,COUNT(*) AS cantidad "
                : "SELECT DATE(a.fecha_ingreso) AS fecha,u.programa,COUNT(*) AS cantidad ";
        String group = porIdentificacion ? " GROUP BY u.identificacion,usuario,u.programa ORDER BY cantidad DESC"
                : " GROUP BY DATE(a.fecha_ingreso),u.programa ORDER BY fecha,u.programa";
        String sql = select + "FROM accesos a JOIN usuarios u ON u.identificacion=a.identificacion WHERE u.tipo_usuario='Estudiante' "
                + "AND (?='' OR DATE(a.fecha_ingreso)>=DATE(?)) AND (?='' OR DATE(a.fecha_ingreso)<=DATE(?)) "
                + "AND (?='' OR u.programa LIKE ?) AND (?='' OR u.identificacion=?)" + group;
        return consultarConteo(sql, desde, hasta, programa, identificacion, porIdentificacion);
    }

    public List<String[]> resumirPrestamos(boolean porIdentificacion, String desde, String hasta, String programa, String identificacion) throws SQLException {
        String select = porIdentificacion
                ? "SELECT u.identificacion,u.nombres||' '||u.apellidos AS usuario,u.programa,COUNT(*) AS cantidad "
                : "SELECT DATE(p.fecha_solicitud) AS fecha,u.programa,COUNT(*) AS cantidad ";
        String group = porIdentificacion ? " GROUP BY u.identificacion,usuario,u.programa ORDER BY cantidad DESC"
                : " GROUP BY DATE(p.fecha_solicitud),u.programa ORDER BY fecha,u.programa";
        String sql = select + "FROM prestamos p JOIN usuarios u ON u.identificacion=p.identificacion WHERE u.tipo_usuario='Estudiante' "
                + "AND (?='' OR DATE(p.fecha_solicitud)>=DATE(?)) AND (?='' OR DATE(p.fecha_solicitud)<=DATE(?)) "
                + "AND (?='' OR u.programa LIKE ?) AND (?='' OR u.identificacion=?)" + group;
        return consultarConteo(sql, desde, hasta, programa, identificacion, porIdentificacion);
    }

    private List<String[]> consultarConteo(String sql, String desde, String hasta, String programa, String identificacion, boolean porIdentificacion) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection c=BaseDatos.conectar(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,vacio(desde)); p.setString(2,vacio(desde)); p.setString(3,vacio(hasta)); p.setString(4,vacio(hasta));
            p.setString(5,vacio(programa)); p.setString(6,"%"+vacio(programa)+"%"); p.setString(7,vacio(identificacion)); p.setString(8,vacio(identificacion));
            try (ResultSet r=p.executeQuery()) {
                while (r.next()) rows.add(porIdentificacion
                        ? new String[]{r.getString("identificacion"),r.getString("usuario"),r.getString("programa"),r.getString("cantidad")}
                        : new String[]{r.getString("fecha"),r.getString("programa"),r.getString("cantidad")});
            }
        }
        return rows;
    }

    public List<String[]> listarSolicitudes() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT p.id,p.fecha_solicitud,u.identificacion,u.nombres||' '||u.apellidos AS usuario,u.programa,u.bloqueado,l.nombre AS ubicacion,p.celular,p.estado,p.num_equipo,p.auxiliar,p.fecha_entrega,p.fecha_devolucion "
                + "FROM prestamos p JOIN usuarios u ON u.identificacion=p.identificacion JOIN ubicaciones l ON l.id=p.ubicacion_id ORDER BY CASE p.estado WHEN 'ALERTA_BLOQUEO' THEN 0 WHEN 'SOLICITADO' THEN 1 WHEN 'PRESTADO' THEN 2 ELSE 3 END,p.fecha_solicitud";
        try (Connection c = BaseDatos.conectar(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) rows.add(new String[]{r.getString("id"),r.getString("fecha_solicitud"),r.getString("identificacion"),r.getString("usuario"),r.getString("programa"),r.getInt("bloqueado") == 1 ? "SÍ" : "NO",r.getString("ubicacion"),r.getString("celular"),r.getString("estado"),vacio(r.getString("num_equipo")),vacio(r.getString("auxiliar")),vacio(r.getString("fecha_entrega")),vacio(r.getString("fecha_devolucion"))});
        }
        return rows;
    }

    public List<String[]> listarIngresosAbiertos() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT a.id,a.fecha_ingreso,u.identificacion,u.nombres||' '||u.apellidos AS usuario,a.num_sala FROM accesos a JOIN usuarios u ON u.identificacion=a.identificacion WHERE a.fecha_salida IS NULL ORDER BY a.fecha_ingreso";
        try (Connection c = BaseDatos.conectar(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) rows.add(new String[]{r.getString("id"),r.getString("fecha_ingreso"),r.getString("identificacion"),r.getString("usuario"),r.getString("num_sala")});
        }
        return rows;
    }

    public static final String[] COLUMNAS_ACCESOS = {"Id","Ingreso","Salida","Documento","Usuario","Tipo","Programa","Sala","Equipo","Curso","Facultad"};
    public static final String[] COLUMNAS_PRESTAMOS = {"Id","Solicitud","Entrega","Devolución","Documento","Usuario","Tipo","Programa","Ubicación","Celular","Equipo","Auxiliar","Estado"};
    public static final String[] COLUMNAS_GESTION = {"Id","Solicitud","Documento","Usuario","Programa","Bloqueado","Ubicación","Celular","Estado","Equipo","Auxiliar","Entrega","Devolución"};
    public static final String[] COLUMNAS_INGRESOS = {"Id","Ingreso","Documento","Usuario","Sala"};

    private static int obtenerPeriodoActivo(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT id FROM periodos WHERE activo=1 LIMIT 1")) {
            if (!r.next()) throw new SQLException("No hay un período académico activo.");
            return r.getInt(1);
        }
    }

    private static Usuario buscarUsuario(Connection c, String id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM usuarios WHERE identificacion=?")) {
            p.setString(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? leerUsuario(r) : null; }
        }
    }

    private static void insertarUsuario(Connection c, Usuario u) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO usuarios(identificacion,nombres,apellidos,tipo_usuario,programa,semestre,jornada,facultad_dependencia,bloqueado) VALUES(?,?,?,?,?,?,?,?,?)")) {
            p.setString(1,u.getIdentificacion()); p.setString(2,u.getNombres()); p.setString(3,vacio(u.getApellidos())); p.setString(4,u.getTipoUsuario()); p.setString(5,vacio(u.getPrograma()));
            if (u.getSemestre()==null) p.setNull(6,java.sql.Types.INTEGER); else p.setInt(6,u.getSemestre());
            p.setString(7,vacio(u.getJornada())); p.setString(8,vacio(u.getFacultadDependencia())); p.setInt(9,u.isEstadoBloqueo()?1:0); p.executeUpdate();
        }
    }

    private static Usuario leerUsuario(ResultSet r) throws SQLException {
        Usuario u = new Usuario(r.getString("identificacion"),r.getString("nombres"),r.getString("apellidos"),r.getString("tipo_usuario"),r.getString("programa"),
                (Integer) (r.getObject("semestre") == null ? null : r.getInt("semestre")),r.getString("jornada"),r.getString("facultad_dependencia"),r.getInt("bloqueado") != 0,false);
        try (PreparedStatement p = r.getStatement().getConnection().prepareStatement("SELECT h.estado FROM habilitaciones h JOIN periodos pe ON pe.id=h.periodo_id WHERE h.identificacion=? AND pe.activo=1")) {
            p.setString(1,u.getIdentificacion());
            try (ResultSet h=p.executeQuery()) { if (h.next()) u.setHabilitadoPeriodo(h.getInt(1)!=0); }
        }
        return u;
    }

    private static int ultimoId(Connection c) throws SQLException {
        try (Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT last_insert_rowid()")) { return r.next()?r.getInt(1):0; }
    }

    private static void validarUsuario(Usuario u) {
        if (u == null || limpiar(u.getIdentificacion()).isEmpty() || limpiar(u.getNombres()).isEmpty() || limpiar(u.getTipoUsuario()).isEmpty())
            throw new IllegalArgumentException("Documento, nombres y tipo de usuario son obligatorios.");
    }
    private static String limpiar(String valor) { return valor == null ? "" : valor.trim(); }
    private static String vacio(String valor) { return limpiar(valor); }
}
