import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Bullet extends Projectile {

    // The bullet trajectory will be determined based on the position where it spawns
    public Bullet(Barrel hostBarrel, float centerX, float centerY, float direction, float cannonLength, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(hostBarrel, centerX, centerY, direction, cannonLength, diameter, bulletStats, fillCol, strokeCol, drawLayer);
    }

    @Override
    protected void setFlags() {
        super.setFlags();
        this.keepInArena = false;
    }

    @Override
    public void updateStats() {
        // Calculate bullet stats
        setCollisionFactors(bulletStats.absorbtionFactor, (7.f / 3 + host.stats.getStat(Stats.BULLET_DAMAGE)) * bulletStats.damage * bulletStats.absorbtionFactor);

        // https://github.com/ABCxFF/diepindepth/blob/b035291bd0bed436d0ffbe2eb707fb96ed5f2bf4/extras/stats.md?plain=1#L34
        float damage = (7 + (3 * host.stats.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.stats.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        float velMax = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed;  // src: not link above (check diepcustom repo)

        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        // Calculate acceleration to converge to max speed (https://www.desmos.com/calculator/9hakym7jxy)
        this.acceleration = (velMax * 25.f/120) * (1-friction);
        float initialSpeed = (velMax * 25.f/120) + (/*30 + */Graphics.randf(-bulletStats.scatterRate, bulletStats.scatterRate)) * (1-friction)/(1-0.9f);  // Initial speed is max speed + 30 - scatter rate
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Math.round(bulletStats.lifeLength * 72 * (120.f / 25));  // Lengthen because 25 fps -> 120 fps
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw() {
        final float scaledRadius = radius * scale;  // scale is always 1 until death animation
        if (Main.onScreen(pos, scaledRadius)) {
            Graphics.drawCircle((int) pos.x, (int) pos.y, scaledRadius, Graphics.strokeWidth, fillCol, strokeCol, opacity * opacity);  // Square opacity for a steeper curve (x^2)
        }
    }
}
