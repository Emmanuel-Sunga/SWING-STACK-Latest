package mino;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Block {
    
    public int x, y;
    public Color c;
    public static final int SIZE = 30;
    private boolean isGhost = false;
    
    public Block(Color c) {
        this.c = c;
    }
    
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setGhost(boolean isGhost) {
        this.isGhost = isGhost;
    }
    
    public void draw(Graphics2D g2) {
        int margin = 2;
        
        if(isGhost) {
            // Draw ghost block with transparency (30% opacity)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2.setColor(c);
            g2.fillRect(x+margin, y+margin, SIZE-(margin*2), SIZE-(margin*2));
            
            // Draw ghost outline (slightly darker)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2.setColor(c.darker());
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(x+margin, y+margin, SIZE-(margin*2), SIZE-(margin*2));
            
            // Reset transparency
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            // Original drawing code for solid blocks
            g2.setColor(c);
            g2.fillRect(x+margin, y+margin, SIZE-(margin*2), SIZE-(margin*2));
            
            // Add highlight effect
            g2.setColor(c.brighter());
            g2.drawRect(x+margin, y+margin, SIZE-(margin*2), SIZE-(margin*2));
        }
    }
    
    // Existing methods (if any)
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Color getColor() {
        return c;
    }
    
    public boolean isGhost() {
        return isGhost;
    }
}