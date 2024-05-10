import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class TankImage extends Tank {
    public TankImage(double spawnX, double spawnY, String buildName, Color fillCol, Color strokeCol) {
        super(new Vector2((float)spawnX, (float)spawnY), new DummyController(), new Stats(0, 0, 0, 0, 0, 0, 0, 0), 45);
        setColor(fillCol, strokeCol);
        setTankBuild(TankBuild.createTankBuild(buildName));
        updateStats();
    }

    @Override
    public void addToPools() {}

    @Override
    public void createId() {}

    @Override
    public void delete() {
        if (healthBar != null) healthBar.delete();
    }
}
