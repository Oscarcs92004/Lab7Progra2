# Reparto de trabajo — Lab 7 Progra II (Editor de texto con formato binario propio)

## Contexto

Proyecto: `C:\Users\cuent\IdeaProjects\Lab7Progra2\Lab7Progra2\src` (paquete default, sin `package`).

La GUI base ya existe (`EditorTexto.java`, `Main.java`): ventana, menú Archivo/Editar, barra de herramientas (combo fuente, combo tamaño, negrita/cursiva/subrayado, botón color, botón tabla), `JTextPane` y barra de estado. Ningún control tiene listener todavía.

**Decisión del equipo:** no hay un "encargado de la GUI". La ventana ya está armada y **cada quien conecta los controles de su propia parte**. El reparto es por requisito, en tres rebanadas verticales:

| Integrante | Requisito | Controles que cablea |
|---|---|---|
| **A** | 1 — Formato de texto | combos de fuente y tamaño, botones N/K/S, botón de color |
| **B** | 2 — Persistencia `.edt` | menú Archivo completo (Nuevo, Abrir, Guardar, Guardar como) |
| **Vos (C)** | 3 — Tablas | botón `▦ Tabla` |

El requisito 4 (que se vea como un editor real) queda cubierto por la GUI base más lo que cada uno conecta.

---

## 1. Contrato compartido (los 3 juntos, primero que nada, ~15 min)

Sin esto nadie puede trabajar en paralelo. Son clases planas: campos privados, constructor, getters/setters.

**`DocumentoEdt.java`** — el objeto que viaja entre las tres partes
```java
public class DocumentoEdt {
    private String texto;
    private ArrayList<Formato> formatos;
    private ArrayList<TablaDoc> tablas;
}
```

**`Formato.java`** (lo usa A) — un rango de texto con un estilo
```java
public class Formato {
    private int inicio, longitud;
    private String fuente;
    private int tamano;
    private boolean negrita, cursiva, subrayado, tachado;
    private int colorRGB;
}
```

**`TablaDoc.java`** (lo usás vos) — una tabla y dónde va
```java
public class TablaDoc {
    private int posicion;      // offset dentro del texto
    private int filas, columnas;
    private String[][] datos;
}
```

También hay que acordar en ese momento **la estructura del archivo `.edt`** (sección 4). Es documentación que el requisito 2 pide explícitamente y que vos necesitás para escribir tu sección de tablas.

---

## 2. Tu parte — Tablas (requisito 3)

Archivos nuevos: `TablaDoc.java`, `DialogoTabla.java`, `GestorTablas.java`.

Es una rebanada vertical completa: creás la tabla, la editás, la extraés al modelo, la volvés a poner al abrir, y la escribís y leés del archivo binario. Nadie más toca tablas.

### `DialogoTabla.java`
`JDialog` modal con dos campos: filas y columnas. Validar que sean enteros mayores a 0 (y ponerle un tope razonable, tipo 50, para que nadie escriba 9999 y cuelgue la ventana). Devuelve las dos cantidades o indica que se canceló.

### `GestorTablas.java`
```java
public void insertar(JTextPane pane, int filas, int columnas)
public void extraer(JTextPane pane, DocumentoEdt doc)
public void aplicar(DocumentoEdt doc, JTextPane pane)
public void escribirSeccion(DataOutputStream salida, ArrayList<TablaDoc> tablas) throws IOException
public ArrayList<TablaDoc> leerSeccion(DataInputStream entrada) throws IOException
```

**`insertar`** — crear un `JTable` con un `DefaultTableModel` de filas × columnas (celdas vacías), meterlo en un `JScrollPane` con `setPreferredSize` fijo (si no, no se ve), y `pane.insertComponent(scroll)` en la posición del cursor. Poner `tabla.setRowHeight(...)` y bordes de grilla para que se vea como tabla de Word y no como un cuadro gris.

**`extraer`** — recorrer los elementos del documento buscando los que tengan componente:
```java
Element raiz = pane.getStyledDocument().getDefaultRootElement();
// recorrer parrafos y sus hijos
// por cada hijo: Component c = StyleConstants.getComponent(hijo.getAttributes());
// si c != null y es el JScrollPane con el JTable -> hijo.getStartOffset() es la posicion
```
Sacar filas, columnas y celdas del `TableModel` y armar el `TablaDoc`.

