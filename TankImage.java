import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class TankImage extends Tank {
    public TankImage(double spawnX, double spawnY, String buildName, Color fillCol, Color strokeCol) {
        super(new Vector2((float)spawnX, (float)spawnY), new DummyController(), new Stats(), 45);
        cullingOff = true;
        setColor(fillCol, strokeCol);
        initTankBuild(TankBuild.createTankBuild(buildName));
        updateStats();
    }

    @Override
    public void draw() {
        // If landmine or auto, do not use texture to draw transparent tank
        tankBuild.addOnDrawBefore();
        tankBuild.draw(); // Draw Turrets
        tankBuild.addOnDrawMiddle();
        Graphics.drawCircleTexture(pos, radius*scale, Graphics.strokeWidth, getDamageLerpColor(fillCol), getDamageLerpColor(strokeCol), opacity);
        tankBuild.addOnDrawAfter();
    }

    @Override
    public void addToPools() {}

    @Override
    public void createId() {}

    @Override
    public void delete() {
        if (healthBar != null) healthBar.delete();
    }
}
