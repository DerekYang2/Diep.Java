import com.raylib.java.core.Color;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.shapes.rShapes;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Mouse.MouseButton.*;

public class TestObj implements Updatable, Drawable {
    protected double x, y, vx, vy;
    protected int id;
    Stopwatch stopwatch;
    final double friction = 0.92f;
    final double mass = 1.0f;
    final double moveForceX = 0.4f;
    final double moveForceY = 0.4f;
    double velMax = 10.0f;  // Max velocity in a single direction

    // Delaying shot frequency
    double shootStart = 0.0;
    boolean canShoot = true;
    double shotDelayMs = 500;
    
    
    public TestObj() {
        createId();  // Remember to create an id on creation
        addToPools();  // Remember to add object to pool on creation

        stopwatch = new Stopwatch();
        stopwatch.start();

        x = 0; y = 0;
        vx = vy = 0;
    }

    public void update() {
        // Cap Velocity
        vx = Math.signum(vx) * Math.min(Math.abs(vx), velMax);
        vy = Math.signum(vy) * Math.min(Math.abs(vy), velMax);
        // Update position
        x += vx;
        y += vy;
        // Apply friction
        vx *= friction;
        vy *= friction;
        if (Graphics.isKeyPressed(KEY_S) ) {
            addForce(0, moveForceY);
        }
        if (Graphics.isKeyPressed(KEY_W)) {
            addForce(0, -moveForceY);
        }
        if (Graphics.isKeyPressed(KEY_A)) {
            addForce(-moveForceX, 0);
        }
        if (Graphics.isKeyPressed(KEY_D)) {
            addForce(moveForceX, 0);
        }

        if (Graphics.isLeftMouseDown()) {
            //new Bullet(x, y, , 50);
        }

/*        if (Main.inputInfo.attackPressed) {
            if (stopwatch.ms() - shootStart > shotDelayMs) {
                shootStart = stopwatch.ms();
                shoot();
            } 
        }*/
    }

    public void addForce(double fx, double fy) {
        vx += fx / mass;
        vy += fy / mass;
    }

    public static void shoot() {
        // new Bullet(x, y, , 50);
    }

    public void draw() {

        int rectWidth = 50;
        int rectHeight = 10;

/*
        // Create an AffineTransform object
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        // Rotate to the cursor
        at.rotate(Math.atan2(Main.inputInfo.mouseY - y, Main.inputInfo.mouseX - x));
        // Centering graphic draw origin
        at.translate(0, -rectHeight / 2.f);
        
        // Apply the transform to the Graphics2D object
        g2d.setTransform(at);
        
        // Draw the rectangle
        g2d.fillRect(0, 0, rectWidth, rectHeight);
        
        // Reset the transformations
        g2d.setTransform(new AffineTransform());
*/


        // Equivalent in raylib
        rShapes.DrawRectanglePro(new Rectangle((float) x, (float) y, rectWidth, rectHeight), new Vector2(0, -rectHeight / 2.f), (float) Math.atan2(Graphics.getVirtualMouse().y - y, Graphics.getVirtualMouse().x - x), Color.WHITE);

        Graphics.drawCircle((int)x-15, (int)y-15, 30, Color.RED);
    }

    // Deletable Methods
    public void createId() {
        this.id = Main.idServer.getId();
    }

    public int getId() {
        return this.id;
    }

    public void addToPools() {
        Main.drawablePool.addObj(this);
        Main.updatablePool.addObj(this);
    }

    public void delete() {
        // All added to wait lists
        Main.drawablePool.deleteObj(this.getId());
        Main.updatablePool.deleteObj(this.getId());
    }
}
