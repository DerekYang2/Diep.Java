import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

import java.util.LinkedList;
import java.util.Queue;

public class EnemyTank extends Tank {
    // 33 upgrades available for level 45
    protected Queue<Integer> statUpgradeQueue = new LinkedList<Integer>();

    public EnemyTank(Vector2 spawn, String buildName) {
        super(spawn, new BotController(), new Stats(), 14);
        setColor(Graphics.RED, Graphics.RED_STROKE);
        initTankBuild(TankBuild.createTankBuild(buildName));

        // Set upgrade paths
        setUpgradePath(TankBuild.getRandomUpgradePath());

        // Set upgrade queue
        for (int i = 0; i < 7; i++)
            statUpgradeQueue.add(Stats.BULLET_PENETRATION);
        for (int i = 0; i < 7; i++)
            statUpgradeQueue.add(Stats.BULLET_DAMAGE);
        for (int i = 0; i < 7; i++)
            statUpgradeQueue.add(Stats.RELOAD);
        for (int i = 0; i < 7; i++)
            statUpgradeQueue.add(Stats.BULLET_SPEED);
        for (int i = 0; i < 5; i++)
            statUpgradeQueue.add(Stats.MAX_HEALTH);
    }

    @Override
    public void initTankBuild(TankBuild build) {
        super.initTankBuild(build);
        TextureLoader.pendingAdd(this);
    }

    @Override
    public void update() {
        super.update();
        while (usedStatPoints < maxStatPoints) {
            incrementStat(statUpgradeQueue.poll());
        }
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
