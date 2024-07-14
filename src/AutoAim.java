

import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.HashSet;
// TODO: when fighting manager or just drones in general, if not tank targets in sight, should shoot drones
public class AutoAim {
    // AI Functions Below

    private static double sqr(double v) {
        return v*v;
    }

    // https://www.desmos.com/calculator/wi99dkcfsy
    private static float scaleFactor(float dist) {
        if (dist >= 2000) return 1.1f;
        return (float) ((dist-2000) * (dist-2000)/(1.7 * 2000 * 2000)+ 1.1);
    }

    /**
     * Get the target shift based on the target's velocity
     * @param target The GameObject target
     * @return The target with velocity accounted for
     */
    public static Vector2 getAdjustedTarget(GameObject target, Vector2 spawnPoint, float bulletSpeed) {
        float projectile_speed = bulletSpeed * scaleFactor(Graphics.distance(spawnPoint, target.pos));

        double a = sqr(target.vel.x) + sqr(target.vel.y) - sqr(projectile_speed);
        double b = 2 * (target.vel.x * (target.pos.x - spawnPoint.x) + target.vel.y * (target.pos.y - spawnPoint.y));
        double c = sqr(target.pos.x - spawnPoint.x) + sqr(target.pos.y - spawnPoint.y);

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return target.pos;
        }

