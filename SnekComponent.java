import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.GradientPaint;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;


public class SnekComponent extends JPanel implements Runnable, KeyListener {
    // This class could be a JPanel, JComponent or java.awt.Canvas; any of them would work.
    // Implements runnable so the panel can have its own thread for running.
    // MainFrame class initializes said thread by calling new Thread().start()


    private LinkedList<SnekPiece> snek;
    private Direction direction;
    private Direction prevFrameDirection;

    private BufferedImage backgroundImage;

    private Clip pickupSound;
    private Clip deathSound;
    private Clip musicLoop;

    private boolean[][] map;

    private int length;

    private SnekPiece food;

    private final Random rng = new Random();

    public SnekComponent() {
        try {
            // Load music files
            pickupSound = AudioSystem.getClip();
            pickupSound.open(AudioSystem.getAudioInputStream(new File("sounds/food.wav")));

            deathSound = AudioSystem.getClip();
            deathSound.open(AudioSystem.getAudioInputStream(new File("sounds/death.wav")));

            musicLoop = AudioSystem.getClip();
            musicLoop.open(AudioSystem.getAudioInputStream(new File("sounds/bgm.wav")));
            // set background music clip to repeat indefinitely when played
            musicLoop.setLoopPoints(0, -1);
            musicLoop.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        // Create a gradient background image which will be painted each frame
        backgroundImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = backgroundImage.createGraphics();
        g.setPaint(new GradientPaint(0, 0, BACKGROUND_COLOR, 255, 255, BACKGROUND_COLOR2));
        g.fillRect(0, 0, 256, 256);

        // set component size to 256x256 px
        setPreferredSize(new Dimension(256, 256));
        // create 16 x 16 grid for the map; each square will be 16x16 px (16 * 16 = 256)
        map = new boolean[16][16];
    }

    /**
     * Contains the game's main loop
     */
    @Override
    public void run() {
        // Start the game in its initial state
        reset();
        // Start listening for key input
        addKeyListener(this);
        // Allow the JPanel to gain focus (required)
        setFocusable(true);
        // Start playing the background music (see contructor for looping)
        musicLoop.start();

        // main loop; moves snake by one block per frame, then waits for a set 
        // number of ms (see sleep())
        while (true) {
            // Get the location of the head in the previous frame
            SnekPiece oldHead = snek.getFirst();
            // Start the new head at the old head's location
            int newX = oldHead.x;
            int newY = oldHead.y;
            // Move the new head by 1 tile in the direction the player is moving
            switch (direction) {
                case RIGHT:
                    newX = (newX + 1) % 16; // we modulo for wrapping
                    break;
                case LEFT:
                    newX = (newX + 15) % 16; // we add 15 before modulo to "subtract" 1
                    break;
                case DOWN:
                    newY = (newY + 1) % 16;
                    break;
                case UP:
                    newY = (newY + 15) % 16; 
            }
            SnekPiece newHead;
            boolean foodEaten = false;
            // Is the player on the food tile? If so, create a new snake segment
            // and make *that* the new head
            if (newX == food.x && newY == food.y) {
                // Player the pickup sound (frame position is set to 0)
                // to make the sound clip start from the beginning
                pickupSound.setFramePosition(0);
                pickupSound.start();
                // head gets the new coordinate
                newHead = new SnekPiece(newX, newY);
                // set food eaten flag to true (needed for moveFood())
                foodEaten = true;
                length++;
            } else {
                // The player is not on the food; move the tail to the head's place
                newHead = snek.removeLast();
                newHead.x = newX;
                newHead.y = newY;
            }
            // Check to see if the player ran into themselves
            boolean collision = false;
            for (SnekPiece p : snek) {
                if (p.x == newHead.x && p.y == newHead.y) {
                    // They did; set collision flag to true
                    collision = true;
                    break;
                }
            }
            // Place the new head at the beginning of the linked list
            snek.offerFirst(newHead);
            // Move the food if it was eaten
            if (foodEaten) {
                moveFood();
            }
            repaint();
            if (collision) {
                // restart the game if the player ran into themselves
                reset();
                // play the death sound
                deathSound.setFramePosition(0);
                deathSound.start();
            }
            // retain the player the direction moved in this frame
            // (required to prevent player from moving in a bad direction)
            prevFrameDirection = direction;
            sleep();
        }
    }

    /**
     * Sleeps for an amount of time dependent on the length of the snake
     * The game sleeps for fewer milliseconds as the snake gets longer,
     * to make the game more challenging.
     */
    private void sleep() {
        try {
            Thread.sleep((long)(30 + (259.0 - length) / 256 * 70));
        } catch (InterruptedException e) {

        }
    }

    /**
     * Resets the game. This moves the food to random spot, moves the snake to
     * the upper-left corner of the screen, resets the game speed (via length),
     * and sets the direction of the snake towards the right
     */
    private void reset() {
        snek = new LinkedList<>();
        snek.add(new SnekPiece(2, 0));
        snek.add(new SnekPiece(1, 0));
        snek.add(new SnekPiece(0, 0));
        length = 3;
        direction = Direction.RIGHT;
        moveFood();
    }

    /**
     * Moves the food to a random, open spot on the map
     */
    private void moveFood() {
        // Start with all tiles available
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                map[x][y] = true;
            }
        }
        // Remove tiles that the snake is sitting on
        for (SnekPiece p : snek) {
            map[p.x][p.y] = false;
        }
        // Create a list of all the tiles the snake *isn't* sitting on
        ArrayList<SnekPiece> possiblePicks = new ArrayList<>(256);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (map[x][y]) {
                    possiblePicks.add(new SnekPiece(x, y));
                }
            }
        }

        // Move the food to a random location open tile
        food = possiblePicks.get(rng.nextInt(possiblePicks.size()));
    }

    // Colors used in drawing
    private static final Color BACKGROUND_COLOR = new Color(255, 250, 199);
    private static final Color BACKGROUND_COLOR2 = new Color(255, 246, 150);
    private static final Color BLOCK_SHADOW_COLOR = new Color(0, 0, 0, 80);
    private static final Color BLOCK_COLOR = new Color(36, 36, 35);
    private static final Color FOOD_COLOR = new Color(222, 0, 0);
    public void paint(Graphics g) {
        // draw the background gradient (generated it the class constructor)
        g.drawImage(backgroundImage, 0, 0, null);

        // draw shadows for the blocks that make up the snake
        g.setColor(BLOCK_SHADOW_COLOR);
        for (SnekPiece p : snek) {
            // bit shift by 4 has the same effect as multiplying by 16
            // but is less cpu-intensive (supposedly)
            g.fillRect(3 + (p.x << 4), 3 + (p.y << 4), 14, 14);
        }

        // draw shadow for the food
        g.fillRect(3 + (food.x << 4), 3 + (food.y << 4), 14, 14);
        // draw the food block
        g.setColor(FOOD_COLOR);
        g.fillRect(1 + (food.x << 4), 1 + (food.y << 4), 14, 14);

        // 
        g.setColor(BLOCK_COLOR);
        for (SnekPiece p : snek) {
            g.fillRect(1 + (p.x << 4), 1 + (p.y << 4), 14, 14);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Method called by Swing when the user pushes down on a key.
     * @param e Event information about the key pressed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // Set the player movement direction to the key pressed,
        // unless that key is the opposite of the direction the
        // player moved in during the last frame.
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                if (prevFrameDirection != Direction.DOWN)
                    direction = Direction.UP;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                if (prevFrameDirection != Direction.LEFT)
                    direction = Direction.RIGHT;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                if (prevFrameDirection != Direction.UP)
                    direction = Direction.DOWN;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                if (prevFrameDirection != Direction.RIGHT)
                    direction = Direction.LEFT;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    
}
