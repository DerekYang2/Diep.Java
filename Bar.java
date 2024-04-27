import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

// TODO: Animation for health bar

public class Bar implements Drawable {
    protected int id;
    protected Vector2 pos;  // CORNER position of the bar, not center
    protected int width, height, strokeWidth;
    protected Color fillCol, strokeCol;
    protected float percentage;
    float opacity;
    boolean isHiding;

    public Bar(float width, float height, float strokeWidth, Color fillCol, Color strokeCol) {
        this.pos = new Vector2(0, 0);
        this.width = (int)Math.ceil(width);
        this.height = Math.round(height);
        this.strokeWidth = Math.round(strokeWidth);
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        percentage = 1.f;
        isHiding = false;
        opacity = 1;
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

        if (isHiding) {
            opacity -= 1.f/12;
            opacity = Math.max(opacity, 0);  // Keep positive
        } else {
            opacity += 1.f/12;
            opacity = Math.min(opacity, 1);  // Keep less than 1
        }
    }

    public void triggerHidden(boolean isHidden) {
        this.isHiding = isHidden;
    }

    public void forceHidden(boolean isHidden) {
        this.isHiding = isHidden;
        if (isHidden) {
            opacity = 0;
        } else {
            opacity = 1;
        }
    }

    @Override
    public void draw() {
        // Culling
        if (pos.x + width < Main.cameraBox.x || pos.x > Main.cameraBox.x + Main.cameraBox.width || pos.y + height < Main.cameraBox.y || pos.y > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        if (!isHidden()) {
            int xInt = Math.round(pos.x);
            int yInt = Math.round(pos.y);
            Graphics.drawRectangleRounded(xInt, yInt, width, height, 1f, Graphics.colAlpha(strokeCol, opacity));
            Graphics.drawRectangleRounded(xInt + strokeWidth, yInt + strokeWidth, width * percentage - 2 * strokeWidth, height - 2 * strokeWidth, 1f, Graphics.colAlpha(fillCol, opacity));
        }
    }

    public void setWidth(float width) {
        this.width = (int)Math.ceil(width);
    }

    public boolean isHiding() {
        return isHiding;
    }

    public boolean isHidden() {
        return isHiding && opacity == 0;
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
        Main.drawablePool.addObj(this, DrawPool.TOP_UI);
    }

    @Override
    public void delete() {
        Main.idServer.returnId(this.getId());
        Main.drawablePool.deleteObj(this.getId(), DrawPool.TOP_UI);
    }
}
