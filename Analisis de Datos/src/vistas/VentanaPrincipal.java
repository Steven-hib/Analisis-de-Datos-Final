package vistas;

import controladores.ControladorSalas;
import modelos.Portatil;
import modelos.Sala;
import modelos.Ubicacion;
import modelos.Usuario;
import servicios.SistemaService;
import utilidades.ExportadorExcel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Interfaz gráfica Swing para los módulos de salas y portátiles. */
public class VentanaPrincipal extends JFrame {
    private final SistemaService sistema = new SistemaService();
    private final ControladorSalas controladorSalas = new ControladorSalas(sistema);
    private final JComboBox<Sala> comboSala = new JComboBox<>();
    private final JComboBox<Ubicacion> comboUbicacion = new JComboBox<>();
    private final JComboBox<Portatil> comboEquipo = new JComboBox<>();
    private final JComboBox<String> tipoReporte = new JComboBox<>(new String[]{
            "Ingresos a salas - detalle", "Ingresos - conteo por fecha y programa", "Ingresos - conteo por documento",
            "Préstamos - detalle", "Préstamos - conteo por fecha y programa", "Préstamos - conteo por documento"});

    private final JTextField accesoDocumento = new JTextField(14);
    private final JTextField invitadoNombre = new JTextField(14);
    private final JTextField invitadoPrograma = new JTextField(14);
    private final JTextField accesoEquipo = new JTextField(12);
    private final JTextField accesoCurso = new JTextField(14);
    private final JTextField accesoFacultad = new JTextField(14);
    private final JLabel resumenAcceso = new JLabel("Digita un documento y presiona Enter para consultar los datos.");
    private final JTable tablaIngresos = crearTabla();

    private final JTextField solicitudDocumento = new JTextField(14);
    private final JTextField solicitudCelular = new JTextField(12);
    private final JLabel resumenSolicitud = new JLabel("Digita un documento y presiona Enter para consultar los datos.");
    private final JTextField nombreAuxiliar = new JTextField(18);
    private final JTable tablaPrestamos = crearTabla();
    private final JLabel alertaPrestamo = new JLabel("Las solicitudes de usuarios bloqueados se marcarán en rojo.");

    private final JTextField usuarioDocumento = new JTextField(12);
    private final JTextField usuarioNombres = new JTextField(14);
    private final JTextField usuarioApellidos = new JTextField(14);
    private final JComboBox<String> usuarioTipo = new JComboBox<>(new String[]{"Estudiante", "Docente", "Funcionario", "Invitado", "Auxiliar_TI"});
    private final JTextField usuarioPrograma = new JTextField(14);
    private final JTextField usuarioSemestre = new JTextField(4);
    private final JTextField usuarioJornada = new JTextField(10);
    private final JTextField usuarioFacultad = new JTextField(14);
    private final JTable tablaUsuarios = crearTabla();

    private final JTextField salaNueva = new JTextField(8);
    private final JTextField salaCapacidad = new JTextField("30", 4);
    private final JTextField salaUbicacion = new JTextField(10);
    private final JTextField portatilNuevo = new JTextField(10);
    private final JTextField ubicacionNueva = new JTextField(10);
    private final JTable tablaSalas = crearTabla();
    private final JTable tablaInventario = crearTabla();
    private final JTable tablaUbicaciones = crearTabla();

    private final JTextField reporteDesde = new JTextField(10);
    private final JTextField reporteHasta = new JTextField(10);
    private final JTextField reportePrograma = new JTextField(12);
    private final JTextField reporteDocumento = new JTextField(12);
    private final JTable tablaReporte = crearTabla();
    private String[] columnasReporte = SistemaService.COLUMNAS_ACCESOS;
    private List<String[]> filasReporte = new ArrayList<>();

