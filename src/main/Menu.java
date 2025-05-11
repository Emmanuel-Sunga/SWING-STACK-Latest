package main;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.AffineTransform;

public class Menu {
    // Constants
    private static final Color BG_TOP_COLOR = new Color(0, 0, 0, 255);
    private static final Color BG_BOTTOM_COLOR = new Color(50, 50, 100, 180);
    private static final Color TITLE_BG_COLOR = new Color(20, 20, 20, 200);
    private static final Color TITLE_BORDER_COLOR = new Color(255, 255, 255, 100);
    private static final Color PLAY_BUTTON_TOP_COLOR = new Color(100, 255, 100);
    private static final Color PLAY_BUTTON_BOTTOM_COLOR = new Color(50, 200, 50);
    private static final Color QUIT_BUTTON_TOP_COLOR = new Color(255, 100, 100);
    private static final Color QUIT_BUTTON_BOTTOM_COLOR = new Color(200, 50, 50);
    private static final int BUTTON_CORNER_RADIUS = 20;
    private static final int TITLE_BOX_CORNER_RADIUS = 30;

    // UI Elements
    private final RoundRectangle2D playButton = new RoundRectangle2D.Double(450, 300, 200, 50, BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
    private final RoundRectangle2D quitButton = new RoundRectangle2D.Double(450, 400, 200, 50, BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
    private final RoundRectangle2D titleBox = new RoundRectangle2D.Double(350, 80, 400, 120, TITLE_BOX_CORNER_RADIUS, TITLE_BOX_CORNER_RADIUS);

    // Animation variables
    private float buttonHoverAnimation = 0f;
    private int currentlyHovered = 0; // 0 = none, 1 = play, 2 = quit
    private float colorShift = 0f;
    private final long startTime = System.currentTimeMillis();

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        GradientPaint bgGradient = new GradientPaint(
                0, 0, BG_TOP_COLOR,
                0, GamePanel.HEIGHT, BG_BOTTOM_COLOR
        );
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // Title box
        g2.setColor(TITLE_BG_COLOR);
        g2.fill(titleBox);
        g2.setColor(TITLE_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.draw(titleBox);

        drawAnimatedTitle(g2);

        // Subtitle
        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        int subtitleOffset = (int)(Math.sin(time * 2 + 1) * 3);
        g2.setColor(new Color(200, 200, 255));
        g2.setFont(new Font("Arial", Font.ITALIC, 24));
        g2.drawString("The Classic Block Game", 418, 190 + subtitleOffset);

        // Buttons
        drawButton(g2, playButton, "PLAY", PLAY_BUTTON_TOP_COLOR, PLAY_BUTTON_BOTTOM_COLOR,
                currentlyHovered == 1 ? buttonHoverAnimation : 0f);
        drawButton(g2, quitButton, "QUIT", QUIT_BUTTON_TOP_COLOR, QUIT_BUTTON_BOTTOM_COLOR,
                currentlyHovered == 2 ? buttonHoverAnimation : 0f);

        // Footer
        g2.setColor(new Color(200, 200, 200, 150));
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("© 2025 Tetris Clone B Group 4", 450, 550);
    }

    private void drawAnimatedTitle(Graphics2D g2) {
        int titleX = 368;
        int titleBaseY = 150;

        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        int offsetY = (int)(Math.sin(time * 1.5) * 5); // Gentle float up/down

        AffineTransform originalTransform = g2.getTransform();

        g2.setFont(new Font("Impact", Font.BOLD, 55));

        // Glow layers
        for (int i = 8; i > 0; i--) {
            float alpha = i / 16f;
            g2.setColor(new Color(255, 255, 255, (int)(alpha * 50)));
            g2.drawString("SWING & STACK", titleX - i/2, titleBaseY + offsetY + i/2);
        }

        // Shadow layer
        g2.setColor(new Color(50, 50, 50, 200));
        g2.drawString("SWING & STACK", titleX + 3, titleBaseY + offsetY + 3);

        // Gradient title fill
        GradientPaint gradient = new GradientPaint(
                titleX, titleBaseY - 40, new Color(255, 50, 50), // Start color
                titleX, titleBaseY + 40, new Color(255, 150, 50)  // End color
        );
        g2.setPaint(gradient);
        g2.drawString("SWING & STACK", titleX, titleBaseY + offsetY);
        g2.setPaint(null);

        g2.setTransform(originalTransform);
    }

    private void drawButton(Graphics2D g2, RoundRectangle2D button, String text,
                            Color topColor, Color bottomColor, float hoverIntensity) {
        Color hoverTop = brighter(topColor, 0.2f * hoverIntensity);
        Color hoverBottom = brighter(bottomColor, 0.2f * hoverIntensity);

        GradientPaint buttonGradient = new GradientPaint(
                (float)button.getX(), (float)button.getY(), hoverTop,
                (float)button.getX(), (float)(button.getY() + button.getHeight()), hoverBottom
        );
        g2.setPaint(buttonGradient);
        g2.fill(button);

        g2.setColor(new Color(255, 255, 255, (int)(80 + 100 * hoverIntensity)));
        g2.setStroke(new BasicStroke(2 + 2 * hoverIntensity));
        g2.draw(button);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        int textX = (int)(button.getX() + (button.getWidth() - g2.getFontMetrics().stringWidth(text)) / 2);
        int textY = (int)(button.getY() + (button.getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);
        g2.drawString(text, textX, textY);
    }

    private Color brighter(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + 255 * factor));
        int g = Math.min(255, (int)(color.getGreen() + 255 * factor));
        int b = Math.min(255, (int)(color.getBlue() + 255 * factor));
        return new Color(r, g, b, color.getAlpha());
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - startTime;

        // Color shift (loop every 10 seconds)
        colorShift = (elapsed % 10000L) / 10000f;

        // Smooth button hover transition
        float target = (currentlyHovered != 0) ? 1f : 0f;
        buttonHoverAnimation += (target - buttonHoverAnimation) * 0.1f;
    }

    public boolean handleClick(int x, int y) {
        if (playButton.contains(x, y)) {
            return true;
        } else if (quitButton.contains(x, y)) {
            System.exit(0);
        }
        return false;
    }

    public void handleHover(int x, int y) {
        if (playButton.contains(x, y)) {
            currentlyHovered = 1;
        } else if (quitButton.contains(x, y)) {
            currentlyHovered = 2;
        } else {
            currentlyHovered = 0;
        }
    }
}
