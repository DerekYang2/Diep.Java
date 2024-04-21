import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public abstract class GameObject implements Updatable, Drawable {
    // Objects always collide each other regardless of group (unless noInternalCollision is true)
    int group;  // Objects in different groups damage each other
    boolean noInternalCollision = false;  // Object does not collide with objects only in the same group (applies to bullets for now)

    protected Vector2 pos, vel;
    float friction = 0.988f;  // default: 0.9^(25/120)
    protected int id;
    float scale = 1.0f;
    float radius;  // For collision detection and sometimes drawing (if circle)

    // Health
    float maxHealth = 0, health = 0;
    float damage = 0;
    boolean isDead = false;
    final int DEATH_ANIMATION_FRAMES = 120/5;
    int deathAnimationFrames = DEATH_ANIMATION_FRAMES;  // A fifth of a second
    float opacity = 1;

    // Health bar variables
    Bar healthBar;  // Null if not initialized
    final float HEALTH_BAR_HEIGHT = 15;
    final float HEALTH_BAR_STROKE = 3;

    // Collision
    float absorptionFactor = 1, pushFactor = 8;  // Default
    public GameObject(Vector2 pos, int radius) {
        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.radius = radius;
        createId();
        addToPools();
        group = id;
    }
    public GameObject(Vector2 pos, int radius, float absorptionFactor, float pushFactor) {
        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.radius = radius;
        this.absorptionFactor = absorptionFactor;
        this.pushFactor = pushFactor;
        createId();
        addToPools();
        group = id;
    }

    /**
     * Dependencies: radius, scale must be set
     */
    public void initHealthBar() {
        healthBar = new Bar(radius * scale * 2, HEALTH_BAR_HEIGHT, HEALTH_BAR_STROKE, Graphics.HEALTH_BAR, Graphics.HEALTH_BAR_STROKE);
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public abstract void draw();

    @Override
    public void update() {
        if (health <= 0) {
            triggerDelete();
        }

        // DO NOT FLIP THE ORDER, first add velocity, then apply friction
        pos = Raymath.Vector2Add(pos, vel);
        vel = Raymath.Vector2Scale(vel, friction);

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

        // Update health bar
        if (healthBar != null) {
            healthBar.setHidden(health >= maxHealth);
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

    public void addForce(Vector2 force) {
        vel = Raymath.Vector2Add(vel, force);
    }

    public void addForce(float forceMagnitude, float radians) {
        Vector2 force = new Vector2((float)Math.cos(radians), (float)Math.sin(radians));
        force = Raymath.Vector2Scale(force, forceMagnitude);
        addForce(force);
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
        float knockbackMagnitude = absorptionFactor * other.pushFactor * 0.024f;
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
        if (health <= 1e-6) {
            triggerDelete();
        }
    }

    public static void receiveDamage(GameObject a, GameObject b) {
        if (a.isDead || b.isDead) {
            return;
        }
        if (a.damage > b.health) {  // Overkill
            float scaleDown = b.health / a.damage;  // Scale down damage so that b just dies
            b.receiveDamage(scaleDown * a.damage);
            a.receiveDamage(scaleDown * b.damage);
        } else if (b.damage > a.health) {
            float scaleDown = a.health / b.damage;  // Scale down damage so that a just dies
            a.receiveDamage(scaleDown * b.damage);
            b.receiveDamage(scaleDown * a.damage);
        } else {
            a.receiveDamage(b.damage);
            b.receiveDamage(a.damage);
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
            healthBar.setHidden(true);
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
