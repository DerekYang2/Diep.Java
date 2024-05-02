import com.raylib.java.raymath.Vector2;

public class BotController implements Controller {
    Tank host;

    float moveDir;

    boolean shouldFire;

    Barrel frontBarrel;

    public float medianBulletSpeed;

    float targetDirection, direction;

    public BotController() {
        moveDir = (float) (Math.random() * 2 * Math.PI);
    }

    @Override
    public void setHost(Tank host) {
        this.host = host;
        targetDirection = direction = 0;
        shouldFire = false;
    }
    @Override
    public void updateTankBuild() {
        frontBarrel = host.tankBuild.getFrontBarrel();
        medianBulletSpeed = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * host.tankBuild.getBulletSpeedMedian() * 25.f/120;
    }

    @Override
    public void update() {
        if (frontBarrel != null) {
            Vector2 closestTarget = AutoAim.getClosestTarget(host.pos, frontBarrel.getSpawnPoint(), 2000, host.group, 0, 2 * Math.PI, medianBulletSpeed);  // Get closest target
            if (closestTarget != null) {  // If there is a closest target
                targetDirection = (float) Math.atan2(closestTarget.y - host.pos.y, closestTarget.x - host.pos.x);
                shouldFire = true;
            } else {
                targetDirection = moveDir;  // Set target direction to move direction
                shouldFire = false;
            }
            direction = (float) Graphics.angle_lerp(direction, targetDirection, 0.17f);
        } else {
            direction = 0;
        }
    }


    @Override
    public boolean toggleAutoFire() {
        return false;
    }

    @Override
    public float barrelDirection() {
        return direction;
    }

    @Override
    public Vector2 getTarget() {
        return Main.player.pos;
    }

    @Override
    public float moveDirection() {
        if (host.stats.getStat(Stats.BODY_DAMAGE) >= 6) {
            // Bot will chase the player
            Vector2 PlayerPos = Main.player.pos;
            moveDir = (float) Math.atan2(PlayerPos.y - host.pos.y, PlayerPos.x - host.pos.x);
            return moveDir;
        }
        // Bot will bounce around the arena
        float xComp = (float) Math.cos(moveDir);
        float yComp = (float) Math.sin(moveDir);
        if (host.pos.x <= 0) {
            xComp = Math.abs(xComp);
        }
        if (host.pos.x >= Main.arenaWidth) {
            xComp = -Math.abs(xComp);
        }
        if (host.pos.y <= 0) {
            yComp = Math.abs(yComp);
        }
        if (host.pos.y >= Main.arenaHeight) {
            yComp = -Math.abs(yComp);
        }
        moveDir = (float) Math.atan2(yComp, xComp);
        //moveDir = -1;
        return moveDir;
    }

    @Override
    public boolean fire() {
        return shouldFire;
    }

    @Override
    public boolean special() {
        return false;
    }
}
