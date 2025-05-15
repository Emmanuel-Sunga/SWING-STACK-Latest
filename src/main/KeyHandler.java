package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public static boolean upPressed, downPressed, leftPressed, rightPressed;
    public static boolean pausePressed, hardDropPressed, holdPressed;
    public static boolean enterPressed;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Pause handling (works globally)
        if (code == KeyEvent.VK_ESCAPE) {
            pausePressed = true;
        }

        // ENTER key handling (works in all states, including GAME_OVER)
        if (PlayManager.gameState == GameState.GAME_OVER) {
            enterPressed = true;
        }

        // Game controls (only apply during PLAYING state)
        if (PlayManager.gameState == GameState.PLAYING) {
            if (code == KeyEvent.VK_W) upPressed = true;
            if (code == KeyEvent.VK_A) leftPressed = true;
            if (code == KeyEvent.VK_S) downPressed = true;
            if (code == KeyEvent.VK_D) rightPressed = true;
            if (code == KeyEvent.VK_SPACE) hardDropPressed = true;
            if (code == KeyEvent.VK_C) holdPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_ESCAPE) pausePressed = false;
        if (code == KeyEvent.VK_SPACE) hardDropPressed = false;
        if (code == KeyEvent.VK_C) holdPressed = false;
        if (code == KeyEvent.VK_ENTER) enterPressed = false;
    }

    public static void resetKeyStates() {
        upPressed = downPressed = leftPressed = rightPressed = false;
        pausePressed = holdPressed = hardDropPressed = false;
        enterPressed = false;
    }
}
