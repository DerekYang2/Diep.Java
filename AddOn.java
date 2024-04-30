import com.raylib.java.core.Color;

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
    FireManager fireManager;
    Barrel barrel;
    protected final static BulletStats bulletStats = new BulletStats("bullet", 1, 1, 0.3f, 1.2f, 1, 1, 1, 0.3f);

    @Override
    public void setHost(Tank tank) {
        this.host = tank;

        barrel = new Barrel(42 * 0.8f, 55, 0, tank.direction, false, false, false);
        barrel.setHost(tank);

        fireManager = new FireManager(new double[][]{{0.01, 1}});
        fireManager.setHost(tank);
    }

    @Override
    public void update() {
        // Updates
        barrel.update(host.pos.x, host.pos.y, host.direction);
        fireManager.setFiring(host.isFiring());

        if (!fireManager.getFireIndices().isEmpty()) {
            host.addForce(barrel.shoot(bulletStats, DrawPool.TOP));
        }
    }

    @Override
    public void drawBefore() {
    }

    @Override
    public void drawAfter() {
        float scaledRadius = host.radius * host.scale;

        if (Main.onScreen(host.pos, barrel.getTurretLength())) {
            barrel.draw();
            final Color fillCol = host.getDamageLerpColor(Graphics.GREY), strokeCol = host.getDamageLerpColor(Graphics.GREY_STROKE);
            Graphics.drawCircleTexture(host.pos.x, host.pos.y, scaledRadius * 0.5f, Graphics.strokeWidth, fillCol, strokeCol, host.opacity);
        }
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