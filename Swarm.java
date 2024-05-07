import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Swarm extends Drone {
    public Swarm(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol);
        VIEW_RADIUS *= 2;
    }

    @Override
    protected void setFlags() {
        super.setFlags();
        super.keepInArena = false;
    }
}
