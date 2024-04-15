import com.raylib.java.raymath.Vector2;

public class Player extends Tank {
    public Player(Vector2 spawn) {
        super(spawn, new PlayerController(), new Stats(0, 7, 7, 7, 7, 0, 7, 0));
        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        setTankBuild(TankBuild.createTankBuild("ranger"));
        Graphics.setZoom(this.tankBuild.fieldFactor, level);  // TODO: this needs to be updated when player is updated (level, etc)
    }

    @Override
    public void update() {
        super.update();
    }
}
