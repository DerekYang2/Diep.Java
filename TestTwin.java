import com.raylib.java.raymath.Vector2;

import java.util.ArrayList;

import static com.raylib.java.core.input.Keyboard.*;

public class TestTwin implements Updatable, Drawable {
    protected float x, y, vx, vy;
    protected float xTranslate, yTranslate;

    float scale = 1.0f;
    // 54.7766480515
    float radius = (float) (54.7766480515);

    protected float direction;
    protected int id;
    Stopwatch stopwatch;
    final float friction = 0.9f;
    final float mass = 1.0f;
    final float moveForceX = 0.3f;
    final float moveForceY = 0.3f;
    float velMax = 5.0f;  // Max velocity in a single direction

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
                new Turret(42, 95, 0, -0.7853981633974483, scale),
                new Turret(42, 95, 0, 0, scale),
                new Turret(42, 95, 0, 0.7853981633974483, scale),
        };
        shootManager = new ShootManager(new int[]{0, 0, 0}, new int[]{16}, 1.0f);

        //TODO: shoot manager first shoots index 0 on click but should alternate
        // fix: after reset, set the counter to the next fire index rather than 0

        // Twins (looks off?)
       turrets = new Turret[]{
                new Turret(42f, 95, -26f, 0, scale),
                new Turret(42f, 95, 26f, 0, scale)
        };

        shootManager = new ShootManager(new int[]{0, 1}, new int[]{8, 8}, 1.0f);

        // Triplet
        turrets = new Turret[]{
                new Turret(42, 80, 26, 0, scale),
                new Turret(42, 80, -26, 0, scale),
                new Turret(42, 95, 0, 0, scale)
        };
        int reloadTime = (int) ((1.f/3) * Math.ceil((15 - 9)*1.0f) * 120 /25);
        shootManager = new ShootManager(new int[]{0, 1, 2}, new int[]{reloadTime, reloadTime, reloadTime}, 1.0f);

        // Pentashot
        /*turrets = new Turret[]{
                new Turret(42, 80, 0, -0.7853981633974483, scale),
                new Turret(42 , 80, 0, 0.7853981633974483, scale),
                new Turret(42, 95, 0, -0.39269908169872414, scale),
                new Turret(42, 95, 0, 0.39269908169872414, scale),
                new Turret(42, 110, 0, 0, scale)
        };
        int reloadTime = (int) ((1.f/3) * Math.ceil((15 - 9)*1.0f) * 120 /25);
        shootManager = new ShootManager(new int[]{0, 0, 1, 1, 2}, new int[]{reloadTime, reloadTime, reloadTime}, 1.0f);*/

        // Predator
        /*turrets = new Turret[]{
                new Turret(42, 110, 0, 0, scale),
                new Turret(1.35f * 42, 95, 0, 0, scale),
                new Turret(1.7f*42, 80, 0, 0, scale)
        };
        shootManager = new ShootManager(new int[]{0, 1, 2}, new int[]{45, 2, 3}, 1.0f);*/

        // Single tank test
/*
        turrets = new Turret[]{
                new Turret(13.5f, 28, 0, 0, scale)
        };
        shootManager = new ShootManager(new int[]{0}, new int[]{8}, 1.0f);
*/



/*        // Fighter
        turrets = new Turret[]{
                new Turret(13.5f, 28, 0, 0, scale),
                new Turret(13.5f, 28, 0, 90, scale),
                new Turret(13.5f, 28, 0, -90, scale),
                new Turret(13.5f, 28,0, 150, scale),
                new Turret(13.5f, 28, 0, -150, scale)
        };
        shootManager = new ShootManager(new int[]{0, 0, 0, 0, 0}, new int[]{16}, 1.0f);*/


        // Destroyer
/*        turrets = new Turret[]{
                new Turret(23f, 30, 0, 0, scale)
        };
        shootManager = new ShootManager(new int[]{0}, new int[]{60}, 1.0f);*/
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
            ArrayList<Integer> fireIndices = shootManager.getFireIndices();
            if (fireIndices != null) {
                for (int i : fireIndices) {
                    addForce(turrets[i].shoot());
                }
            }
        }
        if (Graphics.isLeftMouseReleased()) {
            shootManager.reset();
        }

        // atan2 mouse angle
        for (Turret t : turrets) {
            t.update(x, y, direction);
        }
    }

    public void addForce(Vector2 force) {
        vx += force.x / mass;
        vy += force.y / mass;
    }
    public void addForce(float fx, float fy) {
        vx += fx / mass;
        vy += fy / mass;
    }

    public void draw() {
        // Draw Turretsp
        for (Turret t : turrets) {
            t.draw();
        }
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2, (radius*scale) * 2, Graphics.RED_STROKE);
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2 - 2*Main.strokeWidth, (radius*scale) * 2 - 2*Main.strokeWidth, Graphics.redCol);
        Graphics.drawCircleTexture(x, y, radius*scale, Main.strokeWidth, Graphics.BLUE, Graphics.BLUE_STROKE);
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
