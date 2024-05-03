import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Glider extends Bullet{
    protected static BulletStats BULLET_STAT = new BulletStats("bullet", 1, 0.3f, 0.6f, 0.6f, 1, 0.5f, 1, 4f);
    Barrel[] barrels;
    FireManager fireManager;

    public Glider(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, drawLayer);
        setColor(host.fillCol, host.strokeCol);  // Set color to host color

        fireManager = new FireManager(new double[][] {{0, 25.f/32}, {0, 25.f/32}});  // Reload factor of 0.35
        barrels = new Barrel[] {
                new Barrel(BULLET_STAT, 37.8f, 60, 25.f/32, 0, Math.toRadians(145), false, false, false),
                new Barrel(BULLET_STAT,37.8f, 60, 25.f/32,0, Math.toRadians(-145), false, false, false),
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
    public void updateStats() {
        // Calculate bullet stats
        setCollisionFactors(bulletStats.absorbtionFactor, (7.f / 3 + host.stats.getStat(Stats.BULLET_DAMAGE)) * bulletStats.damage * bulletStats.absorbtionFactor);

        // https://github.com/ABCxFF/diepindepth/blob/b035291bd0bed436d0ffbe2eb707fb96ed5f2bf4/extras/stats.md?plain=1#L34
        float damage = (7 + (3 * host.stats.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.stats.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        // Calculate acceleration to converge to max speed (https://www.desmos.com/calculator/9hakym7jxy)
        this.acceleration = getMaxSpeed() * (1-friction);
        float initialSpeed = getMaxSpeed() + (45 + Graphics.randf(-bulletStats.scatterRate, bulletStats.scatterRate)) * (1-friction)/(1-0.9f);  // Initial speed is max speed + 30 - scatter rate
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Math.round(bulletStats.lifeLength * 72 * (120.f / 25));  // Lengthen because 25 fps -> 120 fps
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
