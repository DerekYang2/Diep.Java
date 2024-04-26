import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Drone extends GameObject {
    protected float acceleration;
    float direction;
    Vector2 target;
    Color fillCol;
    Color strokeCol;
    Barrel hostBarrel;

    public Drone(Barrel hostBarrel, float centerX, float centerY, float direction, float cannonLength, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        super(new Vector2(centerX + cannonLength * (float) Math.cos(direction), centerY + cannonLength * (float) Math.sin(direction)), (int) (diameter * 0.5f), bulletStats.absorbtionFactor, (7.f / 3 + hostBarrel.host.stats.getStat(Stats.BULLET_DAMAGE)) * bulletStats.damage * bulletStats.absorbtionFactor, 1f);
        this.noInternalCollision = true;  // No internal collision for bullets, TODO: collide with each other
        this.keepInArena = true;

        this.hostBarrel = hostBarrel;  // Set the host barrel object
        Tank host = hostBarrel.host;  // Get the tank of the host barrel

        this.group = host.group;  // Set group to host group

        // Calculate bullet stats
        // https://github.com/ABCxFF/diepindepth/blob/b035291bd0bed436d0ffbe2eb707fb96ed5f2bf4/extras/stats.md?plain=1#L34
        float damage = (7 + (3 * host.stats.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.stats.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        float velMax = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed - (float)Math.random() * bulletStats.scatterRate;  // src: not link above (check diepcustom repo)

        super.setDamage(damage * (25.f / 120) * 0.85f);  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        this.direction = direction;

        // Calculate acceleration to converge to max speed (https://www.desmos.com/calculator/9hakym7jxy)
        this.acceleration = (velMax * 25.f/120) * (1-friction);
        //System.out.println(velMax + " " + acceleration + " " + friction);
        float initialSpeed = 30 * (1-friction)/(1-0.9f);
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Drawing variables
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        radius = diameter * 0.5f * bulletStats.sizeRatio;  // Multiply radius by bullet stats size ratio
    }

    @Override
    public void draw() {
        final float scaledRadius = radius * scale;  // scale is always 1 until death animation
        if (pos.x + scaledRadius < Main.cameraBox.x || pos.x - scaledRadius > Main.cameraBox.x + Main.cameraBox.width || pos.y + scaledRadius < Main.cameraBox.y || pos.y - scaledRadius > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        Graphics.drawTriangleRounded(pos, scaledRadius, direction, Graphics.strokeWidth, Graphics.colAlpha(fillCol, opacity), Graphics.colAlpha(strokeCol, opacity));
    }

    @Override
    public void update() {
        super.update();

        if (isDead) return;

        if (hostBarrel.host.isDead) {
            triggerDelete();
        }

        target = hostBarrel.host.getTarget();
        direction = (float) Math.atan2(target.y - pos.y, target.x - pos.x);

        addForce(acceleration, direction);
    }

    @Override
    public void triggerDelete() {
        super.triggerDelete();
    }

    @Override
    public void addToPools() {
        super.addToPools();
        Main.drawablePool.addObj(this, DrawPool.BOTTOM);
    }

    @Override
    public void delete() {
        super.delete();
        Main.drawablePool.deleteObj(this.getId(), DrawPool.BOTTOM);
        hostBarrel.decrementDroneCount();
    }

}
