import com.raylib.java.raymath.Vector2;

public class EnemyTank extends Tank {
    public EnemyTank(Vector2 spawn, String buildName) {
        super(spawn, new BotController(), new Stats(5, 7, 7, 7, 7, 0, 0, 0), 20);
        setColor(Graphics.RED, Graphics.RED_STROKE);
        setTankBuild(TankBuild.createTankBuild(buildName));
        TextureLoader.pendingAdd(this);
    }

    @Override
    public void update() {
        super.update();
    }
}
