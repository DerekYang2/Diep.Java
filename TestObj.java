import java.awt.Color;
import java.awt.Graphics;

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
        if (Main.keyHandler.downPressed) {
            y += vy;
        }
        if (Main.keyHandler.upPressed) {
            y -= vy;
        }
        if (Main.keyHandler.leftPressed) {
            x -= vx;
        }
        if (Main.keyHandler.rightPressed) {
            x += vx;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)x, (int)y, 50, 50);
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
