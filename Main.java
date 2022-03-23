import javax.swing.SwingUtilities;

public class Main {
    private static void createAndShowGUI() {
        new MainFrame().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
}