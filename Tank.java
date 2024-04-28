import com.raylib.java.raymath.Vector2;

import com.raylib.java.core.Color;

/**
 * NOTES:
 * this.cameraEntity.cameraData.respawnLevel = Math.min(Math.max(this.cameraEntity.cameraData.values.level - 1, 1), Math.floor(Math.sqrt(this.cameraEntity.cameraData.values.level) * 3.2796));
 */

public class Tank extends GameObject {
    int level = 45;
    // 54.7766480515
    public Stats stats;
    float direction = 0;

    // Stat variables
    float baseAcceleration;
    float regenPerFrame;
    long lastDamageFrame = -30 * 120;

    // Objects that control the tank
    TankBuild tankBuild;
    Controller controller;

    boolean autoFire = false;

    // Colors
    Color fillCol = Graphics.RED;
    Color strokeCol = Graphics.RED_STROKE;


    // TODO: update stats (health, body damage, movement speed), rest should be auto-updated (verify this)
    public Tank(Vector2 pos, Controller controller, Stats stats) {
        super(pos, 50, 1.f, DrawPool.MIDDLE);
        setCollisionFactors(1, 8);
        initHealthBar();  // Initialize health bar object

        this.stats = stats;
        this.controller = controller;

        updateStats();  // Update tank based on level and stats
        this.controller.setHost(this);  // Set the controller's host to this tank
    }

    @Override
    protected void setFlags() {
        super.noInternalCollision = false;
        super.keepInArena = true;
        super.isProjectile = false;
    }

    public void setTankBuild(TankBuild tankBuild) {
        this.tankBuild = tankBuild;
        tankBuild.setHost(this);
    }

    /**
     * Call whenever level or stats change
     * Should be callable multiple times without issue
     */
    @Override
    public void updateStats() {
        // Max health
        super.setMaxHealth(50 + (2 * (level - 1)) + (20 * stats.getStat(Stats.MAX_HEALTH)));
        // Body Damage (base should be 20 not 25 TODO: see if this is good)
        super.setDamage((25 + 6 * stats.getStat(Stats.BODY_DAMAGE)) * (25.f/120));  // Body damage scaled down because fps TODO: TANK-TANK is different from TANK-OTHER DAMAGE (or maybe not in diepcustom repo, spike is mutliplied though)

        // ACCELERATION: https://www.desmos.com/calculator/qre98xzg76
        float A0 = (float)(2.55 * Math.pow(1.07, stats.getStat(Stats.MOVEMENT_SPEED)) / Math.pow(1.015, level - 1));
        float convergeSpeed = (10 * A0) * (25.f/120);  // 10A0 is max speed, 25/120 is scaling factor for 25->120 fps
        this.baseAcceleration = convergeSpeed * (1 - friction);  // a = Converge * (1-r)
        // Regen (regen per SECOND is 0.1 + (0.4 * P) percent of max health)
        this.regenPerFrame = (maxHealth + 4 * maxHealth * stats.getStat(Stats.HEALTH_REGEN)) / (120 * 1000);  // 120 fps
        // Size of tank
        scale = (float)Math.pow(1.01, (level - 1));

        // Health bar size
        healthBar.setWidth(radius * scale * 2);
    }

    protected void setDirection(double radians) {
        direction = (float) radians;
    }

    protected void setColor(Color fillCol, Color strokeCol) {
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
    }

    @Override
    public void update() {
        super.update();

        if (isDead) {
            tankBuild.update();  // Still update after death so turret positions are updated
            return;
        }

        // Health updates
        if (Main.counter - lastDamageFrame > 30 * 120) {  // After 30 seconds, hyper-regen
            health += maxHealth / (120 * 10);  // 10 percent HP per second
        } else {  // Normal regen
            health += regenPerFrame;
        }
        health = Math.min(health, maxHealth);  // Cap health at maxHealth


        // Update the direction barrel is facing
        setDirection(controller.barrelDirection());

        // Update input movement
        float moveDirection = controller.moveDirection();
        if (moveDirection != -1) {  // If valid direction
            addForce(baseAcceleration, moveDirection);
        }

        // Update auto fire
        if (controller.toggleAutoFire()) {
            autoFire = !autoFire;  // Flip autoFire
        }

        // Update all turrets
        tankBuild.update();
        tankBuild.updateFire(autoFire || controller.fire());
    }

    @Override
    public void draw() {
        // Culling

        // Draw Turrets
        tankBuild.draw();
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2, (radius*scale) * 2, Graphics.RED_STROKE);
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2 - 2*Graphics.strokeWidth, (radius*scale) * 2 - 2*Graphics.strokeWidth, Graphics.redCol);

        if (Main.onScreen(pos, radius*scale)) {
            Graphics.drawCircleTexture(pos.x, pos.y, radius*scale, Graphics.strokeWidth, getDamageLerpColor(fillCol), getDamageLerpColor(strokeCol), opacity);
        }
    }

    public boolean getAutoFire() {
        return autoFire;
    }

    public boolean specialControl() {
        return controller.special();
    }

    @Override
    public void receiveDamage(float damage) {
        super.receiveDamage(damage);
        lastDamageFrame = Main.counter;
    }

    public Vector2 getTarget() {
        return controller.getTarget();
    }
}
