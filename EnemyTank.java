import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

import java.util.LinkedList;
import java.util.Queue;

public class EnemyTank extends Tank {
    // 33 upgrades available for level 45
    protected Queue<Integer> statUpgradeQueue = new LinkedList<Integer>();

    // TODO: add different stat presets, body damager, glass cannon, etc.
    public EnemyTank(Vector2 spawn, String buildName, Color fillCol, Color strokeCol) {
        super(spawn, new BotController(), new Stats(), 1);
        setColor(fillCol, strokeCol);
        initTankBuild(TankBuild.createTankBuild(buildName));

        // Set upgrade paths
        setUpgradePath(TankBuild.getRandomUpgradePath());
        // Set upgrade queue
        for (int i = 0; i < 6; i++)
            statUpgradeQueue.add(Stats.BULLET_PENETRATION);
        for (int i = 0; i < 6; i++)
            statUpgradeQueue.add(Stats.BULLET_DAMAGE);
        for (int i = 0; i < 6; i++)
            statUpgradeQueue.add(Stats.RELOAD);
        for (int i = 0; i < 5; i++)
            statUpgradeQueue.add(Stats.BULLET_SPEED);

        statUpgradeQueue.add(Stats.BULLET_PENETRATION);
        statUpgradeQueue.add(Stats.BULLET_DAMAGE);
        statUpgradeQueue.add(Stats.RELOAD);
        statUpgradeQueue.add(Stats.BULLET_SPEED);
        statUpgradeQueue.add(Stats.BULLET_SPEED);

        int finalStat = (Math.random() < 0.5) ? Stats.MOVEMENT_SPEED : Stats.MAX_HEALTH;
        for (int i = 0; i < 5; i++)
            statUpgradeQueue.add(finalStat);
    }

    @Override
    public void initTankBuild(TankBuild build) {
        super.initTankBuild(build);
        TextureLoader.pendingAdd(this.tankBuild.name, this.fillCol, this.strokeCol);
    }

    @Override
    public void update() {
        super.update();
        boolean hasUpdate = (usedStatPoints < maxStatPoints);
        while (usedStatPoints < maxStatPoints && !statUpgradeQueue.isEmpty()) {
            incrementStat(statUpgradeQueue.poll());
        }
/*        if (hasUpdate) {  // TODO: fix this fire manager delay
            tankBuild.resetFireManagerDelay();
        }*/
    }

    // TODO: always draw username in front
    @Override
    public void draw() {
        super.draw();
        if (!isDead && Main.onScreen(pos, radius*scale) && !isInvisible()) {
            float inverseZoom = 1.f / Graphics.getCameraZoom(), scoreFont = 22 * inverseZoom, yPos = (pos.y - getRadiusScaled());
            Graphics.drawTextCenteredOutline(Leaderboard.formatScoreShort(score), (int) pos.x, (int) (yPos - 1.25f * scoreFont * 0.5f), (int) scoreFont, -8.f, Graphics.colAlpha(Color.WHITE, 0.75f));
            yPos -= scoreFont;
            float usernameFont = 30 * inverseZoom;
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.8f * 0.5f), (int) usernameFont, -8.f, Graphics.colAlpha(Color.WHITE, 0.75f));
        }
    }

    @Override
    public void updateVictim(GameObject victim) {
        super.updateVictim(victim);
        if (victim == Main.player) {  // If killed player
            Main.cameraHost = this;
            Main.killerName = username;
        }
    }

    @Override
    public void delete() {
        super.delete();
        Spawner.enemyCount[Spawner.getTeam(group)]--;  // Decrement enemy count
    }
}
