import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;

import static com.raylib.java.core.input.Keyboard.KEY_K;

public class Player extends Tank {
    float currentZoom;
    float targetZoom;
    Vector2 cameraTarget;
    Vector2 levelBarPos;
    Bar levelBar, scoreBar;
    Stopwatch levelUpWatch = new Stopwatch();
    final float BAR_WIDTH = 500, BAR_HEIGHT = 25;
    String formattedName;

    public Player(Vector2 spawn, String buildName) {
        super(spawn, new PlayerController(), new Stats(0, 7, 7, 7, 7, 0, 0, 5), 1);

        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        setTankBuild(TankBuild.createTankBuild(buildName));
        TextureLoader.pendingAdd(this);

        cameraTarget = pos;
        currentZoom = targetZoom = getZoom();
        Graphics.setZoom(currentZoom);

        formattedName = NameGenerator.formatNameCase(tankBuild.name);
        initBars();
    }

    public void initBars() {
        levelBarPos = new Vector2(Graphics.cameraWidth/2 - BAR_WIDTH/2, Graphics.cameraHeight - 2.7f * BAR_HEIGHT);

        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        float initialLevelPercentage = (level == ScoreHandler.maxPlayerLevel) ? 1 : (score-levelStartScore)/(levelNextScore-levelStartScore);

        levelBar = new Bar(levelBarPos, BAR_WIDTH, BAR_HEIGHT, 3, Graphics.LEVELBAR, Graphics.DARK_GREY_STROKE, 0.08f, initialLevelPercentage);  // If level max level, prevent division by 0 -> infinity
        levelBar.setText("Lvl " + level + " " + formattedName, 21);
        levelUpWatch.start();

        final float scoreBarWidth = BAR_WIDTH*2/3, scoreBarHeight = BAR_HEIGHT*0.8f;
        scoreBar = new Bar(new Vector2((Graphics.cameraWidth - scoreBarWidth) * 0.5f, levelBarPos.y - scoreBarHeight - 3), scoreBarWidth, scoreBarHeight, 2, Graphics.SCORE_GREEN, Graphics.DARK_GREY_STROKE, 0.08f, 0);
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
            levelBar.setText("Lvl " + level + " " + formattedName, 21);
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
    }

/*    public void draw() {
        super.draw();
        if (!isDead && Main.onScreen(pos, radius*scale)) {
            float scoreFont = 20 / Graphics.getCameraZoom();
            float yPos = (pos.y - radius * scale);
            Graphics.drawTextCenteredOutline(String.format("%.1fk", score/1000), (int) pos.x, (int) (yPos - scoreFont * 0.5f), (int) scoreFont, Color.WHITE);
            yPos -= scoreFont;
            float usernameFont = 25 / Graphics.getCameraZoom();
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.5f), (int) usernameFont, Color.WHITE);
        }
    }*/

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
            scoreBar.setText("Score: " + (int) score, 19);
        }
        scoreBar.update((firstTank == null) ? 0 : score/firstTank.score);
    }

    public void drawLevelBar() {
        levelBar.draw();

        //float reverseZoom = 1.f / Graphics.getCameraZoom();
        //Graphics.drawTextCenteredOutline("Level " + level, Graphics.screenWidth/2 , (int) (Graphics.screenHeight - 25 * 0.5f), 21, Color.WHITE);
        scoreBar.draw();
    }

    public void drawUsername() {
        // Draw username
        Graphics.drawTextCenteredOutlineNoAA(username, Graphics.cameraWidth/2, (int) (levelBarPos.y - 0.8f * BAR_HEIGHT - 3 - 20), 40, Color.WHITE);
    }

    @Override
    public void delete() {
        super.delete();
        levelBar.delete();
        Main.resetGame();
    }
}
