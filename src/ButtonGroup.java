

import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

import java.util.ArrayList;

public class ButtonGroup extends UIObject {
    ArrayList<Button> buttons = new ArrayList<>();

    public ButtonGroup(Scene scene, Vector2 centerPos, String[] text, float fontSize, Color fillCol, Runnable[] onClick) {
        super(scene, centerPos, new Vector2(0, 0));
        for (int i = 0; i < text.length; i++) {
            buttons.add(new Button(scene, new Vector2(0, centerPos.y), text[i], fontSize, fillCol, onClick[i]));
        }
        float gapWidth = 20;
        float widthSum = gapWidth * (text.length - 1);
        for (Button button : buttons) {
            widthSum += button.getBoundingBox().width;
        }
        float xPos = centerPos.x - widthSum / 2;
        for (Button button : buttons) {
            button.getBoundingBox().x = xPos;
            xPos += button.getBoundingBox().width + gapWidth;
        }
    }

    public ButtonGroup(Scene scene, Vector2 centerPos, String[] text, float fontSize, Color[] fillCol, Runnable[] onClick) {
        super(scene, centerPos, new Vector2(0, 0));
        for (int i = 0; i < text.length; i++) {
            buttons.add(new Button(scene, new Vector2(0, centerPos.y), text[i], fontSize, fillCol[i], onClick[i]));
        }
        float gapWidth = 20;
        float widthSum = gapWidth * (text.length - 1);
        for (Button button : buttons) {
            widthSum += button.getBoundingBox().width;
        }
        float xPos = centerPos.x - widthSum / 2;
        for (Button button : buttons) {
            button.getBoundingBox().x = xPos;
            xPos += button.getBoundingBox().width + gapWidth;
        }
    }

    @Override
    public void update() {
        buttons.forEach(Button::update);
    }

    @Override
    public void draw() {
        buttons.forEach(Button::draw);
    }
}

