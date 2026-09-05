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
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class EditorTexto extends JFrame {

    private final JTextPane textPane = new JTextPane();
    private final GestorFuentes gestorFuentes = new GestorFuentes();
    private final GestorFormatoTexto gestorFormato;
    private final JComboBox<String> comboFuente;
    private final JComboBox<Integer> comboTamano = new JComboBox<>(new Integer[]{8, 10, 12, 14, 16,18, 20, 24, 28,36, 48});
    
    private final JToggleButton btnNegrita = new JToggleButton("N");
    private final JToggleButton btnCursiva = new JToggleButton("I");
    private final JToggleButton btnSubrayado = new JToggleButton("S");
    private final JToggleButton btnTachado = new JToggleButton("T");

    private final JButton btnColor = new JButton();
    private final JButton btnTabla = new JButton("▦ Tabla");

    private final GestorTablas gestorTablas = new GestorTablas();

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
        gestorFormato = new GestorFormatoTexto(textPane);
        comboFuente = new JComboBox<>(gestorFuentes.obtenerNombres());
        seleccionarFuenteInicial();
        construirMenu();
        construirBarraHerramientas();
        construirAreaTexto();
        construirBarraEstado();
    }

    private void construirMenu() {
        JMenuBar menuBar = new JMenuBar();
        int atajo = InputEvent.CTRL_DOWN_MASK;
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.add(crearItem("Nuevo",KeyEvent.VK_N,atajo));
        menuArchivo.add(crearItem("Abrir...",KeyEvent.VK_O,atajo));
        menuArchivo.addSeparator();
        menuArchivo.add(crearItem("Guardar",KeyEvent.VK_S,atajo));
        menuArchivo.add(crearItem("Guardar como...",KeyEvent.VK_S,atajo | InputEvent.SHIFT_DOWN_MASK));
        menuArchivo.addSeparator();
        menuArchivo.add(crearItem("Salir",0,0));
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
    
    private void aplicarFuente(String nombreFuente) {
        int inicio =textPane.getSelectionStart();
        int fin =textPane.getSelectionEnd();
        if (inicio == fin) {
            StyledDocument documento =textPane.getStyledDocument();
            SimpleAttributeSet atributos =new SimpleAttributeSet();
            StyleConstants.setFontFamily(atributos,nombreFuente);
            documento.setCharacterAttributes(inicio,1,atributos,false);
            return;
        }
        StyledDocument documento = textPane.getStyledDocument();
        SimpleAttributeSet atributos =new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos,nombreFuente);
        documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
    }

    private void aplicarTamano(int tamano) {
        int inicio =textPane.getSelectionStart();
        int fin =textPane.getSelectionEnd();
        StyledDocument documento =textPane.getStyledDocument();
        SimpleAttributeSet atributos =new SimpleAttributeSet();
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


