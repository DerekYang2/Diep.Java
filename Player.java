import com.raylib.java.raymath.Vector2;

public class Player extends Tank {
    public Player(Vector2 spawn) {
        super(spawn, new PlayerController());
        Graphics.setZoom(1.0f, level);
        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        setBarrels(TankBuilds.triplet());
    }

    @Override
    public void update() {
        super.update();
    }
}
