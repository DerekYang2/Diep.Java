import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class AutoTurret {
    Tank host;
    FireManager fireManager;
    Barrel barrel;
    BulletStats bulletStats;
    protected final static float PASSIVE_ROTATION = 0.01f * 25/120;
    protected final static float VIEW_RADIUS = 1700;
    float targetDirection, direction;
    boolean idle;
    double range;
    Vector2 pos, offset;
    Vector2 lastTargetPos;

    public AutoTurret(Tank host, Barrel barrel, FireManager fireManager, BulletStats bulletStats) {
        this.host = host;
        this.barrel = barrel;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;

        targetDirection = direction = 0;
        fireManager.setFiring(true);

        offset = new Vector2(0, 0);
        range = 2 * Math.PI; // Default range is 360 degrees
        // bulletStats.scatterRate = 0; // Test for accuracy
    }

    public void setOffset(Vector2 offset, double range) {
        this.offset = offset;
        this.range = range;
    }

    public void update(Vector2 pos) {
        this.pos = pos;
        Vector2 absPos = getAbsPos();
        double baseAngle = Math.atan2(offset.y, offset.x);
        float projectile_speed = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed * 25.f/120;

        Vector2 closestTarget = AutoAim.getAdjustedTarget(getAbsPos(), barrel.getSpawnPoint(), VIEW_RADIUS * host.scale, host.group, baseAngle, range, projectile_speed);  // Get closest target

        if (closestTarget != null) {  // If there is a closest target
            idle = false;
            targetDirection = (float) Math.atan2(closestTarget.y - absPos.y, closestTarget.x - absPos.x);
        } else {
            idle = true;
            if (offset.x == 0 && offset.y == 0) {
                targetDirection += PASSIVE_ROTATION;
            } else {
                targetDirection = (float) Math.atan2(offset.y, offset.x);
            }
        }

        direction = (float)Graphics.angle_lerp(direction, targetDirection, 0.2f);

        barrel.update(absPos.x, absPos.y, direction);

        fireManager.setFiring(!idle && Graphics.absAngleDistance(direction, targetDirection) < Math.toRadians(20));  // If not idle and within 10 degrees of target direction, fire
    }

    public void shoot(int drawLayer) {
        if (!fireManager.getFireIndices().isEmpty()) {  // If index in fire queue
            host.addForce(barrel.shoot(bulletStats, drawLayer));  // Shoot at top layer and apply recoil
        }
    }

    public void draw() {
        float scaledRadius = host.radius * host.scale;
        Vector2 absPos = getAbsPos();
        if (Main.onScreen(absPos, barrel.getTurretLength())) {
            barrel.draw();
            final Color fillCol = host.getDamageLerpColor(Graphics.GREY), strokeCol = host.getDamageLerpColor(Graphics.GREY_STROKE);
            Graphics.drawCircleTexture(absPos.x, absPos.y, scaledRadius * 0.5f, Graphics.strokeWidth, fillCol, strokeCol, host.opacity);
        }
    }

    public Vector2 getAbsPos() {
        return new Vector2(pos.x + offset.x, pos.y + offset.y);
    }
}
