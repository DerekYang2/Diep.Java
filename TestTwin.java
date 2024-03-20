import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class TestTwin implements Updatable, Drawable {
    protected double x, y, vx, vy;
    protected double xTranslate, yTranslate;

    protected double direction;
    protected int id;
    Stopwatch stopwatch;
    final double friction = 0.92f;
    final double mass = 1.0f;
    final double moveForceX = 0.4f;
    final double moveForceY = 0.4f;
    double velMax = 10.0f;  // Max velocity in a single direction

    Turret[] turrets;

    // Delaying shot frequency
    double shootStart = 0.0;
    boolean canShoot = true;
    double shotDelayMs = 500;

    // Debugging Variables
    double p1x, p1y, p2x, p2y;
    
    
    public TestTwin() {
        createId();  // Remember to create an id on creation
        addToPools();  // Remember to add object to pool on creation
        
        stopwatch = new Stopwatch();
        stopwatch.start();
        
        x = Math.random() * Main.windowWidth;
        y = Math.random() * Main.windowHeight;
        vx = vy = 0;

        // Wait for the circle to be spawned before
        // Triple shot
        turrets = new Turret[]{
                new Turret(10, 30, 0, -Math.PI / 4),
                new Turret(10, 30, 0, 0),
                new Turret(10, 30, 0, Math.PI / 4),
        };
        // Twins
        turrets = new Turret[]{
                new Turret(10, 30, 7, 0),
                new Turret(10, 30, -7, 0)
        };
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

        // Get Direction of mouse
        direction = Math.atan2(Main.inputInfo.mouseY - y, Main.inputInfo.mouseX - x);

        
        if (Main.inputInfo.downPressed) {
            addForce(0, moveForceY);
        } 
        if (Main.inputInfo.upPressed) {
            addForce(0, -moveForceY);
        }
        if (Main.inputInfo.leftPressed) {
            addForce(-moveForceX, 0);
        }
        if (Main.inputInfo.rightPressed) {
            addForce(moveForceX, 0);
        }


        if (Main.inputInfo.attackPressed) {
            if (stopwatch.ms() - shootStart > shotDelayMs) {
                shootStart = stopwatch.ms();
                shoot();
            } 
        }
        // atan2 mouse angle
        for (Turret t : turrets) {
            t.update(x, y, direction);
        }
    }

    public void addForce(double fx, double fy) {
        vx += fx / mass;
        vy += fy / mass;
    }

    public void shoot() {
        // TODO: add bullet params
        for (Turret t : turrets) {
            t.shoot();
        }
    }

    public void draw(Graphics g) {
        // Draw Turrets
        for (Turret t : turrets) {
            t.draw(g);
        }
        g.setColor(Color.red);
        g.fillOval((int)x - 15, (int)y - 15, 30, 30);
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
