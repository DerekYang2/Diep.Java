import com.raylib.java.raymath.Vector2;

import com.raylib.java.core.Color;

public class Tank extends GameObject {
    int level = 45;
    // 54.7766480515
    public Stats stats;
    float baseAcceleration;
    float direction = 0;

    // Objects that control the tank
    TankBuild tankBuild;
    Controller controller;

    // Colors
    Color fillCol = Graphics.RED;
    Color strokeCol = Graphics.RED_STROKE;


    // TODO: update stats (health, body damage, movement speed), rest should be auto-updated (verify this)
    public Tank(Vector2 pos, Controller controller, Stats stats) {
        super(pos, 50);
        this.stats = stats;

        super.setMaxHealth(50 + (2 * (level - 1)) + (20 * stats.getStat(Stats.MAX_HEALTH)));
        super.setDamage((20 + 6 * stats.getStat(Stats.BODY_DAMAGE)) * (25.f/120));  // Body damage scaled down because fps TODO: TANK-TANK is different from TANK-OTHER DAMAGE
        // Spike tank is * 1.5
        // https://www.desmos.com/calculator/qre98xzg76
        float A0 = (float)(2.55 * Math.pow(1.07, stats.getStat(Stats.MOVEMENT_SPEED)) / Math.pow(1.015, level - 1));
        float convergeSpeed = (10 * A0) * (25.f/120);  // 10A0 is max speed, 25/120 is scaling factor for 25->120 fps
        this.baseAcceleration = convergeSpeed * (1 - friction);  // a = Converge * (1-r)

        this.controller = controller;
        this.controller.setHost(this);

        scale = (float)Math.pow(1.01, (level - 1));
        setTankBuild(TankBuild.createTankBuild("tank"));
    }

    public void setTankBuild(TankBuild tankBuild) {
        this.tankBuild = tankBuild;
        tankBuild.setHost(this);
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

        // Update all turrets
        tankBuild.update();

        // Fire
        if (controller.fire()) {
            tankBuild.fire();
        }
        if (controller.unload()) {
            tankBuild.reset();
        }
    }

    @Override
    public void draw() {
        super.draw();
        if (pos.x + (radius*2*scale) < Main.cameraBox.x || pos.x - (radius*2*scale) > Main.cameraBox.x + Main.cameraBox.width || pos.y + (radius*2*scale) < Main.cameraBox.y || pos.y - (radius*2*scale) > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        // Draw Turrets
        tankBuild.draw();
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

    @Override
    public void addToPools() {
        super.addToPools();
        Main.drawablePool.addObj(this, DrawPool.MIDDLE);
    }

    @Override
    public void delete() {
        super.delete();
        Main.drawablePool.deleteObj(this.getId(), DrawPool.MIDDLE);
    }
}
