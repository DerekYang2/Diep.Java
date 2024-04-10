import com.raylib.java.raymath.Vector2;

import java.util.ArrayList;

import static com.raylib.java.core.input.Keyboard.*;

public class Tank extends GameObject {
    int level = 45;
    // 54.7766480515
    Stopwatch stopwatch;
    int movementSpeed = 15;  // Integer stat for upgrade
    float baseAcceleration = (float)((25.f/125) * 0.218 * 2.55 * Math.pow(1.07, movementSpeed) / Math.pow(1.015, level - 1));
    float direction = 0;
    ShootManager shootManager;
    Turret[] turrets;

    // Delaying shot frequency
    float shootStart = 0.f;
    boolean canShoot = true;
    float shotDelayMs = 500;

    public Tank() {
        super(new Vector2(0, 0), 50);
        scale = (float)Math.pow(1.01, (level - 1));
        
        stopwatch = new Stopwatch();
        stopwatch.start();

        Graphics.setZoom(1.0f, level);

        int reloadTime;
        // Wait for the circle to be spawned before
       // Triple shot
       /*turrets = new Turret[]{
                new Turret(42, 95, 0, -0.7853981633974483, scale),
                new Turret(42, 95, 0, 0, scale),
                new Turret(42, 95, 0, 0.7853981633974483, scale),
        };
        shootManager = new ShootManager(new int[]{0, 0, 0}, new int[]{16}, 1.0f);*/

        // Twins (looks off?)
       turrets = new Turret[]{
                new Turret(42f, 95, -26f, 0, scale),
                new Turret(42f, 95, 26f, 0, scale)
        };
       reloadTime = (int) ((1.f/2) * Math.ceil((15 - 9)*1.0f) * 120 /25);

        shootManager = new ShootManager(new int[]{0, 1}, new int[]{reloadTime, reloadTime}, 1.0f);

        // Triplet
        turrets = new Turret[]{
                new Turret(42, 80, 26, 0, scale),
                new Turret(42, 80, -26, 0, scale),
                new Turret(42, 95, 0, 0, scale)
        };
        reloadTime = (int) ((1.f/2) * Math.ceil((15 - 9)*1.0f) * 120.f /25);
        shootManager = new ShootManager(new int[]{1, 1, 0}, new int[]{reloadTime, reloadTime}, 1.0f);

        // Pentashot
        /*turrets = new Turret[]{
                new Turret(42, 80, 0, -0.7853981633974483, scale),
                new Turret(42 , 80, 0, 0.7853981633974483, scale),
                new Turret(42, 95, 0, -0.39269908169872414, scale),
                new Turret(42, 95, 0, 0.39269908169872414, scale),
                new Turret(42, 110, 0, 0, scale)
        };
        reloadTime = (int) ((1.f/3) * Math.ceil((15 - 9)*1.0f) * 120 /25);
        shootManager = new ShootManager(new int[]{0, 0, 1, 1, 2}, new int[]{reloadTime, reloadTime, reloadTime}, 1.0f);*/

        // Predator
        /*turrets = new Turret[]{
                new Turret(42, 110, 0, 0, scale),
                new Turret(1.35f * 42, 95, 0, 0, scale),
                new Turret(1.7f*42, 80, 0, 0, scale)
        };
        reloadTime = (int) (Math.ceil((15 - 9)*3f) * 120 /25);
        shootManager = new ShootManager(new int[]{0, 1, 2}, new int[]{reloadTime, (int) (reloadTime * 0.1f), (int) (reloadTime * 0.1f)}, 1.0f);
        */

        // Single tank test
        /*
        turrets = new Turret[]{
                new Turret(42, 95, 0, 0, scale)
        };
        reloadTime = (int) (Math.ceil((15 - 9)*1) * 120 /25);
        shootManager = new ShootManager(new int[]{0}, new int[]{reloadTime}, 1.0f);
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
        /*turrets = new Turret[]{
                new Turret(1.7f * 42, 95, 0, 0, scale)
        };
        //ceil((15 - reload stat points) * base reload);
        reloadTime = (int) ((Math.ceil((15 - 9)*4.f*120.f /25)));
        shootManager = new ShootManager(new int[]{0}, new int[]{reloadTime}, 1.0f);*/

        for (Turret t : turrets) {
            t.setGroup(group);
        }
    }

    @Override
    public void update() {
        super.update();
        // Get Direction of mouse
        direction = (float) Math.atan2(Graphics.getVirtualMouse().y - pos.y, Graphics.getVirtualMouse().x - pos.x);

        float moveDirection = -1;
        if (Graphics.isKeyDown(KEY_S) ) {
            moveDirection = (float) (Math.PI * 0.5);
        } else if (Graphics.isKeyDown(KEY_W)) {
            moveDirection = (float) (Math.PI * 1.5);
        } else if (Graphics.isKeyDown(KEY_A)) {
            moveDirection = (float) Math.PI;
        } else if (Graphics.isKeyDown(KEY_D)) {
            moveDirection = 0;
        }
        // Two are held
        if (Graphics.isKeyDown(KEY_W) && Graphics.isKeyDown(KEY_A)) {
            moveDirection = (float) (Math.PI * 1.25);
        } else if (Graphics.isKeyDown(KEY_W) && Graphics.isKeyDown(KEY_D)) {
            moveDirection = (float) (Math.PI * 1.75);
        } else if (Graphics.isKeyDown(KEY_S) && Graphics.isKeyDown(KEY_A)) {
            moveDirection = (float) (Math.PI * 0.75);
        } else if (Graphics.isKeyDown(KEY_S) && Graphics.isKeyDown(KEY_D)) {
            moveDirection = (float) (Math.PI * 0.25);
        }

        if (moveDirection != -1) {
            addForce(baseAcceleration, moveDirection);
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
            t.update(pos.x, pos.y, direction);
        }
    }

    @Override
    public void draw() {
        // Draw Turrets
        for (Turret t : turrets) {
            t.draw();
        }
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2, (radius*scale) * 2, Graphics.RED_STROKE);
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2 - 2*Graphics.strokeWidth, (radius*scale) * 2 - 2*Graphics.strokeWidth, Graphics.redCol);
        Graphics.drawCircleTexture(pos.x, pos.y, radius*scale, Graphics.strokeWidth, Graphics.BLUE, Graphics.BLUE_STROKE);
    }
}
