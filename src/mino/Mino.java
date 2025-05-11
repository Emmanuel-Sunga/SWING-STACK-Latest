package mino;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.imageio.plugins.tiff.FaxTIFFTagSet;

import main.GamePanel;
import main.KeyHandler;
import main.PlayManager;

public abstract class Mino {
	
	public Block b[] = new Block[4];
	public Block tempB[] = new Block[4];
	int autoDropCounter = 0;
	public int direction = 1; //There are 4 directions (1/2/3/4)
	boolean leftCollision, rightCollision, bottomCollision;
	public boolean active = true; 
	public boolean deactivating;
	int deactivateCounter = 0;
	public int y;
	
	
	public void create(Color c) {
		b[0] = new Block(c);
		b[1] = new Block(c);
		b[2] = new Block(c);
		b[3] = new Block(c);
		tempB[0] = new Block(c);
		tempB[1] = new Block(c);
		tempB[2] = new Block(c);
		tempB[3] = new Block(c);
	}
	
	public void setXY(int x, int y) {}
	public void updateXY(int direction) {
		
		checkRotationCollision();
		
		if(leftCollision == false && rightCollision == false && bottomCollision == false) {
		
			this.direction = direction;
			b[0].x = tempB[0].x;
			b[0].y = tempB[0].y;
			b[1].x = tempB[1].x;
			b[1].y = tempB[1].y;
			b[2].x = tempB[2].x;
			b[2].y = tempB[2].y;
			b[3].x = tempB[3].x;
			b[3].y = tempB[3].y;
		}	
		
	}
	public void getDirection1() {}
	
	public void getDirection2() {}
	
	public void getDirection3() {}
	
	public void getDirection4() {}
	
	public void checkMovementCollision() {
		
		leftCollision = false;
		rightCollision = false;
		bottomCollision = false;
		
		//Check staticBlockCollision
		checkStaticBlockCollision();
		
		//Check frame collision
		//Left wall
		for (int i = 0; i < b.length; i++) {
			if (b[i].x == PlayManager.left_x) {
				leftCollision = true;
			}
		}
		
		//Right wall
		for (int i = 0; i < b.length; i++) {
			if (b[i].x + Block.SIZE == PlayManager.right_x) {
				rightCollision = true;
			}
		}
		
		//Bottom floor
		for (int i = 0; i < b.length; i++) {
			if (b[i].y + Block.SIZE == PlayManager.bottom_y) {
				bottomCollision = true;
			}
		}
	}
	
	public void checkRotationCollision() {
		
		leftCollision = false;
		rightCollision = false;
		bottomCollision = false;
		
		//Check staticBlockCollision
		checkStaticBlockCollision();
		
		//Check frame collision
		//Left wall
		for (int i = 0; i < b.length; i++) {
			if (tempB[i].x < PlayManager.left_x) {
				leftCollision = true;
			}
		}
		
		//Right wall
		for (int i = 0; i < b.length; i++) {
			if (tempB[i].x + Block.SIZE > PlayManager.right_x) {
				rightCollision = true;
			}
		}
		
		//Bottom floor
		for (int i = 0; i < b.length; i++) {
			if (tempB[i].y + Block.SIZE > PlayManager.bottom_y) {
				bottomCollision = true;
			}
		}
	}
	
	public void checkStaticBlockCollision() {
		
		for(int i = 0; i < PlayManager.staticBlocks.size(); i++) {
			
			int targetX = PlayManager.staticBlocks.get(i).x;
			int targetY = PlayManager.staticBlocks.get(i).y;
			
			//Check down
			for(int ii = 0; ii < b.length; ii++) {
				if(b[ii].y + Block.SIZE == targetY && b[ii].x == targetX) {
					bottomCollision = true;
				} 
			}
			
			//Check left
			for(int ii = 0; ii < b.length; ii++) {
				if(b[ii].x - Block.SIZE == targetX && b[ii].y == targetY) {
					leftCollision = true;
				}
			}
			
			//Check right
			for(int ii = 0; ii < b.length; ii++) {
				if(b[ii].x + Block.SIZE == targetX && b[ii].y == targetY) {
					rightCollision = true;
				}
			}
		}
	}
	
	// Add this method to your Mino class
	public void reset() {
	    // Reset rotation to default
	    direction = 1;
	    
	    // Reset states
	    active = true;
	    deactivating = false;
	    deactivateCounter = 0;
	    autoDropCounter = 0;
	    leftCollision = false;
	    rightCollision = false;
	    bottomCollision = false;
	    
	    // Note: This doesn't reset the position of blocks
	    // Each subclass might need to override this to reset block positions properly
	}
	
