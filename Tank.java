import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

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
    Stopwatch autoFireWatch = new Stopwatch();

    // TODO: update stats (health, body damage, movement speed), rest should be auto-updated (verify this)
    public Tank(Vector2 pos, Controller controller, Stats stats) {
        super(pos, 50, 1.f, DrawPool.MIDDLE);
        setCollisionFactors(1, 8);
        initHealthBar();  // Initialize health bar object

        this.stats = stats;
        this.controller = controller;
        this.controller.setHost(this);  // Set the controller's host to this tank

        updateStats();  // Update tank based on level and stats
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
        // Update controller
        this.controller.updateTankBuild();
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

    @Override
    public void update() {
        super.update();

        if (isDead) {
            tankBuild.update();  // Still update after death so turret positions are updated
            return;
        }

        // Update invisibility opacity
        if (tankBuild.isInvisible) {  // If tank can turn invisible
            addOpacity(-tankBuild.invisibilityRate);  // Decrease opacity
            addOpacity((Graphics.length(vel) > tankBuild.min_movement || damageAnimationFrames > 0) ? tankBuild.visibilityRateMoving : 0);  // Increase opacity if moving or taking damage
            addOpacity(isFiring() ? tankBuild.visibilityRateShooting : 0);  // Increase opacity if shooting
        }

        // Health updates
        if (Main.counter - lastDamageFrame > 30 * 120) {  // After 30 seconds, hyper-regen
            health += maxHealth / (120 * 10);  // 10 percent HP per second
        } else {  // Normal regen
            health += regenPerFrame;
        }
        health = Math.min(health, maxHealth);  // Cap health at maxHealth
        controller.update();

        // Update the direction barrel is facing
        setDirection(controller.barrelDirection());

        // Update input movement
        float moveDirection = controller.moveDirection();
        if (moveDirection != -1) {  // If valid direction
            addForce(baseAcceleration, moveDirection);
        }

        // Update auto fire
        if (autoFireWatch.ms() > 100 && controller.toggleAutoFire()) {
            autoFire = !autoFire;  // Flip autoFire
            autoFireWatch.start();  // Restart the timer
        }

        // Update all turrets
        tankBuild.update();
        tankBuild.updateFire(isFiring());
    }

    public void setPos(Vector2 pos) {
        this.pos = pos;
        tankBuild.setPos(pos);
    }

    @Override
    public void draw() {
        String tankName = tankBuild.name;
        // If landmine or auto, do not use texture to draw transparent tank
        if (opacity >= 0.98 || tankName.equals("landmine") || tankName.equals("auto 3") || tankName.equals("auto 5") || tankName.equals("auto smasher")) {
            tankBuild.addOnDrawBefore();
            tankBuild.draw(); // Draw Turrets
            tankBuild.addOnDrawMiddle();

    //        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2, (radius*scale) * 2, Graphics.RED_STROKE);
    //        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2 - 2*Graphics.strokeWidth, (radius*scale) * 2 - 2*Graphics.strokeWidth, Graphics.redCol);
            if (Main.onScreen(pos, radius*scale)) {
                Graphics.drawCircleTexture(pos.x, pos.y, radius*scale, Graphics.strokeWidth, getDamageLerpColor(fillCol), getDamageLerpColor(strokeCol), opacity);
            }
            tankBuild.addOnDrawAfter();
        } else {
            float originalScale = (float)Math.pow(1.01, (level - 1));
            if (Main.onScreen(pos, radius*scale)) {
                if (tankName.equals("auto gunner")) {
                    Graphics.drawTextureCentered(Main.tankTextures.get(fillCol).get("gunner"), pos, direction, scale/originalScale, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                    tankBuild.addOnDrawAfter();
                } else if (tankName.equals("auto trapper")) {
                  Graphics.drawTextureCentered(Main.tankTextures.get(fillCol).get("trapper"), pos, direction, scale/originalScale, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                    tankBuild.addOnDrawAfter();
                } else {
                    Graphics.drawTextureCentered(Main.tankTextures.get(fillCol).get(tankName), pos, direction, scale / originalScale, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
            }
        }
    }

    public void addOpacity(float opacityChange) {
        opacity += opacityChange * (25.f/120);  // Convert from 25 fps to 120 fps
        opacity = Math.min(1, Math.max(0, opacity));
    }

    public boolean isFiring() {
        return autoFire || controller.fire();
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

    /**
     * Gets the view bounds of a tank
     * @return A rectangle representing the view bounds
     */
    public Rectangle getView() {
        float zoom = (float) ((.55f * this.tankBuild.fieldFactor) / Math.pow(1.01, (level - 1) *0.5f));
        float viewWidth = Graphics.cameraWidth / zoom;
        float viewHeight = Graphics.cameraHeight / zoom;
        float cornerX = pos.x - viewWidth * 0.5f;
        float cornerY = pos.y - viewHeight * 0.5f;
        return new Rectangle(cornerX, cornerY, viewWidth, viewHeight);
    }
}
