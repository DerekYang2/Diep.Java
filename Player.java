import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;

import static com.raylib.java.core.input.Keyboard.KEY_K;

public class Player extends Tank {
    float currentZoom;
    float targetZoom;
    Vector2 cameraTarget;
    Bar levelBar;
    Stopwatch levelUpWatch = new Stopwatch();
    public Player(Vector2 spawn, String buildName) {
        super(spawn, new PlayerController(), new Stats(7, 7, 7, 7, 7, 0, 3, 5), 20);
        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        setTankBuild(TankBuild.createTankBuild(buildName));
        TextureLoader.pendingAdd(this);

        cameraTarget = pos;
        currentZoom = targetZoom = getZoom();
        Graphics.setZoom(currentZoom);

        levelBar = new Bar(500, 25, 3, Graphics.YELLOW, Graphics.DARK_GREY_STROKE, 0.07f, 0);
        Vector2 levelBarPos = new Vector2(Graphics.screenWidth/2 - levelBar.width/2, Graphics.screenHeight - 25);
        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        levelBar.update(levelBarPos, (level == ScoreHandler.maxPlayerLevel || levelUpWatch.s() < 2) ? 1 : (score-levelStartScore)/(levelNextScore-levelStartScore));
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


    public void drawLevelBar() {

        Vector2 levelBarPos = Graphics.getScreenToWorld2D(new Vector2(Graphics.screenWidth/2 - levelBar.width/2, Graphics.screenHeight - 25), Graphics.camera);
        levelBar.setText("Level " + level, 21);
        float levelStartScore = ScoreHandler.levelToScore(level), levelNextScore = ScoreHandler.levelToScore(level+1);
        if (level == ScoreHandler.maxPlayerLevel && levelUpWatch.ms() >= 1000) {
            levelBar.update(levelBarPos, Graphics.clamp((float) (levelUpWatch.ms() - 1400) /1000, 0, 1));
        } else {
            levelBar.update(levelBarPos, Graphics.clamp(levelUpWatch.ms() < 1000? 1 : (score - levelStartScore) / (levelNextScore - levelStartScore), 0, 1));
        }
        levelBar.draw();
        //float reverseZoom = 1.f / Graphics.getCameraZoom();
        //Graphics.drawTextCenteredOutline("Level " + level, Graphics.screenWidth/2 , (int) (Graphics.screenHeight - 25 * 0.5f), 21, Color.WHITE);
    }

    @Override
    public void delete() {
        super.delete();
        levelBar.delete();
        Main.resetGame();
    }
}
