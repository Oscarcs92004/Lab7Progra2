/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscar
 */
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class GestorFormatoTexto {
    private final JTextPane textPane;

    public GestorFormatoTexto(JTextPane textPane) {
        this.textPane = textPane;
    }

    public void alternarNegrita() {
        StyledDocument documento =textPane.getStyledDocument();
        int inicio =textPane.getSelectionStart();
        int fin =textPane.getSelectionEnd();
        if (inicio == fin) {
            return;
        }
        boolean actual =obtenerNegrita(inicio);
        SimpleAttributeSet atributos =new SimpleAttributeSet();
        StyleConstants.setBold(atributos,!actual);
        documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
    }

    public void alternarCursiva() {
        StyledDocument documento =textPane.getStyledDocument();
        int inicio =textPane.getSelectionStart();
        int fin =textPane.getSelectionEnd();
        if (inicio == fin) {
            return;
        }
        boolean actual =obtenerCursiva(inicio);
        SimpleAttributeSet atributos =new SimpleAttributeSet();
        StyleConstants.setItalic(atributos,!actual);
        documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
    }

    public void alternarSubrayado() {
        StyledDocument documento =textPane.getStyledDocument();
        int inicio =textPane.getSelectionStart();
        int fin =textPane.getSelectionEnd();
        if (inicio == fin) {
            return;
        }
        boolean actual =obtenerSubrayado(inicio);
        SimpleAttributeSet atributos =new SimpleAttributeSet();
        StyleConstants.setUnderline(atributos,!actual);
        documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
    }

    private boolean obtenerNegrita(int posicion) {
        StyledDocument documento =textPane.getStyledDocument();
        return StyleConstants.isBold(documento.getCharacterElement(posicion).getAttributes());
    }

    private boolean obtenerCursiva(int posicion) {
        StyledDocument documento =textPane.getStyledDocument();
        return StyleConstants.isItalic(documento.getCharacterElement(posicion).getAttributes());
    }

    private boolean obtenerSubrayado(int posicion) {
        StyledDocument documento =textPane.getStyledDocument();
        return StyleConstants.isUnderline(documento.getCharacterElement(posicion).getAttributes());
    }
    
    private boolean obtenerTachado(int posicion) {
        StyledDocument documento =textPane.getStyledDocument();
        return StyleConstants.isStrikeThrough(documento.getCharacterElement(posicion).getAttributes());
    }   
    
    public void alternarTachado() {
        StyledDocument documento = textPane.getStyledDocument();
        int inicio = textPane.getSelectionStart();
        int fin = textPane.getSelectionEnd();
        if (inicio == fin) {
            return;
        }
        boolean actual = obtenerTachado(inicio);
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setStrikeThrough(atributos,!actual);
        documento.setCharacterAttributes(inicio,fin - inicio,atributos,false);
    }
}
