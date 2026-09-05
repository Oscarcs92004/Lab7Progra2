package persistencia;

/**
 * Excepciones de persistencia.
 */
public class EdtException extends Exception {

    public EdtException(String mensaje) {
        super(mensaje);
    }

    // Archivo no existe.
    public static class ArchivoNoExiste extends EdtException {
        public ArchivoNoExiste(String ruta) {
            super("El archivo no existe: " + ruta);
        }
    }

    // Extension invalida.
    public static class ExtensionInvalida extends EdtException {
        public ExtensionInvalida(String nombre) {
            super("La extension debe ser .edt: " + nombre);
        }
    }

    // Archivo corrupto.
    public static class ArchivoCorrupto extends EdtException {
        public ArchivoCorrupto(String detalle) {
            super("El archivo esta corrupto: " + detalle);
        }
    }

    // Archivo truncado.
    public static class ArchivoTruncado extends EdtException {
        public ArchivoTruncado(String detalle) {
            super("El archivo esta incompleto o truncado: " + detalle);
        }
    }

    // Version no soportada.
    public static class VersionNoSoportada extends EdtException {
        public VersionNoSoportada(int version) {
            super("Version de formato no soportada: " + version);
        }
    }
}
