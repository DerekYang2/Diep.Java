import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.LinkedList;
import java.util.Queue;

/**
 * NOTES:
 * this.cameraEntity.cameraData.respawnLevel = Math.min(Math.max(this.cameraEntity.cameraData.values.level - 1, 1), Math.floor(Math.sqrt(this.cameraEntity.cameraData.values.level) * 3.2796));
 */
// TODO: When upgrading tank, upgrade bars/max upgrade stats need to be upgraded
public class Tank extends GameObject {
    float score;
    int level;
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

    int usedStatPoints = 0;
    int maxStatPoints = 0;

    // Upgrade path variables
    Queue<Pair<String, Integer>> upgradeBuilds;
    int updateStatFrames = 0;
    boolean resetFireDelay = true;

    boolean cullingOff = false;

    // TODO: update stats (health, body damage, movement speed), rest should be auto-updated (verify this)
    public Tank(Vector2 pos, Controller controller, Stats stats, int level) {
        super(pos, 50, 1.f, DrawPool.MIDDLE);
        username = NameGenerator.generateUsername();

        this.level = level;
        score = ScoreHandler.levelToScore(level) + 0.01f;
        setCollisionFactors(1, 8);
        initHealthBar();  // Initialize health bar object

        this.stats = stats;
        this.controller = controller;
        this.controller.setHost(this);  // Set the controller's host to this tank

        updateStats();  // Update tank based on level and stats
        this.health = maxHealth;  // Set health to max health initially
    }

    @Override
    protected void setFlags() {
        super.noInternalCollision = false;
        super.keepInArena = true;
        super.isProjectile = false;
    }

    @Override
    public void setMaxHealth(float maxHealth) {
        this.health = health * maxHealth / this.maxHealth;  // Scale health to new max health
        this.maxHealth = maxHealth;
    }

    public void initTankBuild(TankBuild tankBuild) {
        this.tankBuild = tankBuild;

        // If tankBuild is smasher, reset stats
        if (tankBuild.name.equals("smasher")) {
            for (int statEnum = 0; statEnum < 8; statEnum++) {
                stats.setStat(statEnum, 0);
            }
            usedStatPoints = 0;
        }

        this.tankBuild.setHost(this);
        // Update controller
        this.controller.updateTankBuild();
        this.tankBuild.update();  // In order to have correct position and rotation right away
    }

    public void changeTankBuild(TankBuild tankBuild) {
        if (!tankBuild.name.equals(this.tankBuild.name)) {  // If new tank build is different
            this.tankBuild.delete();  // Delete old tank build
            initTankBuild(tankBuild);  // Initialize new tank build
        }
    }

    public void setUpgradePath(String[] upgradePaths) {
        upgradeBuilds = new LinkedList<>();
        for (String str: upgradePaths) {
            upgradeBuilds.add(new Pair<>(str, TankBuild.getLevelRequirement(str)));
        }
    }

    public void setUpgradePath(Queue<Pair<String, Integer>> upgradePaths) {
        upgradeBuilds = upgradePaths;
    }

    /**
     * Call whenever level or stats change
     * Should be callable multiple times without issue
     */
    @Override
    public void updateStats() {
        // Max health
        this.setMaxHealth(50 + (2 * (level - 1)) + (20 * getStat(Stats.MAX_HEALTH)));
        // Body Damage (base should be 20 not 25 TODO: see if this is good)
        this.setDamage((20 + 6 * getStat(Stats.BODY_DAMAGE)) * (25.f/120));  // Body damage scaled down because fps TODO: TANK-TANK is different from TANK-OTHER DAMAGE (or maybe not in diepcustom repo, spike is mutliplied though)

        // ACCELERATION: https://www.desmos.com/calculator/qre98xzg76
        float A0 = (float)(2.55 * Math.pow(1.07, getStat(Stats.MOVEMENT_SPEED)) / Math.pow(1.015, level - 1));
        float convergeSpeed = (10 * A0) * (25.f/120);  // 10A0 is max speed, 25/120 is scaling factor for 25->120 fps
        this.baseAcceleration = convergeSpeed * (1 - friction);  // a = Converge * (1-r)
        // Regen (regen per SECOND is 0.1 + (0.4 * P) percent of max health)
        this.regenPerFrame = (maxHealth + 4 * maxHealth * getStat(Stats.HEALTH_REGEN)) / (120 * 1000);  // 120 fps
        // Size of tank
        scale = (float)Math.pow(1.01, (level - 1));

        // Health bar size
        healthBar.setWidth(radius * scale * 2);

        // The number of upgrade points the tank has
        maxStatPoints = Stats.getStatCount(level);
    }

