import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Skimmer extends Bullet {
    protected static float BASE_ROTATION = 0.1f * 25/120;
    protected static BulletStats BULLET_STAT = new BulletStats("bullet", 1, 0.3f, 0.6f, 1f, 1, 0.25f, 1, 0);
    Barrel[] barrels;
    FireManager fireManager;
    float bulletRotation;

    public Skimmer(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, drawLayer);
        setColor(host.fillCol, host.strokeCol);  // Set color to host color

        fireManager = new FireManager(new double[][] {{0, 0.35}, {0, 0.35}});  // Reload factor of 0.35
        barrels = new Barrel[] {
                new Barrel(BULLET_STAT, 31.5f * host.scale, 48.5f * host.scale, 0.35f, 0, 0, false, false, false),
                new Barrel(BULLET_STAT,31.5f * host.scale, 48.5f * host.scale, 0.35f,0, Math.PI, false, false, false),
        };

        // Set host
        for (Barrel barrel : barrels) {
            barrel.setHost(host, this);
            barrel.setColor(fillCol, strokeCol);  // Set color to host colors, not default greys
        }
        fireManager.setHost(host);

        // Other initialization
        bulletRotation = direction;  // Set initial rotation to fire direction
        fireManager.setFiring(true);  // Always firing
    }

    @Override
    public void update() {
        super.update();


        bulletRotation += BASE_ROTATION;

        // Update barrel positions and rotation
        for (Barrel barrel : barrels) {
            barrel.update(pos.x, pos.y, bulletRotation);
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

/*
    const SkimmerBarrelDefinition: BarrelDefinition = {
        angle: Math.PI / 2,
        offset: 0,
        size: 70,
        width: 42,
        delay: 0,
        reload: 0.35,
        recoil: 0,
        isTrapezoid: false,
        trapezoidDirection: 0,
        addon: null,
        bullet: {
            type: "bullet",
            health: 0.3,
            damage: 3 / 5,
            speed: 1.1,
            scatterRate: 1,
            lifeLength: 0.25,
            sizeRatio: 1,
            absorbtionFactor: 1
        }
    };
*/