import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Trap extends Projectile {
    int collideFrames;  // Number of frames left to collide with itself
    double rotation = 0;  // Rotation of the trap texture

    public Trap(Barrel hostBarrel, float centerX, float centerY, float direction, float cannonLength, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        super(hostBarrel, centerX, centerY, direction, cannonLength, diameter, bulletStats, fillCol, strokeCol, DrawPool.BOTTOM);
        collideFrames = lifeFrames/8;
        rotation = Math.random() * Math.PI * 2;  // Random rotation
    }

    @Override
    public void updateStats() {
        // Calculate bullet stats
        setCollisionFactors(bulletStats.absorbtionFactor, (7.f / 3 + host.stats.getStat(Stats.BULLET_DAMAGE)) * bulletStats.damage * bulletStats.absorbtionFactor);

        float damage = (7 + (3 * host.stats.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.stats.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        float velMax = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed;  // src: not link above (check diepcustom repo)

        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        this.acceleration = 0;  // No acceleration
        float initialSpeed = (velMax * 25.f/120 + 30 - (float)Math.random() * bulletStats.scatterRate) * (1-friction)/(1-0.9f);  // Initial speed is max speed + 30 - scatter rate
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Math.round(bulletStats.lifeLength * 75 * (120.f / 25));  // Lengthen because 25 fps -> 120 fps
    }

    @Override
    protected void setFlags() {
        super.setFlags();
        super.keepInArena = true;
    }

    @Override
    public void update() {
        super.update();

        if (isDead) return;

        if (collideFrames > 0) collideFrames--;  // Decrement collideFrames
    }

    @Override
    public void draw() {
        final float scaledRadius = radius * scale * 0.74f;  // scale is always 1 until death animation
        if (Main.onScreen(pos, scaledRadius)) {
            Graphics.drawTrap(pos, scaledRadius, (float)rotation, Graphics.strokeWidth, Graphics.colAlpha(getDamageLerpColor(fillCol), opacity), Graphics.colAlpha(getDamageLerpColor(strokeCol), opacity));
        }
    }

    @Override
    protected boolean sameGroupCollision(GameObject obj) {
        return obj instanceof Trap && (collideFrames > 0) && (((Trap)obj).collideFrames > 0);  // If same group obj is a Trap and both collideFrames still active
    }
}
