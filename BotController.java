import com.raylib.java.raymath.Vector2;

import java.util.HashSet;

// TODO: auto turret should be auto fire mode, these guys turn too fast, maybe add lock on time
// TODO: add offensive mode, try out negative fear factors for offensive tanks (triplet, sprayer, etc.)
// TODO: AI for drone-based tanks
public class BotController implements Controller {
    Tank host;
    float moveDir, intendedDir, bounceDir;
    boolean shouldFire;
    Barrel frontBarrel;
    public float frontBulletSpeed;
    float direction;
    final int SAFETY_FRAMES = 120 * 3;  // 3 second
    int safetyFireFrames;  // Extra number of frames to continue firing after target is lost
    Vector2 targetPos, currentPos;
    GameObject targetObj;
    Tank closestTank;  // Closest enemy tank
    Vector2 extForce = new Vector2(0, 0);

    float confidence = 0;  // confidence lowers fear factor
    boolean defenseMode = true;
    HashSet<Integer> targetSet;


    public BotController() {
        bounceDir = moveDir = intendedDir = (float) (Math.random() * 2 * Math.PI);
        targetPos = null;
        currentPos = new Vector2(0, 0);
    }

    @Override
    public void setHost(Tank host) {
        this.host = host;
        shouldFire = false;
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
        if (Main.counter % 2 == 0) {
            targetSet = CollisionManager.queryBoundingBox(host.getView());
        }

        if (frontBarrel != null) {  // If front barrel exists
            safetyFireFrames = Math.max(0, safetyFireFrames - 1);  // Decrement safety fire frames

            if (Main.counter % 2 == 0) {
                updateTarget();

                if (targetPos != null) {  // If there is a closest target
                    currentPos.x += (targetPos.x - currentPos.x) * 0.15f;
                    currentPos.y += (targetPos.y - currentPos.y) * 0.15f;
                    //shouldFire = Graphics.distanceSq(currentPos, targetPos) < 100 * 100;  // If close enough, fire
                    shouldFire = true;
                    safetyFireFrames = SAFETY_FRAMES;  // Set safety fire frames to max
                } else {
                    if (safetyFireFrames == 0) {  // If safety frames has run out
                        if (defenseMode) {
                            targetPos = new Vector2(host.pos.x  + host.vel.x, host.pos.y + host.vel.y);  // Point in movement direction
                        } else {
                            targetPos = new Vector2(host.pos.x - host.vel.x, host.pos.y - host.vel.y);  // Point backwards to boost
                        }
                        currentPos.x += (targetPos.x - currentPos.x) * 0.15f;
                        currentPos.y += (targetPos.y - currentPos.y) * 0.15f;

                        shouldFire = false;
                    }
                }
            }
            direction = (float) Math.atan2(currentPos.y - host.pos.y, currentPos.x - host.pos.x);
        } else {
            direction = 0;
        }
    }