    public void updateLevel() {
        // Update level
        int newLevel = level;
        while (newLevel < ScoreHandler.maxPlayerLevel && score > ScoreHandler.levelToScore(newLevel + 1)) {  // If score is enough to level up
            newLevel++;
        }

        if (newLevel != level) {
            level = newLevel;
            updateStats();
        }
    }

    public void updateStatUpgrade() {
       updateStatFrames = Math.max(0, updateStatFrames - 1);
        if (updateStatFrames == 0 && !resetFireDelay) {
            tankBuild.resetFireManagerDelay();
            resetFireDelay = true;
        }
    }


    public void incrementStat(int statEnum) {
        if (usedStatPoints < maxStatPoints) {
            stats.setStat(statEnum, stats.getStat(statEnum) + 1);
            usedStatPoints++;
            if (statEnum == Stats.RELOAD) {
                updateStatFrames = tankBuild.fireManager.getNextFireFrames();
                resetFireDelay = false;
            }
        }
        updateStats();
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

        // Update all turrets
        tankBuild.update();
        tankBuild.updateFire(isFiring());

        // Update level
        updateLevel();
        updateUpgradePaths();

        updateStatUpgrade();
    }

    public void setPos(Vector2 pos) {
        this.pos = pos;
        tankBuild.setPos(pos);
    }

    public void updateUpgradePaths() {
        String newBuild = tankBuild.name;
        while (!upgradeBuilds.isEmpty()) {
            if (level >= upgradeBuilds.peek().second) {
                newBuild = upgradeBuilds.poll().first;
            } else {
                break;
            }
        }
        changeTankBuild(TankBuild.createTankBuild(newBuild));
    }

    @Override
    public void draw() {
        String tankName = tankBuild.name;
        boolean isOnScreen = Main.onScreen(pos, radius*scale);
        // If landmine or auto, do not use texture to draw transparent tank
        if (opacity >= 0.9 || tankName.equals("landmine") || tankName.equals("auto 3") || tankName.equals("auto 5") || tankName.equals("auto smasher")) {
            tankBuild.addOnDrawBefore();
            tankBuild.draw(); // Draw Turrets
            tankBuild.addOnDrawMiddle();

    //        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2, (radius*scale) * 2, Graphics.RED_STROKE);
    //        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2 - 2*Graphics.strokeWidth, (radius*scale) * 2 - 2*Graphics.strokeWidth, Graphics.redCol);
            if (isOnScreen) {
                Graphics.drawCircleTexture(pos, radius*scale, Graphics.strokeWidth, getDamageLerpColor(fillCol), getDamageLerpColor(strokeCol), opacity);
            }
            tankBuild.addOnDrawAfter();
        } else {
            float originalScale = (float)Math.pow(1.01, (45 - 1));
            if (isOnScreen) {
                switch (tankName) {
                    case "auto gunner" -> {
                        Graphics.drawTextureCentered(TextureLoader.getTankTexture("gunner", fillCol), pos, direction, scale / originalScale, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                        tankBuild.addOnDrawAfter();
                    }
                    case "auto trapper" -> {
                        Graphics.drawTextureCentered(TextureLoader.getTankTexture("trapper", fillCol), pos, direction, scale / originalScale, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                        tankBuild.addOnDrawAfter();
                    }
                    default ->
                        Graphics.drawTextureCentered(TextureLoader.getTankTexture(tankBuild.name, fillCol), pos, direction, scale / originalScale, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
            }
        }
    }

    public void addOpacity(float opacityChange) {
        opacity += opacityChange * (25.f/120);  // Convert from 25 fps to 120 fps
        opacity = Math.min(1, Math.max(0, opacity));
    }

    public boolean isFiring() {
        return getAutoFire() || controller.fire();
    }

    public boolean getAutoFire() {
        return controller.autoFire();
    }

    public boolean specialControl() {
        return controller.holdSpecial();
    }

    @Override
    public void receiveDamage(float damage) {
        super.receiveDamage(damage);
        lastDamageFrame = Main.counter;
    }

    @Override
    protected float getScoreReward() {
        return score;
    }

    @Override
    public void updateVictim(GameObject victim) { score += victim.getScoreReward();}

    public Vector2 getTarget() {
        return controller.getTarget();
    }

    public int getStat(int statEnum) {
        return stats.getStat(statEnum);
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

    @Override
    public void addToPools() {
        super.addToPools();
        Leaderboard.addTank(this);
    }

    @Override
    public void delete() {
        super.delete();
        Leaderboard.removeTank(this);
    }
}
