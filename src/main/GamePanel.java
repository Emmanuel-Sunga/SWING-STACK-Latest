package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    // Screen settings
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 720;
    final int FPS = 60;

    // Game components
    Thread gameThread;
    PlayManager pm;
    Menu menu;
    public static Sound music = new Sound();
    public static Sound se = new Sound();

    public GamePanel() {
        // Panel setup
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);

        // Initialize components
        pm = new PlayManager();
        menu = new Menu();

        // Keyboard input
        addKeyListener(new KeyHandler());
        setFocusable(true);
        requestFocusInWindow();

        // Mouse click handling
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (PlayManager.gameState == GameState.MENU) {
                    if (menu.handleClick(e.getX(), e.getY())) {
                        // Start the game
                        PlayManager.gameState = GameState.PLAYING;
                        PlayManager.staticBlocks.clear();
                        resetGame();
                    }
                }
            }
        });

        // Mouse hover handling
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (PlayManager.gameState == GameState.MENU) {
                    menu.handleHover(e.getX(), e.getY());
                }
            }
        });
    }

    private void resetGame() {
        pm.currentMino = pm.pickMino();
        pm.currentMino.setXY(pm.MINO_START_X, pm.MINO_START_Y);
        pm.nextMino = pm.pickMino();
        pm.nextMino.setXY(pm.NEXTMINO_X, pm.NEXTMINO_Y);
        repaint();
    }

    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();

        music.play(0, true);
        music.loop();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        if (KeyHandler.pausePressed) {
            if (PlayManager.gameState == GameState.PLAYING) {
                PlayManager.gameState = GameState.PAUSED;
                GamePanel.music.pause();
            } else if (PlayManager.gameState == GameState.PAUSED) {
                PlayManager.gameState = GameState.PLAYING;
                GamePanel.music.resume();
            }
            KeyHandler.pausePressed = false;
        }

        if (PlayManager.gameState == GameState.PLAYING && !pm.gameOver) {
            pm.update();
        } else if (PlayManager.gameState == GameState.MENU) {
            menu.update(); // Animate title and hover effects
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Always draw game scene (background, grid, etc.)
        pm.draw(g2);

        // Overlay menu if in MENU state
        if (PlayManager.gameState == GameState.MENU) {
            menu.draw(g2);
        }
    }
}
