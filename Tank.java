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

    // Colors
    Color fillCol = Graphics.RED;
    Color strokeCol = Graphics.RED_STROKE;


    // TODO: update stats (health, body damage, movement speed), rest should be auto-updated (verify this)
    public Tank(Vector2 pos, Controller controller, Stats stats) {
        super(pos, 50);
        this.stats = stats;
        this.controller = controller;

        updateStats();  // Update tank based on level and stats
        this.controller.setHost(this);  // Set the controller's host to this tank
        setTankBuild(TankBuild.createTankBuild("penta shot"));  // Default tank build
    }

    public void setTankBuild(TankBuild tankBuild) {
        this.tankBuild = tankBuild;
        tankBuild.setHost(this);
    }

    /**
     * Call whenever level or stats change
     */
    public void updateStats() {
        // Max health
        super.setMaxHealth(50 + (2 * (level - 1)) + (20 * stats.getStat(Stats.MAX_HEALTH)));
        // Body Damage
        super.setDamage((20 + 6 * stats.getStat(Stats.BODY_DAMAGE)) * (25.f/120));  // Body damage scaled down because fps TODO: TANK-TANK is different from TANK-OTHER DAMAGE (or maybe not in diepcustom repo, spike is mutliplied though)
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

        // Keep tank within the arena
        if (pos.x < 0) {
            pos.x = 0;
            // A bit of bounce right
            vel.x = Math.abs(vel.x * absorptionFactor * 0);
        }
        if (pos.x > Main.arenaWidth) {
            pos.x = Main.arenaWidth;
            // A bit of bounce left
            vel.x = -Math.abs(vel.x * absorptionFactor * 0);
        }
        if (pos.y < 0) {
            pos.y = 0;
            // A bit of bounce down
            vel.y = Math.abs(vel.y * absorptionFactor * 0);
        }
        if (pos.y > Main.arenaHeight) {
            pos.y = Main.arenaHeight;
            // A bit of bounce up
            vel.y = -Math.abs(vel.y * absorptionFactor * 0);
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

        // Update all turrets
        tankBuild.update();
        tankBuild.updateFire(controller.fire());

    }

    @Override
    public void draw() {
        // Culling
        if (pos.x + (radius*2*scale) < Main.cameraBox.x || pos.x - (radius*2*scale) > Main.cameraBox.x + Main.cameraBox.width || pos.y + (radius*2*scale) < Main.cameraBox.y || pos.y - (radius*2*scale) > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        // Draw Turrets
        tankBuild.draw();
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2, (radius*scale) * 2, Graphics.RED_STROKE);
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2 - 2*Graphics.strokeWidth, (radius*scale) * 2 - 2*Graphics.strokeWidth, Graphics.redCol);
        Graphics.drawCircleTexture(pos.x, pos.y, radius*scale, Graphics.strokeWidth, fillCol, strokeCol, opacity);
    }

    @Override
    public void receiveDamage(float damage) {
        super.receiveDamage(damage);
        lastDamageFrame = Main.counter;
    }

    @Override
    public void addToPools() {
        super.addToPools();
        Main.drawablePool.addObj(this, DrawPool.MIDDLE);
    }

    @Override
    public void delete() {
        super.delete();
        Main.drawablePool.deleteObj(this.getId(), DrawPool.MIDDLE);
    }
}
