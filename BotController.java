import com.raylib.java.raymath.Vector2;

import java.util.HashSet;

// TODO: auto turret should be auto fire mode, these guys turn too fast, maybe add lock on time
public class BotController implements Controller {
    Tank host;
    float targetMoveDir, moveDir, intendedDir;
    boolean shouldFire;
    Barrel frontBarrel;
    public float frontBulletSpeed;
    float targetDirection, direction;
    Stopwatch reactionWatch = new Stopwatch();
    final int reactionTime = 250;  // Reaction time in milliseconds for bot to notice player on screen
    final int SAFETY_FRAMES = 120 * 3;  // 3 second
    int safetyFireFrames;  // Extra number of frames to continue firing after target is lost
    Vector2 targetPos;
    Vector2 extForce = new Vector2(0, 0);

    boolean defenseMode = true;
    HashSet<Integer> targetSet;

    public BotController() {
        targetMoveDir = moveDir = intendedDir = (float) (Math.random() * 2 * Math.PI);
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

            if (Main.counter % 2 == 0) {
                targetSet = CollisionManager.queryBoundingBox(host.getView());
            }

            safetyFireFrames = Math.max(0, safetyFireFrames - 1);  // Decrement safety fire frames

            if (Main.counter % 2 == 0) {
                updateTarget();

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
            }

            direction = (float) Graphics.angle_lerp(direction, targetDirection, 0.15f);
        } else {
            direction = 0;
        }
    }

    public void updateTarget() {
        if (defenseMode) {
            GameObject targetObj = AutoAim.getClosestTargetDefense(targetSet, host, host.getView(), host.group);  // Get closest target unadjusted
            if (targetObj != null) {
                targetPos = AutoAim.getAdjustedTarget(targetObj, frontBarrel.getSpawnPoint(), frontBulletSpeed);  // Adjust target position
            } else {  // No defense target, switch to default
                targetPos = AutoAim.getAdjustedTarget(targetSet, host.pos, frontBarrel.getSpawnPoint(), host.getView(), host.group, frontBulletSpeed);  // Get closest target
            }
        } else {
            if (frontBarrel.canControlDrones) {
                Integer targId = AutoAim.getClosestTargetId(targetSet, host.pos, host.getView(), host.group);  // Get closest target unadjusted

                if (targId != null) {
                    GameObject targetObj = Main.gameObjectPool.getObj(targId);
                    if (Graphics.distanceSq(host.pos, targetObj.pos) < 1500 * 1500)  // If target is close enough
                        targetPos = AutoAim.getAdjustedTarget(targetObj, frontBarrel.getSpawnPoint(), frontBulletSpeed);  // Adjust target position
                    else
                        targetPos = targetObj.pos;  // Do not adjust target position
                } else {
                    targetPos = null;  // No target
                }
            } else {
                targetPos = AutoAim.getAdjustedTarget(targetSet, host.pos, frontBarrel.getSpawnPoint(), host.getView(), host.group, frontBulletSpeed);  // Get closest target
            }
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
/*        if (host.getStat(Stats.BODY_DAMAGE) >= 6) {
            // Bot will chase the player
            Vector2 PlayerPos = Main.player.pos;
            moveDir = (float) Math.atan2(PlayerPos.y - host.pos.y, PlayerPos.x - host.pos.x);
            return moveDir;
        }*/

        // Bot will bounce around the arena
        float xComp = (float) Math.cos(intendedDir);
        float yComp = (float) Math.sin(intendedDir);
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
        intendedDir = (float) Math.atan2(yComp, xComp);

        if (Main.counter % 2 == 0) {
            extForce = getExternalForce();
            Graphics.normalize(extForce);  // Normalize vector (unit vector)
            extForce.x *= importanceFactor;
            extForce.y *= importanceFactor;
            extForce.x += xComp;
            extForce.y += yComp;
        }

        targetMoveDir = (float) Math.atan2(extForce.y, extForce.x);

        moveDir = (float) Graphics.angle_lerp(moveDir, targetMoveDir, 0.5f);
        //moveDir = -1;
        return moveDir;
    }

    float importanceFactor = 0;

    private Vector2 getExternalForce() {
        Vector2 netForce = new Vector2(0, 0);
        if (targetSet == null) return netForce;

        // Get closest target
        float minDistSq = Float.MAX_VALUE;
        GameObject closestTarget = null;


        for (int id : targetSet) {
            GameObject obj = Main.gameObjectPool.getObj(id);
            if (obj == null ||  obj.group == host.group) continue;
            float dist = Graphics.distanceSq(host.pos, obj.pos);

            dist /= obj.getRadiusScaled();  // Larger objects are more important
            if (obj.isProjectile) dist *= 0.2f;  // Projectiles are more important
            if (dist < minDistSq) {
                minDistSq = dist;
                closestTarget = obj;
            }
        }

        if (closestTarget == null) return netForce;
        float hostVel = Graphics.length(host.vel);
        float levelFactor = 818.182f * host.level + 3181.82f;

        if (closestTarget.isProjectile) {
            importanceFactor = 20*levelFactor/(minDistSq + hostVel*hostVel);  // closer proj or slower host, more importance
            Vector2 distVec = new Vector2(closestTarget.pos.x - host.pos.x, closestTarget.pos.y - host.pos.y);
            Vector2 perp1 = new Vector2(-distVec.y, distVec.x);  // Perpendicular vector
            Vector2 perp2 = new Vector2(distVec.y, -distVec.x);  // Perpendicular vector
            // Dot product with host velocity, take more negative? dot product
            float dot1 = Graphics.dot(perp1, host.vel);
            float dot2 = Graphics.dot(perp2, host.vel);
            if (dot1 < dot2) {
                // Also add a repulsion force
                perp1.x -= 2f*distVec.x;
                perp1.y -= 2f*distVec.y;
                return perp1;
            } else {
                perp2.x -= 2f*distVec.x;
                perp2.y -= 2f*distVec.y;
                return perp2;
            }
        }

        importanceFactor = levelFactor/(minDistSq + hostVel * hostVel);
        Vector2 distVec = new Vector2(closestTarget.pos.x - host.pos.x, closestTarget.pos.y - host.pos.y);
        Vector2 perp1 = new Vector2(-distVec.y, distVec.x);  // Perpendicular vector
        Vector2 perp2 = new Vector2(distVec.y, -distVec.x);  // Perpendicular vector
        // Dot product with host velocity, take more positive dot product
        float dot1 = Graphics.dot(perp1, host.vel);
        float dot2 = Graphics.dot(perp2, host.vel);
        return dot1 > dot2 ? perp1 : perp2;
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
