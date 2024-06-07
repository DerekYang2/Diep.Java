import com.raylib.java.shapes.Rectangle;

public class Button extends UIObject {
    public Button(Scene scene, Rectangle boundingBox) {
        super(scene, boundingBox);
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        Graphics.drawRectangle(boundingBox, Graphics.RED);
    }
}
