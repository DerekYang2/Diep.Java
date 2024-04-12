import com.raylib.java.raymath.Vector2;

import java.util.ArrayList;

import com.raylib.java.core.Color;

public class Tank extends GameObject {
    int level = 45;
    // 54.7766480515
    int movementSpeed = 5;  // Integer stat for upgrade
    int healthPoints = 0;  // Integer stat for upgrade
    int bulletDamage = 0;  // Integer stat for upgrade
    int bulletPenetration = 9;  // Integer stat for upgrade
    int bodyDamage = 0;  // Integer stat for upgrade
    float baseAcceleration = (float)((25.f/125) * 0.218 * 2.55 * Math.pow(1.07, movementSpeed) / Math.pow(1.015, level - 1));
    float direction = 0;

    // Objects that control the tank
    Turret[] turrets;
    ShootManager shootManager;
    Controller controller;

    // Colors
    Color fillCol = Graphics.RED;
    Color strokeCol = Graphics.RED_STROKE;


    public Tank(Vector2 pos, Controller controller) {
        super(pos, 50);
        super.setMaxHealth(50 + (2 * (level - 1)) + (20 * healthPoints));
        super.setDamage((20 + (4 * bodyDamage)) * (25.f/120));  // Body damage scaled down because fps TODO: TANK-TANK is different from TANK-OTHER DAMAGE
        this.controller = controller;
        this.controller.setHost(this);

        scale = (float)Math.pow(1.01, (level - 1));

        // Set health

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

        turrets = new Turret[]{
                new Turret(42, 95, 0, 0, scale)
        };
        reloadTime = (int) (Math.ceil((15 - 9)*1) * 120 /25);
        shootManager = new ShootManager(new int[]{0}, new int[]{reloadTime}, 1.0f);


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
            t.setHost(this);
        }
    }
    
    @Override
    public void update() {
        super.update();
        // Update the direction barrel is facing
        setDirection(controller.barrelDirection());

        // Update input movement
        float moveDirection = controller.moveDirection();
        if (moveDirection != -1) {  // If valid direction
            addForce(baseAcceleration, moveDirection);
        }

        // Keep tank within the arena
        if (pos.x < 0) {
            pos.x = 0;
            // A bit of bounce right
            vel.x = Math.abs(vel.x * absorptionFactor * 0.3f);
        }
        if (pos.x > Main.arenaWidth) {
            pos.x = Main.arenaWidth;
            // A bit of bounce left
            vel.x = -Math.abs(vel.x * absorptionFactor * 0.3f);
        }
        if (pos.y < 0) {
            pos.y = 0;
            // A bit of bounce down
            vel.y = Math.abs(vel.y * absorptionFactor * 0.3f);
        }
        if (pos.y > Main.arenaHeight) {
            pos.y = Main.arenaHeight;
            // A bit of bounce up
            vel.y = -Math.abs(vel.y * absorptionFactor * 0.3f);
        }

        // Update all turrets
        for (Turret t : turrets) {
            t.update(pos.x, pos.y, direction);
        }

        // Fire
        if (controller.fire()) {
            fire();
        }
        if (controller.unload()) {
            unload();
        }
    }

    @Override
    public void draw() {
        super.draw();
        if (pos.x + (radius*2*scale) < Main.cameraBox.x || pos.x - (radius*2*scale) > Main.cameraBox.x + Main.cameraBox.width || pos.y + (radius*2*scale) < Main.cameraBox.y || pos.y - (radius*2*scale) > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        // Draw Turrets
        for (Turret t : turrets) {
            t.draw();
        }
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2, (radius*scale) * 2, Graphics.RED_STROKE);
//        Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius*scale) * 2 - 2*Graphics.strokeWidth, (radius*scale) * 2 - 2*Graphics.strokeWidth, Graphics.redCol);
        Graphics.drawCircleTexture(pos.x, pos.y, radius*scale, Graphics.strokeWidth, fillCol, strokeCol);
        drawHealthBar();  // TODO: all draw orders need to be redone
    }

    protected void setDirection(double radians) {
        direction = (float) radians;
    }

    protected void setColor(Color fillCol, Color strokeCol) {
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
    }

    protected void fire() {
        ArrayList<Integer> fireIndices = shootManager.getFireIndices();
        if (fireIndices != null) {
            for (int i : fireIndices) {
                addForce(turrets[i].shoot());
            }
        }
    }

    protected void unload() {
        shootManager.reset();
    }
}
