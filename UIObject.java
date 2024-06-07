import com.raylib.java.shapes.Rectangle;

public abstract class UIObject implements Updatable, Drawable {
    protected Scene scene;
    protected int id;
    protected Rectangle boundingBox;

    public UIObject(Scene scene, Rectangle boundingBox) {
        this.scene = scene;
        this.boundingBox = boundingBox;
        createId();
        addToPools();
    }

    @Override
    public abstract void update();
    @Override
    public abstract void draw();
    @Override
    public void delete() {
        // Return id
        Main.idServer.returnId(this.getId());
        // Delete from drawable pool
        Main.drawablePool[scene.ordinal()].deleteObj(this.getId(), DrawPool.TOP_UI);
        // Delete from UI object pool
        Main.UIObjectPool.deleteObj(this.getId());
    }

    @Override
    public void createId() {
        this.id = Main.idServer.getId();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void addToPools() {
        Main.drawablePool[scene.ordinal()].addObj(this, DrawPool.TOP_UI);
        Main.UIObjectPool.addObj(this);
    }

    public Scene getScene() {
        return this.scene;
    }

    public Rectangle getBoundingBox() {
        return this.boundingBox;
    }
}
