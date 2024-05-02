import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.HashSet;

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

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);
            float distSquared = Graphics.distanceSq(sourcePos, obj.pos);

            if (obj.group == group || obj.isProjectile || distSquared > radius * radius) {  // If same group or projectile OR too far, skip
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
        }

        return closestTarget;
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
        HashSet<Integer> targets = CollisionManager.queryBoundingBox(view);
        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;
        Vector2 closestTarget = null;  // Set to null if no target found
        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);

            if (obj.group == group || obj.isProjectile) {  // If same group or projectile OR not in view, skip
                continue;
            }
            Rectangle boundingBox = obj.boundingBox();

            // Increase obj bounding box because of barrel length TODO: assumes obj is a tank
            float turretLength = ((Tank)obj).tankBuild.getFrontBarrel().getTurretLength();
            boundingBox.x -= turretLength;
            boundingBox.y -= turretLength;
            boundingBox.width += 2 * turretLength;
            boundingBox.height += 2 * turretLength;

            float distSquared = Graphics.distanceSq(sourcePos, obj.pos);
            if (Graphics.isIntersecting(boundingBox, view) && distSquared < minDistSquared) {  // New closest target
                minDistSquared = distSquared;
                closestTarget = getAdjustedTarget(obj, spawnPoint, bulletSpeed);  // Update the closest target (adjusted)
            }
        }

        return closestTarget;
    }
}
