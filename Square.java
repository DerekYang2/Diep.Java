import com.raylib.java.core.Color;

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

        x = Math.random() * Graphics.cameraWidth;
        y = Math.random() * Graphics.cameraHeight;
        vx = Math.random() * 5;
        vy = Math.random() * 5;
    }

    public void update() {
        x += vx;
        y += vy;
        // Bounce off walls
        if (x < 0 || x > Graphics.cameraWidth) {
            vx *= -1;
        }
        if (y < 0 || y > Graphics.cameraHeight) {
            vy *= -1;
        }
    }

    public void draw() {
        int squareLength = 10;
        int squareWidth = 10;
        Graphics.drawRectangle((int) x, (int) y, squareWidth, squareLength, Color.BLUE);
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
