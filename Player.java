import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.text.rText;

import java.util.LinkedList;
import java.util.Queue;

import static com.raylib.java.core.input.Keyboard.*;

public class Player extends Tank {
    float currentZoom;
    float targetZoom;
    Vector2 cameraTarget;
    Stopwatch levelUpWatch = new Stopwatch();

    // Variables for HUD
    Vector2 levelBarPos;
    Bar levelBar, scoreBar;
    final float BAR_WIDTH = 450, BAR_HEIGHT = 25;  // Bar should be 16 squares
    String formattedBuildName;
    Vector2 usernamePos;
    float usernameSpacing;
    final int usernameFontSize = 48, levelBarFontSize = 22;

    Queue<Pair<String, Long>> killQueue = new LinkedList<>();  // {message, expireTime (Main.counter)}
    final private static int KILL_MESSAGE_DURATION = 3 * 120;  // 3 seconds

    UpgradeBar[] upgradeBar = new UpgradeBar[8];
    final static int UPGRADE_HUD_DURATION = 3 * 120;
    final static float PERCENT_FADE = 0.05f;
    float upgradeOpacity = 0;
    int upgradeFrames = UPGRADE_HUD_DURATION;
    Vector2 usagePos;

    public Player(Vector2 spawn, String buildName) {
        super(spawn, new PlayerController(), new Stats(), 1);

        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        initTankBuild(TankBuild.createTankBuild(buildName));
        currentZoom = targetZoom;  // Spawn with right zoom level
        cameraTarget = pos;
        Graphics.setZoom(currentZoom);  // Set zoom level
        initBars();

        // Set username variables
        usernameSpacing = -18f*usernameFontSize / Graphics.outlineFont.getBaseSize();
        Vector2 textDimensions = rText.MeasureTextEx(Graphics.outlineFont, username, usernameFontSize, usernameSpacing);
        textDimensions.x *= 0.98f;
        usernamePos = new Vector2((Graphics.cameraWidth - textDimensions.getX()) * 0.5f, levelBarPos.y - 0.8f * BAR_HEIGHT - textDimensions.getY() * 0.5f - BAR_HEIGHT);

        // Set upgrade paths
        setUpgradePath(TankBuild.getRandomUpgradePath());
        //setUpgradePath(new String[]{"auto 3", "auto 5"});
    }

    @Override
    public void initTankBuild(TankBuild tankBuild) {
        super.initTankBuild(tankBuild);
        TextureLoader.pendingAdd(this.tankBuild.name, this.fillCol, this.strokeCol);
        formattedBuildName = NameGenerator.formatNameCase(this.tankBuild.name);

        if (levelBar != null)
            levelBar.setText("Lvl " + level + " " + formattedBuildName, levelBarFontSize);

        targetZoom = getZoom();
        initUpgradeBars();
    }

    public void initBars() {
        levelBarPos = new Vector2((Graphics.cameraWidth- BAR_WIDTH) * 0.5f, Graphics.cameraHeight - 2.5f * BAR_HEIGHT);

        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        float initialLevelPercentage = (level == ScoreHandler.maxPlayerLevel) ? 1 : (score-levelStartScore)/(levelNextScore-levelStartScore);
        levelBar = new Bar(levelBarPos, BAR_WIDTH, BAR_HEIGHT, 3, Graphics.LEVELBAR, Graphics.BAR_GREY, 0.08f, initialLevelPercentage);  // If level max level, prevent division by 0 -> infinity
        levelBar.setText("Lvl " + level + " " + formattedBuildName, levelBarFontSize);
        levelUpWatch.start();

        final float scoreBarWidth = BAR_WIDTH*2/3, scoreBarHeight = BAR_HEIGHT*0.8f;
        scoreBar = new Bar(new Vector2((Graphics.cameraWidth - scoreBarWidth) * 0.5f, levelBarPos.y - scoreBarHeight - 3), scoreBarWidth, scoreBarHeight, 2, Graphics.SCORE_GREEN, Graphics.BAR_GREY, 0.08f, 0);
    }

