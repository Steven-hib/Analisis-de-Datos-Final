package vistas;

import utilidades.BaseDatos;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicación de escritorio. */
public class Main {
    public static void main(String[] args) {
        try {
            BaseDatos.inicializar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudo iniciar la base de datos:\n" + e.getMessage(),
                    "Error de inicio", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignorada) { /* Se mantiene el aspecto predeterminado de Swing. */ }
            new VentanaPrincipal().setVisible(true);
        });
    }
}
