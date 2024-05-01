import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public abstract class AddOn {
    protected Tank host;
    public void setHost(Tank tank) {
        host = tank;
    }
    public abstract void update();
    public abstract void drawBefore();
    public abstract void drawAfter();
    public static AddOn createAddOn(String name) {
        return switch (name) {
            case "spike" -> new SpikeAddOn();
            case "smasher" -> new SmasherAddOn();
            case "landmine" -> new LandmineAddOn();
            case "dombase" -> new DominatorAddOn();
            case "autoturret" -> new AutoTurretAddOn();
            case "autosmasher" -> new AutoSmasherAddOn();
            case "auto5" -> new AutoNAddOn(5);
            case "auto3" -> new AutoNAddOn(3);
            default -> null;
        };
    }
}

class SpikeAddOn extends AddOn {
    float offsetRadians;
    final float radPerTick = 0.17f * 25/120;  // 0.17 radian per tick (25 ticks per second)
    public SpikeAddOn() {
        offsetRadians = 0;
    }

    @Override
    public void update() {
        offsetRadians += radPerTick;
    }

    @Override
    public void drawBefore() {
        final float radius = host.radius * host.scale, scaledRadius = radius * 0.707f * 0.92f;  // scale is always 1 until death animation
        if (Main.onScreen(host.pos, radius * 1.1f)) {  // Use larger radius for culling
            final Color fillCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(host.opacity, 4)), strokeCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(host.opacity, 4));
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians, Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians + (float) (Math.PI/3), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians + (float) (Math.PI/6), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians + (float) (Math.PI/2), Graphics.strokeWidth, fillCol, strokeCol);
        }
    }

    public void drawAfter() {
        //final float radius = tank.radius * tank.scale, scaledRadius = radius * 0.707f * 0.92f;  // scale is always 1 until death animation
        //final Color fillCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(tank.opacity, 4)), strokeCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
        //Graphics.drawTriangleRounded(tank.pos, scaledRadius, 0, Graphics.strokeWidth, fillCol, strokeCol);
    }
}

class SmasherAddOn extends AddOn {
    float offsetRadians;
    final float radPerTick = 0.1f * 25/120;  // 0.1 radian per tick (25 ticks per second)
    public SmasherAddOn() {
        offsetRadians = 0;
    }

    @Override
    public void update() {
        offsetRadians += radPerTick;
    }

    @Override
    public void drawBefore() {
        float sideLen = (host.radius * host.scale) * 1.15f;
        if (Main.onScreen(host.pos, sideLen)) {  // Use larger radius for culling
            final Color col = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(host.opacity, 4));
            Graphics.drawHexagon(host.pos, sideLen, offsetRadians, col);
        }
    }
    @Override
    public void drawAfter() {

    }
}

class LandmineAddOn extends AddOn {
    float offset1, offset2;
    final float rad1 = 0.1f * 25/120;  // 0.1 radian per tick (25 ticks per second)
    final float rad2 = 0.05f * 25/120;  // 0.05 radian per tick (25 ticks per second)

    public LandmineAddOn() {
        offset1 = offset2 = 0;
    }

    public void update() {
        offset1 += rad1;
        offset2 += rad2;
    }

    @Override
    public void drawBefore() {
        final float sideLen = (host.radius * host.scale) * 1.15f;
        if (Main.onScreen(host.pos, sideLen)) {
            final Color col = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(host.opacity, 4));
            Graphics.drawHexagon(host.pos, sideLen, offset1, col);
            Graphics.drawHexagon(host.pos, sideLen, offset2, col);
        }
    }

    @Override
    public void drawAfter() {

    }
}

class DominatorAddOn extends AddOn {
    public DominatorAddOn() {
    }

    @Override
    public void update() {
    }

    @Override
    public void drawBefore() {
        final float sideLen = (host.radius * host.scale) * 1.24f;
        if (Main.onScreen(host.pos, sideLen)) {
            final Color fillCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(host.opacity, 4)), strokeCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(host.opacity, 4));
            Graphics.drawHexagon(host.pos, sideLen, 0, strokeCol);
            Graphics.drawHexagon(host.pos, sideLen - Graphics.strokeWidth, 0, fillCol);
        }
    }

    @Override
    public void drawAfter() {

    }
}

