import javax.swing.JFrame;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Snek");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SnekComponent game = new SnekComponent();
        add(game);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        new Thread(game).start();
    }

}
