import com.raylib.java.raymath.Vector2;
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

    public UIObject(Scene scene, Vector2 centerPos, Vector2 dimensions) {
        this(scene, new Rectangle(centerPos.x - (dimensions.x * 1.5f) / 2, centerPos.y - (1.1f * dimensions.y) / 2, (dimensions.x * 1.5f), (1.1f * dimensions.y)));
    }

    public boolean active() {
        return SceneManager.getScene() == scene.ordinal();
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