**`aplicar`** — el detalle que hace que todo esto funcione: un componente insertado ocupa **exactamente un carácter** en el documento. Cuando A reconstruye el texto, ese carácter viene incluido como un espacio. Entonces no insertás la tabla, la **reemplazás**:
```java
pane.setSelectionStart(posicion);
pane.setSelectionEnd(posicion + 1);
pane.replaceSelection("");
pane.setCaretPosition(posicion);
pane.insertComponent(scrollConLaTabla);
```
Quitás un carácter y ponés uno: el largo del documento no cambia, así que los offsets de todas las tablas siguientes siguen siendo válidos y no importa el orden en que las apliques. Si en vez de esto hacés solo `insertComponent`, cada tabla corre en +1 a todas las demás y la segunda en adelante aparecen movidas.

**`escribirSeccion` / `leerSeccion`** — la sección TABLAS del formato binario (sección 4). B te llama estos dos métodos desde su escritor y su lector; no tiene que saber nada de tablas.

### Cablear tu botón
En `EditorTexto`, el `btnTabla` abre `DialogoTabla` y si el usuario acepta llama a `gestorTablas.insertar(...)`.

### Tu prueba, sin depender de nadie
Un `main` propio con un `JFrame` que solo tenga un `JTextPane` y un botón: insertar dos tablas separadas por texto, llenar celdas, y verificar que `extraer` devuelve las dos con las posiciones y datos correctos. Podés imprimir el `DocumentoEdt` resultante por consola. Así avanzás aunque A y B todavía no tengan nada.

---

## 3. Las otras dos partes (resumen para coordinar)

### Integrante A — Formato de texto (requisito 1)
Archivos: `Formateador.java`, `ConversorFormato.java`.

- `Formateador` aplica atributos a la selección con `SimpleAttributeSet` + `StyledDocument.setCharacterAttributes(inicio, longitud, atributos, false)`. Ese `false` final es lo que permite **combinar** estilos (rojo + negrita + 16 + Arial sobre el mismo texto), que es el punto del requisito.
- Métodos: negrita, cursiva, subrayado, **tachado**, fuente, tamaño, color.
- **Falta el botón de Tachado en la barra** — el requisito lo pide y la GUI actual no lo tiene. Le toca a A agregarlo.
- Si no hay selección, aplicar al `InputAttributes` para que afecte lo que se escriba después.
- `CaretListener` que sincronice los combos y los toggles con la posición del cursor. Sin esto el editor se siente roto.
- `ConversorFormato.extraer(pane, doc)` / `.aplicar(doc, pane)`: texto plano y rangos de formato, hacia y desde `DocumentoEdt`. **No toca tablas.**

### Integrante B — Persistencia `.edt` (requisito 2)
Archivos: `EscritorEdt.java`, `LectorEdt.java`, `FORMATO.md`.

- Solo `DataOutputStream` / `DataInputStream` sobre `FileOutputStream` / `FileInputStream`. Cero librerías externas.
- `guardar(DocumentoEdt doc, File archivo)` y `abrir(File archivo)`.
- Para la sección de tablas llama a tus dos métodos de `GestorTablas`.
- Cablea el menú Archivo entero, con `JFileChooser` + `FileNameExtensionFilter("Documento del editor", "edt")`, agregando `.edt` a mano si el usuario no lo escribe.
- **Orden al abrir:** primero `ConversorFormato.aplicar` (texto y formatos), después `GestorTablas.aplicar` (tablas). Al revés no funciona.
- Escribe `FORMATO.md` con la estructura de abajo — es entregable del requisito 2.

---

## 4. Estructura del archivo `.edt` (orden fijo, acordada por los 3)

