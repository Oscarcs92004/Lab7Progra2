
import javax.swing.SwingUtilities;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscar
 */
public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            EditorTexto editor = new EditorTexto();

            editor.setVisible(true);
        });
    }
}
