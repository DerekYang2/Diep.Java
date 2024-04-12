import com.raylib.java.raymath.Vector2;
import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Keyboard.KEY_D;

public class Player extends Tank {
    public Player(Vector2 spawn) {
        super(spawn, new PlayerController());
        Graphics.setZoom(1.0f, level);
        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
    }

    @Override
    public void update() {
        super.update();
    }
}
