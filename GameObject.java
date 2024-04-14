import com.raylib.java.core.Color;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class GameObject implements Updatable, Drawable {
    // Create a group integer where collision is ignored if group is the same
    int group;
    protected Vector2 pos, vel;
    final float friction = 0.9782890432f;  // 0.9^(25/120)
    protected int id;
    float scale = 1.0f;
    float radius;  // For collision detection and sometimes drawing (if circle)

    // Health
    float maxHealth = 0, health = 0;
    float damage = 0;
    boolean isDead = false;

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

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public void draw() {

    }

    // TODO: Healthbar drawable class probably
    public void drawHealthBar() {
        // Draw health bar
        if (health < maxHealth) {
            float healthBarWidth = radius*scale * 2;
            float healthBarHeight = 10;
            float healthBarX = pos.x - healthBarWidth / 2;
            float healthBarY = pos.y - radius*scale - 1.5f * healthBarHeight;
            float healthBarFill = health / maxHealth;
            Graphics.drawRectangle((int) healthBarX, (int) healthBarY, (int) healthBarWidth, (int) healthBarHeight, Color.BLACK);
            Graphics.drawRectangle((int) healthBarX + 1, (int) healthBarY+1, (int) (healthBarWidth * healthBarFill) - 2, (int) healthBarHeight - 2, Color.GREEN);
        }
    }

    @Override
    public void update() {
        if (health <= 0) {
            delete();  // TODO: delete animation
        }

        // DO NOT FLIP THE ORDER, first add velocity, then apply friction
        pos = Raymath.Vector2Add(pos, vel);
        vel = Raymath.Vector2Scale(vel, friction);

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
        // https://www.desmos.com/calculator/bqqjyewkrs
        float knockbackMagnitude = absorptionFactor * other.pushFactor * 0.046f;
        float diffY = this.pos.y - other.pos.y, diffX = this.pos.x - other.pos.x;
        float knockbackAngle = (float) Math.atan2(diffY, diffX);
        addForce(knockbackMagnitude, knockbackAngle);
    }

    public static void receiveDamage(GameObject a, GameObject b) {
        if (a.isDead || b.isDead) {
            return;
        }
        if (a.damage > b.health) {  // Overkill
            float scaleDown = b.health / a.damage;  // Scale down damage so that b just dies
            b.health -= scaleDown * a.damage;
            a.health -= scaleDown * b.damage;
        } else if (b.damage > a.health) {
            float scaleDown = a.health / b.damage;  // Scale down damage so that a just dies
            a.health -= scaleDown * b.damage;
            b.health -= scaleDown * a.damage;
        } else {
            a.health -= b.damage;
            b.health -= a.damage;
        }
        // Update if dead
        if (a.health <= 0) {
            a.delete();
        }
        if (b.health <= 0) {
            b.delete();
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
        //Main.drawablePool.addObj(this, DrawPool.MIDDLE);
    }

    @Override
    public void delete() {
        isDead = true;
        // Return id
        Main.idServer.returnId(this.getId());
        // All added to wait lists
        Main.gameObjectPool.deleteObj(this.getId());
        //Main.drawablePool.deleteObj(this.getId(), DrawPool.MIDDLE);
    }
}
