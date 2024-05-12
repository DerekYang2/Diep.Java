import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

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
    String text;
    int fontSize;

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
    }

    public void addToGameWorld() {
        createId();
        addToPools();
        inGameWorld = true;
    }

    /**
     * Update the bar's position and percentage filled
     * @param pos The coordinate of the top-left corner of the bar
     * @param percentage The percentage of the bar filled
     */
    public void update(Vector2 pos, float percentage) {
        this.pos = pos;
        this.targetPercentage = percentage;

        // Animate the bar (LERP)
        this.percentage += (this.targetPercentage - this.percentage) * lerpFactor;

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
        if (inGameWorld && (pos.x + width < Main.cameraBox.x || pos.x > Main.cameraBox.x + Main.cameraBox.width || pos.y + height < Main.cameraBox.y || pos.y > Main.cameraBox.y + Main.cameraBox.height)) {
            return;
        }
        if (!isHidden()) {
            int xInt = Math.round(pos.x);
            int yInt = Math.round(pos.y);

            if (inGameWorld) {
                Graphics.drawRectangleRounded(xInt, yInt, width, height, 1f, Graphics.colAlpha(strokeCol, opacity));
                float rectWidth = Math.max(width * percentage - 2 * strokeWidth, height - 2 * strokeWidth);
                Graphics.drawRectangleRounded(xInt + strokeWidth, yInt + strokeWidth, rectWidth, height - 2 * strokeWidth, 1f, Graphics.colAlpha(fillCol, opacity));
            } else
            {
                float reverseZoom = 1.f / Graphics.getCameraZoom();
                Graphics.drawRectangleRounded(xInt, yInt, width * reverseZoom, height * reverseZoom, 1f, Graphics.colAlpha(strokeCol, opacity));
                float rectWidth = Math.max(width * percentage - 2 * strokeWidth, height - 2 * strokeWidth) * reverseZoom;
                Graphics.drawRectangleRounded(xInt + strokeWidth * reverseZoom, yInt + strokeWidth * reverseZoom, rectWidth, (height - 2 * strokeWidth) * reverseZoom, 1f, Graphics.colAlpha(fillCol, opacity));

                if (text != null) {
                    Graphics.drawTextCenteredOutline(text, (int) (xInt + width * reverseZoom / 2), (int) (yInt + height * reverseZoom / 2), (int) (fontSize * reverseZoom), Color.WHITE);
                }
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
