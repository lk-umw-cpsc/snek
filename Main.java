import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Call createAndShowGUI from within the AWT event thread
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    /**
     * Method which will create the GUI on the AWT event thread
     */
    private static void createAndShowGUI() {
        new MainFrame().setVisible(true);
    }
}