    public VentanaPrincipal() {
        super("Sistema de Gestión Tecnológica - UCC Cali");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1040, 680));
        setSize(1200, 780);
        setLocationRelativeTo(null);
        construirInterfaz();
        refrescarTodo();
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
        raiz.setBackground(new Color(246, 248, 251));
        JLabel titulo = new JLabel("Gestión de salas y equipos tecnológicos", SwingConstants.LEFT);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(new Color(24, 55, 92));
        raiz.add(titulo, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Salas", crearPanelSalas());
        pestanas.addTab("Portátiles", crearPanelPortatiles());
        pestanas.addTab("Usuarios y bloqueos", crearPanelUsuarios());
        pestanas.addTab("Inventario", crearPanelInventario());
        pestanas.addTab("Informes", crearPanelInformes());
        raiz.add(pestanas, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearPanelSalas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel formulario = new JPanel(new GridLayout(0, 4, 8, 6));
        formulario.setBorder(BorderFactory.createTitledBorder("Registrar ingreso"));
        campo(formulario, "Documento", accesoDocumento);
        campo(formulario, "Nombre (solo invitado)", invitadoNombre);
        campo(formulario, "Programa (solo invitado)", invitadoPrograma);
        campo(formulario, "Sala obligatoria", comboSala);
        campo(formulario, "Equipo opcional", accesoEquipo);
        campo(formulario, "Curso docente", accesoCurso);
        campo(formulario, "Facultad docente", accesoFacultad);
        JButton registrar = boton("Registrar ingreso");
        formulario.add(new JLabel("")); formulario.add(registrar);
        registrar.addActionListener(e -> accion(() -> {
            Sala sala = (Sala) comboSala.getSelectedItem();
            if (sala == null) throw new IllegalArgumentException("Primero registra una sala en Inventario.");
            int id = controladorSalas.registrarIngreso(accesoDocumento.getText(), invitadoNombre.getText(), invitadoPrograma.getText(),
                    sala.getNumSala(), accesoEquipo.getText(), accesoCurso.getText(), accesoFacultad.getText());
            mensaje("Acceso registrado con número " + id + ".");
            accesoDocumento.setText(""); invitadoNombre.setText(""); invitadoPrograma.setText(""); accesoEquipo.setText(""); accesoCurso.setText(""); accesoFacultad.setText("");
            refrescarIngresos();
        }));

        JPanel inferior = new JPanel(new BorderLayout(8, 8));
        inferior.setBorder(BorderFactory.createTitledBorder("Ingresos sin hora de salida"));
        inferior.add(new JScrollPane(tablaIngresos), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton salida = boton("Registrar salida del seleccionado");
        JButton actualizar = boton("Actualizar");
        acciones.add(salida); acciones.add(actualizar);
        salida.addActionListener(e -> accion(() -> {
            int fila = filaSeleccionada(tablaIngresos);
            int id = Integer.parseInt(String.valueOf(tablaIngresos.getValueAt(fila, 0)));
            controladorSalas.registrarSalida(id);
            refrescarIngresos();
            mensaje("Salida registrada.");
        }));
        actualizar.addActionListener(e -> accion(this::refrescarIngresos));
        inferior.add(acciones, BorderLayout.SOUTH);
        JPanel cabecera = new JPanel(new BorderLayout(6, 6));
        cabecera.add(formulario, BorderLayout.CENTER);
        resumenAcceso.setForeground(new Color(65, 80, 100));
        cabecera.add(resumenAcceso, BorderLayout.SOUTH);
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(inferior, BorderLayout.CENTER);
        agregarConsultaDocumento(accesoDocumento, resumenAcceso, true);
        return panel;
    }

    private JPanel crearPanelPortatiles() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel solicitud = new JPanel(new GridLayout(0, 4, 8, 6));
        solicitud.setBorder(BorderFactory.createTitledBorder("Solicitud de préstamo"));
        campo(solicitud, "Documento", solicitudDocumento);
        campo(solicitud, "Celular obligatorio", solicitudCelular);
        campo(solicitud, "Ubicación", comboUbicacion);
        JButton enviar = boton("Enviar solicitud");
        solicitud.add(new JLabel("")); solicitud.add(enviar);
        enviar.addActionListener(e -> accion(() -> {
            Ubicacion ubicacion = (Ubicacion) comboUbicacion.getSelectedItem();
            if (ubicacion == null) throw new IllegalArgumentException("Registra una ubicación en Inventario.");
            int id = sistema.solicitarPrestamo(solicitudDocumento.getText(), ubicacion.getId(), solicitudCelular.getText());
            Usuario usuario = sistema.buscarUsuario(solicitudDocumento.getText());
            String aviso = usuario != null && usuario.isEstadoBloqueo()
                    ? "Solicitud " + id + " creada con alerta de bloqueo para el auxiliar de TI."
                    : "Solicitud " + id + " enviada al auxiliar de TI.";
            solicitudDocumento.setText(""); solicitudCelular.setText("");
            refrescarPrestamos();
            mensaje(aviso);
        }));

        JPanel gestion = new JPanel(new BorderLayout(8, 8));
        gestion.setBorder(BorderFactory.createTitledBorder("Gestión del auxiliar de TI"));
        alertaPrestamo.setForeground(new Color(150, 60, 20));
        gestion.add(alertaPrestamo, BorderLayout.NORTH);
        tablaPrestamos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado, boolean foco, int fila, int columna) {
                Component c = super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
                Object estado = tabla.getValueAt(fila, 8);
                if (!seleccionado) c.setBackground("ALERTA_BLOQUEO".equals(estado) ? new Color(255, 224, 220) : Color.WHITE);
                return c;
            }
        });
        gestion.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        campoInline(controles, "Equipo disponible", comboEquipo);
        campoInline(controles, "Auxiliar", nombreAuxiliar);
        JButton asignar = boton("Asignar equipo");
        JButton devolver = boton("Registrar devolución");
        JButton actualizar = boton("Actualizar lista");
        controles.add(asignar); controles.add(devolver); controles.add(actualizar);
        asignar.addActionListener(e -> accion(() -> {
            int fila = filaSeleccionada(tablaPrestamos);
            int id = Integer.parseInt(String.valueOf(tablaPrestamos.getValueAt(fila, 0)));
            Portatil equipo = (Portatil) comboEquipo.getSelectedItem();
            if (equipo == null) throw new IllegalArgumentException("No hay equipos disponibles.");
            sistema.asignarPortatil(id, equipo.getNumEquipo(), nombreAuxiliar.getText());
            refrescarPrestamos();
            mensaje("Equipo asignado. La fecha y hora de entrega quedaron registradas.");
        }));
        devolver.addActionListener(e -> accion(() -> {
            int fila = filaSeleccionada(tablaPrestamos);
            int id = Integer.parseInt(String.valueOf(tablaPrestamos.getValueAt(fila, 0)));
            sistema.registrarDevolucion(id);
            refrescarPrestamos();
            mensaje("Devolución registrada; el portátil quedó disponible.");
        }));
        actualizar.addActionListener(e -> accion(this::refrescarPrestamos));
        gestion.add(controles, BorderLayout.SOUTH);
        JPanel cabecera = new JPanel(new BorderLayout(6, 6));
        cabecera.add(solicitud, BorderLayout.CENTER);
        resumenSolicitud.setForeground(new Color(65, 80, 100));
        cabecera.add(resumenSolicitud, BorderLayout.SOUTH);
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(gestion, BorderLayout.CENTER);
        agregarConsultaDocumento(solicitudDocumento, resumenSolicitud, false);
        return panel;
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel formulario = new JPanel(new GridLayout(0, 4, 8, 6));
        formulario.setBorder(BorderFactory.createTitledBorder("Crear o actualizar usuario"));
        campo(formulario, "Documento", usuarioDocumento);
        campo(formulario, "Nombres", usuarioNombres);
        campo(formulario, "Apellidos", usuarioApellidos);
        campo(formulario, "Tipo", usuarioTipo);
        campo(formulario, "Programa", usuarioPrograma);
        campo(formulario, "Semestre", usuarioSemestre);
        campo(formulario, "Jornada", usuarioJornada);
        campo(formulario, "Facultad o dependencia", usuarioFacultad);
        JButton guardar = boton("Guardar usuario");
        JButton importar = boton("Importar TXT / CSV / Excel");
        JButton bloquear = boton("Bloquear / desbloquear seleccionado");
        JButton actualizar = boton("Actualizar lista");
        formulario.add(guardar); formulario.add(importar); formulario.add(bloquear); formulario.add(actualizar);
        guardar.addActionListener(e -> accion(() -> {
            Integer semestre = usuarioSemestre.getText().trim().isEmpty() ? null : Integer.valueOf(usuarioSemestre.getText().trim());
            Usuario u = new Usuario(usuarioDocumento.getText().trim(), usuarioNombres.getText().trim(), usuarioApellidos.getText().trim(),
                    String.valueOf(usuarioTipo.getSelectedItem()), usuarioPrograma.getText().trim(), semestre, usuarioJornada.getText().trim(),
                    usuarioFacultad.getText().trim(), false, true);
            sistema.guardarUsuario(u);
            refrescarUsuarios();
            mensaje("Usuario guardado y habilitado para el período académico activo.");
        }));
        importar.addActionListener(e -> accion(() -> importarUsuarios()));
        bloquear.addActionListener(e -> accion(() -> {
            int fila = filaSeleccionada(tablaUsuarios);
            String id = String.valueOf(tablaUsuarios.getValueAt(fila, 0));
            boolean estaBloqueado = "Sí".equals(String.valueOf(tablaUsuarios.getValueAt(fila, 4)));
            sistema.cambiarBloqueo(id, !estaBloqueado);
            refrescarUsuarios(); refrescarPrestamos();
            mensaje(estaBloqueado ? "Usuario desbloqueado." : "Usuario bloqueado. Las solicitudes pendientes mostrarán alerta.");
        }));
        actualizar.addActionListener(e -> accion(this::refrescarUsuarios));
        panel.add(formulario, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel formularios = new JPanel(new GridLayout(1, 3, 10, 10));
        JPanel formSala = new JPanel(new GridLayout(0, 2, 6, 6));
        formSala.setBorder(BorderFactory.createTitledBorder("Salas"));
        formSala.add(new JLabel("Número")); formSala.add(salaNueva);
        formSala.add(new JLabel("Capacidad")); formSala.add(salaCapacidad);
        formSala.add(new JLabel("Ubicación")); formSala.add(salaUbicacion);
        JButton guardarSala = boton("Guardar sala"); formSala.add(new JLabel("")); formSala.add(guardarSala);
        guardarSala.addActionListener(e -> accion(() -> {
            sistema.guardarSala(salaNueva.getText(), Integer.parseInt(salaCapacidad.getText().trim()), salaUbicacion.getText());
            refrescarInventario(); mensaje("Sala guardada.");
        }));

        JPanel formEquipo = new JPanel(new GridLayout(0, 2, 6, 6));
        formEquipo.setBorder(BorderFactory.createTitledBorder("Portátiles"));
        formEquipo.add(new JLabel("Número de equipo")); formEquipo.add(portatilNuevo);
        JButton guardarEquipo = boton("Agregar portátil"); formEquipo.add(new JLabel("")); formEquipo.add(guardarEquipo);
        guardarEquipo.addActionListener(e -> accion(() -> { sistema.guardarPortatil(portatilNuevo.getText()); refrescarInventario(); refrescarPrestamos(); mensaje("Portátil agregado como disponible."); }));

        JPanel formUbicacion = new JPanel(new GridLayout(0, 2, 6, 6));
        formUbicacion.setBorder(BorderFactory.createTitledBorder("Ubicaciones"));
        formUbicacion.add(new JLabel("Nombre")); formUbicacion.add(ubicacionNueva);
        JButton guardarUbicacion = boton("Agregar ubicación"); formUbicacion.add(new JLabel("")); formUbicacion.add(guardarUbicacion);
        guardarUbicacion.addActionListener(e -> accion(() -> { sistema.guardarUbicacion(ubicacionNueva.getText()); refrescarInventario(); mensaje("Ubicación agregada."); }));

        formularios.add(formSala); formularios.add(formEquipo); formularios.add(formUbicacion);
        JPanel tablas = new JPanel(new GridLayout(1, 3, 10, 10));
        tablas.add(conBorde(tablaSalas, "Salas registradas"));
        tablas.add(conBorde(tablaInventario, "Inventario de portátiles"));
        tablas.add(conBorde(tablaUbicaciones, "Ubicaciones disponibles"));
        panel.add(formularios, BorderLayout.NORTH);
        panel.add(tablas, BorderLayout.CENTER);
        JButton actualizar = boton("Actualizar inventarios");
        actualizar.addActionListener(e -> accion(this::refrescarInventario));
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT)); sur.add(actualizar); panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelInformes() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros (fechas con formato AAAA-MM-DD)"));
        campoInline(filtros, "Tipo", tipoReporte);
        campoInline(filtros, "Desde", reporteDesde);
        campoInline(filtros, "Hasta", reporteHasta);
        campoInline(filtros, "Programa", reportePrograma);
        campoInline(filtros, "Documento", reporteDocumento);
        JButton consultar = boton("Consultar");
        JButton exportar = boton("Exportar a Excel");
        filtros.add(consultar); filtros.add(exportar);
        consultar.addActionListener(e -> accion(this::consultarReporte));
        exportar.addActionListener(e -> accion(this::exportarReporte));
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaReporte), BorderLayout.CENTER);
        return panel;
    }

    private void consultarReporte() throws SQLException {
        String desde = reporteDesde.getText().trim();
        String hasta = reporteHasta.getText().trim();
        if (!desde.isEmpty() && !desde.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException("La fecha inicial debe tener formato AAAA-MM-DD.");
        if (!hasta.isEmpty() && !hasta.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException("La fecha final debe tener formato AAAA-MM-DD.");
        String tipo = String.valueOf(tipoReporte.getSelectedItem());
        boolean accesos = tipo.startsWith("Ingresos");
        boolean conteo = tipo.contains("conteo");
        boolean porDocumento = tipo.contains("documento");
        if (!conteo && accesos) {
            columnasReporte = SistemaService.COLUMNAS_ACCESOS;
            filasReporte = sistema.consultarAccesos(desde, hasta, reportePrograma.getText(), reporteDocumento.getText());
        } else if (!conteo) {
            columnasReporte = SistemaService.COLUMNAS_PRESTAMOS;
            filasReporte = sistema.consultarPrestamos(desde, hasta, reportePrograma.getText(), reporteDocumento.getText());
        } else if (porDocumento) {
            columnasReporte = new String[]{"Documento","Estudiante","Programa","Cantidad"};
            filasReporte = accesos
                    ? sistema.resumirAccesos(true, desde, hasta, reportePrograma.getText(), reporteDocumento.getText())
                    : sistema.resumirPrestamos(true, desde, hasta, reportePrograma.getText(), reporteDocumento.getText());
        } else {
            columnasReporte = new String[]{"Fecha","Programa","Cantidad de estudiantes"};
            filasReporte = accesos
                    ? sistema.resumirAccesos(false, desde, hasta, reportePrograma.getText(), reporteDocumento.getText())
                    : sistema.resumirPrestamos(false, desde, hasta, reportePrograma.getText(), reporteDocumento.getText());
        }
        llenarTabla(tablaReporte, columnasReporte, filasReporte);
    }

    private void exportarReporte() throws Exception {
        if (filasReporte.isEmpty()) consultarReporte();
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar informe Excel");
        selector.setSelectedFile(new File("informe_sistema.xlsx"));
        selector.setFileFilter(new FileNameExtensionFilter("Libro de Excel (*.xlsx)", "xlsx"));
        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ExportadorExcel.exportar(selector.getSelectedFile(), "Reporte", columnasReporte, filasReporte);
            mensaje("Informe exportado.");
        }
    }

    private void importarUsuarios() throws Exception {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccionar archivo de usuarios");
        selector.setFileFilter(new FileNameExtensionFilter("TXT, CSV, TSV o Excel", "txt", "csv", "tsv", "xlsx", "xls"));
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int cantidad = sistema.importarUsuarios(selector.getSelectedFile());
            refrescarUsuarios();
            mensaje("Importación terminada: " + cantidad + " usuarios creados o actualizados.");
        }
    }

    private void agregarConsultaDocumento(JTextField campo, JLabel resumen, boolean esSala) {
        campo.addActionListener(e -> consultarUsuario(campo, resumen, esSala));
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { consultarUsuario(campo, resumen, esSala); }
        });
    }

    private void consultarUsuario(JTextField campo, JLabel resumen, boolean esSala) {
        if (campo.getText().trim().isEmpty()) return;
        accion(() -> {
            Usuario u = sistema.buscarUsuario(campo.getText());
            if (u == null) {
                resumen.setForeground(new Color(150, 90, 20));
                resumen.setText(esSala ? "No está registrado. Completa nombre y programa para crear un invitado." : "No está registrado. Debe importarse o crearse antes de solicitar un portátil.");
                return;
            }
            String detalle = u.getNombres() + " " + u.getApellidos() + " | " + u.getTipoUsuario()
                    + " | Programa: " + u.getPrograma()
                    + (u.getSemestre() == null ? "" : " | Semestre: " + u.getSemestre())
                    + (u.getJornada() == null || u.getJornada().isEmpty() ? "" : " | Jornada: " + u.getJornada())
                    + (u.getFacultadDependencia() == null || u.getFacultadDependencia().isEmpty() ? "" : " | Facultad/dependencia: " + u.getFacultadDependencia())
                    + (u.isEstadoBloqueo() ? " | ALERTA: USUARIO BLOQUEADO" : "");
            resumen.setForeground(u.isEstadoBloqueo() ? new Color(170, 35, 25) : new Color(30, 100, 55));
            resumen.setText(detalle);
            if (esSala && "Docente".equalsIgnoreCase(u.getTipoUsuario()) && accesoFacultad.getText().trim().isEmpty())
                accesoFacultad.setText(u.getFacultadDependencia());
        });
    }

    private void refrescarTodo() {
        accion(() -> { refrescarSalasCombo(); refrescarIngresos(); refrescarPrestamos(); refrescarUsuarios(); refrescarInventario(); refrescarReporte(); });
    }

    private void refrescarSalasCombo() throws SQLException {
        comboSala.removeAllItems();
        for (Sala s : sistema.listarSalas()) comboSala.addItem(s);
        comboUbicacion.removeAllItems();
        for (Ubicacion u : sistema.listarUbicaciones()) comboUbicacion.addItem(u);
    }

    private void refrescarIngresos() throws SQLException {
        llenarTabla(tablaIngresos, SistemaService.COLUMNAS_INGRESOS, sistema.listarIngresosAbiertos());
    }

    private void refrescarPrestamos() throws SQLException {
        List<String[]> rows = sistema.listarSolicitudes();
        llenarTabla(tablaPrestamos, SistemaService.COLUMNAS_GESTION, rows);
        long alertas = rows.stream().filter(r -> "ALERTA_BLOQUEO".equals(r[8])).count();
        alertaPrestamo.setText(alertas == 0 ? "Las solicitudes de usuarios bloqueados se marcarán en rojo."
                : "ALERTA: hay " + alertas + " solicitud(es) de usuarios bloqueados; no se asignará equipo.");
        comboEquipo.removeAllItems();
        for (Portatil p : sistema.listarPortatilesDisponibles()) comboEquipo.addItem(p);
    }

    private void refrescarUsuarios() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        for (Usuario u : sistema.listarUsuarios()) rows.add(new String[]{u.getIdentificacion(),u.getNombres()+" "+u.getApellidos(),u.getTipoUsuario(),u.getPrograma(),u.isEstadoBloqueo()?"Sí":"No",u.isHabilitadoPeriodo()?"Sí":"No"});
        llenarTabla(tablaUsuarios, new String[]{"Documento","Nombre","Tipo","Programa","Bloqueado","Habilitado período"}, rows);
    }

    private void refrescarInventario() throws SQLException {
        List<String[]> salas = new ArrayList<>();
        for (Sala s : sistema.listarSalas()) salas.add(new String[]{s.getNumSala(),String.valueOf(s.getCapacidad()),s.getUbicacion()});
        llenarTabla(tablaSalas, new String[]{"Número","Capacidad","Ubicación"}, salas);
        List<String[]> portatiles = new ArrayList<>();
        for (Portatil p : sistema.listarPortatiles()) portatiles.add(new String[]{p.getNumEquipo(),p.getEstado()});
        llenarTabla(tablaInventario, new String[]{"Equipo","Estado"}, portatiles);
        List<String[]> ubicaciones = new ArrayList<>();
        for (Ubicacion u : sistema.listarUbicaciones()) ubicaciones.add(new String[]{String.valueOf(u.getId()),u.getNombre()});
        llenarTabla(tablaUbicaciones, new String[]{"Id","Ubicación"}, ubicaciones);
        refrescarSalasCombo();
    }

    private void refrescarReporte() throws SQLException { consultarReporte(); }

    private static JTable crearTabla() {
        JTable tabla = new JTable(new DefaultTableModel(new Object[0][0], new String[0]) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        });
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        return tabla;
    }

    private static void llenarTabla(JTable tabla, String[] columnas, List<String[]> filas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        for (String[] fila : filas) modelo.addRow(fila);
        tabla.setModel(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private static int filaSeleccionada(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona primero una fila de la tabla.");
        return fila;
    }

    private static void campo(JPanel panel, String texto, Component componente) {
        panel.add(new JLabel(texto)); panel.add(componente);
    }
    private static void campoInline(JPanel panel, String texto, Component componente) {
        panel.add(new JLabel(texto)); panel.add(componente);
    }
    private static JButton boton(String texto) { JButton b = new JButton(texto); b.setFocusPainted(false); return b; }
    private static JPanel conBorde(Component componente, String titulo) {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBorder(BorderFactory.createTitledBorder(titulo)); panel.add(new JScrollPane(componente),BorderLayout.CENTER); return panel;
    }
    private void mensaje(String texto) { JOptionPane.showMessageDialog(this, texto, "Sistema de Gestión", JOptionPane.INFORMATION_MESSAGE); }
    private void accion(Accion accion) {
        try { accion.ejecutar(); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage() == null ? e.toString() : e.getMessage(), "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE); }
    }
    @FunctionalInterface private interface Accion { void ejecutar() throws Exception; }
}
