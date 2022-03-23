import javax.swing.JFrame;

/**
 * A window with a Snake game in it!
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        // Set window title to "Snek"
        super("Snek");

        // Exit the game when X is chosen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the game panel and add it to the window
        SnekComponent game = new SnekComponent();
        add(game);

        // Resize the window to fit the game
        pack();

        // Prevent resizing the window
        setResizable(false);
        
        // Center the window
        setLocationRelativeTo(null);

        // Start the game's thread, causing it to
        // animate and become interactive
        new Thread(game).start();
    }

}
