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
        // load music?
        try {
            pickupSound = AudioSystem.getClip();
            pickupSound.open(AudioSystem.getAudioInputStream(new File("sounds/food.wav")));

            deathSound = AudioSystem.getClip();
            deathSound.open(AudioSystem.getAudioInputStream(new File("sounds/death.wav")));

            musicLoop = AudioSystem.getClip();
            musicLoop.open(AudioSystem.getAudioInputStream(new File("sounds/bgm.wav")));
            musicLoop.setLoopPoints(0, -1);
            musicLoop.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        backgroundImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = backgroundImage.createGraphics();
        g.setPaint(new GradientPaint(0, 0, BACKGROUND_COLOR, 255, 255, BACKGROUND_COLOR2));
        g.fillRect(0, 0, 256, 256);
        setPreferredSize(new Dimension(256, 256));
        map = new boolean[16][16];
    }

    @Override
    public void run() {
        reset();
        addKeyListener(this);
        setFocusable(true);
        musicLoop.start();
        while (true) {
            SnekPiece oldHead = snek.getFirst();
            int newX = oldHead.x;
            int newY = oldHead.y;
            switch (direction) {
                case RIGHT:
                    newX = (newX + 1) % 16;
                    break;
                case LEFT:
                    newX = (newX + 15) % 16;
                    break;
                case DOWN:
                    newY = (newY + 1) % 16;
                    break;
                case UP:
                    newY = (newY + 15) % 16; 
            }
            SnekPiece newHead;
            boolean foodEaten = false;
            if (newX == food.x && newY == food.y) {
                pickupSound.setFramePosition(0);
                pickupSound.start();
                newHead = new SnekPiece(newX, newY);
                foodEaten = true;
                length++;
            } else {
                newHead = snek.removeLast();
                newHead.x = newX;
                newHead.y = newY;
            }
            boolean collision = false;
            for (SnekPiece p : snek) {
                if (p.x == newHead.x && p.y == newHead.y) {
                    collision = true;
                    break;
                }
            }
            snek.offerFirst(newHead);
            if (foodEaten) {
                moveFood();
            }
            repaint();
            if (collision) {
                reset();
                deathSound.setFramePosition(0);
                deathSound.start();
            }
            prevFrameDirection = direction;
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep((long)(30 + (259.0 - length) / 256 * 70));
        } catch (InterruptedException e) {

        }
    }

    private void reset() {
        snek = new LinkedList<>();
        snek.add(new SnekPiece(2, 0));
        snek.add(new SnekPiece(1, 0));
        snek.add(new SnekPiece(0, 0));
        length = 3;
        direction = Direction.RIGHT;
        moveFood();
    }

    private void moveFood() {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                map[x][y] = true;
            }
        }
        for (SnekPiece p : snek) {
            map[p.x][p.y] = false;
        }
        ArrayList<SnekPiece> possiblePicks = new ArrayList(256);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (map[x][y]) {
                    possiblePicks.add(new SnekPiece(x, y));
                }
            }
        }
        if (possiblePicks.isEmpty()) {
            // winner!
        } else {
            food = possiblePicks.get(rng.nextInt(possiblePicks.size()));
        }
    }

    private static final Color BACKGROUND_COLOR = new Color(255, 250, 199);
    private static final Color BACKGROUND_COLOR2 = new Color(255, 246, 150);
    // private static final Color BACKGROUND_COLOR2 = new Color(168, 164, 114);
    private static final Color BLOCK_SHADOW_COLOR = new Color(0, 0, 0, 80);
    private static final Color BLOCK_COLOR = new Color(36, 36, 35);
    private static final Color FOOD_COLOR = new Color(222, 0, 0);
    public void paint(Graphics g) {
        // g.setColor(BACKGROUND_COLOR);
        g.drawImage(backgroundImage, 0, 0, null);

        g.setColor(BLOCK_SHADOW_COLOR);
        for (SnekPiece p : snek) {
            g.fillRect(3 + (p.x << 4), 3 + (p.y << 4), 14, 14);
        }

        g.fillRect(3 + (food.x << 4), 3 + (food.y << 4), 14, 14);
        g.setColor(FOOD_COLOR);
        g.fillRect(1 + (food.x << 4), 1 + (food.y << 4), 14, 14);

        g.setColor(BLOCK_COLOR);
        for (SnekPiece p : snek) {
            g.fillRect(1 + (p.x << 4), 1 + (p.y << 4), 14, 14);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
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
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }
    
}
