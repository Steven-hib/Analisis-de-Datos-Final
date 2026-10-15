package utilidades;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/** Genera reportes XLSX que pueden abrirse en Excel o LibreOffice. */
public final class ExportadorExcel {
    private ExportadorExcel() { }

    public static void exportar(File archivo, String nombreHoja, String[] columnas, List<String[]> datos) throws IOException {
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) archivo = new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
        try (XSSFWorkbook libro = new XSSFWorkbook(); FileOutputStream salida = new FileOutputStream(archivo)) {
            Sheet hoja = libro.createSheet(nombreHoja == null || nombreHoja.trim().isEmpty() ? "Reporte" : nombreHoja);
            CellStyle estiloCabecera = libro.createCellStyle();
            estiloCabecera.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font fuente = libro.createFont();
            fuente.setBold(true);
            fuente.setColor(IndexedColors.WHITE.getIndex());
            estiloCabecera.setFont(fuente);
            Row encabezado = hoja.createRow(0);
            for (int i=0; i<columnas.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloCabecera);
            }
            for (int f=0; f<datos.size(); f++) {
                Row fila = hoja.createRow(f+1);
                String[] valores = datos.get(f);
                for (int c=0; c<columnas.length; c++) fila.createCell(c).setCellValue(c<valores.length && valores[c]!=null ? valores[c] : "");
            }
            if (!columnasIsEmpty(columnas)) hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, datos.size()), 0, columnas.length-1));
            for (int i=0; i<columnas.length; i++) hoja.autoSizeColumn(i);
            libro.write(salida);
        }
    }

    private static boolean columnasIsEmpty(String[] columnas) { return columnas == null || columnas.length == 0; }
}
