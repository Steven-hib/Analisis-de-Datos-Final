package utilidades;

import modelos.Usuario;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Importa usuarios desde TXT/CSV delimitado o desde un libro XLSX. */
public final class LectorArchivos {
    private LectorArchivos() { }

    public static List<Usuario> cargarUsuariosDesdeArchivo(File archivo) throws IOException {
        if (archivo == null || !archivo.isFile()) throw new IOException("Selecciona un archivo de datos válido.");
        String nombre = archivo.getName().toLowerCase();
        if (nombre.endsWith(".xlsx") || nombre.endsWith(".xls")) return cargarExcel(archivo);
        if (nombre.endsWith(".txt") || nombre.endsWith(".csv") || nombre.endsWith(".tsv")) return cargarTexto(archivo);
        throw new IOException("Formato no soportado. Usa .txt, .csv, .tsv o .xlsx.");
    }

    private static List<Usuario> cargarTexto(File archivo) throws IOException {
        List<Usuario> usuarios = new ArrayList<>();
        try (BufferedReader lector = Files.newBufferedReader(archivo.toPath(), StandardCharsets.UTF_8)) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (linea.trim().isEmpty()) continue;
                List<String> campos = parsearLinea(linea);
                if (numero == 1 && pareceEncabezado(campos)) continue;
                usuarios.add(crearUsuario(campos, numero));
            }
        }
        return usuarios;
    }

    private static List<Usuario> cargarExcel(File archivo) throws IOException {
        List<Usuario> usuarios = new ArrayList<>();
        DataFormatter formato = new DataFormatter();
        try (FileInputStream entrada = new FileInputStream(archivo); Workbook libro = WorkbookFactory.create(entrada)) {
            if (libro.getNumberOfSheets() == 0) return usuarios;
            Sheet hoja = libro.getSheetAt(0);
            for (Row fila : hoja) {
                List<String> campos = new ArrayList<>();
                for (int i=0; i<10; i++) campos.add(formato.formatCellValue(fila.getCell(i)));
                if (campos.stream().allMatch(String::isEmpty)) continue;
                if (fila.getRowNum() == 0 && pareceEncabezado(campos)) continue;
                usuarios.add(crearUsuario(campos, fila.getRowNum()+1));
            }
        }
        return usuarios;
    }

    private static Usuario crearUsuario(List<String> datos, int fila) throws IOException {
        if (datos.size() < 10) throw new IOException("La fila " + fila + " debe tener 10 columnas: Documento, Nombres, Apellidos, Tipo, Programa, Semestre, Jornada, Facultad/Dependencia, Bloqueado, Activo.");
        String semestreTexto = datos.get(5).trim();
        Integer semestre = null;
        if (!semestreTexto.isEmpty() && !"null".equalsIgnoreCase(semestreTexto)) {
            try { semestre = (int) Double.parseDouble(semestreTexto.replace(',', '.')); }
            catch (NumberFormatException e) { throw new IOException("Semestre inválido en la fila " + fila + ".", e); }
        }
        return new Usuario(datos.get(0).trim(), datos.get(1).trim(), datos.get(2).trim(), datos.get(3).trim(),
                datos.get(4).trim(), semestre, datos.get(6).trim(), datos.get(7).trim(),
                booleano(datos.get(8)), booleano(datos.get(9)));
    }

    private static boolean booleano(String texto) {
        String valor = texto == null ? "" : texto.trim().toLowerCase();
        return "true".equals(valor) || "1".equals(valor) || "si".equals(valor) || "sí".equals(valor) || "x".equals(valor) || "bloqueado".equals(valor);
    }

    private static boolean pareceEncabezado(List<String> campos) {
        return !campos.isEmpty() && (campos.get(0).toLowerCase().contains("ident") || campos.get(0).toLowerCase().contains("document"));
    }

    private static List<String> parsearLinea(String linea) {
        char separador = linea.indexOf('\t') >= 0 ? '\t' : (linea.indexOf(';') >= 0 ? ';' : ',');
        List<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;
        for (int i=0; i<linea.length(); i++) {
            char actual = linea.charAt(i);
            if (actual == '"') {
                if (entreComillas && i+1 < linea.length() && linea.charAt(i+1) == '"') { campo.append('"'); i++; }
                else entreComillas = !entreComillas;
            } else if (actual == separador && !entreComillas) {
                campos.add(campo.toString()); campo.setLength(0);
            } else campo.append(actual);
        }
        campos.add(campo.toString());
        return campos;
    }
}
