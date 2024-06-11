import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.text.rText;

public class Button extends UIObject {
    protected Runnable onClick;
    String text;
    float fontSize;
    boolean hover;
    public Button(Scene scene, Vector2 centerPos, String text, float fontSize, Runnable onClick) {
        super(scene, centerPos, rText.MeasureTextEx(Graphics.outlineFont, text, fontSize, -fontSize/10));
        this.text = text;
        this.fontSize = fontSize;
        this.onClick = onClick;
    }

    @Override
    public void update() {
        hover = Graphics.isIntersecting(Graphics.getMouse(), boundingBox);
        if (Graphics.isLeftMousePressed() && hover) {
            onClick.run();
        }
    }

    @Override
    public void draw() {
        Graphics.drawRectangleRounded(boundingBox.x - 4, boundingBox.y - 4, boundingBox.width + 2 * 4, boundingBox.height + 2 * 4, 0.3f, 10, Graphics.DARK_GREY_STROKE);
        Graphics.drawRectangleRounded(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height, 0.3f, 10, Graphics.GREY);
        Graphics.drawTextCenteredOutline(text, (int) (boundingBox.x  + 1.03f*boundingBox.width / 2), (int)(boundingBox.y + 1.05f * boundingBox.height / 2), (int) fontSize, -fontSize/10, Color.WHITE);
        if (hover) {
            Graphics.drawRectangleRounded(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height, 0.3f, 10, Graphics.rgba(0,0,0, 75));
        }
    }
}
