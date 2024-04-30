import com.raylib.java.core.Color;

public interface AddOn {
    void drawBefore(Tank tank);
    void drawAfter(Tank tank);
    static AddOn createAddOn(String name) {
        switch (name) {
            case "spike":
                return new spikeAddOn();
            default:
                return null;
        }
    }
}

class spikeAddOn implements AddOn {
    float offsetRadians;
    final float radPerTick = 0.1f * 25/120;  // 2.5 radian per second
    public spikeAddOn() {
        offsetRadians = 0;
    }

    @Override
    public void drawBefore(Tank tank) {
        offsetRadians += radPerTick;
        final float radius = tank.radius * tank.scale, scaledRadius = radius * 0.707f * 0.92f;  // scale is always 1 until death animation
        if (Main.onScreen(tank.pos, radius)) {  // Use larger radius for culling
            final Color fillCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY), (float) Math.pow(tank.opacity, 4)), strokeCol = Graphics.colAlpha(tank.getDamageLerpColor(Graphics.DARK_GREY_STROKE), (float) Math.pow(tank.opacity, 4));
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians, Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/3), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/6), Graphics.strokeWidth, fillCol, strokeCol);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/2), Graphics.strokeWidth, fillCol, strokeCol);
        }
    }
    @Override
    public void drawAfter(Tank tank) {

    }
}

