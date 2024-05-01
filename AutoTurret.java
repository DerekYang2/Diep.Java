import com.raylib.java.core.Color;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.ArrayList;

public class AutoTurret {
    Tank host;
    FireManager fireManager;
    Barrel barrel;
    BulletStats bulletStats;
    protected final static float PASSIVE_ROTATION = 0.01f * 25/120;
    protected final static float VIEW_RADIUS = 1700;
    float targetDirection, direction;
    boolean idle;
    Stopwatch idleWatch;  // For a delay for idle -> firing
    double range;
    Vector2 pos, offset;
    Vector2 lastTargetPos;

    public AutoTurret(Tank host, Barrel barrel, FireManager fireManager, BulletStats bulletStats) {
        this.host = host;
        this.barrel = barrel;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;
        idleWatch = new Stopwatch();

        targetDirection = direction = 0;
        fireManager.setFiring(true);

        offset = new Vector2(0, 0);
        range = 2 * Math.PI; // Default range is 360 degrees
    }

    public void setOffset(Vector2 offset, double range) {
        this.offset = offset;
        this.range = range;
    }

    public void update(Vector2 pos) {
        this.pos = pos;
        Vector2 absPos = getAbsPos();
        Vector2 closestTarget = getClosestTarget();  // Get closest target

        if (closestTarget != null) {  // If there is a closest target
            if (idle) {
                idle = false;
                idleWatch.start();
            }
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
        fireManager.setFiring(!idle && idleWatch.ms() > 250);  // If not idle and idleWatch is over 250ms, start firing
    }

    protected static double sqr(double v) {
        return v*v;
    }
    Vector2 getTargetShift(GameObject target) {
        Vector2 absPos = getAbsPos();
        double bLength = barrel.getTurretLength();
        absPos.x += bLength * Math.cos(direction);
        absPos.y += bLength * Math.sin(direction);

        double projectile_speed = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed * 25.f/120;

        double a = sqr(target.vel.x) + sqr(target.vel.y) - sqr(projectile_speed);
        double b = 2 * (target.vel.x * (target.pos.x - absPos.x) + target.vel.y * (target.pos.y - absPos.y));
        double c = sqr(target.pos.x - absPos.x) + sqr(target.pos.y - absPos.y);

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return target.pos;
        }

        // Quad formula
        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a), t2 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double t = (t1 > 0 && t2 > 0) ? Math.min(t1, t2) : Math.max(t1, t2);
        assert t >= 0;
        return new Vector2((float) (target.pos.x + target.vel.x * t), (float) (target.pos.y + target.vel.y * t));
    }

    Vector2 getClosestTarget() {
        Vector2 pos = getAbsPos();

        float radius = VIEW_RADIUS * host.scale;
        int group = host.group;

        Rectangle view = new Rectangle(pos.x - radius, pos.y - radius, 2*radius, 2*radius);
        ArrayList<Integer> targets = CollisionManager.queryBoundingBox(view, group);

        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;
        Vector2 closestTarget = null;  // Set id to some impossible value

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);
            float distSquared = Graphics.distanceSq(pos, obj.pos);

            if (obj.group == group || obj.isProjectile || distSquared > radius * radius) {  // If same group or projectile OR too far, skip
                continue;
            }

            Vector2 shiftedTarget = getTargetShift(obj);
            // Within angle range
            if (range < 2 * Math.PI) {
                double baseAngle = Math.atan2(offset.y, offset.x);
                double startAngle = baseAngle - range * 0.5, endAngle = baseAngle + range * 0.5;  // End angle is ccw
                double targetAngle = Math.atan2(shiftedTarget.y - pos.y, shiftedTarget.x - pos.x);
                if (!Graphics.isAngleBetween(targetAngle, startAngle, endAngle)) {
                    continue;
                }
            }

            if (distSquared < minDistSquared) {
                minDistSquared = distSquared;
                closestTarget = shiftedTarget;
            }
        }
        return closestTarget;
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
