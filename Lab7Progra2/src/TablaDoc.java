public class TablaDoc {

    private int posicion;
    private int filas;
    private int columnas;
    private String[][] datos;

    public TablaDoc(int posicion, int filas, int columnas) {
        this.posicion = posicion;
        this.filas = filas;
        this.columnas = columnas;
        this.datos = new String[filas][columnas];
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                this.datos[f][c] = "";
            }
        }
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public String getDato(int fila, int columna) {
        return datos[fila][columna];
    }

    public void setDato(int fila, int columna, String valor) {
        if (valor == null) {
            valor = "";
        }
        datos[fila][columna] = valor;
    }
}
