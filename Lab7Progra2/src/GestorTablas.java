import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Gestiona tablas dentro del editor.
 */
public class GestorTablas {

    public static final int ID_SECCION = 3;
    public static final int MAXIMO = 50;

    // Inserta una tabla en el editor.
    public void insertar(JTextPane pane, int filas, int columnas) {
        pane.insertComponent(crearTabla(filas, columnas));
    }

    // Lee las tablas que hay en el documento.
    public ArrayList<TablaDoc> extraer(JTextPane pane) {
        ArrayList<TablaDoc> tablas = new ArrayList<>();
        Element raiz = pane.getStyledDocument().getDefaultRootElement();

        for (int i = 0; i < raiz.getElementCount(); i++) {
            Element parrafo = raiz.getElement(i);

            for (int j = 0; j < parrafo.getElementCount(); j++) {
                Element hijo = parrafo.getElement(j);
                Component componente = StyleConstants.getComponent(hijo.getAttributes());

                if (componente instanceof JTable) {
                    JTable tabla = (JTable) componente;

                    if (tabla.isEditing()) {
                        tabla.getCellEditor().stopCellEditing();
                    }

                    TablaDoc datos = new TablaDoc(hijo.getStartOffset(),
                            tabla.getRowCount(), tabla.getColumnCount());

                    for (int f = 0; f < tabla.getRowCount(); f++) {
                        for (int c = 0; c < tabla.getColumnCount(); c++) {
                            Object valor = tabla.getValueAt(f, c);
                            if (valor == null) {
                                datos.setDato(f, c, "");
                            } else {
                                datos.setDato(f, c, valor.toString());
                            }
                        }
                    }

                    tablas.add(datos);
                }
            }
        }

        return tablas;
    }

    // Vuelve a poner las tablas en el editor.
    public void aplicar(ArrayList<TablaDoc> tablas, JTextPane pane) throws IOException {
        StyledDocument documento = pane.getStyledDocument();

        for (int i = 0; i < tablas.size(); i++) {
            TablaDoc datos = tablas.get(i);

            if (datos.getPosicion() < 0 || datos.getPosicion() >= documento.getLength()) {
                throw new IOException("El archivo está corrupto (posición de tabla inválida).");
            }

            JTable tabla = crearTabla(datos.getFilas(), datos.getColumnas());
            for (int f = 0; f < datos.getFilas(); f++) {
                for (int c = 0; c < datos.getColumnas(); c++) {
                    tabla.setValueAt(datos.getDato(f, c), f, c);
                }
            }

            try {
                documento.remove(datos.getPosicion(), 1);
            } catch (BadLocationException e) {
                throw new IOException("El archivo está corrupto (posición de tabla inválida).");
            }

            pane.setCaretPosition(datos.getPosicion());
            pane.insertComponent(tabla);
        }
    }

    // Guarda la sección de tablas.
    public void escribirSeccion(DataOutputStream salida, ArrayList<TablaDoc> tablas) throws IOException {
        salida.writeInt(ID_SECCION);
        salida.writeInt(tablas.size());

        for (int i = 0; i < tablas.size(); i++) {
            TablaDoc datos = tablas.get(i);
            salida.writeInt(datos.getPosicion());
            salida.writeInt(datos.getFilas());
            salida.writeInt(datos.getColumnas());

            for (int f = 0; f < datos.getFilas(); f++) {
                for (int c = 0; c < datos.getColumnas(); c++) {
                    salida.writeUTF(datos.getDato(f, c));
                }
            }
        }
    }

    // Lee la sección de tablas.
    public ArrayList<TablaDoc> leerSeccion(DataInputStream entrada) throws IOException {
        int id = entrada.readInt();
        if (id != ID_SECCION) {
            throw new IOException("El archivo está corrupto (sección inválida).");
        }

        int cantidad = entrada.readInt();
        if (cantidad < 0) {
            throw new IOException("El archivo está corrupto (datos inválidos).");
        }

        ArrayList<TablaDoc> tablas = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            int posicion = entrada.readInt();
            int filas = entrada.readInt();
            int columnas = entrada.readInt();

            if (posicion < 0 || filas < 1 || filas > MAXIMO || columnas < 1 || columnas > MAXIMO) {
                throw new IOException("El archivo está corrupto (datos inválidos).");
            }

            TablaDoc datos = new TablaDoc(posicion, filas, columnas);
            for (int f = 0; f < filas; f++) {
                for (int c = 0; c < columnas; c++) {
                    datos.setDato(f, c, entrada.readUTF());
                }
            }

            tablas.add(datos);
        }

        return tablas;
    }

    // Crea la tabla visual.
    private JTable crearTabla(int filas, int columnas) {
        DefaultTableModel modelo = new DefaultTableModel(filas, columnas);
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.setShowGrid(true);
        tabla.setGridColor(Color.GRAY);
        tabla.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tabla.setPreferredSize(new Dimension(columnas * 110, filas * 24));
        return tabla;
    }
}
