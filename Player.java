import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.text.rText;

import static com.raylib.java.core.input.Keyboard.KEY_K;

public class Player extends Tank {
    float currentZoom;
    float targetZoom;
    Vector2 cameraTarget;
    Stopwatch levelUpWatch = new Stopwatch();

    // Variables for HUD
    Vector2 levelBarPos;
    Bar levelBar, scoreBar;
    final float BAR_WIDTH = 500, BAR_HEIGHT = 25;
    String formattedBuildName;
    Vector2 usernamePos;
    float usernameSpacing;
    final float usernameFontSize = 40;

    public Player(Vector2 spawn, String buildName) {
        super(spawn, new PlayerController(), new Stats(7, 7, 7, 7, 7, 0, 0, 5), 1);

        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        initTankBuild(TankBuild.createTankBuild(buildName));
        currentZoom = targetZoom;  // Spawn with right zoom level
        cameraTarget = pos;
        Graphics.setZoom(currentZoom);  // Set zoom level
        initBars();

        // Set username variables
        usernameSpacing = -16f*usernameFontSize / Graphics.outlineFont.getBaseSize();
        Vector2 textDimensions = rText.MeasureTextEx(Graphics.outlineFont, username, usernameFontSize, usernameSpacing);
        usernamePos = new Vector2((Graphics.cameraWidth - textDimensions.getX()) * 0.5f, levelBarPos.y - 0.8f * BAR_HEIGHT - 21 - textDimensions.getY() * 0.5f);
    }

    public void initBars() {
        levelBarPos = new Vector2(Graphics.cameraWidth/2 - BAR_WIDTH/2, Graphics.cameraHeight - 2.5f * BAR_HEIGHT);

        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        float initialLevelPercentage = (level == ScoreHandler.maxPlayerLevel) ? 1 : (score-levelStartScore)/(levelNextScore-levelStartScore);
        levelBar = new Bar(levelBarPos, BAR_WIDTH, BAR_HEIGHT, 3, Graphics.LEVELBAR, Graphics.BAR_GREY, 0.08f, initialLevelPercentage);  // If level max level, prevent division by 0 -> infinity
        levelBar.setText("Lvl " + level + " " + formattedBuildName, 21);
        levelUpWatch.start();

        final float scoreBarWidth = BAR_WIDTH*2/3, scoreBarHeight = BAR_HEIGHT*0.8f;
        scoreBar = new Bar(new Vector2((Graphics.cameraWidth - scoreBarWidth) * 0.5f, levelBarPos.y - scoreBarHeight - 3), scoreBarWidth, scoreBarHeight, 2, Graphics.SCORE_GREEN, Graphics.BAR_GREY, 0.08f, 0);
    }

    @Override
    public void initTankBuild(TankBuild tankBuild) {
        super.initTankBuild(tankBuild);
        TextureLoader.pendingAdd(this);
        formattedBuildName = NameGenerator.formatNameCase(tankBuild.name);
        targetZoom = getZoom();
        tankBuild.update();  // In order to have correct position and rotation right away
    }

    public void changeTankBuild(TankBuild tankBuild) {
        this.tankBuild.delete();  // Delete old tank build
        initTankBuild(tankBuild);  // Initialize new tank build
        levelBar.setText("Lvl " + level + " " + formattedBuildName, 21);
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
            levelBar.setText("Lvl " + level + " " + formattedBuildName, 21);
        }
        targetZoom = getZoom();
    }

    protected float getZoom() {
        return (float) ((.55f * this.tankBuild.fieldFactor) / Math.pow(1.01, (level - 1) * 0.5f));
    }
    boolean testFlag = false;
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
        if (Graphics.isKeyPressed(Keyboard.KEY_T)) {  // Test
            changeTankBuild(TankBuild.createTankBuild(testFlag?"battleship":"overlord"));
            testFlag = !testFlag;
        }
    }

/*    public void draw() {
        super.draw();
        if (!isDead && Main.onScreen(pos, radius * scale)) {
            float inverseZoom = 1.f / Graphics.getCameraZoom();
            float scoreFont = 21 * inverseZoom;
            float yPos = (pos.y - radius * scale);
            Graphics.drawTextCenteredOutline(Graphics.round(score / 1000, 1) + "k", (int) pos.x, (int) (yPos - scoreFont * 1.2f * 0.5f), (int) scoreFont, Color.WHITE);
            yPos -= scoreFont;
            float usernameFont = 31 * inverseZoom;
            Graphics.drawTextCenteredOutline(username, (int) pos.x, (int) (yPos - usernameFont * 0.8f * 0.5f), (int) usernameFont, Color.WHITE);
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
        Graphics.drawTextOutline(username, usernamePos, (int) usernameFontSize, usernameSpacing, Color.WHITE);
        //Graphics.drawTextCenteredOutline(username, Graphics.cameraWidth/2, (int) (levelBarPos.y - 0.8f * BAR_HEIGHT - 20), 40, Color.WHITE);
    }

    @Override
    public void delete() {
        super.delete();
        levelBar.delete();
        Main.resetGame();
    }
}
