import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Glider extends Bullet{
    // TODO: verify big glider bullet velocity, small glider bullet velocity
    protected static BulletStats BULLET_STAT = new BulletStats("bullet", 1, 0.6f, 0.6f, 0.6f, 1, 0.5f, 1, 3.77f);
    Barrel[] barrels;
    FireManager fireManager;

    public Glider(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, drawLayer);
        setColor(host.fillCol, host.strokeCol);  // Set color to host color

        fireManager = new FireManager(new double[][] {{0, 0.8}, {0, 0.8}});  // Reload factor of 0.35
        barrels = new Barrel[] {
                new Barrel(BULLET_STAT, 42f, 62, 0.8f, 0, Math.toRadians(145), false, false, false),
                new Barrel(BULLET_STAT,42f, 62, 0.8f,0, Math.toRadians(-145), false, false, false),
        };

        // Set host
        for (Barrel barrel : barrels) {
            barrel.setHost(host, this);
            barrel.setColor(fillCol, strokeCol);  // Set color to host colors, not default greys
        }

        fireManager.setHost(host);
        fireManager.setFiring(true);  // Always firing
    }

    @Override
    public void update() {
        super.update();

        // Update barrel positions and rotation
        for (Barrel barrel : barrels) {
            barrel.update(pos.x, pos.y, direction);
        }

        if (isDead) return;  // Don't fire if dead

        // Fire bullets
        for (int i : fireManager.getFireIndices()) {
            addForce(barrels[i].shoot());  // Add force to bullet
        }
    }

    @Override
    public void draw() {
        for (Barrel barrel : barrels) {  // Draw barrels first
            barrel.draw();
        }
        super.draw();  // Draw bullet
    }

    /**
     * Projectiles do not have a damage animation
     * @param col The color to lerp
     * @return The same color
     */
    @Override
    public Color getDamageLerpColor(Color col) {
        return col;
    }
}
