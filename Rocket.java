import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Rocket extends Bullet {
    protected static BulletStats BULLET_STAT = new BulletStats("bullet", 1, 0.3f, 0.6f, 1.5f, 5, 0.1f, 1, 3.3f);
    Barrel[] barrels;
    FireManager fireManager;
    int delayFrames = 1 * 120;  // 1 second delay before rocket boost

    public Rocket(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, drawLayer);
        setColor(host.fillCol, host.strokeCol);  // Set color to host color

        fireManager = new FireManager(new double[][] {{0, 0.15}});  // Reload factor of 0.15
        barrels = new Barrel[] {
                new Barrel(BULLET_STAT, 26.5f * host.scale, 37f * host.scale, 0.15f, 0, Math.PI, true, false, false)
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

        delayFrames = Math.max(0, delayFrames - 1);  // Decrement delay frames
        if (delayFrames == 0) {
            // Fire bullets
            for (int i : fireManager.getFireIndices()) {
                addForce(barrels[i].shoot());  // Add force to bullet
            }
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


/*
const RocketBarrelDefinition: BarrelDefinition = {
angle: Math.PI,
offset: 0,
size: 70,
width: 72,
delay: 0,
reload: 0.15,
recoil: 3.3,
isTrapezoid: true,
trapezoidDirection: 0,
addon: null,
bullet: {
    type: "bullet",
    health: 0.3,
    damage: 3 / 5,
    speed: 1.5,
    scatterRate: 5,
    lifeLength: 0.1,
    sizeRatio: 1,
    absorbtionFactor: 1
}
        };

 */