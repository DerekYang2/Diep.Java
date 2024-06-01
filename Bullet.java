import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Bullet extends Projectile {

    /**
     * Creates a Bullet game object and spawns right away
     * @param hostBarrel The pointer to the barrel that fired this projectile
     * @param spawnPos The position where the projectile spawns (center position of projectile)
     * @param direction The direction the projectile is fired (radians)
     * @param diameter The diameter of the projectile
     * @param bulletStats The bullet stats of the projectile
     * @param fillCol The fill color of the projectile
     * @param strokeCol The stroke color of the projectile
     * @param drawLayer The draw layer of the projectile
     */
    public Bullet(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, drawLayer);
/*        if (diameter > 50)
            initHealthBar();*/
    }

    @Override
    protected void setFlags() {
        super.setFlags();
        this.keepInArena = false;
    }

    @Override
    public void updateStats() {
        // Calculate bullet stats
        setCollisionFactors(bulletStats.absorbtionFactor, 0.8f*(7.f / 3 + host.getStat(Stats.BULLET_DAMAGE)) * bulletStats.damage * bulletStats.absorbtionFactor);

        // https://github.com/ABCxFF/diepindepth/blob/b035291bd0bed436d0ffbe2eb707fb96ed5f2bf4/extras/stats.md?plain=1#L34
        float damage = (7 + (3 * host.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        // Calculate acceleration to converge to max speed (https://www.desmos.com/calculator/9hakym7jxy)
        this.acceleration = getMaxSpeed() * (1-friction);
        float initialSpeed = getMaxSpeed() + (30 + Graphics.randf(-bulletStats.scatterRate, bulletStats.scatterRate)) * (1-friction)/(1-0.9f);  // Initial speed is max speed + 30 - scatter rate
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Math.round(bulletStats.lifeLength * 72 * (120.f / 25));  // Lengthen because 25 fps -> 120 fps
    }

    @Override
    public float getMaxSpeed() {
        return (20 + 3 * host.getStat(Stats.BULLET_SPEED)) * bulletStats.speed * 25.f/120;
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw() {
        final float scaledRadius = getRadiusScaled();  // scale is always 1 until death animation
        if (Main.onScreen(pos, scaledRadius)) {
            Graphics.drawCircle(pos, scaledRadius, Graphics.strokeWidth, fillCol, strokeCol, opacity * opacity);
        }
    }
}
