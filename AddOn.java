public interface AddOn {
    public void drawBefore(Tank tank);
    public void drawAfter(Tank tank);
    public static AddOn createAddOn(String name) {
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
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians, Graphics.strokeWidth, Graphics.DARK_GREY, Graphics.DARK_GREY_STROKE);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/3), Graphics.strokeWidth, Graphics.DARK_GREY, Graphics.DARK_GREY_STROKE);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/6), Graphics.strokeWidth, Graphics.DARK_GREY, Graphics.DARK_GREY_STROKE);
            Graphics.drawTriangleRounded(tank.pos, scaledRadius, offsetRadians + (float) (Math.PI/2), Graphics.strokeWidth, Graphics.DARK_GREY, Graphics.DARK_GREY_STROKE);
        }
    }
    @Override
    public void drawAfter(Tank tank) {

    }
}

