/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscar
 */
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import persistencia.Documento;
import persistencia.EdtException;
import persistencia.PersistenciaEDT;
import persistencia.Tabla;

public class EditorTexto extends JFrame {

    private final JTextPane textPane = new JTextPane();
    private final GestorFuentes gestorFuentes = new GestorFuentes();
    private final GestorFormatoTexto gestorFormato;
    private final JComboBox<String> comboFuente;
    private final GestorColorTexto gestorColor;
    private final JComboBox<Integer> comboTamano = new JComboBox<>(new Integer[]{6, 8, 10, 12, 14, 16,18, 20, 24, 28,36, 48, 60, 72, 96});
    
    private final JToggleButton btnNegrita = new JToggleButton("N");
    private final JToggleButton btnCursiva = new JToggleButton("I");
    private final JToggleButton btnSubrayado = new JToggleButton("S");
    private final JToggleButton btnTachado = new JToggleButton("T");

    private final JButton btnColor = new JButton();
    private final JButton btnTabla = new JButton("▦ Tabla");

    private final GestorTablas gestorTablas = new GestorTablas();
    private File archivoActual = null;

    private final JLabel etiquetaEstado = new JLabel("0 palabras");

    private void seleccionarFuenteInicial() {
        if (gestorFuentes.obtenerFuente("Arial")!= null) {
            comboFuente.setSelectedItem("Arial");
        } else if (comboFuente.getItemCount() > 0) {
            comboFuente.setSelectedIndex(0);
        }
    }

    
    public EditorTexto() {
        super("Bloc de Notas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        comboFuente = new JComboBox<>(gestorFuentes.obtenerNombres());
        seleccionarFuenteInicial();
        construirMenu();
        construirBarraHerramientas();
        construirAreaTexto();
        construirBarraEstado();
        gestorFormato = new GestorFormatoTexto(textPane);
        gestorColor = new GestorColorTexto(textPane);
    }

    private void construirMenu() {
        JMenuBar menuBar = new JMenuBar();
        int atajo = InputEvent.CTRL_DOWN_MASK;
        JMenu menuArchivo = new JMenu("Archivo");

        JMenuItem itemNuevo = crearItem("Nuevo",KeyEvent.VK_N,atajo);
        JMenuItem itemAbrir = crearItem("Abrir...",KeyEvent.VK_O,atajo);
        JMenuItem itemGuardar = crearItem("Guardar",KeyEvent.VK_S,atajo);
        JMenuItem itemGuardarComo = crearItem("Guardar como...",KeyEvent.VK_S,atajo | InputEvent.SHIFT_DOWN_MASK);
        JMenuItem itemSalir = crearItem("Salir",0,0);

        itemNuevo.addActionListener(e -> accionNuevo());
        itemAbrir.addActionListener(e -> accionAbrir());
        itemGuardar.addActionListener(e -> accionGuardar());
        itemGuardarComo.addActionListener(e -> accionGuardarComo());
        itemSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        JMenu menuEditar = new JMenu("Editar");
        menuEditar.add(crearItem("Deshacer",KeyEvent.VK_Z,atajo));
        menuEditar.add(crearItem("Rehacer",KeyEvent.VK_Y,atajo));
        menuEditar.addSeparator();
        menuEditar.add(crearItem("Seleccionar todo",KeyEvent.VK_A,atajo));
        menuBar.add(menuArchivo);
        menuBar.add(menuEditar);
        setJMenuBar(menuBar);
    }

    private JMenuItem crearItem(String texto,int tecla,int modificadores) {
        JMenuItem item = new JMenuItem(texto);
        if (tecla != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(tecla, modificadores));
        }

        return item;
    }

    // ----- Menu Archivo: Nuevo / Abrir / Guardar / Guardar como -----

    private void accionNuevo() {
        textPane.setText("");
        archivoActual = null;
        setTitle("Bloc de Notas");
    }

    private void accionAbrir() {
        JFileChooser selector = crearSelector();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        try {
            // 1. Leer el archivo .edt
            Documento documento = PersistenciaEDT.abrir(archivo);

            // 2. Poner el texto con formato en el editor
            PersistenciaEDT.aplicarA(documento, textPane.getStyledDocument());

            // 3. Volver a poner las tablas en su posicion
            java.util.ArrayList<TablaDoc> tablas = new java.util.ArrayList<>();
            for (Tabla t : documento.getTablas()) {
                TablaDoc td = new TablaDoc(t.getPosicion(), t.getFilas(), t.getColumnas());
                for (int f = 0; f < t.getFilas(); f++) {
                    for (int c = 0; c < t.getColumnas(); c++) {
                        td.setDato(f, c, t.getCelda(f, c));
                    }
                }
                tablas.add(td);
            }
            gestorTablas.aplicar(tablas, textPane);

            archivoActual = archivo;
            setTitle("Bloc de Notas - " + archivo.getName());
        } catch (EdtException | BadLocationException | IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "No se pudo abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionGuardar() {
        // Si el documento nunca se ha guardado, se comporta como "Guardar como"
        if (archivoActual == null) {
            accionGuardarComo();
        } else {
            guardarEn(archivoActual);
        }
    }

    private void accionGuardarComo() {
        JFileChooser selector = crearSelector();
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();

        // Asegurar que el nombre termine en .edt
        if (!archivo.getName().toLowerCase().endsWith(".edt")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".edt");
        }
        guardarEn(archivo);
    }

