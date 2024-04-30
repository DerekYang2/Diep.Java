import com.raylib.java.core.Color;

public interface AddOn {
    void drawBefore(Tank tank);
    void drawAfter(Tank tank);
    static AddOn createAddOn(String name) {
        return switch (name) {
            case "spike" -> new SpikeAddOn();
            case "smasher" -> new SmasherAddOn();
            case "landmine" -> new LandmineAddOn();
            case "dombase" -> new DominatorAddOn();
            default -> null;
        };
    }
}

class SpikeAddOn implements AddOn {
    float offsetRadians;
    final float radPerTick = 0.17f * 25/120;  // 0.17 radian per tick (25 ticks per second)
    public SpikeAddOn() {
        offsetRadians = 0;
    }

    @Override
    public void drawBefore(Tank tank) {
        offsetRadians += radPerTick;
        final float radius = tank.radius * tank.scale, scaledRadius = radius * 0.707f * 0.92f;  // scale is always 1 until death animation
        if (Main.onScreen(tank.pos, radius * 1.1f)) {  // Use larger radius for culling
            final Color fillCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(tank.opacity, 4)), strokeCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians, Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/3), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/6), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/2), Graphics.strokeWidth, fillCol, strokeCol);
        }
    }
    @Override
    public void drawAfter(Tank tank) {
        //final float radius = tank.radius * tank.scale, scaledRadius = radius * 0.707f * 0.92f;  // scale is always 1 until death animation
        //final Color fillCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(tank.opacity, 4)), strokeCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
        //Graphics.drawTriangleRounded(tank.pos, scaledRadius, 0, Graphics.strokeWidth, fillCol, strokeCol);
    }
}

class SmasherAddOn implements AddOn {
    float offsetRadians;
    final float radPerTick = 0.1f * 25/120;  // 0.1 radian per tick (25 ticks per second)
    public SmasherAddOn() {
        offsetRadians = 0;
    }
    @Override
    public void drawBefore(Tank tank) {
        offsetRadians += radPerTick;
        float sideLen = (tank.radius * tank.scale) * 1.15f;
        if (Main.onScreen(tank.pos, sideLen)) {  // Use larger radius for culling
            final Color col = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
            Graphics.drawHexagon(tank.pos, sideLen, offsetRadians, col);
        }
    }
    @Override
    public void drawAfter(Tank tank) {

    }
}

class LandmineAddOn implements AddOn {
    float offset1, offset2;
    final float rad1 = 0.1f * 25/120;  // 0.1 radian per tick (25 ticks per second)
    final float rad2 = 0.05f * 25/120;  // 0.05 radian per tick (25 ticks per second)

    public LandmineAddOn() {
        offset1 = offset2 = 0;
    }

    @Override
    public void drawBefore(Tank tank) {
        offset1 += rad1;
        offset2 += rad2;
        final float sideLen = (tank.radius * tank.scale) * 1.15f;
        if (Main.onScreen(tank.pos, sideLen)) {
            final Color col = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
            Graphics.drawHexagon(tank.pos, sideLen, offset1, col);
            Graphics.drawHexagon(tank.pos, sideLen, offset2, col);
        }
    }

    @Override
    public void drawAfter(Tank tank) {

    }
}

class DominatorAddOn implements AddOn {

    @Override
    public void drawBefore(Tank tank) {
        final float sideLen = (tank.radius * tank.scale) * 1.24f;
        if (Main.onScreen(tank.pos, sideLen)) {
            final Color fillCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(tank.opacity, 4)), strokeCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
            Graphics.drawHexagon(tank.pos, sideLen, 0, strokeCol);
            Graphics.drawHexagon(tank.pos, sideLen - Graphics.strokeWidth, 0, fillCol);
        }
    }

    @Override
    public void drawAfter(Tank tank) {

    }
}