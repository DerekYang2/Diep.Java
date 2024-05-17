import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class EnemyTank extends Tank {
    public EnemyTank(Vector2 spawn, String buildName) {
        super(spawn, new BotController(), new Stats(5, 7, 7, 7, 7, 0, 0, 0), 45);
        setColor(Graphics.RED, Graphics.RED_STROKE);
        initTankBuild(TankBuild.createTankBuild(buildName));
        TextureLoader.pendingAdd(this);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw() {
        super.draw();
        if (!isDead && Main.onScreen(pos, radius*scale)) {
            float inverseZoom = 1.f / Graphics.getCameraZoom();
            float scoreFont = 21 * inverseZoom;
            float yPos = (pos.y - radius * scale);
            Graphics.drawTextCenteredOutline(Graphics.round(score/1000, 1) + "k", (int) pos.x, (int) (yPos - scoreFont * 1.2f * 0.5f), (int) scoreFont, Color.WHITE);
            yPos -= scoreFont;
            float usernameFont = 30 * inverseZoom;
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.8f * 0.5f), (int) usernameFont, Color.WHITE);
        }
    }
}
