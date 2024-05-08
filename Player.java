import com.raylib.java.raymath.Vector2;

import static com.raylib.java.core.input.Keyboard.KEY_K;

public class Player extends Tank {
    public Player(Vector2 spawn, String buildName) {
        super(spawn, new PlayerController(), new Stats(7, 7, 7, 7, 7, 0, 3, 5));
        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        setTankBuild(TankBuild.createTankBuild(buildName));
        Graphics.setZoom(this.tankBuild.fieldFactor, level);  // Set zoom level, remember to call on level and tank build change
        TextureLoader.pendingAdd(this);
    }

    // For timing speed
    Stopwatch debug = new Stopwatch();
    boolean startedRace = false;
    boolean crossedFinish = false;

    @Override
    public void updateLevel() {
        super.updateLevel();
        Graphics.setZoom(this.tankBuild.fieldFactor, level);
    }

    @Override
    public void update() {
        super.update();

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
            score += Math.max(0, Math.min(ScoreHandler.levelToScore(45) - score, 23000.f/(2 * 120)));  // 2 seconds
            score = Math.min(score, ScoreHandler.levelToScore(45));
            System.out.println(score);
        }
    }

    @Override
    public void delete() {
        super.delete();
        Main.resetGame();
    }
}
