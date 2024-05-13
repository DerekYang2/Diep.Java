import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class EnemyTank extends Tank {
    public EnemyTank(Vector2 spawn, String buildName) {
        super(spawn, new BotController(), new Stats(5, 7, 7, 7, 7, 0, 0, 0), 45);
        setColor(Graphics.RED, Graphics.RED_STROKE);
        setTankBuild(TankBuild.createTankBuild(buildName));
        TextureLoader.pendingAdd(this);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw() {
        super.draw();
        super.draw();
        if (!isDead && Main.onScreen(pos, radius*scale)) {
            float scoreFont = 20 / Graphics.getCameraZoom();
            float yPos = (pos.y - radius * scale);
            Graphics.drawTextCenteredOutline(String.format("%.1fk", score/1000), (int) pos.x, (int) (yPos - scoreFont * 0.5f), (int) scoreFont, Color.WHITE);
            yPos -= scoreFont;
            float usernameFont = 25 / Graphics.getCameraZoom();
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.5f), (int) usernameFont, Color.WHITE);
        }
    }
}