    public void initUpgradeBars() {
        float yPos = Graphics.cameraHeight - 54;
        final float upgradeBarHeight = 22;
        String[] upgradeText = {"Movement Speed", "Reload", "Bullet Damage", "Bullet Penetration", "Bullet Speed", "Body Damage", "Max Health", "Health Regen"};
        Color[] colors = {Graphics.MOVEMENT_SPEED, Graphics.RELOAD, Graphics.BULLET_DAMAGE, Graphics.BULLET_PENETRATION, Graphics.BULLET_SPEED, Graphics.BODY_DAMAGE, Graphics.MAX_HEALTH, Graphics.HEALTH_REGEN};

        for (int i = 0; i < 8; i++) {
            int maxStat = tankBuild.getMaxStat(upgradeText[i]);
            if (maxStat > 0) {
                upgradeBar[i] = new UpgradeBar(10, yPos, 23, maxStat, upgradeBarHeight, 2, colors[i], upgradeText[i], "[" + (8 - i) + "]");
                yPos -= upgradeBarHeight + 3;
            } else {
                upgradeBar[i] = null;
            }
        }
        usagePos = new Vector2(10 + upgradeBar[0].getWidth(), yPos);
        for (int statEnum = 0; statEnum < 8; statEnum++) {
            if (upgradeBar[statEnum] != null) {
                upgradeBar[statEnum].setRects(getStat(statEnum));
            }
        }
    }

    // For timing speed
    Stopwatch debug = new Stopwatch();
    boolean startedRace = false;
    boolean crossedFinish = false;

    @Override
    public void updateLevel() {
        // Update level
        int newLevel = level;
        while (newLevel < ScoreHandler.maxPlayerLevel && score > ScoreHandler.levelToScore(newLevel + 1)) {  // If score is enough to level up
            newLevel++;
        }

        if (newLevel != level) {
            level = newLevel;
            updateStats();
            levelUpWatch.start();
            levelBar.setText("Lvl " + level + " " + formattedBuildName, levelBarFontSize);
        }
        targetZoom = getZoom();
    }

    protected float getZoom() {
        return (float) ((.55f * this.tankBuild.fieldFactor) / Math.pow(1.01, (level - 1) * 0.5f));
    }

