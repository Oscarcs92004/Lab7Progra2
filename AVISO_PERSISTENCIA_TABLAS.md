# Aviso: las tablas no sobreviven guardar/reabrir

Para quien tiene `persistencia/` — no es un reclamo, es lo que encontré probando el flujo completo.

## El problema

`Documento`/`Run`/`Tabla` (paquete `persistencia`) es un modelo distinto al que habíamos acordado en `REPARTO.md` (`DocumentoEdt`/`Formato`/`TablaDoc`). No pasa nada por sí solo, pero tiene una consecuencia concreta:

**`Tabla` no guarda dónde va la tabla dentro del texto** (no hay campo `posicion`). Y `PersistenciaEDT.aplicarA(...)` nunca vuelve a insertar un `JTable` en el `JTextPane` — solo reconstruye el texto plano a partir de los `Run`. Las celdas quedan en `documento.getTablas()`, pero nada las pone de vuelta en pantalla.

Además, `desdeStyledDocument(...)` recorre el documento carácter por carácter (`doc.getText(i, 1)`) sin comprobar `StyleConstants.getComponent(attr)`. Cuando en esa posición hay una tabla insertada con `insertComponent(...)`, ese carácter placeholder se cuela como si fuera texto normal dentro de un `Run`.

**Efecto de punta a punta:** insertás una tabla, escribís texto, guardás, cerrás, abrís de nuevo → el texto vuelve, pero la tabla desaparece del editor. El requisito 3 ("se conserva al reabrir el archivo") no se cumple aunque `GestorTablas` (mi parte) esté probada y funcionando en aislamiento.

## Lo que ya existe para no reinventar nada

`GestorTablas.java` (raíz de `src/`, sin paquete) ya tiene los dos métodos que hacen falta, probados con 13/13 casos:

```java
public ArrayList<TablaDoc> extraer(JTextPane pane)                       // saca las tablas con su posicion
public void aplicar(ArrayList<TablaDoc> tablas, JTextPane pane) throws IOException  // las vuelve a insertar en su lugar
```

`TablaDoc` ya tiene `getPosicion()`/`setPosicion(int)`.

## Dos formas de arreglarlo, la que prefieras

**Opción A — la más chica:** agregar `posicion` a `Tabla` (persistencia), llenarlo con `GestorTablas.extraer(pane)` en vez de recorrer el `JTextPane` a mano, escribirlo/leerlo en el archivo, y en `aplicarA` llamar a `GestorTablas.aplicar(tablas, pane)` **después** de reconstruir el texto y los runs (si se hace antes, los offsets de las tablas quedan mal porque el texto aún no existe).

**Opción B — saltarse Tabla/Run propios:** usar directamente `TablaDoc` en vez de tu `Tabla`, así no hay que sincronizar dos clases con el mismo dato.

En ambos casos, en `desdeStyledDocument` conviene saltar las posiciones donde `StyleConstants.getComponent(attr) != null` (no meterlas en el buffer de texto de ningún `Run`) — esas posiciones las maneja `GestorTablas`, no la sección de texto.

## No toqué nada

Este archivo es solo el aviso. No modifiqué `PersistenciaEDT.java`, `Documento.java`, `Run.java` ni `Tabla.java` — son tuyos y la decisión de cómo resolverlo (o si preferís mantener las tablas "al final" a propósito, aunque eso no es lo que pide el enunciado) es tuya.
