import java.awt.*;

// Add back updatadable
public class Square implements Drawable, Updatable {
    protected double x, y, vx, vy;
    protected int id;
    Stopwatch stopwatch;

    public Square() {
        createId();  // Remember to create an id on creation
        addToPools();  // Remember to add object to pool on creation

        stopwatch = new Stopwatch();
        stopwatch.start();

        x = Math.random() * Main.windowWidth;
        y = Math.random() * Main.windowHeight;
        vx = Math.random() * 5;
        vy = Math.random() * 5;
    }

    public void update() {
        x += vx;
        y += vy;
        // Bounce off walls
        if (x < 0 || x > Main.windowWidth) {
            vx *= -1;
        }
        if (y < 0 || y > Main.windowHeight) {
            vy *= -1;
        }
    }

    public void draw(Graphics g) {
        int squareLength = 10;
        int squareWidth = 10;
        // this is not good probably
        g.setColor(Color.BLUE);
        g.fillRect((int) x, (int) y, squareWidth, squareLength);
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
