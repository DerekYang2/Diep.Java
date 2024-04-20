import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Bar implements Drawable {
    protected int id;
    protected Vector2 pos;  // CORNER position of the bar, not center
    protected int width, height, strokeWidth;
    protected Color fillCol, strokeCol;
    protected float percentage;
    boolean isHidden;

    public Bar(float width, float height, float strokeWidth, Color fillCol, Color strokeCol) {
        this.pos = new Vector2(0, 0);
        this.width = (int)Math.ceil(width);
        this.height = Math.round(height);
        this.strokeWidth = Math.round(strokeWidth);
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        percentage = 1.f;
        isHidden = false;
        createId();
        addToPools();
    }

    /**
     * Update the bar's position and percentage filled
     * @param pos The coordinate of the top-left corner of the bar
     * @param percentage The percentage of the bar filled
     */
    public void update(Vector2 pos, float percentage) {
        this.pos = pos;
        this.percentage = percentage;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    @Override
    public void draw() {
        // Culling
        if (pos.x + width < Main.cameraBox.x || pos.x > Main.cameraBox.x + Main.cameraBox.width || pos.y + height < Main.cameraBox.y || pos.y > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        if (!isHidden) {
            int xInt = Math.round(pos.x);
            int yInt = Math.round(pos.y);
            Graphics.drawRectangleRounded(xInt, yInt, width, height, 1f, strokeCol);
            Graphics.drawRectangleRounded(xInt + strokeWidth, yInt + strokeWidth, width * percentage - 2 * strokeWidth, height - 2 * strokeWidth, 1f, fillCol);
        }
    }

    public void setWidth(float width) {
        this.width = (int)Math.ceil(width);
    }

    public boolean isHidden() {
        return isHidden;
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
        Main.drawablePool.addObj(this, DrawPool.TOP);
    }

    @Override
    public void delete() {
        Main.idServer.returnId(this.getId());
        Main.drawablePool.deleteObj(this.getId(), DrawPool.TOP);
    }
}
