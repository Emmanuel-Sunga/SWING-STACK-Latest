package main;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("SWING & STACK");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gp = new GamePanel();
        window.add(gp);
        
        // ===== ADD THESE 2 CRITICAL LINES =====
        gp.requestFocusInWindow();  // Ensures panel receives key inputs
        window.pack();              // Properly sizes the window
        
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gp.launchGame();
    }
}