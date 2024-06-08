import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.text.rText;

import java.util.LinkedList;
import java.util.Queue;

import static com.raylib.java.core.input.Keyboard.*;

public class Player extends Tank {
    float currentZoom;
    float targetZoom;
    Stopwatch levelUpWatch = new Stopwatch();

    // Variables for HUD
    Vector2 levelBarPos;
    Bar levelBar, scoreBar;
    final float BAR_WIDTH = 450, BAR_HEIGHT = 25;  // Bar should be 16 squares
    String formattedBuildName;
    Vector2 usernamePos;
    float usernameSpacing;
    final int usernameFontSize = 48, levelBarFontSize = 22;

    Queue<Pair<String, Long>> messageQueue = new LinkedList<>();  // {message, expireTime (Main.counter)}
    final private static int KILL_MESSAGE_DURATION = 3 * 120;  // 3 seconds

    //Variables for upgrade
    UpgradeBar[] upgradeBar = new UpgradeBar[8];
    final static int UPGRADE_HUD_DURATION = 3 * 120;
    final static float PERCENT_FADE = 0.05f;
    float upgradeOpacity = 0;
    int upgradeFrames = UPGRADE_HUD_DURATION;
    Vector2 usagePos;

    // New timer measuring how long the player has been alive
    Stopwatch aliveTimer = new Stopwatch();

    /**
     * Constructs new Tank with spawn position and build name
     * @param spawn Spawn position of the player
     * @param buildName Name of player
     */
    public Player(Vector2 spawn, String buildName) {
        super(spawn, new PlayerController(), new Stats(), 1);
        aliveTimer.start();
        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        initTankBuild(TankBuild.createTankBuild(buildName));
        currentZoom = targetZoom;  // Spawn with right zoom level
        Graphics.setZoom(currentZoom);  // Set zoom level
        initBars();

        // Set username variables
        usernameSpacing = -18f*usernameFontSize / Graphics.outlineFont.getBaseSize();
        Vector2 textDimensions = rText.MeasureTextEx(Graphics.outlineFont, username, usernameFontSize, usernameSpacing);
        textDimensions.x *= 0.98f;
        usernamePos = new Vector2((Graphics.cameraWidth - textDimensions.getX()) * 0.5f, levelBarPos.y - 0.8f * BAR_HEIGHT - textDimensions.getY() * 0.5f - BAR_HEIGHT);

        // Set upgrade paths
        setUpgradePath(TankBuild.getRandomUpgradePath());
        setUpgradePath(new String[]{"ranger"});
    }

    /**
     * Initializes Tank build for the player
     * @param tankBuild Represents build for the player
     */
    @Override
    public void initTankBuild(TankBuild tankBuild) {
        super.initTankBuild(tankBuild);
        TextureLoader.pendingAdd(this.tankBuild.name, this.fillCol, this.strokeCol); //Add tank build textures
        formattedBuildName = NameGenerator.formatNameCase(this.tankBuild.name); //Format build name
        //Update level bar text
        if (levelBar != null)
            levelBar.setText("Lvl " + level + " " + formattedBuildName, levelBarFontSize);
        //Set zoom
        targetZoom = getZoom();
        initUpgradeBars();
    }

    /**
     * Initializes level and score bar for player
     */
    public void initBars() {
        //Calculate position of level bar
        levelBarPos = new Vector2((Graphics.cameraWidth- BAR_WIDTH) * 0.5f, Graphics.cameraHeight - BAR_HEIGHT - 10);

        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        float initialLevelPercentage = (level == ScoreHandler.maxPlayerLevel) ? 1 : (score-levelStartScore)/(levelNextScore-levelStartScore);
        //Initializes level bar
        levelBar = new Bar(levelBarPos, BAR_WIDTH, BAR_HEIGHT, 3, Graphics.LEVELBAR, Graphics.BAR_GREY, 0.08f, initialLevelPercentage);  // If level max level, prevent division by 0 -> infinity
        levelBar.setText("Lvl " + level + " " + formattedBuildName, levelBarFontSize);
        levelUpWatch.start();

        //Initializes score bar
        final float scoreBarWidth = BAR_WIDTH*2/3, scoreBarHeight = BAR_HEIGHT*0.8f;
        scoreBar = new Bar(new Vector2((Graphics.cameraWidth - scoreBarWidth) * 0.5f, levelBarPos.y - scoreBarHeight - 3), scoreBarWidth, scoreBarHeight, 2, Graphics.SCORE_GREEN, Graphics.BAR_GREY, 0.08f, 0);
    }

