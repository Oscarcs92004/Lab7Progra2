package persistencia;

import java.util.Arrays;

/**
 * Tabla del documento.
 */
public class Tabla {

    private final int filas;
    private final int columnas;
    private final String[][] celdas;

    public Tabla(int filas, int columnas) {
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException("filas y columnas deben ser mayores que 0");
        }
        this.filas = filas;
        this.columnas = columnas;
        this.celdas = new String[filas][columnas];
        for (String[] fila : celdas) {
            Arrays.fill(fila, "");
        }
    }

    public int getFilas()    { return filas; }
    public int getColumnas() { return columnas; }

    public String getCelda(int fila, int col) {
        return celdas[fila][col];
    }

    public void setCelda(int fila, int col, String valor) {
        celdas[fila][col] = (valor == null) ? "" : valor;
    }
}
