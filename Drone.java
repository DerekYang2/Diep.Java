import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.ArrayList;

public class Drone extends GameObject {
    protected float acceleration;
    float targetDirection;
    float direction;
    Vector2 target;
    Color fillCol;
    Color strokeCol;
    Barrel hostBarrel;
    boolean aiOn = false;  // Default to off

    public Drone(Barrel hostBarrel, float centerX, float centerY, float direction, float cannonLength, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        super(new Vector2(centerX + cannonLength * (float) Math.cos(direction), centerY + cannonLength * (float) Math.sin(direction)), (int) (diameter * 0.5f), bulletStats.absorbtionFactor, 4, 1f);

        // Flags
        super.noInternalCollision = true;  // Does not collide with bullets from same group, exception: collides with drones of same group
        super.keepInArena = true;
        super.isProjectile = true;

        this.hostBarrel = hostBarrel;  // Set the host barrel object
        Tank host = hostBarrel.host;  // Get the tank of the host barrel

        this.group = host.group;  // Set group to host group

        // Calculate bullet stats
        // https://github.com/ABCxFF/diepindepth/blob/b035291bd0bed436d0ffbe2eb707fb96ed5f2bf4/extras/stats.md?plain=1#L34
        float damage = (7 + (3 * host.stats.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.stats.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        float velMax = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed - (float)Math.random() * bulletStats.scatterRate;  // src: not link above (check diepcustom repo)
        velMax *= 1.1f;  // TODO: TEST IF THIS IS RIGHT
        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        this.targetDirection = this.direction = (float) Graphics.normalizeAngle(direction);  // Set direction to normalized angle

        // Calculate acceleration to converge to max speed (https://www.desmos.com/calculator/9hakym7jxy)
        this.acceleration = (velMax * 25.f/120) * (1-friction);
        //System.out.println(velMax + " " + acceleration + " " + friction);
        float initialSpeed = 30 * (1-friction)/(1-0.9f);
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Drawing variables
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        radius = diameter * 0.5f * bulletStats.sizeRatio;  // Multiply radius by bullet stats size ratio
    }

    @Override
    public void draw() {
        final float scaledRadius = radius * scale * 0.74f;  // scale is always 1 until death animation
        if (pos.x + scaledRadius < Main.cameraBox.x || pos.x - scaledRadius > Main.cameraBox.x + Main.cameraBox.width || pos.y + scaledRadius < Main.cameraBox.y || pos.y - scaledRadius > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        Graphics.drawTriangleRounded(pos, scaledRadius, direction, Graphics.strokeWidth, Graphics.colAlpha(getDamageLerpColor(fillCol), opacity), Graphics.colAlpha(getDamageLerpColor(strokeCol), opacity));
    }

    @Override
    public void update() {
        super.update();

        if (isDead) return;

        if (hostBarrel.host.isDead) {
            triggerDelete();
        }

        // Update AI on from host tank
        aiOn = hostBarrel.host.getAutoFire() || !hostBarrel.canControlDrones;  // If host tank has autofire or cannot control drones, turn on AI

        if (aiOn) {
            Integer closestTarget = getClosestTarget();
            if (closestTarget != null) {  // If there is a closest target
                target = Main.gameObjectPool.getObj(closestTarget).pos;
            } else {  // If no target in range, set target to the tank pos
                target = hostBarrel.host.pos;
            }
        } else {
            target = hostBarrel.host.getTarget();
        }

        targetDirection = (float) Math.atan2(target.y - pos.y, target.x - pos.x);

        if (!aiOn && hostBarrel.host.specialControl()) {  // Repel
            targetDirection += (float) Math.PI;  // Reverse direction of target
        }

        direction = (float)Graphics.angle_lerp(direction, targetDirection, 0.17f);

        addForce(acceleration, direction);
    }

    private Integer getClosestTarget() {
        float rectWidth =  2 * 850 * hostBarrel.host.scale;
        Rectangle view = new Rectangle(pos.x - rectWidth * 0.5f, pos.y - rectWidth * 0.5f, rectWidth, rectWidth);

        ArrayList<Integer> targets = CollisionManager.queryBoundingBox(view, this.group);
        // Get the closest target
        float minDistSquared = Float.MAX_VALUE;
        Integer closestTarget = null;  // Set id to some impossible value

        for (int id : targets) {
            GameObject obj = Main.gameObjectPool.getObj(id);

            if (obj.group == this.group) {  // If same group, skip
                continue;
            }
            float distSquared = (pos.x - obj.pos.x) * (pos.x - obj.pos.x) + (pos.y - obj.pos.y) * (pos.y - obj.pos.y);
            if (distSquared < minDistSquared) {
                minDistSquared = distSquared;
                closestTarget = id;
            }
        }
        return closestTarget;
    }

    @Override
    public void triggerDelete() {
        super.triggerDelete();
    }

    @Override
    public void addToPools() {
        super.addToPools();
        Main.drawablePool.addObj(this, DrawPool.BOTTOM);
    }

    @Override
    public void delete() {
        super.delete();
        Main.drawablePool.deleteObj(this.getId(), DrawPool.BOTTOM);
        hostBarrel.decrementDroneCount();
    }

}