    /**
     * Initializes upgrade bars of player
     */
    public void initUpgradeBars() {
        final float upgradeBarHeight = 22; //Height of upgrade bar
        float yPos = Graphics.cameraHeight - upgradeBarHeight - 10; //y-position of upgrade bar
        String[] upgradeText = {"Movement Speed", "Reload", "Bullet Damage", "Bullet Penetration", "Bullet Speed", "Body Damage", "Max Health", "Health Regen"}; //Array of stats
        for (int i = 0; i < upgradeText.length; i++) { 
            upgradeText[i] = tankBuild.getProperStat(upgradeText[i]);  // Get proper stat name
        }

        //Color of each stat
        Color[] colors = {Graphics.MOVEMENT_SPEED, Graphics.RELOAD, Graphics.BULLET_DAMAGE, Graphics.BULLET_PENETRATION, Graphics.BULLET_SPEED, Graphics.BODY_DAMAGE, Graphics.MAX_HEALTH, Graphics.HEALTH_REGEN};

        for (int i = 0; i < 8; i++) {
            int maxStat = tankBuild.getMaxStat(upgradeText[i]);
            if (maxStat > 0) {
                //Create upgrade bar
                upgradeBar[i] = new UpgradeBar(10, yPos, 23, maxStat, upgradeBarHeight, 2, colors[i], upgradeText[i], "[" + (8 - i) + "]");
                yPos -= upgradeBarHeight + 3; //y-position for the upgrade bar
            } else {
                upgradeBar[i] = null;
            }
        }
        //Set position of text display
        usagePos = new Vector2(10 + upgradeBar[0].getWidth() - 5, yPos + 5);
        for (int statEnum = 0; statEnum < 8; statEnum++) {
            if (upgradeBar[statEnum] != null) {
                upgradeBar[statEnum].setRects(getStat(statEnum));
            }
        }
    }

    /**
     * Updates player based on their score
     */
    @Override
    public void updateLevel() {
        // Update level
        int newLevel = level;
        while (newLevel < ScoreHandler.maxPlayerLevel && score > ScoreHandler.levelToScore(newLevel + 1)) {  // If score is enough to level up
            newLevel++; //Levels up
        }

        if (newLevel != level) {
            level = newLevel;
            updateStats(); //Update stats based on new level
            levelUpWatch.start();
            levelBar.setText("Lvl " + level + " " + formattedBuildName, levelBarFontSize);
        }
        targetZoom = getZoom();
    }

    @Override
    public void update() {
        super.update();

        if (Math.abs(targetZoom - currentZoom) > 1e-3) {
            currentZoom += (targetZoom - currentZoom) * 0.05f; //Adjusts camera
            Graphics.setZoom(currentZoom);
        }

        //For testing and faster matches
        if (Graphics.isKeyDown(KEY_K)) { //Increases score if K is pressed
            score += Math.max(0, Math.min(ScoreHandler.levelToScore(45) + 0.01f - score, 23000.f/(2 * 120)));  // 2 seconds
        }

        while (!messageQueue.isEmpty() && Main.counter >= messageQueue.peek().second) {  // If not empty and counter is past the expire time
            messageQueue.remove();  // Remove the top element
        }

        LeaderPointer.update(); //Update arrow pointing to leader
    }