    private void guardarEn(File archivo) {
        try {
            // 1. Pasar el contenido del editor a un Documento
            Documento documento = PersistenciaEDT.desdeStyledDocument(textPane.getStyledDocument());

            // 2. Agregar las tablas que haya en el area de texto
            for (TablaDoc td : gestorTablas.extraer(textPane)) {
                Tabla tabla = new Tabla(td.getFilas(), td.getColumnas());
                tabla.setPosicion(td.getPosicion());
                for (int f = 0; f < td.getFilas(); f++) {
                    for (int c = 0; c < td.getColumnas(); c++) {
                        tabla.setCelda(f, c, td.getDato(f, c));
                    }
                }
                documento.agregarTabla(tabla);
            }

            // 3. Guardar en el archivo .edt
            PersistenciaEDT.guardar(documento, archivo);

            archivoActual = archivo;
            setTitle("Bloc de Notas - " + archivo.getName());
        } catch (EdtException | BadLocationException | IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "No se pudo guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JFileChooser crearSelector() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Documento del editor (*.edt)", "edt"));
        return selector;
    }
    
    private void aplicarFuente(String nombreFuente) {
        int inicio = textPane.getSelectionStart();
        int fin = textPane.getSelectionEnd();
        if (inicio == fin) {
            StyledDocument documento = textPane.getStyledDocument();
            SimpleAttributeSet atributos = new SimpleAttributeSet();
            StyleConstants.setFontFamily(atributos,nombreFuente);
            documento.setCharacterAttributes(inicio,1,atributos,false);
            return;
        }
        StyledDocument documento = textPane.getStyledDocument();
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos,nombreFuente);
        documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
    }

    private void aplicarTamano(int tamano) {
        int inicio = textPane.getSelectionStart();
        int fin = textPane.getSelectionEnd();
        StyledDocument documento = textPane.getStyledDocument();
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontSize(atributos,tamano
        );
        if (inicio != fin) {
            documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
        } else {
            textPane.setCharacterAttributes(atributos,false);
        }
    }


    private void construirBarraHerramientas() {

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        comboFuente.setMaximumSize(new Dimension(220, 28));
        comboFuente.setSelectedItem("Arial");
        comboTamano.setMaximumSize(new Dimension(70, 28));
        comboTamano.setSelectedItem(14);
        btnNegrita.setFont(btnNegrita.getFont().deriveFont(Font.BOLD));
        btnCursiva.setFont(btnCursiva.getFont().deriveFont(Font.ITALIC));
        btnSubrayado.setToolTipText("Subrayado (Ctrl+U)");
        btnNegrita.setToolTipText("Negrita (Ctrl+B)");
        btnCursiva.setToolTipText("Cursiva (Ctrl+I)");
        btnTachado.setToolTipText("Tachado (Ctrl+Shift+X)");
        btnColor.setPreferredSize(new Dimension(28, 28));
        btnColor.setToolTipText("Color de texto");
        btnColor.setBackground(Color.BLACK);
        btnTabla.setToolTipText("Insertar tabla");
        btnTabla.addActionListener(e -> {
            DialogoTabla dialogo = new DialogoTabla();
            if (dialogo.mostrar(this)) {
                gestorTablas.insertar(textPane, dialogo.getFilas(), dialogo.getColumnas());
            }
        });
        barra.add(comboFuente);
        barra.add(comboTamano);
        barra.addSeparator();
        barra.add(btnNegrita);
        barra.add(btnCursiva);
        barra.add(btnSubrayado);
        barra.add(btnTachado);
        barra.addSeparator();
        barra.add(btnColor);
        barra.addSeparator();
        barra.add(btnTabla);
        add(barra, BorderLayout.NORTH);
        
        comboFuente.addActionListener(e-> {
            String nombre = (String) comboFuente.getSelectedItem();
            if(nombre != null){
                aplicarFuente(nombre);
            }
        });
        
        comboTamano.addActionListener(e -> {
            Integer tamano = (Integer) comboTamano.getSelectedItem();
            if (tamano != null) {
                aplicarTamano(tamano);
            }
        });
        
        btnNegrita.addActionListener(e-> {
            gestorFormato.alternarNegrita();
        });

        btnCursiva.addActionListener(e-> {
            gestorFormato.alternarCursiva();
        });
        
        btnSubrayado.addActionListener(e-> {
            gestorFormato.alternarSubrayado();
        });
        
        btnTachado.addActionListener(e-> {
            gestorFormato.alternarTachado();
        });
        
        btnColor.addActionListener(e -> {
            gestorColor.seleccionarColor();
       });
        
    }

    private void construirAreaTexto() {
        textPane.setMargin(new Insets(50, 70, 50, 70));
        textPane.setFont(new Font("Arial",Font.PLAIN,14));
        JScrollPane scroll = new JScrollPane(textPane);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll,BorderLayout.CENTER);
    }

    private void construirBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(3,10,3,10));
        panel.add(etiquetaEstado,BorderLayout.WEST);
        add(panel,BorderLayout.SOUTH);
    }
}


