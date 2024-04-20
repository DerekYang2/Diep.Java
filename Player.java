import com.raylib.java.raymath.Vector2;

public class Player extends Tank {
    public Player(Vector2 spawn) {
        super(spawn, new PlayerController(), new Stats(7, 7 , 7, 7, 7, 0, 3, 0));
        setColor(Graphics.BLUE, Graphics.BLUE_STROKE);
        setTankBuild(TankBuild.createTankBuild("triplet"));
        Graphics.setZoom(this.tankBuild.fieldFactor, level);  // TODO: this needs to be updated when player is updated (level, etc)
    }

    // For timing speed
    Stopwatch debug = new Stopwatch();
    boolean startedRace = false;
    boolean crossedFinish = false;

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
    }
}