	public void update() {
		
		if(deactivating) {
			deactivating();
		}
		
		//move the mino
		if(KeyHandler.upPressed) {
			switch(direction) {
			case 1: getDirection2();break;
			case 2: getDirection3();break;
			case 3: getDirection4();break;
			case 4: getDirection1();break;
			}
			KeyHandler.upPressed = false;
			GamePanel.se.play(3, false);
		}
		
		checkMovementCollision();
		
		
		if(KeyHandler.downPressed) {
			//If the mino's bottom is not hitting, it can go down
			if (bottomCollision == false) {
				b[0].y += Block.SIZE;
				b[1].y += Block.SIZE;
				b[2].y += Block.SIZE;
				b[3].y += Block.SIZE;		

				//when moved down, reset the autoDropCounter
				autoDropCounter = 0;
			}
			KeyHandler.downPressed = false;
			
		}
		if(KeyHandler.leftPressed) {
			if (leftCollision == false ) {
				b[0].x -= Block.SIZE;
				b[1].x -= Block.SIZE;
				b[2].x -= Block.SIZE;
				b[3].x -= Block.SIZE;				
			}
			KeyHandler.leftPressed = false;
			
		}
		if(KeyHandler.rightPressed) {
			if (rightCollision == false) {
				b[0].x += Block.SIZE;
				b[1].x += Block.SIZE;
				b[2].x += Block.SIZE;
				b[3].x += Block.SIZE;				
			}	
			KeyHandler.rightPressed = false;
		}
		
		if(bottomCollision) {
			if(deactivating == false) {
				GamePanel.se.play(4, false);
			}
			deactivating = true;
		} else {
			autoDropCounter++; //the counter increases in every frame
			if(autoDropCounter == PlayManager.dropInterval) {
				//the mino goes down
				b[0].y += Block.SIZE;
				b[1].y += Block.SIZE;
				b[2].y += Block.SIZE;
				b[3].y += Block.SIZE;
				autoDropCounter = 0;
			}
		}
	}
	
	public int getWidth() {
	    int minX = Integer.MAX_VALUE;
	    int maxX = Integer.MIN_VALUE;
	    
	    for(Block block : b) {
	        if(block.x < minX) minX = block.x;
	        if(block.x > maxX) maxX = block.x;
	    }
	    
	    return (maxX - minX) / Block.SIZE + 1 ;
	}
	
	public int getHeight() {
	    int minY = Integer.MAX_VALUE;
	    int maxY = Integer.MIN_VALUE;
	    
	    for(Block block : b) {
	        if(block.y < minY) minY = block.y;
	        if(block.y > maxY) maxY = block.y;
	    }
	    
	    return (maxY - minY) / Block.SIZE + 1;
	}
	
	private void deactivating() {
		
		deactivateCounter++;
		
		//Wait 45 frames until deactivate
		if(deactivateCounter == 45) {
			
			deactivateCounter = 0;
			checkMovementCollision(); //Check if the bottom is still hitting
			
			//If bottom is still hitting after 45 frames, deactivate the mino
			if(bottomCollision) {
				active = false;
			}			
		}
	}
	
	
	
	public void draw(Graphics2D g2) {
		
		int margin = 2;
		g2.setColor(b[0].c);
		g2.fillRect(b[0].x+margin, b[0].y+margin, Block.SIZE-(margin*2), Block.SIZE-(margin*2));
		g2.fillRect(b[1].x+margin, b[1].y+margin, Block.SIZE-(margin*2), Block.SIZE-(margin*2));
		g2.fillRect(b[2].x+margin, b[2].y+margin, Block.SIZE-(margin*2), Block.SIZE-(margin*2));
		g2.fillRect(b[3].x+margin, b[3].y+margin, Block.SIZE-(margin*2), Block.SIZE-(margin*2));
	}
	
	public Mino createGhost() {
	    Mino ghost = null;
	    try {
	        // Create new instance of same Mino type
	        ghost = this.getClass().getDeclaredConstructor().newInstance();
	        ghost.create(this.b[0].c); // Initialize with same color

	        // Copy all block positions and properties
	        for(int i = 0; i < 4; i++) {
	            ghost.b[i].x = this.b[i].x;
	            ghost.b[i].y = this.b[i].y;
	            ghost.b[i].setGhost(true); // Mark as ghost block
	            ghost.tempB[i].x = this.tempB[i].x;
	            ghost.tempB[i].y = this.tempB[i].y;
	        }

	        // Copy all state variables
	        ghost.direction = this.direction;
	        ghost.autoDropCounter = this.autoDropCounter;
	        ghost.leftCollision = this.leftCollision;
	        ghost.rightCollision = this.rightCollision;
	        ghost.bottomCollision = this.bottomCollision;
	        ghost.active = false; // Ghost should never be active
	        ghost.deactivating = false;
	        ghost.deactivateCounter = 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        // Fallback to basic Mino if reflection fails
	        ghost = new Mino() {
	            @Override
	            public void setXY(int x, int y) {}
	        };
	        ghost.create(this.b[0].c);
	        for(int i = 0; i < 4; i++) {
	            ghost.b[i].x = this.b[i].x;
	            ghost.b[i].y = this.b[i].y;
	            ghost.b[i].setGhost(true);
	        }
	    }
	    return ghost;
	}

	// Add this helper method to update ghost position
	public void updateGhost(Mino ghost) {
	    if(ghost == null) return;
	    
	    // Update position to match current mino
	    for(int i = 0; i < 4; i++) {
	        ghost.b[i].x = this.b[i].x;
	        ghost.b[i].y = this.b[i].y;
	    }
	    ghost.direction = this.direction;
	    
	    // Update collision states
	    ghost.leftCollision = this.leftCollision;
	    ghost.rightCollision = this.rightCollision;
	    ghost.bottomCollision = this.bottomCollision;
	}
}