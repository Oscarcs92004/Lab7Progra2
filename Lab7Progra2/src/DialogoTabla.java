import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DialogoTabla {

    private int filas;
    private int columnas;

    public boolean mostrar(Component padre) {
        JTextField campoFilas = new JTextField("3");
        JTextField campoColumnas = new JTextField("3");

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Filas:"));
        panel.add(campoFilas);
        panel.add(new JLabel("Columnas:"));
        panel.add(campoColumnas);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(padre, panel, "Insertar tabla",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (opcion != JOptionPane.OK_OPTION) {
                return false;
            }

            try {
                int cantidadFilas = Integer.parseInt(campoFilas.getText().trim());
                int cantidadColumnas = Integer.parseInt(campoColumnas.getText().trim());

                if (cantidadFilas < 1 || cantidadFilas > GestorTablas.MAXIMO
                        || cantidadColumnas < 1 || cantidadColumnas > GestorTablas.MAXIMO) {
                    JOptionPane.showMessageDialog(padre,
                            "Filas y columnas deben estar entre 1 y " + GestorTablas.MAXIMO + ".",
                            "Dato inválido", JOptionPane.WARNING_MESSAGE);
                } else {
                    filas = cantidadFilas;
                    columnas = cantidadColumnas;
                    return true;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(padre, "Ingrese números enteros.",
                        "Dato inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }
}
