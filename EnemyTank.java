import com.raylib.java.raymath.Vector2;

public class EnemyTank extends Tank {
    public EnemyTank(Vector2 spawn, String buildName) {
        super(spawn, new BotController(), new Stats(7, 7, 7, 7, 7, 0, 0, 5));
        setColor(Graphics.RED, Graphics.RED_STROKE);
        setTankBuild(TankBuild.createTankBuild(buildName));
        autoFire = true;
    }

    @Override
    public void update() {
        super.update();
    }
}
