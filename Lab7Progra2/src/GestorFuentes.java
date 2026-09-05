/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscar
 */
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GestorFuentes {

    private final Map<String, Font> fuentes = new LinkedHashMap<>();

    public void cargarFuentes(String rutaCarpeta) {
        File carpeta = new File(rutaCarpeta);
        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.err.println("No se encontró la carpeta de fuentes: "+ carpeta.getAbsolutePath());
            return;
        }
        cargarDesdeCarpeta(carpeta);
        System.out.println("Fuentes cargadas: " + fuentes.size());
    }

    private void cargarDesdeCarpeta(File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return;
        }
        for (File archivo : archivos) {
            if (archivo.isDirectory()) {
                cargarDesdeCarpeta(archivo);
                continue;
            }
            String nombre = archivo.getName().toLowerCase();
            if (nombre.endsWith(".ttf") || nombre.endsWith(".otf")) {
                registrarFuente(archivo);
            }
        }
    }

    private void registrarFuente(File archivo) {
        try {
            Font fuente = Font.createFont(Font.TRUETYPE_FONT, archivo);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(fuente);
            String nombreFuente = fuente.getFamily();
            if (!fuentes.containsKey(nombreFuente)) {
                fuentes.put(nombreFuente, fuente);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la fuente: "+ archivo.getName());
        }
    }

    public String[] obtenerNombres() {
        List<String> nombres = new ArrayList<>(fuentes.keySet());
        Collections.sort(nombres, String.CASE_INSENSITIVE_ORDER);
        return nombres.toArray(new String[0]);
    }

    public Font obtenerFuente(String nombre) {
        return fuentes.get(nombre);
    }

    public int cantidadFuentes() {
        return fuentes.size();
    }
}