    public void updateCamera() {
        if (tankBuild.zoomAbility && controller.holdSpecial()) {
            if (controller.pressSpecial()) {  // Only update target if the button is pressed
                // TODO: check if predator zoom amount is right
                cameraTarget = new Vector2((float) (Math.cos(direction) * 1000 * scale + pos.x), (float) (Math.sin(direction) * 1000 * scale + pos.y));
            }
        } else {
            cameraTarget = pos;
        }

        Vector2 difference = Raymath.Vector2Subtract(cameraTarget, Graphics.getCameraTarget());
        Graphics.shiftCameraTarget(Graphics.scale(difference, 0.05f));

        // Zoom in and out feature (beta testing)
        float delta = Graphics.getCameraZoom()/100;
        if (Graphics.isKeyDown(Keyboard.KEY_DOWN)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() - delta);
        }
        if (Graphics.isKeyDown(Keyboard.KEY_UP)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() + delta);
        }
        // Cap the zoom level
        Graphics.setCameraZoom(Math.max(0.1f, Math.min(10f, Graphics.getCameraZoom())));
    }

    @Override
    public void update() {
        super.update();

        if (Math.abs(targetZoom - currentZoom) > 1e-3) {
            currentZoom += (targetZoom - currentZoom) * 0.05f;
            Graphics.setZoom(currentZoom);
        }
        updateCamera();

        if (pos.x > Main.ARENA_PADDING && !startedRace) {
            System.out.println("Started");
            debug.start();
            startedRace = true;
        }

        if (pos.x > Main.arenaWidth - Main.ARENA_PADDING && !crossedFinish) {
            System.out.format("Time: %.2f\n", debug.s());
            crossedFinish = true;
        }

        if (Graphics.isKeyDown(KEY_K)) {
            score += Math.max(0, Math.min(ScoreHandler.levelToScore(45) + 0.01f - score, 23000.f/(2 * 120)));  // 2 seconds
        }

        while (!killQueue.isEmpty() && Main.counter >= killQueue.peek().second) {  // If not empty and counter is past the expire time
            killQueue.remove();  // Remove the top element
        }

        LeaderPointer.update();
    }

    @Override
    public void updateStatUpgrade() {
        super.updateStatUpgrade();
        if (usedStatPoints == maxStatPoints) return;  // If no stat points to use

        if (Graphics.isKeyPressed(KEY_ONE)) {
            incrementStat(Stats.HEALTH_REGEN);
        }
        if (Graphics.isKeyPressed(KEY_TWO)) {
            incrementStat(Stats.MAX_HEALTH);
        }
        if (Graphics.isKeyPressed(KEY_THREE)) {
            incrementStat(Stats.BODY_DAMAGE);
        }
        if (Graphics.isKeyPressed(KEY_FOUR)) {
            incrementStat(Stats.BULLET_SPEED);
        }
        if (Graphics.isKeyPressed(KEY_FIVE)) {
            incrementStat(Stats.BULLET_PENETRATION);
        }
        if (Graphics.isKeyPressed(KEY_SIX)) {
            incrementStat(Stats.BULLET_DAMAGE);
        }
        if (Graphics.isKeyPressed(KEY_SEVEN)) {
            incrementStat(Stats.RELOAD);
        }
        if (Graphics.isKeyPressed(KEY_EIGHT)) {
            incrementStat(Stats.MOVEMENT_SPEED);
        }
    }

    public void incrementStat(int statEnum) {
        if (upgradeBar[statEnum] == null || getStat(statEnum) >= tankBuild.getMaxStat(upgradeBar[statEnum].text)) {  // If stat doesn't exist or is maxed
            return;
        }
        super.incrementStat(statEnum);
        upgradeBar[statEnum].setRects(getStat(statEnum));
        if (upgradeFrames >= UPGRADE_HUD_DURATION * (1-PERCENT_FADE))  // If bar is fading out
            upgradeFrames = UPGRADE_HUD_DURATION - upgradeFrames;  // Set frames to symmetric fading in
        else if (upgradeFrames >= UPGRADE_HUD_DURATION * PERCENT_FADE)  // If bar is fully visible
            upgradeFrames = (int) (UPGRADE_HUD_DURATION * PERCENT_FADE);  // Reset to start of being fully visible
    }

    public void draw() {
        super.draw();
        LeaderPointer.draw();

/*        if (!isDead && Main.onScreen(pos, radius * scale)) {
            float inverseZoom = 1.f / Graphics.getCameraZoom();
            float scoreFont = levelBarFontSize * inverseZoom;
            float yPos = (pos.y - radius * scale);
            Graphics.drawTextCenteredOutline(Graphics.round(score / 1000, 1) + "k", (int) pos.x, (int) (yPos - scoreFont * 1.2f * 0.5f), (int) scoreFont, Color.WHITE);
            yPos -= scoreFont;
            float usernameFont = 31 * inverseZoom;
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.8f * 0.5f), (int) usernameFont, Color.WHITE);
        }*/
    }

    public void updateBars() {
        if (Main.counter % (2 - Graphics.PERFORMANCE_MODE) != 0) return;  // Only update every other frame

        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        if (level == ScoreHandler.maxPlayerLevel && levelUpWatch.ms() >= 1000) {
            levelBar.update((float) (levelUpWatch.ms() - 1600) /1000);
        } else {
            levelBar.update(levelUpWatch.ms() < 1000? 1 : (score - levelStartScore) / (levelNextScore - levelStartScore));
        }

        Tank firstTank = Leaderboard.getTankRank(0);
        if (Main.counter % 4 == 0) {
            scoreBar.setText(String.format("Score: %,d", (int)score), 20);
        }
        scoreBar.update((firstTank == null) ? 0 : score/firstTank.score);  // Percentage compared to top scorer
    }

    float prevUpgradeOpacity = 0;

    public void updateUpgradeBars() {
        upgradeFrames++;
        upgradeFrames = Math.min(UPGRADE_HUD_DURATION, upgradeFrames);  // Cap the frames

        if (usedStatPoints < maxStatPoints) {  // If has upgrade points
            if (upgradeFrames == UPGRADE_HUD_DURATION) {  // If bar is invisible
                upgradeFrames = 0;  // Restart animation
            }
            if (upgradeFrames >= UPGRADE_HUD_DURATION * PERCENT_FADE && upgradeFrames < UPGRADE_HUD_DURATION * (1-PERCENT_FADE)) { // If has upgrade points and bar is fully visible
                upgradeFrames = (int) (UPGRADE_HUD_DURATION * PERCENT_FADE);  // Reset to start of being fully visible
            }
        }

        prevUpgradeOpacity = upgradeOpacity;
        upgradeOpacity = upgradeBarOpacity(upgradeFrames);


        for (UpgradeBar bar : upgradeBar) {
            if (bar != null) {
                bar.update(upgradeOpacity);
            }
        }
    }

    public void drawLevelBar() {
        levelBar.draw();
        //float reverseZoom = 1.f / Graphics.getCameraZoom();
        //Graphics.drawTextCenteredOutline("Level " + level, Graphics.screenWidth/2 , (int) (Graphics.screenHeight - 25 * 0.5f), 22, Color.WHITE);
        scoreBar.draw();
    }

    public void drawUsername() {
        // Draw username
        Graphics.drawTextOutline(username, usernamePos, usernameFontSize, usernameSpacing, Color.WHITE);
        //Graphics.drawTextCenteredOutline(username, Graphics.cameraWidth/2, (int) (levelBarPos.y - 0.8f * BAR_HEIGHT - 20), 40, Color.WHITE);
    }

    public void drawKillQueue() {
        float inverseZoom = 1.f / Graphics.getCameraZoom();
        float x = Graphics.cameraWidth * 0.5f, y = 10;
        Vector2 pos = Graphics.getScreenToWorld2D(new Vector2(x, y), Graphics.camera);
        float textHeight = 22 * inverseZoom;

        float yPos = pos.y;
        for (Pair<String, Long> killData : killQueue) {
            int framesLeft = (int) (killData.second - Main.counter);
            float opacity = killMessageOpacity(framesLeft);
            Graphics.drawTextCenteredBackground("You killed " + killData.first, (int) pos.x, (int) (yPos + textHeight * 0.5f), (int)(22 * inverseZoom), -3 * inverseZoom, Graphics.colAlpha(Color.WHITE, opacity), Graphics.colAlpha(Graphics.BAR_GREY, opacity * 1.1f));
            yPos += textHeight + 3 * inverseZoom;  // Extra 3 spacing
        }
    }


    /**
     * Function to calculate the opacity of the kill message
     * <a href="https://www.desmos.com/calculator/x8adbicvxa">function graph</a>
     * Assumes frames is between 0 and KILL_MESSAGE_DURATION
     * @param frames The number of frames since the kill message was added
     * @return The opacity of the kill message
     */
    private static float killMessageOpacity(int frames) {
        final float O_max = 0.75f, T = KILL_MESSAGE_DURATION, p = 0.07f;
        if (frames < T*p) {
            return O_max/(T*p) * frames;
        } else if (frames < T*(1-p)){
            return O_max;
        } else {
            return -O_max/(T*p) * (frames - T*(1-p)) + O_max;
        }
    }

    private static float upgradeBarOpacity(int frames) {
        final float O_max = .8f, T = UPGRADE_HUD_DURATION, p = PERCENT_FADE;
        if (frames < T*p) {
            return O_max/(T*p) * frames;
        } else if (frames < T*(1-p)){
            return O_max;
        } else {
            return -O_max/(T*p) * (frames - T*(1-p)) + O_max;
        }
    }

    @Override
    public void updateVictim(GameObject victim) {
        super.updateVictim(victim);
        if (victim instanceof Tank) {  // Only add tanks to kill queue
            killQueue.add(new Pair<>(victim.username, Main.counter + KILL_MESSAGE_DURATION));
        }
    }

    @Override
    public void delete() {
        super.delete();
        levelBar.delete();
        Main.resetGame();
    }

    public void drawUpgradeBars() {
        if (upgradeOpacity < 0.01f) return;  // If opacity is too low, don't draw
        for (UpgradeBar bar : upgradeBar) {
            if (bar != null) {
                bar.draw();
            }
        }
        if (maxStatPoints - usedStatPoints > 0) {
            Graphics.drawTextOutline("x" + (maxStatPoints - usedStatPoints), usagePos, 30, -2, Graphics.colAlpha(Color.WHITE, upgradeOpacity));
        }
    }
}
