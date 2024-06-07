import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.text.rText;

import java.util.function.Consumer;

public class Bar implements Drawable {
    protected int id;
    protected Vector2 pos;  // CORNER position of the bar, not center
    protected int width, height, strokeWidth;
    protected Color fillCol, strokeCol;
    protected float targetPercentage;
    protected float percentage;
    float opacity;
    float lerpFactor;
    boolean isHiding;
    boolean inGameWorld;

    // Delay for bar to show
    int BEGIN_DELAY = 15;
    int delayFrames = 0;

    // Text variables
    private String text;
    private int fontSize;
    private float spacing;
    private Vector2 textDrawPos;
    // Custom draw
    private Consumer<Rectangle> customDraw = null;

    public Bar(Vector2 pos, float width, float height, float strokeWidth, Color fillCol, Color strokeCol, float lerpFactor, float initialPercentage) {
        this.pos = pos;
        this.width = (int)Math.ceil(width);
        this.height = Math.round(height);
        this.strokeWidth = Math.round(strokeWidth);
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        this.targetPercentage = this.percentage = initialPercentage;
        this.lerpFactor = lerpFactor;
        isHiding = false;
        opacity = 1;
        inGameWorld = false;
    }

    public Bar(float width, float height, float strokeWidth, Color fillCol, Color strokeCol, float lerpFactor, float initialPercentage) {
        this.pos = new Vector2(0, 0);
        this.width = (int)Math.ceil(width);
        this.height = Math.round(height);
        this.strokeWidth = Math.round(strokeWidth);
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        this.targetPercentage = this.percentage = initialPercentage;
        this.lerpFactor = lerpFactor;
        isHiding = false;
        opacity = 1;
        inGameWorld = false;
    }

    public void setText(String text, int fontSize) {
        this.text = text;
        this.fontSize = fontSize;
        spacing = -8.f * fontSize / Graphics.outlineSmallFont.getBaseSize();
        Vector2 textDimensions = rText.MeasureTextEx(Graphics.outlineSmallFont, text, fontSize, spacing);
        textDrawPos = new Vector2((int)pos.x + width * 0.5f - textDimensions.x * 0.5f, pos.y + height * 0.5f - (int)(0.99f * textDimensions.y * 0.5f));
    }

    public void setCustomDraw(Consumer<Rectangle> customDraw) {
        this.customDraw = customDraw;
    }

    public void addToGameWorld() {
        createId();
        addToPools();
        inGameWorld = true;
    }

    public void update(float percentage) {
        this.targetPercentage = Graphics.clamp(percentage, 0, 1);

        // Animate the bar (LERP)
        this.percentage += (this.targetPercentage - this.percentage) * lerpFactor;

        delayFrames = Math.max(delayFrames - 1, 0);

        if (isHiding) {
            opacity -= 1.f/12;
            opacity = Math.max(opacity, 0);  // Keep positive
        } else {
            if (delayFrames == 0) {
                opacity += 1.f / 12;
                opacity = Math.min(opacity, 1);  // Keep less than 1
            }
        }
    }

    /**
     * Update the bar's position and percentage filled
     * @param pos The coordinate of the top-left corner of the bar
     * @param percentage The percentage of the bar filled
     */
    public void update(Vector2 pos, float percentage) {
        this.pos = pos;
        update(percentage);
    }

    public void triggerHidden(boolean isHidden) {
        if (this.isHiding && !isHidden) {  // If hiding and now showing, reset delay
            delayFrames = BEGIN_DELAY;
        }
        this.isHiding = isHidden;
    }

    public void forceHidden(boolean isHidden) {
        this.isHiding = isHidden;
        if (isHidden) {
            opacity = 0; //Hides object
        } else {
            opacity = 1; //Shows object
        }
    }
    @Override
    public void draw() {
        // Culling
        if (inGameWorld && (pos.x + width < Main.cameraBox.x || pos.x > Main.cameraBox.x + Main.cameraBox.width || pos.y + height < Main.cameraBox.y || pos.y > Main.cameraBox.y + Main.cameraBox.height)) {
            return;
        }
        if (!isHidden()) {
            int xInt = (int)pos.x;
            int yInt = (int)pos.y;
            int segments = (int)(height  * 0.7f);
            Graphics.drawRectangleRounded(xInt, yInt, width, height, 1f, segments, Graphics.colAlpha(strokeCol, opacity));
            float rectWidth = Math.max(width * percentage - 2 * strokeWidth, height - 2 * strokeWidth);
            Graphics.drawRectangleRounded(xInt + strokeWidth, yInt + strokeWidth, rectWidth, height - 2 * strokeWidth, 1f, segments, Graphics.colAlpha(fillCol, opacity));

            if (customDraw != null) {
                customDraw.accept(new Rectangle(xInt, yInt, width, height));
            }
            if (text != null) {
                Graphics.drawTextOutline(text, textDrawPos, fontSize, spacing, Color.WHITE);
            }
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
        if (inGameWorld) {
            Main.idServer.returnId(this.getId());
            Main.drawablePool.deleteObj(this.getId(), DrawPool.TOP_UI);
        }
    }
}
