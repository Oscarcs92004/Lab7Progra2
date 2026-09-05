package persistencia;

import java.awt.Color;
import java.awt.Component;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Persistencia del editor.
 */
public class PersistenciaEDT {

    private static final String MAGIC = "EDT1";
    private static final int VERSION = 1;
    private static final int MINIMO = 16 + 4;

    // Formato del archivo: cabecera, runs, tablas y CRC.

    public static void guardar(Documento documento, File archivo) throws EdtException, IOException {
        if (!archivo.getName().toLowerCase().endsWith(".edt")) {
            throw new EdtException.ExtensionInvalida(archivo.getName());
        }

        RandomAccessFile rEdt = new RandomAccessFile(archivo, "rw");
        try {
            rEdt.setLength(0);

            rEdt.writeBytes(MAGIC);
            rEdt.writeByte(VERSION);
            rEdt.writeByte(0);
            rEdt.writeInt(documento.getRuns().size());
            rEdt.writeInt(documento.getTablas().size());
            rEdt.writeShort(0);

            for (Run r : documento.getRuns()) {
                escribirRun(rEdt, r);
            }

            for (Tabla t : documento.getTablas()) {
                rEdt.writeInt(t.getPosicion());
                rEdt.writeInt(t.getFilas());
                rEdt.writeInt(t.getColumnas());
                for (int f = 0; f < t.getFilas(); f++) {
                    for (int c = 0; c < t.getColumnas(); c++) {
                        rEdt.writeUTF(t.getCelda(f, c));
                    }
                }
            }

            long fin = rEdt.getFilePointer();
            byte[] datos = new byte[(int) fin];
            rEdt.seek(0);
            rEdt.readFully(datos);
            rEdt.seek(fin);
            rEdt.writeInt(calcularCRC(datos));
        } finally {
            rEdt.close();
        }
    }

    private static void escribirRun(RandomAccessFile rEdt, Run r) throws IOException {
        rEdt.writeUTF(r.getTexto());
        rEdt.writeUTF(r.getFuente());
        rEdt.writeInt(r.getTamano());
        rEdt.writeBoolean(r.isNegrita());
        rEdt.writeBoolean(r.isCursiva());
        rEdt.writeBoolean(r.isSubrayado());
        rEdt.writeBoolean(r.isTachado());
        rEdt.writeInt(r.getColorRGB() & 0xFFFFFF);
    }

    public static Documento abrir(File archivo) throws EdtException, IOException {
        if (!archivo.getName().toLowerCase().endsWith(".edt")) {
            throw new EdtException.ExtensionInvalida(archivo.getName());
        }
        if (!archivo.exists()) {
            throw new EdtException.ArchivoNoExiste(archivo.getAbsolutePath());
        }
        if (archivo.length() < MINIMO) {
            throw new EdtException.ArchivoTruncado("mide menos de " + MINIMO + " bytes");
        }

        RandomAccessFile rEdt = new RandomAccessFile(archivo, "r");
        try {
            long total = rEdt.length();

            rEdt.seek(total - 4);
            int crcGuardado = rEdt.readInt();
            byte[] datos = new byte[(int) (total - 4)];
            rEdt.seek(0);
            rEdt.readFully(datos);
            if (calcularCRC(datos) != crcGuardado) {
                throw new EdtException.ArchivoCorrupto("el CRC no coincide (el archivo fue modificado)");
            }

            rEdt.seek(0);
            byte[] firma = new byte[4];
            rEdt.readFully(firma);
            if (!new String(firma).equals(MAGIC)) {
                throw new EdtException.ArchivoCorrupto("no es un documento .edt (firma invalida)");
            }
            int version = rEdt.readUnsignedByte();
            if (version != VERSION) {
                throw new EdtException.VersionNoSoportada(version);
            }
            rEdt.readUnsignedByte();
            int cantRuns = rEdt.readInt();
            int cantTablas = rEdt.readInt();
            rEdt.readShort();
            if (cantRuns < 0 || cantTablas < 0) {
                throw new EdtException.ArchivoCorrupto("cabecera con valores invalidos");
            }

            Documento documento = new Documento();
            try {
                for (int i = 0; i < cantRuns; i++) {
                    documento.agregarRun(leerRun(rEdt));
                }

                for (int i = 0; i < cantTablas; i++) {
                    int posicion = rEdt.readInt();
                    int filas = rEdt.readInt();
                    int columnas = rEdt.readInt();
                    if (posicion < 0 || filas <= 0 || columnas <= 0) {
                        throw new EdtException.ArchivoCorrupto("tabla con datos invalidos");
                    }
                    Tabla tabla = new Tabla(filas, columnas);
                    tabla.setPosicion(posicion);
                    for (int f = 0; f < filas; f++) {
                        for (int c = 0; c < columnas; c++) {
                            tabla.setCelda(f, c, rEdt.readUTF());
                        }
                    }
                    documento.agregarTabla(tabla);
                }
            } catch (EOFException e) {
                throw new EdtException.ArchivoTruncado("se acabo el archivo antes de leer todo el contenido");
            }

            return documento;
        } finally {
            rEdt.close();
        }
    }

