import java.awt.*;
import java.awt.geom.AffineTransform;

public class TestTwin implements Updatable, Drawable {
    protected static double x, y, vx, vy;
    protected static double xTranslate, yTranslate;

    protected static double direction, direction2;
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
    }

    public void addForce(double fx, double fy) {
        vx += fx / mass;
        vy += fy / mass;
    }

    public static void shoot() {
      yTranslate = Math.abs(Math.cos(direction) * (26 / 4 + 0.5));
      xTranslate = Math.abs(Math.sin(direction) * (26 / 4 + 0.5));
        System.out.println("Direction: " + direction);
        System.out.println("Cos: " + Math.signum(Math.cos(direction)) + " Sin: " + Math.signum(Math.sin(direction)));
        // Calculate origin based on rotation 
        // TODO: simplify this trig calculation
        yTranslate = Math.abs(Math.cos(direction) * (26 / 4 + 0.5));
        xTranslate = Math.abs(Math.sin(direction) * (26 / 4 + 0.5));

        
        if (direction > 0 && direction < Math.PI / 2) {
          new Bullet(x + xTranslate, y - yTranslate, Main.inputInfo.mouseX + xTranslate, Main.inputInfo.mouseY - yTranslate, 40);
          new Bullet(x - xTranslate, y + yTranslate, Main.inputInfo.mouseX - xTranslate, Main.inputInfo.mouseY + yTranslate, 40);
          p1x = x + xTranslate;
          p1y = y - yTranslate;
          p2x = x - xTranslate;
          p2y = y + yTranslate;
        } else if (direction > Math.PI /2 && direction < Math.PI) {
          new Bullet(x + xTranslate, y + yTranslate, Main.inputInfo.mouseX + xTranslate, Main.inputInfo.mouseY + yTranslate, 40);
          new Bullet(x - xTranslate, y - yTranslate, Main.inputInfo.mouseX - xTranslate, Main.inputInfo.mouseY - yTranslate, 40);
          p1x = x + xTranslate;
          p1y = y + yTranslate;
          p2x = x - xTranslate;
          p2y = y - yTranslate;
        } else if (direction < 0 && direction > -Math.PI / 2) {
          new Bullet(x - xTranslate, y - yTranslate, Main.inputInfo.mouseX - xTranslate, Main.inputInfo.mouseY - yTranslate, 40);
          new Bullet(x + xTranslate, y + yTranslate, Main.inputInfo.mouseX + xTranslate, Main.inputInfo.mouseY + yTranslate, 40);
          p1x = x - xTranslate;
          p1y = y - yTranslate;
          p2x = x + xTranslate;
          p2y = y + yTranslate;
        } else if (direction < -Math.PI / 2 && direction > -Math.PI) {
          new Bullet(x - xTranslate, y + yTranslate, Main.inputInfo.mouseX - xTranslate, Main.inputInfo.mouseY + yTranslate, 40);
          new Bullet(x + xTranslate, y - yTranslate, Main.inputInfo.mouseX + xTranslate, Main.inputInfo.mouseY - yTranslate, 40);
          p1x = x - xTranslate;
          p1y = y + yTranslate;
          p2x = x + xTranslate;
          p2y = y - yTranslate;
        }


        // System.out.println("xTranslate: " + xTranslate);
        // System.out.println("xTranslate1: " + (x - xTranslate));
        // System.out.println("xTranslate2: " + (x + xTranslate));
        // System.out.println("yTranslate:" + yTranslate);


        // The bullet must be aimed at some distance away from the cursor, making the bullets parallel
        
       
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);

        int[] xPoints = {0, 0, 40, 40, 20, 20, 40, 40, 0};
        int[] yPoints = {0, 26, 26, 14, 14, 12, 12, 0, 0};

        


        // Create an AffineTransform object
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        // Rotate to the cursor
        at.rotate(direction);
        // Centering the transformation
        at.translate(0, -26 / 2.f);

        // Apply the transform to the Graphics2D object
        g2d.setTransform(at);
        
        g2d.fillPolygon(xPoints, yPoints, 9);
        
        g2d.setTransform(new AffineTransform());
        
        g.setColor(Color.red);
        
        g.fillOval((int)x - 15, (int)y - 15, 30, 30);

        
        
        g.setColor(Color.green);
        g.fillOval((int)p1x - 2, (int)p1y - 2, 4, 4);
        g.setColor(Color.pink);
        g.fillOval((int)p2x - 2, (int)p2y - 2, 4, 4);

        g.setColor(Color.cyan);
        g.fillOval((int)x - 2, (int)y - 2, 4, 4);


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
