package main;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import mino.*;
/**
 * Manages the core gameplay mechanics, rendering, and game state.
 * This version includes optimized animations for 60 FPS.
 */
public class PlayManager {
    // Game State
    public static GameState gameState = GameState.MENU;
    
    // Play area
    final int WIDTH = 360;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;
    
    // Mino
    public Mino currentMino;
    int MINO_START_X;
    int MINO_START_Y;
    public Mino nextMino;
    public static int NEXTMINO_X;
    public static int NEXTMINO_Y;
    public Mino holdMino;
    public static int HOLDMINO_X;
    public static int HOLDMINO_Y;
    private boolean canHold = true;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();
    
    // Mino randomization
    private ArrayList<Class<? extends Mino>> minoTypes;
    private ArrayList<Mino> minoBag;
    private Random random;
    
    // Drop speed - SIGNIFICANTLY REDUCED FOR EASIER GAMEPLAY
    public static int dropInterval = 90; // Much slower initial speed (was 60)
    private final int LINES_PER_LEVEL = 10; // Many more lines needed per level (was 10)
    private final int SPEED_DECREASE_PER_LEVEL = 10; // Much smaller speed increase (was 10)
    
    // Game Over
    boolean gameOver;
    
    // Effects
    boolean effectCounterOn;
    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();
    
    // Score
    int level = 1;
    int lines;
    int score;
    
    // Ghost Block
    private GhostMino ghostMino;
    
    // Graphics variables
    private Color backgroundColor = new Color(10, 10, 35);  // Dark blue background
    private Color gridLineColor = new Color(50, 50, 100, 80); // Subtle grid lines
    private Color playAreaBorderColor = new Color(65, 105, 225); // Royal blue border
    private Color playAreaBackground = new Color(0, 0, 20); // Darker blue for play area
    private Color panelBackground = new Color(20, 20, 50, 200);
    private Color panelBorder = new Color(100, 100, 240);
    private Color panelTitleColor = new Color(220, 220, 255);
    private Font scoreFont = new Font("Arial", Font.BOLD, 24);
    private Font valueFont = new Font("Arial", Font.BOLD, 30);
    private Color scoreLabelColor = new Color(180, 180, 255);
    private Color scoreValueColor = new Color(255, 255, 255);
    private long startTime = System.currentTimeMillis();
    private int gameTime = 0;
    

    private boolean showLevelUpEffect = false;
    private int levelUpEffectCounter = 0;
    private final int LEVEL_UP_DURATION = 60; // Exactly 1 second at 60 FPS
    
    // Background stars
    private ArrayList<BackgroundStar> stars = new ArrayList<>();

    public PlayManager() {
        // Set up play area coordinates
        left_x = (GamePanel.WIDTH - WIDTH) / 2;
        right_x = left_x + WIDTH;
        top_y = 50;                             
        bottom_y = top_y + HEIGHT;
        
        // Set up starting positions
        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE; 
        MINO_START_Y = top_y;                 
        
        // Initialize piece generation
        random = new Random();
        initializeMinoTypes();
        minoBag = new ArrayList<>();
        refillBag();
        
        // Create starting pieces - with proper initialization
        currentMino = pickMino();
        currentMino.reset();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        
        // Get next mino ready
        nextMino = pickMino();
        nextMino.reset();
        Point nextPos = getCenteredMinoPosition(right_x + 130, bottom_y - 200, nextMino, true);
        NEXTMINO_X = nextPos.x;
        NEXTMINO_Y = nextPos.y;
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
        
        // Initialize hold position (will be set when mino is actually held)
        Point holdPos = getCenteredMinoPosition(left_x - 270, bottom_y - 180, new Mino_Square(), false);
        HOLDMINO_X = holdPos.x;
        HOLDMINO_Y = holdPos.y;
        
        // Create ghost mino
        ghostMino = new GhostMino(currentMino);
        ghostMino.dropToBottom();
        
        // Create background stars
        for (int i = 0; i < 100; i++) {
            stars.add(new BackgroundStar());
        }
    }

