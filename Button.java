import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.text.rText;

public class Button extends UIObject {
    protected Runnable onClick;
    String text;
    float fontSize;
    boolean hover;
    Color fillCol;
    Stopwatch pressWatch;

    public Button(Scene scene, Vector2 centerPos, String text, float fontSize, Color fillCol, Runnable onClick) {
        super(scene, centerPos, rText.MeasureTextEx(Graphics.outlineSmallFont, text, fontSize, -fontSize/7));
        this.text = text;
        this.fontSize = fontSize;
        this.onClick = onClick;
        this.fillCol = fillCol;
        this.pressWatch = new Stopwatch();
    }

    @Override
    public void update() {
        hover = Graphics.isIntersecting(Graphics.getMouse(), boundingBox);
        if (Graphics.isLeftMousePressed() && hover && pressWatch.ms() > 100) {
            pressWatch.start();
            onClick.run();
        }
    }

    @Override
    public void draw() {
        float hoverPadding = hover ? 2 : 0;
        Graphics.drawRectangleRounded(boundingBox.x - (4 + hoverPadding), boundingBox.y - (4 + hoverPadding), boundingBox.width + (4 + hoverPadding) * 2, boundingBox.height + (4 + hoverPadding) * 2, 0.2f, 10, Graphics.DARK_GREY_STROKE);
        Graphics.drawRectangleRounded(boundingBox.x - hoverPadding, boundingBox.y - hoverPadding, boundingBox.width + hoverPadding * 2, boundingBox.height + hoverPadding * 2, 0.1f, 1, fillCol);
        Graphics.drawRectangle(boundingBox.x - hoverPadding, boundingBox.y - hoverPadding + 0.6f * (boundingBox.height + 2*hoverPadding), boundingBox.width + hoverPadding * 2 + 2, (boundingBox.height + hoverPadding * 2) * 0.4f + 2, Graphics.rgba(0, 0, 0, 80));
        Graphics.drawTextCenteredOutline(text, (int) (boundingBox.x + 1.07f * boundingBox.width / 2), (int)(boundingBox.y + 1.1f * boundingBox.height / 2), (int) (fontSize + hoverPadding), -fontSize/7, Color.WHITE);
        if (hover) {
            Graphics.drawRectangleRounded(boundingBox.x - hoverPadding, boundingBox.y - hoverPadding, boundingBox.width + hoverPadding * 2, boundingBox.height + hoverPadding * 2, 0.2f, 10, Graphics.rgba(255, 255, 255, 40));
        }
    }
}
