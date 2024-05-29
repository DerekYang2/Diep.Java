import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public abstract class AddOn {
    protected Tank host;
    public void setHost(Tank tank) {
        host = tank;
    }
    public abstract void update();
    public abstract void drawBefore();
    public abstract void drawMiddle();
    public abstract void drawAfter();
    public abstract float maxRadius();  // Maximum radius of add-on
    public void setPos(Vector2 pos) {

    }
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
            case "pronounced" -> new PronouncedAddOn();
            case "launcher" -> new LauncherAddOn();
            case "glider" -> new InvertedLauncherAddOn();
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
        if (host.cullingOff || Main.onScreen(host.pos, maxRadius())) {  // Use larger radius for culling
            final Color fillCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY), host.opacity), strokeCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), host.opacity * host.opacity);
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians, Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians + (float) (Math.PI/3), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians + (float) (Math.PI/6), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(host.pos, scaledRadius, offsetRadians + (float) (Math.PI/2), Graphics.strokeWidth, fillCol, strokeCol);
        }
    }

    @Override
    public void drawMiddle() {
    }

    public void drawAfter() {
        //final float radius = tank.radius * tank.scale, scaledRadius = radius * 0.707f * 0.92f;  // scale is always 1 until death animation
        //final Color fillCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(tank.opacity, 4)), strokeCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
        //Graphics.drawTriangleRounded(tank.pos, scaledRadius, 0, Graphics.strokeWidth, fillCol, strokeCol);
    }

    // From circumcenter to vertex of equilateral triangle
    @Override
    public float maxRadius() {
        return 2 * (host.radius * host.scale * 0.707f * 0.92f);
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
        if (host.cullingOff || Main.onScreen(host.pos, sideLen)) {  // Use larger radius for culling
            final Color col = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), host.opacity);
            Graphics.drawHexagon(host.pos, sideLen, offsetRadians, col);
        }
    }

    @Override
    public void drawMiddle() {}

    @Override
    public void drawAfter() {}

    @Override
    public float maxRadius() {
        return (host.radius * host.scale) * 1.15f;
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
        if (host.cullingOff || Main.onScreen(host.pos, sideLen)) {
            final Color col = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), host.opacity);
            Graphics.drawHexagon(host.pos, sideLen, offset1, col);
            Graphics.drawHexagon(host.pos, sideLen, offset2, col);
        }
    }

    @Override
    public void drawMiddle() {}

    @Override
    public void drawAfter() {}

    @Override
    public float maxRadius() {
        return (host.radius * host.scale) * 1.15f;
    }
}

class DominatorAddOn extends AddOn {
    public DominatorAddOn() {}

    @Override
    public void update() {}

    @Override
    public void drawBefore() {
        final float sideLen = (host.radius * host.scale) * 1.24f;
        if (host.cullingOff || Main.onScreen(host.pos, sideLen)) {
            final Color fillCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY), host.opacity), strokeCol = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), host.opacity);
            Graphics.drawHexagon(host.pos, sideLen, 0, strokeCol);
            Graphics.drawHexagon(host.pos, sideLen - Graphics.strokeWidth, 0, fillCol);
        }
    }

    @Override
    public void drawMiddle() {
        float offsetDist = host.radius * host.scale * 4.f / 5;
        float length = host.radius * host.scale * 2.1f / 5;
        if (host.cullingOff || Main.onScreen(host.pos, offsetDist + length)) {
            float width = 35 * host.scale * 0.94f;
            Graphics.drawTurretTrapezoid((float) (host.pos.x + offsetDist * Math.cos(host.direction)), (float) (host.pos.y + offsetDist * Math.sin(host.direction)), length, width, host.direction, Graphics.strokeWidth, host.getDamageLerpColor(Graphics.GREY), host.getDamageLerpColor(Graphics.GREY_STROKE), host.opacity, true);
        }
    }

    @Override
    public void drawAfter() {}

    @Override
    public float maxRadius() {
        return (host.radius * host.scale) * 1.24f;
    }
}

class AutoTurretAddOn extends AddOn {
    AutoTurret autoTurret;

    public AutoTurretAddOn() {

    }

    @Override
    public void setHost(Tank tank) {
        this.host = tank;
        final BulletStats BULLET_STATS = new BulletStats("bullet", 1, 1, 0.3f, 1.2f, 1, 1, 1, 0.3f);

        Barrel barrel = new Barrel(BULLET_STATS, 42 * 0.8f, 55, 1, 0, 0, false, false, false);
        barrel.setHost(tank);

        FireManager fireManager = new FireManager(new double[][]{{0, 1}});
        fireManager.setHost(tank);


        autoTurret = new AutoTurret(tank, barrel, fireManager);
        autoTurret.setOffset(new Vector2(0, 0), 2 * Math.PI);  // No offset
    }


    @Override
    public void update() {
        autoTurret.update(host.pos);
        autoTurret.shoot(DrawPool.TOP);
    }

    @Override
    public void drawBefore() {}

    @Override
    public void drawMiddle() {}

    @Override
    public void drawAfter() {
        autoTurret.draw();
    }

    @Override
    public float maxRadius() {
        return host.scale * host.radius + autoTurret.barrel.getTurretLength();
    }

    @Override
    public void setPos(Vector2 pos) {
        autoTurret.setPos(pos);
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
        if (host.cullingOff || Main.onScreen(host.pos, sideLen)) {  // Use larger radius for culling
            final Color col = Graphics.colAlpha(host.getDamageLerpColor(Graphics.DARK_GREY_STROKE), host.opacity);
            Graphics.drawHexagon(host.pos, sideLen, offsetRadians, col);
        }
    }
}

