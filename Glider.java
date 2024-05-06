import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Glider extends Bullet{
    // Speed is around 7.08 units per frame (850 units per second)
    // TODO: verify big glider bullet velocity, small glider bullet velocity
    protected static BulletStats BULLET_STAT = new BulletStats("bullet", 1, 0.6f, 0.6f, 0.5f, 1, 0.5f, 1, 2.5f);
    Barrel[] barrels;
    FireManager fireManager;

    public Glider(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(hostBarrel, spawnPos, direction, diameter * 1.05f, bulletStats, fillCol, strokeCol, drawLayer);  // Increase diameter by 5% due to trapezoid size inaccuracies
        setColor(host.fillCol, host.strokeCol);  // Set color to host color

        initHealthBar();

        fireManager = new FireManager(new double[][] {{0, 0.8}, {0, 0.8}});  // Reload factor of 0.35
        barrels = new Barrel[] {
                new Barrel(BULLET_STAT, 42f, 60, 0.8f, 0, Math.toRadians(145), false, false, false),
                new Barrel(BULLET_STAT,42f, 60, 0.8f,0, Math.toRadians(-145), false, false, false),
        };

        // Set host
        for (Barrel barrel : barrels) {
            barrel.setHost(host, this);
            barrel.setColor(fillCol, strokeCol);  // Set color to host colors, not default greys
        }

        fireManager.setHost(host);
        fireManager.setFiring(true);  // Always firing
    }

    //ArrayList<Float> speeds = new ArrayList<Float>();
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
        //speeds.add(Graphics.length(vel));
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

    @Override
    public void delete() {
        super.delete();

/*        float avgSpeed = 0;
        for (float speed : speeds) {
            avgSpeed += speed;
        }
        avgSpeed /= speeds.size();
        System.out.println("Glider speed: " + avgSpeed);*/
    }
}
