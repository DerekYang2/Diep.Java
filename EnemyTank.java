import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class EnemyTank extends Tank {
    public EnemyTank(Vector2 spawn, String buildName) {
        super(spawn, new BotController(), new Stats(0, 7, 7, 7, 7, 0, 0, 0), 14);
        setColor(Graphics.RED, Graphics.RED_STROKE);
        initTankBuild(TankBuild.createTankBuild(buildName));
    }

    @Override
    public void initTankBuild(TankBuild build) {
        super.initTankBuild(build);
        TextureLoader.pendingAdd(this);
    }

    @Override
    public void update() {
        super.update();
    }

    // TODO: always draw username in front
    @Override
    public void draw() {
        super.draw();
        if (!isDead && Main.onScreen(pos, radius*scale) && !isInvisible()) {
            float inverseZoom = 1.f / Graphics.getCameraZoom(), scoreFont = 22 * inverseZoom, yPos = (pos.y - radius * scale);
            Graphics.drawTextCenteredOutline(Leaderboard.formatScoreShort(score), (int) pos.x, (int) (yPos - 1.25f * scoreFont * 0.5f), (int) scoreFont, -8.f, Graphics.colAlpha(Color.WHITE, 0.75f));
            yPos -= scoreFont;
            float usernameFont = 30 * inverseZoom;
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.8f * 0.5f), (int) usernameFont, -8.f, Graphics.colAlpha(Color.WHITE, 0.75f));
        }
    }
}
