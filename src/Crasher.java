

import com.raylib.java.raymath.Vector2;

public class Crasher extends GameObject {
    final static float aiRadius = 1700;

    boolean isLarge;
    private float baseAcceleration;
    double direction, targetDirection;
    private int scoreReward;
    Vector2 spawnPoint;
    public Crasher(Vector2 pos, boolean isLarge) {
        super(pos, (int) (0.5 * Math.sqrt(2) * (isLarge ? 55 : 35)), 1, DrawPool.BOTTOM);
        initSpawnAnimation(40);
        spawnPoint = pos;
        group = Polygon.polyGroup;
        this.isLarge = isLarge;

        targetDirection = direction = (float) (Math.random() * Math.PI * 2);
        username = "crasher";

        initHealthBar();
        updateStats();
    }

    @Override
    public void updateStats() {
        setCollisionFactors(isLarge ? 0.1f : 2f, isLarge ? 8 : 6);
        setMaxHealth(isLarge ? 30 : 10);
        setDamage(8 * 25.f / 120);
        scoreReward = isLarge ? 15 : 10;  // 25, 15 on diep, but nerfed in this version

        float velocity = 1.414f*(isLarge ? 2.7f : 2.65f);
        baseAcceleration = velocity * (1-friction);
        healthBar.setWidth(getRadiusScaled() * 2);
    }

    @Override
    protected void setFlags() {
        noInternalCollision = false;
        keepInArena = true;
        isProjectile = false;
        isPolygon = true;
        spawnProtection = false;
    }

    @Override
    public void update() {
        super.update();
        float minDistSq = Float.MAX_VALUE;
        Vector2 closestTarget = spawnPoint;  // By default, target the spawn point
        // Get the closest tank inside of the crasher zone
        for (Tank tank : Leaderboard.tankList) {
            // Check if tank is dead, out of bounds, or invisible
            if (tank.isDead || tank.isInvisible() || !Graphics.isIntersecting(tank.pos, Spawner.crasherZone)) {
                continue;
            }
            float distSq = Graphics.distanceSq(pos, tank.pos);
            if (distSq > aiRadius * aiRadius) {
                continue;
            }
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closestTarget = tank.pos;
            }
        }

        targetDirection = Math.atan2(closestTarget.y - pos.y, closestTarget.x - pos.x);
        direction = Graphics.angle_lerp(direction, targetDirection, 0.17f);
        addForce(baseAcceleration, (float) direction);
    }

    @Override
    public void draw() {
        if (Main.onScreen(pos, getRadiusScaled() * 2/3)) {
            Graphics.drawTriangleRounded(pos, getRadiusScaled() * 2/3, (float) direction, Graphics.strokeWidth, Graphics.colAlpha(getDamageLerpColor(Graphics.CRASHER), opacity), Graphics.colAlpha(getDamageLerpColor(Graphics.CRASHER_STROKE), opacity));
        }
    }

    @Override
    protected float getScoreReward() {
        return scoreReward * Polygon.multiplier;
    }

    @Override
    public void delete() {
        super.delete();
        Spawner.crasherCount--;
    }

    @Override
    public void updateVictim(GameObject victim) {
        super.updateVictim(victim);
        if (victim == Main.player) {  // If killed player
            Main.cameraHost = this;
            Main.killerName = username;
        }
    }
}
