import com.raylib.java.raymath.Vector2;

// TODO: auto turret should be auto fire mode, these guys turn too fast, maybe add lock on time
public class BotController implements Controller {
    Tank host;
    float moveDir;
    boolean shouldFire;
    Barrel frontBarrel;
    public float frontBulletSpeed;
    float targetDirection, direction;
    Stopwatch reactionWatch = new Stopwatch();
    final int reactionTime = 250;  // Reaction time in milliseconds for bot to notice player on screen
    final int SAFETY_FRAMES = 120 * 3;  // 3 second
    int safetyFireFrames;  // Extra number of frames to continue firing after target is lost
    Vector2 targetPos;

    public BotController() {
        moveDir = (float) (Math.random() * 2 * Math.PI);
    }

    @Override
    public void setHost(Tank host) {
        this.host = host;
        targetDirection = direction = 0;
        shouldFire = false;
        reactionWatch.start();
        safetyFireFrames = 0;
    }
    @Override
    public void updateTankBuild() {
        frontBarrel = host.tankBuild.getFrontBarrel();
        if (frontBarrel != null) {
            BulletStats bulletStats = host.tankBuild.getBarrelBulletStats(frontBarrel);
            frontBulletSpeed = (20 + 3 * host.getStat(Stats.BULLET_SPEED)) * bulletStats.speed * 25.f / 120;
        }
    }

    @Override
    public boolean autoFire() {
        return host.tankBuild.name.contains("auto");
    }

    @Override
    public void update() {
        if (frontBarrel != null) {
            safetyFireFrames = Math.max(0, safetyFireFrames - 1);  // Decrement safety fire frames

            if (frontBarrel.canControlDrones) {
                Integer targId = AutoAim.getClosestTargetId(host.pos, host.getView(), host.group);  // Get closest target unadjusted

                if (targId != null) {
                    GameObject targObj = Main.gameObjectPool.getObj(targId);
                    if (Graphics.distanceSq(host.pos, targObj.pos) < 1500*1500)  // If target is close enough
                        targetPos = AutoAim.getAdjustedTarget(targObj, frontBarrel.getSpawnPoint(), frontBulletSpeed);  // Adjust target position
                    else
                        targetPos = targObj.pos;  // Do not adjust target position
                } else {
                    targetPos = null;  // No target
                }
            } else {
                targetPos = AutoAim.getAdjustedTarget(host.pos, frontBarrel.getSpawnPoint(), host.getView(), host.group, frontBulletSpeed);  // Get closest target
            }

            if (targetPos != null) {  // If there is a closest target
                if (reactionWatch.ms() > reactionTime) {  // If reaction time has passed
                    targetDirection = (float) Math.atan2(targetPos.y - host.pos.y, targetPos.x - host.pos.x);
                    shouldFire = Graphics.absAngleDistance(direction, targetDirection) < Math.toRadians(10);   // Only fire if direction is close to target direction
                }
                safetyFireFrames = SAFETY_FRAMES;  // Set safety fire frames to max
            } else {
                if (safetyFireFrames == 0) {  // If safety frames has run out
                    targetDirection = moveDir;  // Set target direction to move direction
                    shouldFire = false;
                    reactionWatch.start();  // Restart reaction watch when no target
                }
            }
            direction = (float) Graphics.angle_lerp(direction, targetDirection, 0.1f);
        } else {
            direction = 0;
        }
    }

    @Override
    public float barrelDirection() {
        return direction;
    }

    /**
     * For drones
     * @return
     */
    @Override
    public Vector2 getTarget() {
        if (targetPos == null) {  // Return host position plus its direction vector
            float vel = host.radius * host.scale + 20*Graphics.length(host.vel);  // Multiply by some amount so drones stay in front
            return new Vector2(host.pos.x + vel * (float) Math.cos(moveDir), host.pos.y + vel * (float) Math.sin(moveDir));
        }
        return targetPos;
    }

    @Override
    public float moveDirection() {
        if (host.getStat(Stats.BODY_DAMAGE) >= 6) {
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
    public boolean holdSpecial() {
        return false;
    }

    @Override
    public boolean pressSpecial() {
        return false;
    }
}
