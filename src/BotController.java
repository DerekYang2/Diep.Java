

import com.raylib.java.raymath.Vector2;

import java.util.HashSet;

// TODO: auto turret should be auto fire mode, these guys turn too fast, maybe add lock on time
// TODO: AI for drone-based tanks, body damager
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
    double rand;

    public BotController() {
        bounceDir = moveDir = intendedDir = (float) (Math.random() * 2 * Math.PI);
        targetPos = null;
        currentPos = new Vector2(0, 0);
        rand = Math.random();  // Persistent random number for a tank
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
            if (rand < 0.4 || host.level < 20) {  // 40% of tanks do not go to center or if too low level
                // Corner farmer
                if (rand < 0.1f) {  // Corners
                    if (rand < 0.025) {  // Top left
                        intendedDir = (float) Math.atan2(0.1 * Main.arenaWidth + 0.1 * Main.arenaWidth * Math.cos(Main.counter / 1000F) - host.pos.y, 0.1 * Main.arenaHeight + 0.1f * Main.arenaHeight * Math.sin(Main.counter / 1000F) - host.pos.x);
                    } else if (rand < 0.05) {  // Top right
                        intendedDir = (float) Math.atan2(0.9 * Main.arenaWidth + 0.1 * Main.arenaWidth * Math.cos(Main.counter / 1000F) - host.pos.y, 0.1 * Main.arenaHeight + 0.1f * Main.arenaHeight * Math.sin(Main.counter / 1000F) - host.pos.x);
                    } else if (rand < 0.075) {  // Bottom right
                        intendedDir = (float) Math.atan2(0.9 * Main.arenaWidth + 0.1 * Main.arenaWidth * Math.cos(Main.counter / 1000F) - host.pos.y, 0.9 * Main.arenaHeight + 0.1f * Main.arenaHeight * Math.sin(Main.counter / 1000F) - host.pos.x);
                    } else {  // Bottom left
                        intendedDir = (float) Math.atan2(0.1 * Main.arenaWidth + 0.1 * Main.arenaWidth * Math.cos(Main.counter / 1000F) - host.pos.y, 0.9 * Main.arenaHeight + 0.1f * Main.arenaHeight * Math.sin(Main.counter / 1000F) - host.pos.x);
                    }
                } else {  // Loop around the map (square)
                    final Vector2 center = new Vector2(Main.arenaWidth / 2, Main.arenaHeight / 2);
                    float radTarget = (float) (0.25f * (1.1f + rand) * Main.arenaWidth);  // Manhattan distance to center
                    float currentRad = Graphics.manhattanDistance(host.pos, center);

                    float angle = (float) Graphics.normalizeAngle(Math.atan2(host.pos.y - center.x, host.pos.x - center.y));
                    if ((0 <= angle && angle < Math.PI / 4) || (7 * Math.PI / 4 <= angle && angle < 2 * Math.PI)) {  // up
                        intendedDir = (float) Math.PI / 2;
                    } else if (Math.PI / 4 <= angle && angle < 3 * Math.PI / 4) {  // left
                        intendedDir = (float) Math.PI;
                    } else if (3 * Math.PI / 4 <= angle && angle < 5 * Math.PI / 4) {  // down
                        intendedDir = (float) -Math.PI / 2;
                    } else {  // right
                        intendedDir = 0;
                    }
                    if (rand < 0.2) {  // Half of the 40% rotate in opposite direction
                        intendedDir *= -1;
                    }

                    // Tweak rotation radius
                    if (currentRad < radTarget) {  // If inside target radius
                        intendedDir = (float) Graphics.angle_lerp(intendedDir, Math.atan2(host.pos.y - center.x, host.pos.x - center.y), 0.5);  // Go away from center
                    } else if (currentRad > radTarget + Main.arenaWidth / 8) {  // If too far outside target radius
                        intendedDir = (float) Graphics.angle_lerp(intendedDir, Math.atan2(center.y - host.pos.y, center.x - host.pos.x), 0.5);  // Go towards center
                    }
                }
            } else {
                intendedDir = (float) Math.atan2(Main.arenaHeight / 2 - host.pos.y + 750 * Math.cos(Main.counter/1200F + rand), Main.arenaWidth / 2 - host.pos.x + 750 * Math.sin(Main.counter/1200F + rand));
            }
        } else {
            String buildName = host.tankBuild.name;
            boolean bulletSpammer = buildName.contains("triplet") || buildName.contains("sprayer") || buildName.contains("auto gunner") || buildName.contains("streamliner");
            boolean bounce = true;
            Tank leader = Leaderboard.getTankRank(0);

            if (host.level == 45)
                confidence = 0.3f;
            if (bulletSpammer) {
                confidence = 0.4f;
            }
            if (host == leader && host.level == 45) confidence = 0.7f;  // Leader is confident

            if (leader != null && leader.group != host.group) {  // Chase leader
                intendedDir = (float) Math.atan2(leader.pos.y - host.pos.y + 300 * Math.cos(Main.counter/1200F + rand), leader.pos.x - host.pos.x + 300 * Math.sin(Main.counter/1200F + rand));
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
            // More confidence, move in intended direction
            extForce.x += xComp * (1+confidence);
            extForce.y += yComp * (1+confidence);
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
                teamConfidence += 0.05f;  // Close-by teammates increase confidence
            }

            if (obj == null || obj.group == host.group) continue;
            float dist = Graphics.distance(host.pos, obj.pos);
            float totRad = host.getRadiusScaled() + obj.getRadiusScaled();
            if (obj.isProjectile) {
                if (AutoAim.willCollide(obj, host) || obj instanceof Drone) {
                    dist /= ((Projectile) obj).getMaxSpeed();  // Projectiles are more important
                } else {
                    dist /= 3f;
                }
            }
            dist = (dist - totRad) * (dist - totRad);  // Square distance

            if (dist < minDistSq) {
                minDistSq = dist;
                closestTarget = obj;
            }
        }

        if (closestTarget == null) return netForce;
        float objRad = closestTarget.getRadiusScaled();

        float levelFactor = (float) Math.max(0, (Math.sqrt(Graphics.length(host.vel)) * 750 * objRad * (1-(confidence+teamConfidence) * 0.4f))); /*818.182f * host.level + 3181.82f*/;


        importanceFactor = levelFactor / minDistSq;  // closer proj or slower host, more importance
/*        if (Main.counter % 120 == 0)
            System.out.println(importanceFactor);*/
        if (closestTarget.isProjectile) {
            Vector2 repelVec = new Vector2(host.pos.x - closestTarget.pos.x, host.pos.y - closestTarget.pos.y);  // Vector from projectile to host
            Graphics.normalize(repelVec);

            Vector2 perp1 = new Vector2(-closestTarget.vel.y, closestTarget.vel.x);  // Perpendicular vector of projectile velocity
            Vector2 perp2 = new Vector2(closestTarget.vel.y, -closestTarget.vel.x);  // Perpendicular vector of projectile velocity
            Graphics.normalize(perp1);
            Graphics.normalize(perp2);
            // Dot product with repelVec, take more positive dot product (perp vector more aligned with repelVec)
            float dot1 = Graphics.dot(perp1, repelVec), dot2 = Graphics.dot(perp2, repelVec);

            float fearFactor = 0.707f * (Math.max(0.6f * (45 - host.level) / 44 + 0.1f - teamConfidence - confidence, 0));  // Higher level, less fear
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

/*      if (Main.counter % 20 == 0)
            Main.debugText = String.valueOf(importanceFactor);*/

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
