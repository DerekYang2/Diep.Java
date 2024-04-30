import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class AutoTurret {
    Tank host;
    FireManager fireManager;
    Barrel barrel;
    BulletStats bulletStats;
    protected final static float PASSIVE_ROTATION = 0.02f * 25/120;
    protected final static float VIEW_RADIUS = 1700;
    float targetDirection, direction;
    boolean idle;
    Stopwatch idleWatch;  // For a delay for idle -> firing

    Vector2 pos, offset;


    public AutoTurret(Tank host, Barrel barrel, FireManager fireManager, BulletStats bulletStats) {
        this.host = host;
        this.barrel = barrel;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;
        idleWatch = new Stopwatch();

        targetDirection = direction = 0;
        fireManager.setFiring(true);
    }

    public void setOffset(Vector2 offset) {
        this.offset = offset;
    }

    public void update(Vector2 pos) {
        this.pos = pos;
        Vector2 absPos = getAbsPos();
        Integer closestTarget = CollisionManager.getClosestTarget(absPos, VIEW_RADIUS * host.scale, host.group);  // Get closest target

        if (closestTarget != null) {  // If there is a closest target
            if (idle) {
                idle = false;
                idleWatch.start();
            }
            Vector2 target = Main.gameObjectPool.getObj(closestTarget).pos;
            targetDirection = (float) Math.atan2(target.y - absPos.y, target.x - absPos.x);
        } else {
            idle = true;
        }

        if (idle) {
            direction += PASSIVE_ROTATION;
        } else {
            direction = (float)Graphics.angle_lerp(direction, targetDirection, 0.17f);
        }

        barrel.update(absPos.x, absPos.y, direction);
        fireManager.setFiring(!idle && idleWatch.ms() > 250);  // If not idle and idleWatch is over 250ms, start firing
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
