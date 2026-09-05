/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscar
 */
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;

public class GestorColorTexto {
    
    private final JTextPane textPane;

    public GestorColorTexto(JTextPane textPane) {
        this.textPane = textPane;
    }

    public void seleccionarColor() {
        Color colorActual = obtenerColorActual();
        Color color = JColorChooser.showDialog(textPane,"Seleccionar color del texto",colorActual);
        if (color != null) {
            aplicarColor(color);
        }
    }

    private void aplicarColor(Color color) {
        StyledDocument documento = textPane.getStyledDocument();
        int inicio = textPane.getSelectionStart();
        int fin = textPane.getSelectionEnd();
        if (inicio == fin) {
            SimpleAttributeSet atributos = new SimpleAttributeSet();
            StyleConstants.setForeground(atributos,color);
            textPane.setCharacterAttributes(atributos,false);
            return;
        }
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos,color);
        documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
    }

    private Color obtenerColorActual() {
        StyledDocument documento = textPane.getStyledDocument();
        int posicion = textPane.getCaretPosition();
        if (posicion >= documento.getLength()) {
            return Color.BLACK;
        }
        return StyleConstants.getForeground(documento.getCharacterElement(posicion).getAttributes());
    }
}