    private Point getCenteredMinoPosition(int panelX, int panelY, Mino mino, boolean isNextPanel) {
        int panelWidth = 200;
        int panelHeight = 200;
        
        // Calculate center of the panel
        int centerX = panelX + panelWidth / 2;
        int centerY = panelY + panelHeight / 2;
        
        // Calculate mino dimensions in pixels
        int minoWidth = mino.getWidth() * Block.SIZE;
        int minoHeight = mino.getHeight() * Block.SIZE;
        
        // Special adjustment for Next panel (wider display)
        if (isNextPanel) {
            minoWidth = Math.max(minoWidth, Block.SIZE * 4); // Minimum width of 4 blocks
        }
        
        // Calculate centered position
        int x = centerX - minoWidth / 2;
        int y = centerY - minoHeight / 2;
        
        return new Point(x, y);
    }

    private void initializeMinoTypes() {
        minoTypes = new ArrayList<>();
        minoTypes.add(Mino_L1.class);
        minoTypes.add(Mino_L2.class);
        minoTypes.add(Mino_Square.class);
        minoTypes.add(Mino_Bar.class);
        minoTypes.add(Mino_T.class);
        minoTypes.add(Mino_Z1.class);
        minoTypes.add(Mino_Z2.class);
    }

    private void refillBag() {
        ArrayList<Class<? extends Mino>> shuffled = new ArrayList<>(minoTypes);
        Collections.shuffle(shuffled, random);
        try {
            for (Class<? extends Mino> minoClass : shuffled) {
                minoBag.add(minoClass.getDeclaredConstructor().newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Mino pickMino() {
        if (minoBag.isEmpty()) {
            refillBag();
        }
        return minoBag.remove(0);
    }

    public void update() {
        // Update the game time
        gameTime++;
        
        // Update background stars
        for (BackgroundStar star : stars) {
            star.update();
        }
        
        // Handle restart if game is over and ENTER is pressed
        if (gameOver && KeyHandler.enterPressed) {
            KeyHandler.enterPressed = false;
            resetGame();
            return;
        }
        
        // IMPORTANT: Update level up effect regardless of game state
        // This ensures the animation completes properly
        if (showLevelUpEffect) {
            levelUpEffectCounter++;
            if (levelUpEffectCounter > LEVEL_UP_DURATION) {  // OPTIMIZED: 60 frames = 1 second at 60 FPS
                showLevelUpEffect = false;
                levelUpEffectCounter = 0;
            }
        }
        
        if (gameState != GameState.PLAYING) return;
        
        // Handle special moves
        if(KeyHandler.holdPressed && canHold) {
            holdCurrentMino();
            KeyHandler.holdPressed = false;
        }
        
        if(KeyHandler.hardDropPressed) {
            hardDrop();
            KeyHandler.hardDropPressed = false;
        }
        
        // Update ghost preview
        if (currentMino != null) {
            if (ghostMino == null) {
                ghostMino = new GhostMino(currentMino);
            }
            ghostMino.updatePosition(currentMino);
            ghostMino.dropToBottom();
        }
        
        // Handle active/landed pieces
        if (!currentMino.active) {
            addToStaticBlocks();
            checkGameOver();
            spawnNewMino();
            checkDelete();
        } else {
            currentMino.update();
        }
    }

    private void addToStaticBlocks() {
        for (int i = 0; i < 4; i++) {
            staticBlocks.add(currentMino.b[i]);
        }
        currentMino.deactivating = false;
    }
   
    private void checkGameOver() {
        if (currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y) {
            gameOver = true;
            gameState = GameState.GAME_OVER; // Add this if you have a GAME_OVER state
            GamePanel.music.stop();
            GamePanel.se.play(2, false);
        }
    }
    
    public void resetGame() {
        // Reset game state
        gameOver = false;
        gameState = GameState.PLAYING;
        
        // Clear all blocks
        staticBlocks.clear();
        
        // Reset game stats
        level = 1;
        lines = 0;
        score = 0;
        dropInterval = 90;
        
        // Reset mino bag
        minoBag.clear();
        refillBag();
        
        // Create new current mino
        currentMino = pickMino();
        currentMino.reset();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        
        // Create new next mino
        nextMino = pickMino();
        nextMino.reset();
        Point nextPos = getCenteredMinoPosition(right_x + 130, bottom_y - 200, nextMino, true);
        nextMino.setXY(nextPos.x, nextPos.y);
        
        // Reset hold
        holdMino = null;
        canHold = true;
        
        // Reset ghost
        ghostMino = new GhostMino(currentMino);
        ghostMino.dropToBottom();
        
        // Restart music
        GamePanel.music.play(0, true);
    }

    private void spawnNewMino() {
        currentMino = nextMino;
        currentMino.reset();   
        currentMino.setXY(MINO_START_X, MINO_START_Y);
   
        nextMino = pickMino();
        nextMino.reset();
        Point nextPos = getCenteredMinoPosition(right_x + 100, bottom_y - 200, nextMino, true);
        nextMino.setXY(nextPos.x, nextPos.y);
        
        canHold = true;
        
        if(checkCollision()) {
            gameOver = true;
            GamePanel.music.stop();
            GamePanel.se.play(2, false);
        }
        
        ghostMino = new GhostMino(currentMino);
        ghostMino.dropToBottom();
    }
    
    private void holdCurrentMino() {
        // If no mino is currently held
        if(holdMino == null) {
            holdMino = currentMino;
            holdMino.reset();
            Point holdPos = getCenteredMinoPosition(left_x - 270, bottom_y - 180, holdMino, false);
            holdMino.setXY(holdPos.x, holdPos.y);
            spawnNewMino();
        }
        // If swapping with held mino
        else {
            Mino temp = currentMino;
            currentMino = holdMino;
            currentMino.reset();
            currentMino.setXY(MINO_START_X, MINO_START_Y);
            
            holdMino = temp;
            holdMino.reset();
            Point holdPos = getCenteredMinoPosition(left_x - 270, bottom_y - 180, holdMino, false);
            holdMino.setXY(holdPos.x, holdPos.y);
        }
        
        ghostMino = new GhostMino(currentMino);
        ghostMino.dropToBottom();
        canHold = false;
    }
    
    private boolean checkCollision() {
        for(Block block : currentMino.b) {
            if(block.y == MINO_START_Y) {
                for(Block staticBlock : staticBlocks) {
                    if(staticBlock.x == block.x && staticBlock.y == block.y) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void hardDrop() {
        if (ghostMino != null && ghostMino.b.length > 0) {
            GamePanel.se.play(4, false);
            
            int dropDistance = ghostMino.b[0].y - currentMino.b[0].y;
            
            currentMino.y += dropDistance;
            for(Block block : currentMino.b) {
                block.y += dropDistance;
            }
            
            currentMino.active = false;
            update();
        }
    }

    private void checkDelete() {
        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        int lineCount = 0;
        
        while(x < right_x && y < bottom_y) {
            for(int i = 0; i < staticBlocks.size(); i++) {
                if(staticBlocks.get(i).x == x && staticBlocks.get(i).y == y) {
                    blockCount++;
                }
            }
            
            x += Block.SIZE;
            
            if(x == right_x) {
                if(blockCount == 12) {
                    effectCounterOn = true;
                    effectY.add(y);
                    
                    for(int i = staticBlocks.size()-1; i > -1; i--) {
                        if(staticBlocks.get(i).y == y) {
                            staticBlocks.remove(i);
                        }
                    }
                    
                    lineCount++;
                    lines++;
                    
                    if(lines % LINES_PER_LEVEL == 0 && dropInterval >= 10) {
                        level++;
                        
                        if(dropInterval > SPEED_DECREASE_PER_LEVEL * 2) {
                            dropInterval -= SPEED_DECREASE_PER_LEVEL;
                        }
                        else {
                            dropInterval -= 1;
                        }
                        
                        // ADD SOUND EFFECT FOR LEVEL UP
                        GamePanel.se.play(3, false);  // Use a different sound number for level up
                        showLevelUpEffect = true;
                        levelUpEffectCounter = 0;
                    }
                  
                    for(int i = 0; i < staticBlocks.size(); i++) {
                        if(staticBlocks.get(i).y < y) {
                            staticBlocks.get(i).y += Block.SIZE;
                        }
                    }
                }
                
                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }
        
        if(lineCount > 0) {
            GamePanel.se.play(1, false);
            int singleLineScore = 50 * level;
            score += singleLineScore * lineCount;
        }
    }
    
    public void draw(Graphics2D g2) {
        // Draw background
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        
        // Draw background stars
        for (BackgroundStar star : stars) {
            star.draw(g2);
        }
        
        // Draw play area background
        g2.setColor(playAreaBackground);
        g2.fillRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);
        
        // Draw grid lines in play area
        g2.setColor(gridLineColor);
        // Vertical grid lines
        for (int i = 1; i < WIDTH/Block.SIZE; i++) {
            g2.drawLine(left_x + i*Block.SIZE, top_y, left_x + i*Block.SIZE, bottom_y);
        }
        // Horizontal grid lines
        for (int i = 1; i < HEIGHT/Block.SIZE; i++) {
            g2.drawLine(left_x, top_y + i*Block.SIZE, right_x, top_y + i*Block.SIZE);
        }
        
        // Draw play area border (with glossy effect)
        g2.setColor(playAreaBorderColor);
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);
        
        // Add a highlight effect on top edge
        g2.setColor(new Color(150, 150, 255, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(left_x - 4, top_y - 4, right_x + 4, top_y - 4);

        // Draw next mino panel
        drawPanel(g2, right_x + 100, bottom_y - 200, 200, 200, "NEXT");
        
        // Draw hold mino panel
        drawPanel(g2, left_x - 275, bottom_y - 200, 200, 200, "HOLD");
        
        // Draw score panel
        drawPanel(g2, right_x + 100, top_y, 250, 300, "STATS");
        drawScoreInfo(g2, right_x + 100, top_y);
        
        // Draw hold mino if it exists
        if(holdMino != null) {
            holdMino.draw(g2);
        }

        if (gameState != GameState.MENU) {
            // Draw ghost mino first (behind everything)
            if (ghostMino != null) {
                ghostMino.draw(g2);
            }

            // Draw current mino
            if (currentMino != null) {
                currentMino.draw(g2);
            }

            // Draw next mino
            nextMino.draw(g2);

            // Draw static blocks
            for (Block block : staticBlocks) {
                block.draw(g2);
            }

            // Draw line clear effects - optimized for 60 FPS
            if (effectCounterOn) {
                effectCounter++;
                // Increase flash rate for 60 FPS (4 frames per flash instead of 2)
                Color flashColor = (effectCounter % 6 < 3) ? Color.red : Color.white;
                g2.setColor(flashColor);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                for (int lineY : effectY) {
                    g2.fillRect(left_x, lineY, WIDTH, Block.SIZE);
                }
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                // Shorter effect duration for 60 FPS
                if (effectCounter >= 12) {
                    effectCounterOn = false;
                    effectCounter = 0;
                    effectY.clear();
                }
            }

            // Draw game over screen
            if (gameOver) {
                drawOverlayScreen(g2, "GAME OVER", Color.RED);
            } 
            // Draw pause screen
            else if (gameState == GameState.PAUSED) {
                drawOverlayScreen(g2, "PAUSED", Color.YELLOW);
            }
        }

        // Draw title
        drawTitle(g2);
        
        // Draw level up effect (on top of everything)
        // IMPORTANT: This is drawn regardless of game state
        if (showLevelUpEffect) {
            drawLevelUpEffect(g2);
        }
    }

    private void drawPanel(Graphics2D g2, int x, int y, int width, int height, String title) {
        // Draw panel background with rounded corners
        g2.setColor(panelBackground);
        g2.fillRoundRect(x, y, width, height, 15, 15);
        
        // Draw panel border with glossy effect
        g2.setColor(panelBorder);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, width, height, 15, 15);
        
        // Draw highlight on top edge
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawLine(x + 5, y + 2, x + width - 5, y + 2);
        
        // Draw panel title
        if (title != null) {
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            g2.setColor(panelTitleColor);
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, x + (width - titleWidth)/2, y + 35);
        }
    }

    private void drawScoreInfo(Graphics2D g2, int x, int y) {
        int padding = 30;
        x += padding;
        y += 80;
        
        // Draw level with colored indicator
        g2.setFont(scoreFont);
        g2.setColor(scoreLabelColor);
        g2.drawString("LEVEL", x, y);
        
        g2.setFont(valueFont);
        g2.setColor(scoreValueColor);
        String levelStr = String.valueOf(level);
        g2.drawString(levelStr, x + 120, y);
        
        // Draw level indicator bar
        int barWidth = 180;
        int barHeight = 8;
        g2.setColor(new Color(50, 50, 100));
        g2.fillRoundRect(x, y + 10, barWidth, barHeight, 5, 5);
        
        float levelProgress = (lines % 10) / 10f;
        int progressWidth = (int)(barWidth * levelProgress);
        g2.setColor(getLevelColor(level));
        g2.fillRoundRect(x, y + 10, progressWidth, barHeight, 5, 5);
        
        y += 70;
        
        // Draw lines cleared
        g2.setFont(scoreFont);
        g2.setColor(scoreLabelColor);
        g2.drawString("LINES", x, y);
        
        g2.setFont(valueFont);
        g2.setColor(scoreValueColor);
        String linesStr = String.valueOf(lines);
        g2.drawString(linesStr, x + 120, y);
        
        y += 70;
        
        // Draw score with animated glow for high scores - optimized for 60 FPS
        g2.setFont(scoreFont);
        g2.setColor(scoreLabelColor);
        g2.drawString("SCORE", x, y);
        
        if (score > 5000) {
            // Increase frequency for 60 FPS
            float alpha = (float)(0.3f + 0.1f * Math.sin(System.currentTimeMillis() / 250.0));
            g2.setColor(new Color(255, 255, 100, (int)(alpha * 255)));
            g2.setFont(valueFont);
            g2.drawString(String.valueOf(score), x + 121, y + 1);
        }
        
        g2.setFont(valueFont);
        g2.setColor(scoreValueColor);
        String scoreStr = String.valueOf(score);
        g2.drawString(scoreStr, x + 120, y);
    }
     
    private class BackgroundStar {
        float x, y;
        float size;
        float brightness;
        float pulseSpeed;
        
        public BackgroundStar() {
            x = (float)(Math.random() * GamePanel.WIDTH);
            y = (float)(Math.random() * GamePanel.HEIGHT);
            size = (float)(Math.random() * 3 + 1);
            brightness = (float)(Math.random() * 0.5f + 0.5f);
            // Faster pulse for 60 FPS
            pulseSpeed = (float)(Math.random() * 0.1f + 0.02f);
        }
        
        public void update() {
            brightness = (float)(0.5f + 0.5f * Math.sin(gameTime * pulseSpeed));
        }
        
        public void draw(Graphics2D g2) {
            int alpha = (int)(brightness * 255);
            g2.setColor(new Color(200, 200, 255, alpha));
            g2.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
        }
    }
    
    private void drawOverlayScreen(Graphics2D g2, String message, Color textColor) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(left_x, top_y, WIDTH, HEIGHT);
        
        Font messageFont = new Font("Impact", Font.BOLD, 50);
        g2.setFont(messageFont);
        
        int textWidth = g2.getFontMetrics(messageFont).stringWidth(message);
        int textX = left_x + (WIDTH - textWidth) / 2;
        int textY = top_y + HEIGHT / 2;
        
        // Simplified glow effect for 60 FPS
        g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 80));
        g2.drawString(message, textX + 1, textY + 1);
        g2.drawString(message, textX - 1, textY - 1);
        
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(message, textX + 2, textY + 2);
        
        g2.setColor(textColor);
        g2.drawString(message, textX, textY);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(Color.WHITE);
        
        String instruction = message.equals("GAME OVER") ? 
                        "Press ENTER to restart" : 
                        "Press Esc to resume";
                        
        int instructionWidth = g2.getFontMetrics().stringWidth(instruction);
        int instructionX = left_x + (WIDTH - instructionWidth) / 2;
        
        g2.drawString(instruction, instructionX, textY + 60);
    }
    
    private void drawTitle(Graphics2D g2) {
        int titleX = left_x - 275 + 90;
        int titleY = top_y + 300;
        int titleFontSize = 50;
        
        // Optimized animation speed for 60 FPS
        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        int offsetY = (int)(Math.sin(time * 3.0) * 2);
        
        String titleText = "SWING & STACK";
        
        g2.setFont(new Font("Impact", Font.BOLD, titleFontSize));
        int textWidth = g2.getFontMetrics().stringWidth(titleText);
        
        // Simplified glow effect for 60 FPS
        for (int i = 3; i > 0; i--) {
            float alpha = i / 10f;
            g2.setColor(new Color(100, 200, 255, (int)(alpha * 50)));
            g2.drawString(titleText, titleX - textWidth/2 - i/2, titleY + offsetY + i/2);
        }
        
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(titleText, titleX - textWidth/2 + 1, titleY + offsetY + 1);
        
        GradientPaint gradient = new GradientPaint(
            titleX - textWidth/2, titleY - 15, new Color(255, 50, 50),
            titleX + textWidth/2, titleY + 15, new Color(255, 150, 50)
        );
        g2.setPaint(gradient);
        g2.drawString(titleText, titleX - textWidth/2, titleY + offsetY);
        
        g2.setColor(new Color(180, 220, 255));
        g2.setFont(new Font("Arial", Font.ITALIC, 15));
        String subtitle = "The Classic Block Game";
        int subtitleWidth = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, titleX - subtitleWidth/2, titleY + offsetY + 20);
    }
    
    /**
     * Optimized level up effect for 60 FPS
     */
    private void drawLevelUpEffect(Graphics2D g2) {
        // Save original graphics state
        AffineTransform originalTransform = g2.getTransform();
        Composite originalComposite = g2.getComposite();
        Color originalColor = g2.getColor();
        Font originalFont = g2.getFont();
        
        try {
            // Calculate progress based on 60 frames (1 second) duration
            float progress = levelUpEffectCounter / (float)LEVEL_UP_DURATION;
            
            // Center in entire game panel
            int centerX = GamePanel.WIDTH / 2;
            int centerY = GamePanel.HEIGHT / 2;
            
            // Flash effect - optimized timing for 60fps
            if (progress < 0.3f) {
                // Faster flash cycle (3 cycles in 0.3 seconds)
                float flashIntensity = (float)Math.sin(progress * Math.PI * 10) * 0.7f;
                g2.setColor(new Color(1f, 1f, 0.8f, flashIntensity * 0.7f));
                g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
            }
            
            // Pulse effect - optimized timing
            float pulse = (float)(0.5f + 0.5f * Math.sin(progress * Math.PI * 8)); // 4 pulse cycles
            float circleSize = progress < 0.5f ? progress * 2 : (1 - progress) * 2;
            float circleRadius = GamePanel.WIDTH * 0.3f * circleSize; // Slightly smaller for performance
            
            // Radial gradient
            Point2D center = new Point2D.Float(centerX, centerY);
            float[] dist = {0.0f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(255, 255, 0, (int)(220 * pulse * (1 - progress))),
                new Color(255, 150, 0, (int)(180 * pulse * (1 - progress))),
                new Color(255, 50, 0, 0)
            };
            
            RadialGradientPaint gradient = new RadialGradientPaint(
                center, circleRadius, dist, colors
            );
            
            g2.setPaint(gradient);
            g2.fillOval(
                (int)(centerX - circleRadius), 
                (int)(centerY - circleRadius), 
                (int)(circleRadius * 2), 
                (int)(circleRadius * 2)
            );
            
            // "LEVEL UP!" text - simplified shadow for performance
            g2.setFont(new Font("Impact", Font.BOLD, 60));
            String levelUpText = "LEVEL UP!";
            int textWidth = g2.getFontMetrics().stringWidth(levelUpText);
            
            // Simple shadow (fewer iterations for better performance)
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(levelUpText, centerX - textWidth/2 + 2, centerY + 2);
            
            // Text gradient
            GradientPaint textGradient = new GradientPaint(
                centerX - textWidth/2, centerY - 30, 
                new Color(255, 255, 100),
                centerX + textWidth/2, centerY + 30, 
                new Color(255, 200, 0)
            );
            g2.setPaint(textGradient);
            g2.drawString(levelUpText, centerX - textWidth/2, centerY);
            
            // Level number display
            g2.setFont(new Font("Impact", Font.BOLD, 80));
            String levelText = "LEVEL " + level;
            int levelTextWidth = g2.getFontMetrics().stringWidth(levelText);
            
            // Background for level number - faster pulse
            float bgPulse = (float)(0.4f + 0.2f * Math.sin(progress * Math.PI * 12));
            g2.setColor(new Color(255, 255, 255, (int)(120 * bgPulse)));
            g2.fillRoundRect(
                centerX - levelTextWidth/2 - 20, 
                centerY + 40, 
                levelTextWidth + 40, 
                70, 
                20, 
                20
            );
            
            // Simple shadow for level number
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(levelText, centerX - levelTextWidth/2 + 2, centerY + 92);
            
            // Level number gradient
            Color levelColor = getLevelColor(level);
            Color levelColorEnd = new Color(
                Math.min(255, levelColor.getRed() + 50),
                Math.min(255, levelColor.getGreen() + 50),
                Math.min(255, levelColor.getBlue() + 50)
            );
            
            GradientPaint levelGradient = new GradientPaint(
                centerX - levelTextWidth/2, centerY + 50, 
                levelColor,
                centerX + levelTextWidth/2, centerY + 120, 
                levelColorEnd
            );
            g2.setPaint(levelGradient);
            g2.drawString(levelText, centerX - levelTextWidth/2, centerY + 90);
            
            // Sparkles - optimized for performance
            if (progress > 0.2f && progress < 0.8f) {
                float sparkleProgress = (progress - 0.2f) / 0.6f;
                int sparkleCount = 30; // Reduced from 50 for better performance
                
                for (int i = 0; i < sparkleCount; i++) {
                    float angle = (float)(Math.PI * 2 * i / sparkleCount) + (progress * 6); // Faster rotation
                    float distance = GamePanel.WIDTH/3f * sparkleProgress;
                    int x = (int)(centerX + Math.cos(angle) * distance);
                    int y = (int)(centerY + Math.sin(angle) * distance);
                    
                    float sparkleSize = 4f + 4f * (float)Math.sin((sparkleProgress + i * 0.1f) * Math.PI * 6);
                    
                    // Simplified color calculation - using pre-defined colors in a cycle
                    Color[] sparkleColors = {
                        Color.WHITE, 
                        new Color(255, 255, 100), 
                        new Color(255, 100, 100), 
                        new Color(100, 100, 255),
                        new Color(100, 255, 100)
                    };
                    
                    Color sparkleColor = sparkleColors[i % sparkleColors.length];
                    
                    float sparkleAlpha = 1.0f - sparkleProgress;
                    g2.setColor(new Color(
                        sparkleColor.getRed(),
                        sparkleColor.getGreen(),
                        sparkleColor.getBlue(),
                        (int)(sparkleAlpha * 255)
                    ));
                    
                    g2.fillOval(
                        (int)(x - sparkleSize/2), 
                        (int)(y - sparkleSize/2), 
                        (int)sparkleSize, 
                        (int)sparkleSize
                    );
                }
            }
        } finally {
            // Restore original graphics state
            g2.setTransform(originalTransform);
            g2.setComposite(originalComposite);
            g2.setColor(originalColor);
            g2.setFont(originalFont);
        }
    }
    
    private Color getLevelColor(int level) {
        switch (level % 10) {
            case 1: return new Color(30, 144, 255);
            case 2: return new Color(50, 205, 50);
            case 3: return new Color(255, 165, 0);
            case 4: return new Color(255, 69, 0);
            case 5: return new Color(138, 43, 226);
            case 6: return new Color(220, 20, 60);
            case 7: return new Color(32, 178, 170);
            case 8: return new Color(255, 215, 0);
            case 9: return new Color(255, 20, 147);
            case 0: return new Color(255, 0, 0);
            default: return new Color(30, 144, 255);
        }
    }
}
