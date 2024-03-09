import java.awt.Color;
import java.awt.Graphics;

public class TestObj implements Updatable, Drawable {
    protected int x, y, id;
    Stopwatch stopwatch;
    public TestObj() {
        createId();  // Remember to create an id on creation
        addToPools();  // Remember to add object to pool on creation

        stopwatch = new Stopwatch();
        stopwatch.start();

        x = 0;
        y = 0;
    }

    public void update() {
        x++;
        y++;
        if (stopwatch.s() > 3) {
            delete();
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, 50, 50);
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
