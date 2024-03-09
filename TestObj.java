import java.awt.*;
import java.awt.geom.AffineTransform;

public class TestObj implements Updatable, Drawable {
    protected double x, y, vx, vy;
    protected int id;
    Stopwatch stopwatch;
    public TestObj() {
        createId();  // Remember to create an id on creation
        addToPools();  // Remember to add object to pool on creation

        stopwatch = new Stopwatch();
        stopwatch.start();

        x = Math.random() * Main.windowWidth;
        y = Math.random() * Main.windowHeight;
        vx = vy = 5;
    }

    public void update() {
        if (Main.inputInfo.downPressed) {
            y += vy;
        }
        if (Main.inputInfo.upPressed) {
            y -= vy;
        }
        if (Main.inputInfo.leftPressed) {
            x -= vx;
        }
        if (Main.inputInfo.rightPressed) {
            x += vx;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);

        int rectWidth = 50;
        int rectHeight = 10;

        // Create an AffineTransform object
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        // Rotate to the cursor
        at.rotate(Math.atan2(Main.inputInfo.mouseY - y, Main.inputInfo.mouseX - x));
        // Move the transform back
        at.translate(0, -rectHeight / 2.f);

        // Apply the transform to the Graphics2D object
        g2d.setTransform(at);

        // Draw the rectangle
        g2d.fillRect(0, 0, rectWidth, rectHeight);

        // Reset the transformations
        g2d.setTransform(new AffineTransform());

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