    private static Run leerRun(RandomAccessFile rEdt) throws IOException {
        Run r = new Run();
        r.setTexto(rEdt.readUTF());
        r.setFuente(rEdt.readUTF());
        r.setTamano(rEdt.readInt());
        r.setNegrita(rEdt.readBoolean());
        r.setCursiva(rEdt.readBoolean());
        r.setSubrayado(rEdt.readBoolean());
        r.setTachado(rEdt.readBoolean());
        r.setColorRGB(rEdt.readInt() & 0xFFFFFF);
        return r;
    }

    private static int calcularCRC(byte[] datos) {
        CRC32 crc = new CRC32();
        crc.update(datos);
        return (int) crc.getValue();
    }

    // Convierte el documento visual en runs.
    public static Documento desdeStyledDocument(StyledDocument doc) throws BadLocationException {
        Documento documento = new Documento();
        StringBuilder buffer = new StringBuilder();
        Run actual = null;

        for (int i = 0; i < doc.getLength(); i++) {
            AttributeSet at = doc.getCharacterElement(i).getAttributes();

            String fuente = StyleConstants.getFontFamily(at);
            int tamano = StyleConstants.getFontSize(at);
            boolean negrita = StyleConstants.isBold(at);
            boolean cursiva = StyleConstants.isItalic(at);
            boolean subrayado = StyleConstants.isUnderline(at);
            boolean tachado = StyleConstants.isStrikeThrough(at);
            int colorRGB = StyleConstants.getForeground(at).getRGB() & 0xFFFFFF;

            // Deja un espacio cuando hay una tabla insertada.
            Component componente = StyleConstants.getComponent(at);
            char letra = (componente != null) ? ' ' : doc.getText(i, 1).charAt(0);

            if (actual != null
                    && actual.getFuente().equals(fuente)
                    && actual.getTamano() == tamano
                    && actual.getColorRGB() == colorRGB
                    && actual.isNegrita() == negrita
                    && actual.isCursiva() == cursiva
                    && actual.isSubrayado() == subrayado
                    && actual.isTachado() == tachado) {
                buffer.append(letra);
            } else {
                cerrarRun(documento, actual, buffer);
                actual = new Run("", fuente, tamano, colorRGB, negrita, cursiva, subrayado, tachado);
                buffer.append(letra);
            }
        }
        cerrarRun(documento, actual, buffer);
        return documento;
    }

    // Vuelve a aplicar el formato guardado.
    public static void aplicarA(Documento documento, StyledDocument doc) throws BadLocationException {
        doc.remove(0, doc.getLength());
        for (Run r : documento.getRuns()) {
            SimpleAttributeSet at = new SimpleAttributeSet();
            StyleConstants.setFontFamily(at, r.getFuente());
            StyleConstants.setFontSize(at, r.getTamano());
            StyleConstants.setBold(at, r.isNegrita());
            StyleConstants.setItalic(at, r.isCursiva());
            StyleConstants.setUnderline(at, r.isSubrayado());
            StyleConstants.setStrikeThrough(at, r.isTachado());
            StyleConstants.setForeground(at, new Color(r.getColorRGB()));
            doc.insertString(doc.getLength(), r.getTexto(), at);
        }
    }

    private static void cerrarRun(Documento documento, Run run, StringBuilder buffer) {
        if (run != null && buffer.length() > 0) {
            run.setTexto(buffer.toString());
            documento.agregarRun(run);
        }
        buffer.setLength(0);
    }
}
