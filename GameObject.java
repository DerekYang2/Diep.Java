import com.raylib.java.core.Color;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public abstract class GameObject implements Updatable, Drawable {
    // Objects always collide each other regardless of group (unless noInternalCollision is true)
    int group;  // Objects in different groups damage each other

    // Special flags
    boolean noInternalCollision = false;  // Object does not collide with objects only in the same group (applies to bullets for now)
    boolean keepInArena = true;  // Object does not go out of the arena
    boolean isProjectile;  // If object is projectile, used for collision cases

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
    final int DEATH_ANIMATION_FRAMES = 10;
    int deathAnimationFrames = DEATH_ANIMATION_FRAMES;  // A fifth of a second
    float opacity = 1;
    final int DAMAGE_ANIMATION_FRAMES = 5;
    int damageAnimationFrames = 0;

    // Health bar variables
    Bar healthBar;  // Null if not initialized
    final float HEALTH_BAR_HEIGHT = 15;
    final float HEALTH_BAR_STROKE = 3;

    // Collision
    float absorptionFactor = 1, pushFactor = 8;  // Default
    public GameObject(Vector2 pos, int radius, float damageFactor) {
        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.radius = radius;
        this.damageFactor = damageFactor;
        createId();
        addToPools();
        group = id;
    }
    public GameObject(Vector2 pos, int radius, float absorptionFactor, float pushFactor, float damageFactor) {
        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.radius = radius;
        this.absorptionFactor = absorptionFactor;
        this.pushFactor = pushFactor;
        this.damageFactor = damageFactor;
        createId();
        addToPools();
        group = id;
    }

    /**
     * Dependencies: radius, scale must be set
     */
    public void initHealthBar() {
        healthBar = new Bar(radius * scale * 2, HEALTH_BAR_HEIGHT, HEALTH_BAR_STROKE, Graphics.HEALTH_BAR, Graphics.HEALTH_BAR_STROKE);
        healthBar.forceHidden(true);
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
        vel = Raymath.Vector2Scale(vel, friction);

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
            vel = Raymath.Vector2Scale(vel, 1.0f / (float)Math.sqrt(velSquared));
            vel = Raymath.Vector2Scale(vel, velMax);
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
        return Graphics.lerpColor(col, Color.RED, 0.4f*damageAnimationFrames/DAMAGE_ANIMATION_FRAMES);
    }

    public void addForce(Vector2 force) {
        vel = Raymath.Vector2Add(vel, force);
    }

    public void addForce(float forceMagnitude, float radians) {
        Vector2 force = new Vector2((float)Math.cos(radians), (float)Math.sin(radians));
        force = Raymath.Vector2Scale(force, forceMagnitude);
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
        // https://www.desmos.com/calculator/p9tyewb18m
        float knockbackMagnitude = absorptionFactor * other.pushFactor * 0.0245f;
        float diffY = this.pos.y - other.pos.y, diffX = this.pos.x - other.pos.x;
        float knockbackAngle = (float) Math.atan2(diffY, diffX);
        addForce(knockbackMagnitude, knockbackAngle);
    }

    public static void receiveKnockback(GameObject a, GameObject b) {
        if (a.isDead || b.isDead) {
            return;
        }
        a.receiveKnockback(b);
        b.receiveKnockback(a);
    }

    public void receiveDamage(float damage) {
        if (isDead) {
            return;
        }

        health -= damage;
        damageAnimationFrames = DAMAGE_ANIMATION_FRAMES;  // Start damage animation

        if (health <= 1e-6) {  // Close enough to 0
            triggerDelete();
        }
    }

    public static void receiveDamage(GameObject a, GameObject b) {
        if (a.isDead || b.isDead) {
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

        assert !a.isDead && !b.isDead;  // Should not happen

        // Apply the last tick
        if (aDamage > b.health) {  // Overkill
            float scaleDown = b.health / aDamage;  // Scale down damage so that b just dies
            b.receiveDamage(scaleDown * aDamage);
            a.receiveDamage(scaleDown * bDamage);
        } else if (bDamage > a.health) {
            float scaleDown = a.health / bDamage;  // Scale down damage so that a just dies
            a.receiveDamage(scaleDown * bDamage);
            b.receiveDamage(scaleDown * aDamage);
        }
    }

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
    }

    public void triggerDelete() {
        isDead = true;  // Begin deletion animation
        if (healthBar != null)
            healthBar.forceHidden(true);
    }

    @Override
    public void delete() {
        // Return id
        Main.idServer.returnId(this.getId());
        // All added to wait lists
        Main.gameObjectPool.deleteObj(this.getId());
        // Delete health bar
        if (healthBar != null)
            healthBar.delete();
    }
}
