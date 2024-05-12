import com.raylib.java.core.Color;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public abstract class GameObject implements Updatable, Drawable {
    // Objects always collide each other regardless of group (unless noInternalCollision is true)
    int group;  // Objects in different groups damage each other
    int DRAW_LAYER;

    // Special flags
    boolean noInternalCollision = false;  // Object does not collide with objects only in the same group (applies to bullets for now)
    boolean keepInArena = true;  // Object does not go out of the arena
    boolean isProjectile;  // If object is projectile, used for collision cases

    // Technical physics
    protected Vector2 pos, vel;
    float friction = 0.988f;  // default: 0.9^(25/120)
    protected int id;
    float scale = 1.0f;
    float radius;  // For collision detection and sometimes drawing (if circle)

    // Health
    float maxHealth = 0, health = 0;
    float damage = 0;
    float damageFactor = 1;
    boolean isDead = false;
    final int DEATH_ANIMATION_FRAMES = 24;
    int deathAnimationFrames = DEATH_ANIMATION_FRAMES;
    float opacity = 1;
    final int DAMAGE_ANIMATION_FRAMES = 12;
    int damageAnimationFrames = 0;

    // Colors
    Color fillCol = Graphics.RED;
    Color strokeCol = Graphics.RED_STROKE;

    // Health bar variables
    Bar healthBar;  // Null if not initialized
    final float HEALTH_BAR_HEIGHT = 15;
    final float HEALTH_BAR_STROKE = 3;

    // Collision
    float absorptionFactor = 1, pushFactor = 8;  // Default
    public GameObject(Vector2 pos, int radius, float damageFactor, int drawLayer) {
        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.radius = radius;
        this.damageFactor = damageFactor;
        this.DRAW_LAYER = drawLayer;
        createId();
        addToPools();
        group = id;
        setFlags();
    }

    protected void setCollisionFactors(float absorptionFactor, float pushFactor) {
        this.absorptionFactor = absorptionFactor;
        this.pushFactor = pushFactor;
    }

    public abstract void updateStats();
    protected abstract void setFlags();

    protected void setColor(Color fillCol, Color strokeCol) {
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
    }

    /**
     * Dependencies: radius, scale must be set
     */
    public void initHealthBar() {
        healthBar = new Bar(radius * scale * 2, HEALTH_BAR_HEIGHT, HEALTH_BAR_STROKE, Graphics.HEALTH_BAR, Graphics.HEALTH_BAR_STROKE, .15f, 1);
        healthBar.forceHidden(true);
        healthBar.addToGameWorld();
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public void update() {
        if (health <= 0) {
            triggerDelete();
        }

        // DO NOT FLIP THE ORDER, first add velocity, then apply friction
        pos = Raymath.Vector2Add(pos, vel);
        vel = Graphics.scale(vel, friction);

        if (keepInArena) {
            restrictPosition();
        }

        // Deletion animation
        if (isDead) {
            deathAnimationFrames--;
            scale *= 1.02f;

            // https://www.desmos.com/calculator/o4j6oqlaop
            opacity = (float)deathAnimationFrames/(DEATH_ANIMATION_FRAMES);

            if (deathAnimationFrames <= 0) {
                delete();
            }
            return;
        }

        // Damage animation
        if (damageAnimationFrames > 0) {
            damageAnimationFrames--;
        }

        // Update health bar
        if (healthBar != null) {
            healthBar.triggerHidden(health >= maxHealth);
            if (!healthBar.isHidden()) {
                float healthBarWidth = radius * scale * 2;
                float healthBarX = pos.x - healthBarWidth / 2;
                float healthBarY = pos.y + radius * scale + 40 - HEALTH_BAR_HEIGHT;
                healthBar.update(new Vector2(healthBarX, healthBarY), health / maxHealth);
            }
        }

        /*
        // Max speed limiting
        float velSquared = vel.x * vel.x + vel.y * vel.y;
        if (velSquared > velMax * velMax) {
            // Normalize the vector
            vel = Graphics.scale(vel, 1.0f / (float)Math.sqrt(velSquared));
            vel = Graphics.scale(vel, velMax);
        }
        */

        /**
         *         if (this.velocity.magnitude < 0.01) this.velocity.magnitude = 0;
         *         // when being deleted, entities slow down half speed
         *         else if (this.deletionAnimation) this.velocity.magnitude /= 2;
         *         this.positionData.x += this.velocity.x;
         *         this.positionData.y += this.velocity.y;
         */
    }

    @Override
    public abstract void draw();

    /**
     * Returns a color that is a lerp between col and red based on damageAnimationFrames
     * @param col
     * @return
     */
    public Color getDamageLerpColor(Color col) {
        if (damageAnimationFrames >= DAMAGE_ANIMATION_FRAMES - 2)
            return Graphics.lerpColor(col, Color.WHITE, 0.85f*damageAnimationFrames/DAMAGE_ANIMATION_FRAMES);
        else
            return Graphics.lerpColor(col, Color.RED, (float)damageAnimationFrames/DAMAGE_ANIMATION_FRAMES);
    }

    public void addForce(Vector2 force) {
        vel = Raymath.Vector2Add(vel, force);
    }

    public void addForce(float forceMagnitude, float radians) {
        Vector2 force = new Vector2((float)Math.cos(radians), (float)Math.sin(radians));
        force = Graphics.scale(force, forceMagnitude);
        addForce(force);
    }

    private void restrictPosition() {
        // Keep object within the arena
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
    }

    public Rectangle boundingBox() {
        return new Rectangle(pos.x - radius*scale, pos.y - radius*scale, radius*scale*2, radius*scale*2);
    }

    public boolean checkCollision(GameObject other) {
        float touchingDistance = radius*scale + other.radius*other.scale;
        return ((pos.x - other.pos.x) * (pos.x - other.pos.x) + (pos.y - other.pos.y) * (pos.y - other.pos.y) < touchingDistance * touchingDistance);
    }

    public void receiveKnockback(GameObject other) {
        if (isDead || other.isDead) {
            return;
        }
        // https://www.desmos.com/calculator/p9tyewb18m and https://www.desmos.com/calculator/julb0fev5e
        float scaleFactor = -1.96251f * friction + 1.96557f;

        float knockbackMagnitude = absorptionFactor * other.pushFactor * scaleFactor;
        float diffY = this.pos.y - other.pos.y, diffX = this.pos.x - other.pos.x;
        if (diffX == 0 && diffY == 0) {  // If objects are perfectly stacked
            diffX = (float)Math.random() + 0.1f;  // Randomize
            diffY = (float)Math.random() + 0.1f;
        }
        float knockbackAngle = (float) Math.atan2(diffY, diffX);
        addForce(knockbackMagnitude, knockbackAngle);
    }

    public static void receiveKnockback(GameObject a, GameObject b) {
        if (a.isDead || b.isDead) {
            return;
        }

        // If different group OR same group but neither have no collision flag OR same group drone/trap
        if (a.group != b.group || (!a.noInternalCollision && !b.noInternalCollision) || a.sameGroupCollision(b)) {
            a.receiveKnockback(b);
            b.receiveKnockback(a);
        }
    }

    public void receiveDamage(float damage) {
        if (isDead) {
            return;
        }

        health -= damage;
        if (damageAnimationFrames <= 1)  // If basically finished damage animation
            damageAnimationFrames = DAMAGE_ANIMATION_FRAMES;  // Start damage animation

        if (health <= 1e-6) {  // Close enough to 0
            triggerDelete();
        }
    }

    public static void receiveDamage(GameObject a, GameObject b) {
        if (a.isDead || b.isDead || a.group == b.group) {  // If either is dead or same group
            return;
        }

        if ((a.isProjectile && b.isProjectile) && (a instanceof Drone || b instanceof Drone)) {  // If both are projectiles and at least one is a drone
            receiveDamageInstant(a, b);  // Instant damage
            return;
        }

        float aDamage = a.damage * b.damageFactor;
        float bDamage = b.damage * a.damageFactor;

/*
        // If both are tanks
        if (a instanceof Tank && b instanceof Tank) {
            aDamage *= 1.5f;
            bDamage *= 1.5f;
        }
*/

        if (aDamage > b.health) {  // Overkill
            float scaleDown = b.health / aDamage;  // Scale down damage so that b just dies
            b.receiveDamage(scaleDown * aDamage);
            a.receiveDamage(scaleDown * bDamage);
        } else if (bDamage > a.health) {
            float scaleDown = a.health / bDamage;  // Scale down damage so that a just dies
            a.receiveDamage(scaleDown * bDamage);
            b.receiveDamage(scaleDown * aDamage);
        } else {
            a.receiveDamage(bDamage);
            b.receiveDamage(aDamage);
        }

        if (a.isDead) {  // B killed A
            b.updateVictim(a);
        }
        if (b.isDead) {  // A killed B
            a.updateVictim(b);
        }
    }

    /**
     * Applies damage instantly, happens between drones and other projectiles (drone-drone, drone-bullet)
     * @param a First game object
     * @param b Second game object
     */
    public static void receiveDamageInstant(GameObject a, GameObject b) {
        if (a.isDead || b.isDead) {
            return;
        }
        float aDamage = a.damage * b.damageFactor;
        float bDamage = b.damage * a.damageFactor;

        // Calculate ticks until A dies, a.health - ticks * bDamage = 0, solve for ticks
        int ticksA = (int)Math.ceil(a.health / bDamage);
        int ticksB = (int)Math.ceil(b.health / aDamage);

        int ticks = Math.min(ticksA, ticksB);
        // Apply ticks-1 full damage to both
        if (ticks >= 1) {
            a.receiveDamage((ticks-1) * bDamage);
            b.receiveDamage((ticks-1) * aDamage);
        }

        // Apply the last tick
        if (aDamage > b.health) {  // A overkills B
            float scaleDown = b.health / aDamage;  // Scale down damage so that b just dies
            b.receiveDamage(scaleDown * aDamage);
            a.receiveDamage(scaleDown * bDamage);
        } else {  // B overkills A
            float scaleDown = a.health / bDamage;  // Scale down damage so that a just dies
            a.receiveDamage(scaleDown * bDamage);
            b.receiveDamage(scaleDown * aDamage);
        }
    }

    public void updateVictim(GameObject victim) {}

    /**
     * A custom method to override
     * Returns true if two objects of the same group should collide
     * @param other Game object guaranteed to be in the same group
     * @return True if two objects of the same group should collide (drone-drone, trap-trap, etc.)
     */
    protected boolean sameGroupCollision(GameObject other) {
        return false;
    }

    protected abstract float getScoreReward();

    @Override
    public void createId() {
        this.id = Main.idServer.getId();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void addToPools() {
        Main.gameObjectPool.addObj(this);
        Main.drawablePool.addObj(this, DRAW_LAYER);
    }

    public void triggerDelete() {
        if (isDead) return;
        isDead = true;  // Begin deletion animation
        if (healthBar != null)
            healthBar.forceHidden(true);
        damageAnimationFrames = 0;  // Stop damage animation
    }

    @Override
    public void delete() {
        // Return id
        Main.idServer.returnId(this.getId());
        // All added to wait lists
        Main.gameObjectPool.deleteObj(this.getId());
        // Delete from drawable pool
        Main.drawablePool.deleteObj(this.getId(), DRAW_LAYER);
        // Delete health bar
        if (healthBar != null)
            healthBar.delete();
    }

    public boolean isInvisible() {
        return opacity < 0.001f && healthBar.isHidden();
    }
}
