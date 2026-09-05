# Formato binario `.edt` (versión 1)

Editor de texto con formato — sección de persistencia.
Todo se escribe con `RandomAccessFile` (orden de bytes **big-endian**, igual que `DataOutput`).
Cadenas de texto: bytes UTF-8, precedidas por su longitud. Sin librerías de formato externas.

## Orden fijo del archivo

```
+-----------------------------+
| CABECERA (16 bytes fijos)   |
+-----------------------------+
| SECCION TEXTO  (nRuns)      |
+-----------------------------+
| SECCION TABLAS (nTablas)    |
+-----------------------------+
| PIE (4 bytes: CRC32)        |
+-----------------------------+
```

### CABECERA — 16 bytes exactos

| Campo      | Tipo      | Bytes | Valor / descripción                         |
|------------|-----------|-------|---------------------------------------------|
| magic      | byte[4]   | 4     | `'E' 'D' 'T' '1'`  (0x45 0x44 0x54 0x31)     |
| version    | byte      | 1     | `1`                                         |
| flags      | byte      | 1     | `0` (reservado para uso futuro)             |
| nRuns      | int       | 4     | cantidad de runs de texto                   |
| nTablas    | int       | 4     | cantidad de tablas                          |
| reservado  | short     | 2     | `0`                                         |

### SECCION TEXTO — se repite `nRuns` veces, en orden de aparición

Un **RUN** = un tramo de texto contiguo que comparte exactamente el mismo formato.

| Campo      | Tipo      | Bytes    | Descripción                                            |
|------------|-----------|----------|-------------------------------------------------------|
| texto      | String    | 2 + N    | texto del run — escrito con `writeUTF` (2 bytes de largo + N bytes UTF-8) |
| fuente     | String    | 2 + N    | familia tipográfica, ej. `Arial` — `writeUTF`         |
| tamano     | int       | 4        | tamaño de fuente en puntos                            |
| negrita    | boolean   | 1        | `1` = activo, `0` = inactivo                          |
| cursiva    | boolean   | 1        | `1` / `0`                                             |
| subrayado  | boolean   | 1        | `1` / `0`                                             |
| tachado    | boolean   | 1        | `1` / `0`                                             |
| colorRGB   | int       | 4        | color de la fuente, formato `0x00RRGGBB`              |

Los 4 estilos son independientes: se pueden combinar todos sobre el mismo run
(ej. negrita + cursiva + tachado a la vez).

Nota: `writeUTF` / `readUTF` guardan primero un `short` con el largo en bytes y
después el texto en UTF-8 (los mismos métodos que se usaron en el laboratorio de
empleados). Límite: 65535 bytes por cadena, de sobra para un run.

### SECCION TABLAS — se repite `nTablas` veces

Una **TABLA**:

| Campo   | Tipo | Bytes | Descripción          |
|---------|------|-------|----------------------|
| filas   | int  | 4     | número de filas      |
| columnas| int  | 4     | número de columnas   |
| celdas  | ...  | ...   | `filas * columnas` celdas, recorridas por filas (row-major) |

Una **CELDA** (equipo de 3 → texto plano, sin formato interno):

| Campo | Tipo   | Bytes | Descripción                                   |
|-------|--------|-------|-----------------------------------------------|
| texto | String | 2 + N | contenido de la celda — `writeUTF` / `readUTF` |

> Nota: el formato **dentro** de las celdas era el punto extra "(Equipo de 4)".
> Para equipos de 4 se sustituiría esta celda por `nRuns(int) + nRuns × RUN`.

### PIE — 4 bytes

| Campo  | Tipo | Bytes | Descripción                                                        |
|--------|------|-------|-------------------------------------------------------------------|
| crc32  | int  | 4     | CRC32 de **todos los bytes anteriores** (offset 0 hasta el pie).  |

## Lectura y manejo de errores

La lectura reconstruye byte por byte lo que se guardó. Casos de error:

| Situación                                   | Cómo se detecta                                              | Excepción lanzada |
|---------------------------------------------|-------------------------------------------------------------|--------------------------|
| El archivo no existe                        | `!archivo.exists()`                                         | `EdtException.ArchivoNoExiste` |
| Extensión equivocada (no `.edt`)            | el nombre no termina en `.edt` (sin importar mayúsculas)    | `EdtException.ExtensionInvalida` |
| No es un `.edt` (magic distinto)            | los primeros 4 bytes ≠ `EDT1`                               | `EdtException.ArchivoCorrupto` |
| Versión desconocida                         | `version != 1`                                              | `EdtException.VersionNoSoportada` |
| Archivo truncado / cortado a la mitad       | se llega al fin del archivo antes de leer un campo (`EOFException`) o el archivo mide menos de 20 bytes | `EdtException.ArchivoTruncado` |
| Contenido alterado / corrupto               | el CRC32 recalculado ≠ el CRC32 del pie                     | `EdtException.ArchivoCorrupto` |
| Valores imposibles (nRuns/filas negativos)  | validación tras leer la cabecera                            | `EdtException.ArchivoCorrupto` |

Todas heredan de `EdtException`. La GUI puede capturarlas por separado
(mensaje distinto a cada una) o solo `EdtException` para todas, y mostrar
`getMessage()` en un diálogo.
