import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Drone extends Projectile {
    float targetDirection;
    Vector2 target;
    Barrel hostBarrel;
    boolean aiOn = false;  // Default to off
    protected static final float VIEW_RADIUS = 850;

    public Drone(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        // super(new Vector2(centerX + cannonLength * (float) Math.cos(direction), centerY + cannonLength * (float) Math.sin(direction)), (int) (diameter * 0.5f), bulletStats.absorbtionFactor, 4, 1f, DrawPool.TOP_PROJECTILE);
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, DrawPool.TOP);
        this.hostBarrel = hostBarrel;  // Set the host barrel object
        this.targetDirection = this.direction = direction;  // Set direction to normalized angle
    }

    @Override
    public void updateStats() {
        setCollisionFactors(bulletStats.absorbtionFactor, 4);

        // Calculate bullet stats
        float damage = (7 + (3 * host.stats.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.stats.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        // Calculate acceleration to converge to max speed
        this.acceleration = getMaxSpeed() * (1-friction);
        float initialSpeed = getMaxSpeed() + (30 + Graphics.randf(-bulletStats.scatterRate, bulletStats.scatterRate)) * (1-friction)/(1-0.9f);
        initialSpeed /= 3;
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Integer.MAX_VALUE;  // Set life length to infinity
    }

    @Override
    public float getMaxSpeed() {
        // velMax *= 1.1f;  // TODO: test drone speed
        return 1.1f * (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed * 25.f/120;
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

        // Update AI on from host tank
        aiOn = host.getAutoFire() || !hostBarrel.canControlDrones;  // If host tank has autofire or cannot control drones, turn on AI

        // Get the target
        if (aiOn) {
            Integer closestTarget = CollisionManager.getClosestTarget(host.pos, Drone.VIEW_RADIUS * host.scale, group);
            if (closestTarget != null) {  // If there is a closest target
                target = Main.gameObjectPool.getObj(closestTarget).pos;
            } else {  // If no target in range, set target to the tank pos
                float scaleFactor = (12-17)/7.f * host.stats.getStat(Stats.BULLET_SPEED) + 17;  // Linear regression, stat 0: 17, stat 7: 12

                double radians = (scaleFactor) * acceleration * Math.toRadians(Main.counter) + id;  // Add id for a random offset
                float rotationRadius = host.radius * host.scale * 2;

                target = new Vector2(host.pos.x + rotationRadius * (float) Math.cos(radians), host.pos.y + rotationRadius * (float) Math.sin(radians));
            }
        } else {
            target = host.getTarget();
        }

        // Set target direction based on target position
        targetDirection = (float) Math.atan2(target.y - pos.y, target.x - pos.x);

        if (!aiOn && host.specialControl()) {  // Repel
            targetDirection += (float) Math.PI;  // Reverse direction of target
        }

        // Lerp direction to target direction
        direction = (float)Graphics.angle_lerp(direction, targetDirection, 0.17f);
    }

    @Override
    public void draw() {
        final float scaledRadius = (float)(radius * scale * Math.sqrt(2)/2);  // scale is always 1 until death animation
        if (Main.onScreen(pos, radius * scale)) {  // Use larger radius for culling
            Graphics.drawTriangleRounded(pos, scaledRadius, direction, Graphics.strokeWidth, Graphics.colAlpha(getDamageLerpColor(fillCol), opacity), Graphics.colAlpha(getDamageLerpColor(strokeCol), opacity));
        }
    }

    @Override
    protected boolean sameGroupCollision(GameObject obj) {
        return obj instanceof Drone;  // If same group obj is a drone, collide
    }

    @Override
    public void delete() {
        super.delete();
        hostBarrel.decrementDroneCount();
    }

}