class AutoTurretAddOn extends AddOn {
    AutoTurret autoTurret;

    public AutoTurretAddOn() {

    }

    @Override
    public void setHost(Tank tank) {
        this.host = tank;

        Barrel barrel = new Barrel(42 * 0.8f, 55, 0, tank.direction, false, false, false);
        barrel.setHost(tank);

        FireManager fireManager = new FireManager(new double[][]{{0, 1}});
        fireManager.setHost(tank);

        final BulletStats BULLET_STATS = new BulletStats("bullet", 1, 1, 0.3f, 1.2f, 1, 1, 1, 0.3f);

        autoTurret = new AutoTurret(tank, barrel, fireManager, BULLET_STATS);
        autoTurret.setOffset(new Vector2(0, 0), 2 * Math.PI);  // No offset
    }


    @Override
    public void update() {
        autoTurret.update(host.pos);
        autoTurret.shoot(DrawPool.TOP);
    }

    @Override
    public void drawBefore() {
    }

    @Override
    public void drawAfter() {
        autoTurret.draw();
    }
}

class AutoSmasherAddOn extends AutoTurretAddOn {
    float offsetRadians;
    final float radPerTick = 0.1f * 25/120;  // 0.1 radian per tick (25 ticks per second)

    public AutoSmasherAddOn() {
        super();
        offsetRadians = 0;
    }

    @Override
    public void update() {
        super.update();
        offsetRadians += radPerTick;
    }

    @Override
    public void drawBefore() {
        float sideLen = (host.radius * host.scale) * 1.15f;
        if (Main.onScreen(host.pos, sideLen)) {  // Use larger radius for culling
            final Color col = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(host.opacity, 4));
            Graphics.drawHexagon(host.pos, sideLen, offsetRadians, col);
        }
    }
}

/**
 * TODO: use Atan2 to get absolute angle, check if targets are within absolute angle range (turret angle +- 85 degrees)
 * Normalize angles
 */
class AutoNAddOn extends AddOn {
    AutoTurret[] autoTurrets;
    int numTurrets;
    float offsetRadians;
    final float radPerTick = 0.01f * 25/120;  // Rotation of turret, 0.01 radian per tick (25 ticks per second)
    final double rangeRad = Math.toRadians(180);  // Range of turret

    public AutoNAddOn(int numTurrets) {
        this.numTurrets = numTurrets;
        autoTurrets = new AutoTurret[numTurrets];
        offsetRadians = 0;
    }

    @Override
    public void setHost(Tank tank) {
        this.host = tank;
        for (int i = 0; i < numTurrets; i++) {
            Barrel barrel = new Barrel(42 * 0.7f, 55, 0, tank.direction, false, false, false);
            barrel.setHost(tank);

            FireManager fireManager = new FireManager(new double[][]{{0.01, 1}});
            fireManager.setHost(tank);

            final BulletStats BULLET_STATS = new BulletStats("bullet", 1, 1, 0.4f, 1.2f, 1, 1, 1, 0.3f);

            autoTurrets[i] = new AutoTurret(tank, barrel, fireManager, BULLET_STATS);
        }
    }

    @Override
    public void update() {
        offsetRadians += radPerTick;

        // Set relative positions
        float radiusScaled = host.radius * host.scale * 0.8f;
        for (int i = 0; i < numTurrets; i++) {
            double angle = offsetRadians + (2*Math.PI/numTurrets) * i;
            autoTurrets[i].setOffset(new Vector2((float) (radiusScaled * Math.cos(angle)), (float) (radiusScaled * Math.sin(angle))), rangeRad);
        }

        // Update and shoot
        for (AutoTurret autoTurret : autoTurrets) {
            autoTurret.update(host.pos);  // Call update function and set absoulte position
            autoTurret.shoot(DrawPool.BOTTOM);  // Shoot at bottom layer
        }
    }

    @Override
    public void drawBefore() {
        for (AutoTurret autoTurret : autoTurrets) {
            autoTurret.draw();
        }
    }

    @Override
    public void drawAfter() {
    }
}