        // Quad formula
        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a), t2 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double t = (t1 > 0 && t2 > 0) ? Math.min(t1, t2) : Math.max(t1, t2);
        return new Vector2((float) (target.pos.x + target.vel.x * t), (float) (target.pos.y + target.vel.y * t));
    }

    /**
     * Get the closest target within the view (in a different group)
     * @param sourcePos The position of the source
     * @param spawnPoint The spawn point of the bullet
     * @param radius The radius of the view
     * @param group The group of the source
     * @param sourceAngle The angle of the source
     * @param angleRange The range of the angle sector
     * @param bulletSpeed The speed of the bullet
     * @return The closest target position
     */
    public static Vector2 getAdjustedTarget(Vector2 sourcePos, Vector2 spawnPoint, float radius, int group, double sourceAngle, double angleRange, float bulletSpeed) {
        Rectangle view = new Rectangle(sourcePos.x - radius, sourcePos.y - radius, 2*radius, 2*radius);
        HashSet<Integer> targets = CollisionManager.queryBoundingBox(view);
        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;
        Vector2 closestTarget = null;  // Set to null if no target found

        float minDistTank = Float.MAX_VALUE;
        Vector2 closestTank = null;

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);
            if (obj == null) continue;
            float distSquared = Graphics.distanceSq(sourcePos, obj.pos);

            if (obj.group == group || obj.isProjectile || distSquared > radius * radius || obj.isInvisible()) {  // If same group or projectile OR too far, skip
                continue;
            }

            if (distSquared < minDistSquared) {  // Potential closest target
                Vector2 shiftedTarget = getAdjustedTarget(obj, spawnPoint, bulletSpeed);  // Get the corrected target position
                if (angleRange < 2 * Math.PI) {  // Check if within angle range
                    double startAngle = sourceAngle - angleRange * 0.5, endAngle = sourceAngle + angleRange * 0.5;  // End angle is ccw
                    double targetAngle = Math.atan2(shiftedTarget.y - sourcePos.y, shiftedTarget.x - sourcePos.x);  // Angle towards the target
                    if (!Graphics.isAngleBetween(targetAngle, startAngle, endAngle)) {  // If target not within angle range, skip
                        continue;
                    }
                }
                minDistSquared = distSquared;
                closestTarget = shiftedTarget;  // Update closest target
            }
            if ((obj instanceof Tank) && distSquared < minDistTank) {  // New closest tank
                Vector2 shiftedTarget = getAdjustedTarget(obj, spawnPoint, bulletSpeed);  // Get the corrected target position
                if (angleRange < 2 * Math.PI) {  // Check if within angle range
                    double startAngle = sourceAngle - angleRange * 0.5, endAngle = sourceAngle + angleRange * 0.5;  // End angle is ccw
                    double targetAngle = Math.atan2(shiftedTarget.y - sourcePos.y, shiftedTarget.x - sourcePos.x);  // Angle towards the target
                    if (!Graphics.isAngleBetween(targetAngle, startAngle, endAngle)) {  // If target not within angle range, skip
                        continue;
                    }
                }
                minDistTank = distSquared;
                closestTank = shiftedTarget;
            }
        }

        return closestTank != null ? closestTank : closestTarget;
    }

    public static Vector2 getAdjustedTarget(HashSet<Integer> targets, Vector2 sourcePos, Vector2 spawnPoint, Rectangle view, int group, float bulletSpeed) {
        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;
        Vector2 closestTarget = null;  // Set to null if no target found

        float minDistTank = Float.MAX_VALUE;
        Vector2 closestTank = null;

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);

            if (obj.group == group || obj.isDead || obj.isInvisible()) {  // If same group or projectile OR not in view, skip
                continue;
            }
            Rectangle boundingBox = obj.boundingBox();
            float distSquared = Graphics.distanceSq(sourcePos, obj.pos);
            if (Graphics.isIntersecting(boundingBox, view)) {  // New closest target
                if (obj instanceof Tank) {
                    if (distSquared < minDistTank) {  // New closest tank
                        Vector2 shiftedTarget = getAdjustedTarget(obj, spawnPoint, bulletSpeed);  // Get the corrected target position
                        minDistTank = distSquared;
                        closestTank = shiftedTarget;
                    }
                } else if (distSquared < minDistSquared) {  // Potential closest target
                    Vector2 shiftedTarget = getAdjustedTarget(obj, spawnPoint, bulletSpeed);  // Get the corrected target position
                    minDistSquared = distSquared;
                    closestTarget = shiftedTarget;  // Update closest target
                }
            }
        }

        return closestTank != null ? closestTank : closestTarget;
    }

    /**
     * Get the closest target within the view (in a different group)
     * @param sourcePos The position of the source
     * @param spawnPoint The spawn point of the bullet
     * @param view The view rectangle
     * @param group The group of the source
     * @param bulletSpeed The speed of the bullet
     * @return The closest target position
     */
    public static Vector2 getAdjustedTarget(Vector2 sourcePos, Vector2 spawnPoint, Rectangle view, int group, float bulletSpeed) {
        return getAdjustedTarget(CollisionManager.queryBoundingBox(view), sourcePos, spawnPoint, view, group, bulletSpeed);
    }

    /**
     * Get the closest target within the view (in a different group)
     * @param currentPos The position of the object in question (closest to this position)
     * @param viewOrigin The position of the center of the view radius
     * @param radius The radius of the view
     * @param group The group of the source
     * @return The closest target position
     * TODO: similarly priotize tank like method below
     */
    public static Vector2 getClosestTarget(Vector2 currentPos, Vector2 viewOrigin, float radius, int group) {
        Rectangle view = new Rectangle(viewOrigin.x - radius, viewOrigin.y - radius, 2*radius, 2*radius);
        HashSet<Integer> targets = CollisionManager.queryBoundingBox(view);
        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;
        Vector2 closestTarget = null;  // Set to null if no target found

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);

            if (obj.group == group || obj.isDead || obj.isProjectile || Graphics.distanceSq(viewOrigin, obj.pos) > radius * radius || obj.isInvisible()) {  // If same group or projectile OR too far, skip
                continue;
            }

            float distToCurrent = Graphics.distanceSq(currentPos, obj.pos);

            if (distToCurrent < minDistSquared) {  // Potential closest target
                minDistSquared = distToCurrent;
                closestTarget = obj.pos;  // Update closest target
            }
        }

        return closestTarget;
    }

    /**
     * Get the closest target id within the view (in a different group)
     * @param sourcePos The position of the source
     * @param view The view rectangle
     * @param group The group of the source
     * @return The closest target position
     */
    public static Integer getClosestTargetId(HashSet<Integer> targets, Vector2 sourcePos, Rectangle view, int group) {
        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;
        Integer closestId = null;  // Set to null if no target found

        float minDistTank = Float.MAX_VALUE;
        Integer closestTankId = null;

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);
            if (obj.group == group || obj.isDead || obj.isProjectile || obj.isInvisible()) {  // If same group or projectile OR not in view, skip
                continue;
            }
            Rectangle boundingBox = obj.boundingBox();
            float distSquared = Graphics.distanceSq(sourcePos, obj.pos);

            if (Graphics.isIntersecting(boundingBox, view)) {
                if (distSquared < minDistSquared) {  // New closest target
                    minDistSquared = distSquared;
                    closestId = obj.id;  // Update the closest target (adjusted)
                }
                if ((obj instanceof Tank) && distSquared < minDistTank) {  // New closest tank
                    minDistTank = distSquared;
                    closestTankId = obj.id;
                }
            }
        }
        return closestTankId != null ? closestTankId : closestId;
    }

    public static boolean willCollide(GameObject obj1, GameObject obj2) {
        // TODO: custom case when vel.x is close to 0
        float m1 = obj1.vel.y/obj1.vel.x, m2 = obj2.vel.y/obj2.vel.x;
        float b1 = -m1* obj1.pos.x + obj1.pos.y, b2 = -m2 * obj2.pos.x + obj2.pos.y;

        float x = (b2 - b1) / (m1 - m2);
        float T = (x - obj1.pos.x) / obj1.vel.x;  // Time to collision
        float dT = 60;
        Vector2 pos1 = new Vector2(obj1.pos.x + obj1.vel.x * (T-3*dT), obj1.pos.y + obj1.vel.y * (T-3*dT));
        Vector2 pos2 = new Vector2(obj2.pos.x + obj2.vel.x * (T-3*dT), obj2.pos.y + obj2.vel.y * (T-3*dT));
        for (int i = 0; i < 5; i++) {
            pos1.x += obj1.vel.x * dT;
            pos1.y += obj1.vel.y * dT;
            pos2.x += obj2.vel.x * dT;
            pos2.y += obj2.vel.y * dT;
            if (Graphics.distance(pos1, pos2) < (obj1.getRadiusScaled() + obj2.getRadiusScaled())) {
                return true;
            }
        }
        return false;
    }

    public static GameObject getClosestTargetDefense(HashSet<Integer> targets, GameObject sourceObj, Rectangle view, int group) {
        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;  // Closest distance to projectile that will hit
        GameObject closestObj = null;  // Set to null if no target found

        float minDistAll = Float.MAX_VALUE;  // Closest distance to projectile that may not hit
        GameObject closestObjAll = null;  // Set to null if no target found

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);
            if (obj.group == group || obj.isDead || obj.isInvisible() || obj.isPolygon) {  // If same group or projectile OR not in view, skip
                continue;
            }
            Rectangle boundingBox = obj.boundingBox();
            float distSquared = Graphics.distanceSq(sourceObj.pos, obj.pos);

            if (Graphics.isIntersecting(boundingBox, view)) {
                if (distSquared < minDistSquared && willCollide(sourceObj, obj)) {  // New closest target that will collide
                    minDistSquared = distSquared;
                    closestObj = obj;  // Update the closest target (adjusted)
                }
                if (distSquared < minDistAll) {  // New closest target
                    minDistAll = distSquared;
                    closestObjAll = obj;  // Update the closest target (adjusted)
                }
            }
        }

        return closestObj == null ? closestObjAll : closestObj;
    }
}
