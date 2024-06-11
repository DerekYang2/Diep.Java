import com.raylib.java.shapes.Rectangle;

public class Button extends UIObject {
    protected Runnable onClick;
    public Button(Scene scene, Rectangle boundingBox, Runnable onClick) {
        super(scene, boundingBox);
        this.onClick = onClick;
    }

    @Override
    public void update() {
        if (Graphics.isLeftMousePressed() && Graphics.isIntersecting(Graphics.getMouse(), boundingBox)) {
            onClick.run();
        }
    }

    @Override
    public void draw() {
        Graphics.drawCircle(Graphics.getMouse(), 5, Graphics.RED, 1);
        Graphics.drawRectangle(boundingBox, Graphics.RED);
    }
}