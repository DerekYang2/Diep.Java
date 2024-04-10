import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class GameObject implements Updatable, Drawable {
    // Create a group integer where collision is ignored if group is the same
    int group;
    protected Vector2 pos, vel;
    float friction = 0.9782890432f;  // 0.9^(25/120)
    protected int id;
    float scale = 1.0f;
    float radius;  // For collision detection and sometimes drawing (if circle)

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

    @Override
    public void draw() {
        // Default draw
        Graphics.drawCircle(pos.x, pos.y, radius*scale, Graphics.RED);
    }

    @Override
    public void update() {
        vel = Raymath.Vector2Scale(vel, friction);
/*        float velSquared = vel.x * vel.x + vel.y * vel.y;
        if (velSquared > velMax * velMax) {
            // Normalize the vector
            vel = Raymath.Vector2Scale(vel, 1.0f / (float)Math.sqrt(velSquared));
            vel = Raymath.Vector2Scale(vel, velMax);
        }*/
        pos = Raymath.Vector2Add(pos, vel);

        /**
         *         if (this.velocity.magnitude < 0.01) this.velocity.magnitude = 0;
         *         // when being deleted, entities slow down half speed
         *         else if (this.deletionAnimation) this.velocity.magnitude /= 2;
         *         this.positionData.x += this.velocity.x;
         *         this.positionData.y += this.velocity.y;
         */

        // Keep things within the arena
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
        float distanceCenter = Raymath.Vector2Distance(pos, other.pos);
        return (distanceCenter < radius*scale + other.radius*other.scale);
    }

    public void receiveKnockback(GameObject other) {
        float knockbackMagnitude = this.absorptionFactor * other.pushFactor * 0.04f;
        float diffY = this.pos.y - other.pos.y, diffX = this.pos.x - other.pos.x;
        float knockbackAngle = (float) Math.atan2(diffY, diffX);
        addForce(knockbackMagnitude, knockbackAngle);
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
        Main.drawablePool.addObj(this);
        Main.updatablePool.addObj(this);
        Main.gameObjectPool.addObj(this);
    }

    @Override
    public void delete() {
        // All added to wait lists
        Main.drawablePool.deleteObj(this.getId());
        Main.updatablePool.deleteObj(this.getId());
        Main.gameObjectPool.deleteObj(this.getId());
    }
}