/**
 * Normalize angles
 */
class AutoNAddOn extends AddOn {
    AutoTurret[] autoTurrets;
    int numTurrets;
    float offsetRadians;
    final float radPerTick = 0.01f * 25/120;  // Rotation of turret, 0.01 radian per tick (25 ticks per second)
    final double rangeRad = Math.toRadians(170);  // Range of turret

    public AutoNAddOn(int numTurrets) {
        this.numTurrets = numTurrets;
        autoTurrets = new AutoTurret[numTurrets];
        offsetRadians = 0;
    }

    @Override
    public void setHost(Tank tank) {
        this.host = tank;
        final BulletStats BULLET_STATS = new BulletStats("bullet", 1, 1, 0.35f, 1.2f, 1, 1, 1, 0.3f);
        for (int i = 0; i < numTurrets; i++) {
            Barrel barrel = new Barrel(BULLET_STATS, 42 * 0.8f, 55, 1,0, 0, false, false, false);
            barrel.setHost(tank);

            FireManager fireManager = new FireManager(new double[][]{{1, 1}});
            fireManager.setHost(tank);

            autoTurrets[i] = new AutoTurret(tank, barrel, fireManager);
        }
        offsetRadians = 0;
        // Set relative positions
        float radiusScaled = host.radius * host.scale * 0.8f;
        for (int i = 0; i < numTurrets; i++) {
            double angle = offsetRadians + (2*Math.PI/numTurrets) * i;
            float offsetX = (float) (radiusScaled * Math.cos(angle));
            float offsetY = (float) (radiusScaled * Math.sin(angle));
            autoTurrets[i].setOffset(new Vector2(offsetX, offsetY), rangeRad);
            autoTurrets[i].direction = (float)Math.atan2(offsetY, offsetX);
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
            autoTurret.update(host.pos);  // Call update function and set absolute position
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
    public void drawMiddle() {}
    @Override
    public void drawAfter() {}

    @Override
    public float maxRadius() {
        return host.scale * host.radius + autoTurrets[0].barrel.getTurretLength();
    }

    @Override
    public void setPos(Vector2 pos) {
        for (AutoTurret autoTurret : autoTurrets) {
            autoTurret.setPos(pos);
        }
    }
}

class PronouncedAddOn extends AddOn {
    public PronouncedAddOn() {}
    @Override
    public void update() {}
    @Override
    public void drawBefore() {}
    @Override
    public void drawMiddle() {
        float offsetDist = (host.radius * host.scale) * 3.1f / 5;
        float length = (host.radius * host.scale) * 3.3f / 5;
        if (host.cullingOff || Main.onScreen(host.pos, offsetDist + length)) {
            float width = host.tankBuild.getBarrel(0).getTurretWidth() * 0.94f;
            Graphics.drawTurretTrapezoid((float) (host.pos.x + offsetDist * Math.cos(host.direction)), (float) (host.pos.y + offsetDist * Math.sin(host.direction)), length, width, host.direction, Graphics.strokeWidth, host.getDamageLerpColor(Graphics.GREY), host.getDamageLerpColor(Graphics.GREY_STROKE), host.opacity, true);
        }
    }
    @Override
    public void drawAfter() {}

    @Override
    public float maxRadius() {
        return (host.radius * host.scale);
    }
}

// TODO: add culling to add-ons below

class LauncherAddOn extends AddOn {
    public LauncherAddOn() {}
    @Override
    public void update() {}
    @Override
    public void drawBefore() {
        float radius = host.radius * host.scale;
        float offsetDist = (host.radius * host.scale) * 3.1f / 5;
        Graphics.drawTurretTrapezoid((float) (host.pos.x + offsetDist * Math.cos(host.direction)), (float) (host.pos.y + offsetDist * Math.sin(host.direction)), radius * 1.2f, 0.74f * radius, host.direction, Graphics.strokeWidth, host.getDamageLerpColor(Graphics.GREY), host.getDamageLerpColor(Graphics.GREY_STROKE), host.opacity, false);
    }
    @Override
    public void drawMiddle() {}
    @Override
    public void drawAfter() {
        //drawBefore();
    }

    @Override
    public float maxRadius() {
        return (host.radius * host.scale) * (3.1f / 5 + 1.2f);
    }

    @Override
    public void setPos(Vector2 pos) {}
}

class InvertedLauncherAddOn extends AddOn {
    public InvertedLauncherAddOn() {}
    @Override
    public void update() {}
    @Override
    public void drawBefore() {
        float radius = host.radius * host.scale;
        float offsetDist = (host.radius * host.scale) * 3.1f / 5;
        Graphics.drawTurretTrapezoid((float) (host.pos.x + offsetDist * Math.cos(host.direction)), (float) (host.pos.y + offsetDist * Math.sin(host.direction)), radius * 1.2f, 0.707f * radius, host.direction, Graphics.strokeWidth, host.getDamageLerpColor(Graphics.GREY), host.getDamageLerpColor(Graphics.GREY_STROKE), host.opacity, true);
    }
    @Override
    public void drawMiddle() {}
    @Override
    public void drawAfter() {
        //drawBefore();
    }

    @Override
    public float maxRadius() {
        return (host.radius * host.scale) * (3.1f / 5 + 1.2f);
    }
}