    /**
     * Updates player's stats based on key pressed
     */
    @Override
    public void updateStatUpgrade() {
        super.updateStatUpgrade();
        if (usedStatPoints == maxStatPoints) return;  // If no stat points to use

        //Increments stats based on number key pressed
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

    /**
     * Increments specific stat if not at max value
     * @param statEnum Index of stat
     */
    public void incrementStat(int statEnum) {
        if (upgradeBar[statEnum] == null || getStat(statEnum) >= tankBuild.getMaxStat(upgradeBar[statEnum].text)) {  // If stat doesn't exist or is maxed
            return;
        }
        super.incrementStat(statEnum);
        upgradeBar[statEnum].setRects(getStat(statEnum)); //Update display
        if (upgradeFrames >= UPGRADE_HUD_DURATION * (1-PERCENT_FADE))  // If bar is fading out
            upgradeFrames = UPGRADE_HUD_DURATION - upgradeFrames;  // Set frames to symmetric fading in
        else if (upgradeFrames >= UPGRADE_HUD_DURATION * PERCENT_FADE)  // If bar is fully visible
            upgradeFrames = (int) (UPGRADE_HUD_DURATION * PERCENT_FADE);  // Reset to start of being fully visible
    }

    /**
     * Draws player and pointer
     */
    public void draw() {
        super.draw();
        LeaderPointer.draw();

/*        if (!isDead && Main.onScreen(pos, getRadiusScaled())) {
            float inverseZoom = 1.f / Graphics.getCameraZoom();
            float scoreFont = levelBarFontSize * inverseZoom;
            float yPos = (pos.y - getRadiusScaled());
            Graphics.drawTextCenteredOutline(Graphics.round(score / 1000, 1) + "k", (int) pos.x, (int) (yPos - scoreFont * 1.2f * 0.5f), (int) scoreFont, Color.WHITE);
            yPos -= scoreFont;
            float usernameFont = 31 * inverseZoom;
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.8f * 0.5f), (int) usernameFont, Color.WHITE);
        }*/
    }

    /**
     * Updates level and score bar displayed on screen
     * Calculates percentage compared to top scorer
     */
    public void updateBars() {
        if (Main.counter % (2 - Graphics.PERFORMANCE_MODE) != 0) return;  // Only update every other frame

        //Score values
        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        if (level == ScoreHandler.maxPlayerLevel && levelUpWatch.ms() >= 1000) { //Updates progress
            levelBar.update((float) (levelUpWatch.ms() - 1600) /1000);
        } else {
            levelBar.update(levelUpWatch.ms() < 1000? 1 : (score - levelStartScore) / (levelNextScore - levelStartScore));
        }

        Tank firstTank = Leaderboard.getTankRank(0);
        if (Main.counter % 4 == 0) { //Update text and progress
            scoreBar.setText(String.format("Score: %,d", (int)score), 20);
        }
        scoreBar.update((firstTank == null) ? 0 : score/firstTank.score);  // Percentage compared to top scorer
    }

    float prevUpgradeOpacity = 0;

    /**
     * Updates upgrade bars on screen
     */
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

        //Update each individual bar with opacity
        for (UpgradeBar bar : upgradeBar) {
            if (bar != null) {
                bar.update(upgradeOpacity);
            }
        }
    }

    /**
     * Draws level and score bar
     */
    public void drawLevelBar() {
        levelBar.draw();
        //float reverseZoom = 1.f / Graphics.getCameraZoom();
        //Graphics.drawTextCenteredOutline("Level " + level, Graphics.screenWidth/2 , (int) (Graphics.screenHeight - 25 * 0.5f), 22, Color.WHITE);
        scoreBar.draw();
    }

    /**
     * Draws username
     */
    public void drawUsername() {
        Graphics.drawTextOutline(username, usernamePos, usernameFontSize, usernameSpacing, Color.WHITE);
        //Graphics.drawTextCenteredOutline(username, Graphics.cameraWidth/2, (int) (levelBarPos.y - 0.8f * BAR_HEIGHT - 20), 40, Color.WHITE);
    }

