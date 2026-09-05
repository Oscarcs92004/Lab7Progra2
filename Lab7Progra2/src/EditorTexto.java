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
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class EditorTexto extends JFrame {

    private final JTextPane textPane = new JTextPane();
    // hardcodeado en lo que busco el import
    private final JComboBox<String> comboFuente = new JComboBox<>(new String[]{"Arial","Calibri","Times New Roman","Georgia","Courier New","Verdana","SansSerif","Serif"});
    private final JComboBox<Integer> comboTamano = new JComboBox<>(new Integer[]{8, 10, 12, 14, 16, 18, 20, 24, 28, 36, 48});

    private final JToggleButton btnNegrita = new JToggleButton("N");
    private final JToggleButton btnCursiva = new JToggleButton("K");
    private final JToggleButton btnSubrayado = new JToggleButton("S");

    private final JButton btnColor = new JButton();
    private final JButton btnTabla = new JButton("▦ Tabla");

    private final GestorTablas gestorTablas = new GestorTablas();

    private final JLabel etiquetaEstado = new JLabel("0 palabras");

    public EditorTexto() {
        super("Bloc de Notas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
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

    private void construirBarraHerramientas() {

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        comboFuente.setMaximumSize(
        new Dimension(160, 28));
        comboFuente.setSelectedItem("Arial");
        comboTamano.setMaximumSize(
        new Dimension(60, 28));
        comboTamano.setSelectedItem(14);
        btnNegrita.setFont(btnNegrita.getFont().deriveFont(Font.BOLD));
        btnCursiva.setFont(btnCursiva.getFont().deriveFont(Font.ITALIC));
        btnSubrayado.setToolTipText("Subrayado (Ctrl+U)");
        btnNegrita.setToolTipText("Negrita (Ctrl+B)");
        btnCursiva.setToolTipText("Cursiva (Ctrl+I)");
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
        barra.addSeparator();
        barra.add(btnColor);
        barra.addSeparator();
        barra.add(btnTabla);
        add(barra, BorderLayout.NORTH);
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