    public void updateTarget() {
        if (closestTank != null) {  // Check if should be defense mode
            if (Graphics.distanceSq(closestTank.pos, host.pos) < 400 * 400) {  // If tank is too close, simply attack
                defenseMode = false;
            } else {
                defenseMode = true;
            }
        }
        if (closestTank == null) defenseMode = true;  // No tank in sight, but potentially projectiles

        if (defenseMode) {
            targetObj = AutoAim.getClosestTargetDefense(targetSet, host, host.getView(), host.group);  // Get closest target unadjusted (projectile or tank)

            if (targetObj != null) {  // If there is a target
                targetPos = AutoAim.getAdjustedTarget(targetObj, frontBarrel.getSpawnPoint(), frontBulletSpeed);  // Adjust target position
            } else {  // No defense target, switch to default
                targetPos = AutoAim.getAdjustedTarget(targetSet, host.pos, frontBarrel.getSpawnPoint(), host.getView(), host.group, frontBulletSpeed);  // Get closest target
            }
        }

        if (!defenseMode) {
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
                targetPos = AutoAim.getAdjustedTarget(closestTank, frontBarrel.getSpawnPoint(), frontBulletSpeed);  // Adjust target position
            }
        }
    }

    @Override
    public float barrelDirection() {
        return direction;
    }

    /**
     * For drones
     *
     * @return
     */
    @Override
    public Vector2 getTarget() {
        if (targetPos == null) {  // Return host position plus its direction vector
            float vel = host.radius * host.scale + 20 * Graphics.length(host.vel);  // Multiply by some amount so drones stay in front
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

        // Bot will go towards the center of the map (for now)
        if (host.level < 45) {
            intendedDir = (float) Math.atan2(Main.arenaHeight / 2 - host.pos.y, Main.arenaWidth / 2 - host.pos.x);
        } else {
            String buildName = host.tankBuild.name;
            boolean bulletSpammer = buildName.contains("triplet") || buildName.contains("sprayer") || buildName.contains("auto gunner") || buildName.contains("streamliner");
            boolean bounce = true;

            if (bulletSpammer) {
                confidence = 0.2f;
            }
            if (host == LeaderPointer.leader) confidence = 0.5f;  // Leader is confident

            if (LeaderPointer.leader != null && LeaderPointer.leader != host) {  // Chase leader
                intendedDir = (float) Math.atan2(LeaderPointer.leader.pos.y - host.pos.y, LeaderPointer.leader.pos.x - host.pos.x);
                bounce = false;  // No need to pick random direction
            }

            if (bounce) {  // Randomly bounce around the map
                float xComp = (float) Math.cos(bounceDir);
                float yComp = (float) Math.sin(bounceDir);
                // Check if bot should bounce off walls
                if (host.pos.x <= 0) {
                    xComp = Math.abs(xComp);
                    bounceDir = (float) Math.atan2(yComp, xComp);
                } else if (host.pos.x >= Main.arenaWidth) {
                    xComp = -Math.abs(xComp);
                    bounceDir = (float) Math.atan2(yComp, xComp);
                } else if (host.pos.y <= 0) {
                    yComp = Math.abs(yComp);
                    bounceDir = (float) Math.atan2(yComp, xComp);
                } else if (host.pos.y >= Main.arenaHeight) {
                    yComp = -Math.abs(yComp);
                    bounceDir = (float) Math.atan2(yComp, xComp);
                }
                intendedDir = bounceDir;
            }
        }

        float xComp = (float) Math.cos(intendedDir);
        float yComp = (float) Math.sin(intendedDir);

        if (Main.counter % 2 == 0) {
            extForce = getExternalForce();
            Graphics.normalize(extForce);  // Normalize vector (unit vector)
            extForce.x *= importanceFactor;
            extForce.y *= importanceFactor;
            extForce.x += xComp;
            extForce.y += yComp;
        }

        moveDir = (float) Math.atan2(extForce.y, extForce.x);
        //moveDir = -1;
        return moveDir;
    }

    float importanceFactor = 0;

    // TODO: if trapped against wall, swap dot1 and dot2 and remove fear factor

    private Vector2 getExternalForce() {
        Vector2 netForce = new Vector2(0, 0);
        if (targetSet == null) return netForce;

        // Get closest target
        float minDistSq = Float.MAX_VALUE;
        GameObject closestTarget = null;

        // Get closest tank while at it
        float minDistTankSq = Float.MAX_VALUE;
        closestTank = null;

        float teamConfidence = 0;  // Confidence from teammates

        for (int id : targetSet) {
            GameObject obj = Main.gameObjectPool.getObj(id);

            // Get closest tank (different group)
            if (obj instanceof Tank && obj != host) {
                float distTank = Graphics.distanceSq(host.pos, obj.pos);
                if (distTank < minDistTankSq && obj.group != host.group) {
                    minDistTankSq = distTank;
                    closestTank = (Tank) obj;
                }
                if (obj.group == host.group && distTank < 500 * 500) teamConfidence += 0.05f;  // Close-by teammates increase confidence
            }

            if (obj == null || obj.group == host.group) continue;
            float dist = Graphics.distance(host.pos, obj.pos);
            float totRad = host.getRadiusScaled() + obj.getRadiusScaled();
            if (obj.isProjectile) {
                dist /= ((Projectile)obj).getMaxSpeed();  // Projectiles are more important
            }
            dist = (dist - totRad) * (dist - totRad);  // Square distance

            if (dist < minDistSq) {
                minDistSq = dist;
                closestTarget = obj;
            }
        }

        if (closestTarget == null) return netForce;
        float objRad = closestTarget.getRadiusScaled();
        float levelFactor = (float) (Math.sqrt(Graphics.length(host.vel)) * 750 * objRad * (1 - (confidence + teamConfidence) * 0.5f)); /*818.182f * host.level + 3181.82f*/;
        importanceFactor = levelFactor / minDistSq;  // closer proj or slower host, more importance

        if (closestTarget.isProjectile) {
            Vector2 repelVec = new Vector2(host.pos.x - closestTarget.pos.x, host.pos.y - closestTarget.pos.y);  // Vector from projectile to host
            Graphics.normalize(repelVec);

            Vector2 perp1 = new Vector2(-closestTarget.vel.y, closestTarget.vel.x);  // Perpendicular vector of projectile velocity
            Vector2 perp2 = new Vector2(closestTarget.vel.y, -closestTarget.vel.x);  // Perpendicular vector of projectile velocity
            Graphics.normalize(perp1);
            Graphics.normalize(perp2);
            // Dot product with repelVec, take more positive dot product (perp vector more aligned with repelVec)
            float dot1 = Graphics.dot(perp1, repelVec), dot2 = Graphics.dot(perp2, repelVec);

            float fearFactor = 0.707f * (Math.max(0.4f * (45 - host.level) / 44 - teamConfidence - confidence, 0) + 0.1f);  // Higher level, less fear
            // Also add a repulsion force scaled by fear
            if (dot1 > dot2) {
                perp1.x += fearFactor * repelVec.x;
                perp1.y += fearFactor * repelVec.y;
                return perp1;
            } else {
                perp2.x += fearFactor * repelVec.x;
                perp2.y += fearFactor * repelVec.y;
                return perp2;
            }
        }

      if (Main.counter % 20 == 0)
            Main.debugText = String.valueOf(importanceFactor);

        Vector2 distVec = new Vector2(closestTarget.pos.x - host.pos.x, closestTarget.pos.y - host.pos.y);  // Vector from host to target
        Graphics.normalize(distVec);
        Vector2 perp1 = new Vector2(-distVec.y, distVec.x);  // Perpendicular vector
        Vector2 perp2 = new Vector2(distVec.y, -distVec.x);  // Perpendicular vector
        // Dot product with host velocity, take more positive dot product
        float dot1 = Graphics.dot(perp1, host.vel), dot2 = Graphics.dot(perp2, host.vel);
        return dot1 > dot2 ? perp1 : perp2;
    }

    @Override
    public boolean fire() {
        return true;
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
