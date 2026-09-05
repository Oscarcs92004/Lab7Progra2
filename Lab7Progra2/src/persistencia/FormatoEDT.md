# Formato binario `.edt` (versión 1)

Editor de texto con formato — documento del equipo.
Todo el archivo se escribe y se lee con `RandomAccessFile` (orden de bytes
big-endian). Sin librerías de formato externas.

Las cadenas usan `writeUTF` / `readUTF`: 2 bytes con el largo + el texto en
UTF-8 (los mismos métodos vistos en el laboratorio de empleados).

## Orden fijo del archivo

```
+------------------------------+
| CABECERA  (16 bytes)         |
+------------------------------+
| SECCIÓN TEXTO   (cantRuns)   |
+------------------------------+
| SECCIÓN TABLAS  (cantTablas) |
+------------------------------+
| PIE  (CRC32, 4 bytes)        |
+------------------------------+
```

### CABECERA — 16 bytes

| Campo      | Tipo    | Bytes | Valor / descripción                          |
|------------|---------|-------|---------------------------------------------|
| magic      | byte[4] | 4     | `'E' 'D' 'T' '1'` (`writeBytes("EDT1")`)     |
| version    | byte    | 1     | `1`                                         |
| flags      | byte    | 1     | `0` (reservado para uso futuro)             |
| cantRuns   | int     | 4     | cantidad de runs de texto                   |
| cantTablas | int     | 4     | cantidad de tablas                          |
| reservado  | short   | 2     | `0`                                         |

### SECCIÓN TEXTO — se repite `cantRuns` veces, en orden de aparición

Un **RUN** = un tramo de texto contiguo que comparte exactamente el mismo formato.

| Campo     | Tipo    | Bytes | Descripción                                    |
|-----------|---------|-------|------------------------------------------------|
| texto     | String  | 2 + N | texto del run (`writeUTF`)                     |
| fuente    | String  | 2 + N | familia tipográfica, ej. `Arial` (`writeUTF`)  |
| tamano    | int     | 4     | tamaño de fuente en puntos                     |
| negrita   | boolean | 1     | `1` = activo, `0` = inactivo                   |
| cursiva   | boolean | 1     | `1` / `0`                                      |
| subrayado | boolean | 1     | `1` / `0`                                      |
| tachado   | boolean | 1     | `1` / `0`                                      |
| colorRGB  | int     | 4     | color de la fuente, formato `0x00RRGGBB`       |

Los 4 estilos son independientes: se combinan sobre el mismo run
(ej. rojo + negrita + tamaño 16 + Arial a la vez).

### SECCIÓN TABLAS — se repite `cantTablas` veces

Una **TABLA**:

| Campo    | Tipo   | Bytes | Descripción                                          |
|----------|--------|-------|-----------------------------------------------------|
| posicion | int    | 4     | offset dentro del texto donde va la tabla            |
| filas    | int    | 4     | número de filas                                     |
| columnas | int    | 4     | número de columnas                                  |
| celdas   | String | ...   | `filas * columnas` celdas (`writeUTF`), fila por fila|

Donde el usuario insertó una tabla, la sección de texto guarda un **espacio** en
esa posición. Al reabrir, se pone la tabla encima de ese espacio.

Grupo de 3: las celdas guardan texto plano. El formato **dentro** de las celdas
era el punto extra "(Equipo de 4)".

### PIE — 4 bytes

| Campo | Tipo | Bytes | Descripción                                             |
|-------|------|-------|--------------------------------------------------------|
| crc32 | int  | 4     | CRC32 de **todos los bytes anteriores** (offset 0 al pie) |

## Lectura y manejo de errores

Al abrir, se reconstruye byte por byte lo que se guardó. Casos de error:

| Situación                              | Cómo se detecta                                         | Excepción                        |
|----------------------------------------|--------------------------------------------------------|----------------------------------|
| El archivo no existe                   | `!archivo.exists()`                                     | `EdtException.ArchivoNoExiste`   |
| Extensión equivocada (no `.edt`)       | el nombre no termina en `.edt`                          | `EdtException.ExtensionInvalida` |
| No es un `.edt` (magic distinto)       | los primeros 4 bytes ≠ `EDT1`                           | `EdtException.ArchivoCorrupto`   |
| Versión desconocida                    | `version != 1`                                          | `EdtException.VersionNoSoportada`|
| Archivo truncado / cortado a la mitad  | se acaba el archivo antes de leer un campo (`EOFException`) o mide menos de 20 bytes | `EdtException.ArchivoTruncado`   |
| Contenido alterado / corrupto          | el CRC32 recalculado ≠ el CRC32 del pie                 | `EdtException.ArchivoCorrupto`   |
| Valores imposibles (cantRuns negativo) | validación tras leer la cabecera                        | `EdtException.ArchivoCorrupto`   |

Todas heredan de `EdtException`. La GUI captura una sola y muestra `getMessage()`
en un `JOptionPane`.

## Conexión con la GUI

```
JTextPane --PersistenciaEDT.desdeStyledDocument--> Documento --PersistenciaEDT.guardar--> archivo.edt
archivo.edt --PersistenciaEDT.abrir--> Documento --PersistenciaEDT.aplicarA--> JTextPane
```

Las tablas insertadas en el `JTextPane` las arma la parte de tablas del equipo
(pasa cada una como `Tabla` a `Documento.agregarTabla`, y al abrir recorre
`Documento.getTablas()`).
