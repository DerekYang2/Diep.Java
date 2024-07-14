

import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Trap extends Projectile {
    int collideFrames;  // Number of frames left to collide with itself
    double rotation;  // Rotation of the trap texture

    /**
     * Creates a trap game object and spawns right away
     * @param hostBarrel The pointer to the barrel that fired this projectile
     * @param spawnPos The position where the projectile spawns (center position of projectile)
     * @param direction The direction the projectile is fired (radians)
     * @param diameter The diameter of the projectile
     * @param bulletStats The bullet stats of the projectile
     * @param fillCol The fill color of the projectile
     * @param strokeCol The stroke color of the projectile
     */
    public Trap(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, DrawPool.BOTTOM);
        collideFrames = lifeFrames/8;
        rotation = Graphics.randf(0, Math.PI * 2);  // Random rotation
    }

    @Override
    public void updateStats() {
        // Calculate bullet stats
        setCollisionFactors(bulletStats.absorbtionFactor, (7.f / 3 + host.getStat(Stats.BULLET_DAMAGE)) * bulletStats.damage * bulletStats.absorbtionFactor);

        float damage = (7 + (3 * host.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above

        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        this.acceleration = 0;  // No acceleration
        // TODO: test initial speed of trapper, with speed upgrades
        float initialSpeed = (getMaxSpeed() + 30 + Graphics.randf(-bulletStats.scatterRate, bulletStats.scatterRate)) * (1-friction)/(1-0.9f);  // Initial speed is max speed + 30 - scatter rate
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Math.round(bulletStats.lifeLength * 75 * (120.f / 25));  // Lengthen because 25 fps -> 120 fps
    }

    @Override
    public float getMaxSpeed() {
        // Scale factor of bullet speed stat: https://www.desmos.com/calculator/nganqiqqf3 (scale factor is 1.6 at bs=0 and 5.1 at bs=7)
        float scaleFactor = (float)(1.32288 * Math.sqrt(host.getStat(Stats.BULLET_PENETRATION)) + 1.6);
        float velMax = (20 + scaleFactor * 3 * host.getStat(Stats.BULLET_SPEED)) * bulletStats.speed;  // src: not link above (check diepcustom repo)
        return velMax * 25.f/120;
    }

    @Override
    protected void setFlags() {
        super.setFlags();
        super.keepInArena = false;
    }

    @Override
    public void update() {
        super.update();

        if (isDead) return;

        if (collideFrames > 0) collideFrames--;  // Decrement collideFrames
    }

    @Override
    public void draw() {
        final float scaledRadius = getRadiusScaled() * 0.74f;  // scale is always 1 until death animation
        if (Main.onScreen(pos, scaledRadius)) {
            Graphics.drawTrap(pos, scaledRadius, (float)rotation, Graphics.strokeWidth, Graphics.colAlpha(getDamageLerpColor(fillCol), opacity), Graphics.colAlpha(getDamageLerpColor(strokeCol), opacity));
        }
    }

    @Override
    protected boolean sameGroupCollision(GameObject obj) {
        return obj instanceof Trap && (collideFrames > 0) && (((Trap)obj).collideFrames > 0);  // If same group obj is a Trap and both collideFrames still active
    }
}