    /**
     * Draws kill messages on the screen with proper formatting
     */
    public void drawKillQueue() {
        //Calculates position
        float inverseZoom = 1.f / Graphics.getCameraZoom();
        float x = Graphics.cameraWidth * 0.5f, y = 10;
        Vector2 pos = Graphics.getScreenToWorld2D(new Vector2(x, y), Graphics.camera);
        float textHeight = 22 * inverseZoom;

        float yPos = pos.y;
        for (Pair<String, Long> dataPair : messageQueue) {
            int framesLeft = (int) (dataPair.second - Main.counter);
            float opacity = messageOpacity(framesLeft);
            //Draws message
            Graphics.drawTextCenteredBackground(dataPair.first, (int) pos.x, (int) (yPos + textHeight * 0.5f), (int)(22 * inverseZoom), -2.5f * inverseZoom, Graphics.colAlpha(Color.WHITE, opacity), Graphics.colAlpha(Graphics.BAR_GREY, opacity * 1.1f));
            yPos += textHeight + 3 * inverseZoom;  // Extra 3 spacing
        }
    }


    /**
     * Function to calculate the opacity of the message
     * <a href="https://www.desmos.com/calculator/x8adbicvxa">function graph</a>
     * Assumes frames is between 0 and MESSAGE_DURATION
     * @param frames The number of frames since the message was added
     * @return The opacity of the message
     */
    private static float messageOpacity(int frames) {
        final float O_max = 0.75f, T = KILL_MESSAGE_DURATION, p = 0.07f;
        if (frames < T*p) {
            return O_max/(T*p) * frames;
        } else if (frames < T*(1-p)){
            return O_max;
        } else {
            return -O_max/(T*p) * (frames - T*(1-p)) + O_max;
        }
    }

    /**
     * Function to calculate the opacity of the bar
     * Assumes frames is between 0 and UPGRADE_HUD_DURATION
     * @param frames The number of frames since the bar was added
     * @return The opacity of the bar
     */
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

    /**
     * Adds a message to the kill queue
     * @param message Message to be added to the queue
     * @param duration Duration of the message's appearance in frames
     */
    public void addMessage(String message, int duration) {
        messageQueue.add(new Pair<>(message, Main.counter + duration));
    }

    /**
     * Updates score and adds kill message to queue
     * @param victim Tank that was killed
     */
    @Override
    public void updateVictim(GameObject victim) {
        super.updateVictim(victim);
        if (victim instanceof Tank) {  // Only add tanks to kill queue
            addMessage("You killed " + victim.username, KILL_MESSAGE_DURATION);
        }
    }

    /**
     * Deletes player from game once killed
     * Displays who killed the player, score, and time alive
     */
    @Override
    public void delete() {
        super.delete();
        levelBar.delete();
        Main.deathScreenFrames = 1;  // Begin death screen
        Main.deathTexture = TextureLoader.getTankTexture(tankBuild.name, fillCol);
        Main.deathBuild = NameGenerator.formatNameCase(tankBuild.name);
        Main.deathScore = String.format("%,d", (int)score);   // Format score with commas
        Main.deathLevel = String.valueOf(level);

        //Time alive
        int seconds = (int) aliveTimer.s();
        int hours = seconds / 3600;
        seconds %= 3600;
        int minutes = seconds / 60;
        seconds %= 60;
        Main.aliveTime = (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + seconds + "s";
    }

    /**
     * Draws upgrade bars on the screen
     */
    public void drawUpgradeBars() {
        if (upgradeOpacity < 0.01f) return;  // If opacity is too low, don't draw
        for (UpgradeBar bar : upgradeBar) {
            if (bar != null) { //If bar is non-null
                bar.draw();
            }
        }
        if (maxStatPoints - usedStatPoints >= 0) {
            //Draw remaining stats
            Graphics.drawTextOutline("x" + (maxStatPoints - usedStatPoints), usagePos, 30, (float) (-Math.PI/6), -2, Graphics.colAlpha(Color.WHITE, upgradeOpacity));
        }
    }
}
