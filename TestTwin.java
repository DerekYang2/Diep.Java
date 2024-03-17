import java.awt.*;
import java.awt.geom.AffineTransform;

public class TestTwin implements Updatable, Drawable {
    protected static double x, y, vx, vy;
    protected static double xTranslate, yTranslate;

    protected static double direction;
    protected int id;
    Stopwatch stopwatch;
    final double friction = 0.92f;
    final double mass = 1.0f;
    final double moveForceX = 0.4f;
    final double moveForceY = 0.4f;
    double velMax = 10.0f;  // Max velocity in a single direction

    static Turret t1, t2;

    // Delaying shot frequency
    double shootStart = 0.0;
    boolean canShoot = true;
    double shotDelayMs = 500;

    // Debugging Variables
    static double p1x, p1y, p2x, p2y; 
    
    
    public TestTwin() {
        createId();  // Remember to create an id on creation
        addToPools();  // Remember to add object to pool on creation
        
        stopwatch = new Stopwatch();
        stopwatch.start();
        
        x = Math.random() * Main.windowWidth;
        y = Math.random() * Main.windowHeight;
        vx = vy = 0;

        // Wait for the circle to be spawned before drawing 
        t1 = new Turret(x, y, 10, 35, 7, -1);
        t2 = new Turret(x, y, 10, 35, -7, -1);
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

        t1.updatePos(x, y, direction);
        t2.updatePos(x, y, direction);
    }

    public void addForce(double fx, double fy) {
        vx += fx / mass;
        vy += fy / mass;
    }

    public static void shoot() {
        // TODO: add bullet params
        t1.shoot();
        t2.shoot();
    }

    public void draw(Graphics g) {        
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
        Main.idServer.returnId(this.getId());
    }
}
