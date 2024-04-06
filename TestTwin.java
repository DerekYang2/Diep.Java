import com.raylib.java.core.Color;

import static com.raylib.java.core.input.Keyboard.*;

public class TestTwin implements Updatable, Drawable {
    protected float x, y, vx, vy;
    protected float xTranslate, yTranslate;

    float radius = 30;

    protected float direction;
    protected int id;
    Stopwatch stopwatch;
    final float friction = 0.92f;
    final float mass = 1.0f;
    final float moveForceX = 0.4f;
    final float moveForceY = 0.4f;
    float velMax = 10.0f;  // Max velocity in a single direction

    ShootManager shootManager;
    Turret[] turrets;

    // Delaying shot frequency
    float shootStart = 0.f;
    boolean canShoot = true;
    float shotDelayMs = 500;

    // Debugging Variables
    float p1x, p1y, p2x, p2y;
    
    public TestTwin() {
        createId();  // Remember to create an id on creation
        addToPools();  // Remember to add object to pool on creation
        
        stopwatch = new Stopwatch();
        stopwatch.start();
        
        x = 0; y= 0;
        vx = vy = 0;

        // Wait for the circle to be spawned before
        // Triple shot
        turrets = new Turret[]{
                new Turret(radius / 1.5f, radius * 2.f, 0, -Math.PI / 4),
                new Turret(radius / 1.5f, radius * 2.f, 0, 0),
                new Turret(radius / 1.5f, radius * 2.f, 0, Math.PI / 4),
        };
        // Twins
        turrets = new Turret[]{
                new Turret(radius / 1.5f, radius * 2.f, radius * 7.f/15.f, 0),
                new Turret(radius / 1.5f, radius * 2.f, - radius * 7.f/15.f, 0)
        };
        shootManager = new ShootManager(new int[]{0, 8}, 16);
    }

    public void update() {
        // Cap Velocity
        vx = Math.signum(vx) * Math.min(Math.abs(vx), velMax);
        vy = Math.signum(vy) * Math.min(Math.abs(vy), velMax);
        // Update position
        x += vx;
        y += vy;
        // Apply friction
        vx *= friction;
        vy *= friction;

        // Get Direction of mouse
        direction = (float) Math.atan2(Graphics.getVirtualMouse().y - y, Graphics.getVirtualMouse().x - x);

        if (Graphics.isKeyDown(KEY_S) ) {
            addForce(0, moveForceY);
        }
        if (Graphics.isKeyDown(KEY_W)) {
            addForce(0, -moveForceY);
        }
        if (Graphics.isKeyDown(KEY_A)) {
            addForce(-moveForceX, 0);
        }
        if (Graphics.isKeyDown(KEY_D)) {
            addForce(moveForceX, 0);
        }

        if (Graphics.isLeftMouseDown()) {
            shootManager.update();
            int fireIndex = shootManager.getFireIndex();
            if (fireIndex != -1) {
                turrets[fireIndex].shoot();
            }
        } else {
            shootManager.reset();
        }

        // atan2 mouse angle
        for (Turret t : turrets) {
            t.update(x, y, direction);
        }
    }

    public void addForce(float fx, float fy) {
        vx += fx / mass;
        vy += fy / mass;
    }

    public void shoot() {
        // TODO: add bullet params
        for (Turret t : turrets) {
            t.shoot();
        }
    }

    public void draw() {
        // Draw Turrets
        for (Turret t : turrets) {
            t.draw();
        }
        Graphics.drawCircle(x, y, radius, Color.RED);
    }

    // Deletable Methods
    public void createId() {
        this.id = Main.idServer.getId();
    }

    public int getId() {
        return this.id;
    }

    public void addToPools() {
        Main.drawablePool.addObj(this);
        Main.updatablePool.addObj(this);
    }

    public void delete() {
        // All added to wait lists
        Main.drawablePool.deleteObj(this.getId());
        Main.updatablePool.deleteObj(this.getId());
    }
}
