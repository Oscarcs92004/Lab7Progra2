package persistencia;

import java.util.ArrayList;
import java.util.List;

/**
 * Documento en memoria.
 */
public class Documento {

    private final List<Run> runs = new ArrayList<>();
    private final List<Tabla> tablas = new ArrayList<>();

    public List<Run> getRuns()     { return runs; }
    public List<Tabla> getTablas() { return tablas; }

    public void agregarRun(Run r)     { runs.add(r); }
    public void agregarTabla(Tabla t) { tablas.add(t); }

    public String textoPlano() {
        StringBuilder sb = new StringBuilder();
        for (Run r : runs) {
            sb.append(r.getTexto());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Documento{runs=" + runs.size() + ", tablas=" + tablas.size() + "}";
    }
}