```
CABECERA
  magia        4 bytes    'E' 'D' 'T' '1'
  version      int (4)    = 1
  reservado    int (4)    = 0

SECCION TEXTO
  idSeccion    int        = 1
  largo        int        cantidad de caracteres
  caracteres   largo*2    writeChars(texto)

SECCION FORMATO
  idSeccion    int        = 2
  cantidad     int        numero de rangos
  por cada rango:
    inicio     int
    longitud   int
    fuente     UTF
    tamano     int
    negrita    boolean    1 byte
    cursiva    boolean    1 byte
    subrayado  boolean    1 byte
    tachado    boolean    1 byte
    colorRGB   int        (Color.getRGB)

SECCION TABLAS          <- tuya
  idSeccion    int        = 3
  cantidad     int
  por cada tabla:
    posicion   int
    filas      int
    columnas   int
    celdas     filas*columnas de UTF, fila por fila

CIERRE
  magiaFin     int        0x454E4400   ('E','N','D',0)
```

**Por qué el texto va con `writeInt` + `writeChars` y no con `writeUTF`:** `writeUTF` revienta pasando los 64 KB. Los nombres de fuente y las celdas sí caben cómodos en `writeUTF`.

La `magiaFin` no es adorno: es lo único que detecta un archivo truncado que por casualidad tenía cabecera válida.

### Manejo de errores (lo hace B, pero afecta tu sección)

| Caso | Cómo se detecta | Mensaje |
|---|---|---|
| No existe | `archivo.exists() == false` | "El archivo no existe." |
| Extensión equivocada | el nombre no termina en `.edt` | "El archivo no tiene extensión .edt." |
| No es un `.edt` | los 4 bytes de magia no son `EDT1` | "El archivo no es un documento del editor o está dañado." |
| Versión desconocida | `version != 1` | "Versión de archivo no soportada." |
| Truncado | `EOFException` en cualquier lectura | "El archivo está incompleto o fue truncado." |
| Sección fuera de orden | el `idSeccion` leído no es el esperado | "El archivo está corrupto (sección inválida)." |
| Datos inconsistentes | cantidad negativa, filas/columnas ≤ 0, o un rango que excede el largo del texto | "El archivo está corrupto (datos inválidos)." |
| Falta el cierre | se llega al final sin leer `magiaFin` | "El archivo está incompleto o fue truncado." |

**Regla de oro:** `abrir` arma el `DocumentoEdt` completo en memoria y solo lo devuelve si terminó bien. Si algo falla, lanza la excepción y el documento que el usuario tenía abierto queda intacto. Tu `leerSeccion` sigue la misma regla: validá `cantidad`, `filas` y `columnas` antes de reservar el arreglo.

---

## 5. Orden de trabajo

1. Los 3 juntos: crear `DocumentoEdt`, `Formato`, `TablaDoc` y acordar el formato binario de la sección 4. **Nadie arranca antes de esto.**
2. En paralelo: vos con tablas, A con formato, B con persistencia. Cada uno con su prueba aislada.
3. Integración: conectar los controles (cada quien los suyos) y probar el ciclo completo.

---

## 6. Verificación

**Tablas (lo tuyo)**
1. Insertar una tabla 3×4, llenar todas las celdas, escribir texto con formato antes y después.
2. Insertar una segunda tabla más abajo. Guardar como `prueba.edt`, Archivo → Nuevo, Archivo → Abrir.
3. Las dos tablas vuelven con sus datos, en sus posiciones correctas, y el texto de alrededor conserva su formato. Si la segunda tabla aparece corrida, el problema es el reemplazo del carácter en `aplicar`.

**Ida y vuelta de formato (A)**
Dos párrafos, uno Arial 16 rojo negrita y otro Courier New 10 cursiva subrayado tachado. Guardar, Nuevo, Abrir: idéntico.

**Errores (B)** — los cuatro casos que pide el enunciado
1. *No existe:* borrar el archivo y abrirlo desde el menú.
2. *Extensión equivocada:* renombrar un `.txt` a `.edt` y abrirlo.
3. *Truncado:* cortar el archivo a la mitad.
   ```powershell
   $b=[IO.File]::ReadAllBytes("roto.edt"); [IO.File]::WriteAllBytes("roto.edt",$b[0..([int]($b.Length/2))])
   ```
4. *Corrupto:* abrir un `.docx` renombrado a `.edt`.

En los cuatro: mensaje claro en `JOptionPane`, sin traza de excepción en consola, y el documento abierto no se pierde.
