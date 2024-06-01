import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class Spawner {
    public static int count = 0, nestCount = 0, crasherCount = 0;  // Current amount of polygons
    public static int polygonAmount = 0, pentagonNestAmount = 0, crasherAmount = 0;  // Target amount of polygons
    public static Rectangle nestBox, crasherZone;

    // Static polygon spawning methods ---------------------------------------------------------------------------------
    public static void updateSpawn() {
        while (count < polygonAmount) {
            spawnRandomPolygon();
        }
        while (nestCount < pentagonNestAmount) {
            spawnNestPolygon();
        }
        while (crasherCount < crasherAmount) {
            spawnCrasher();
        }
    }

    public static void reset() {
        // Reset nest and crasher zone
        float nestSide = Main.arenaWidth / 6.5f, crasherSide = nestSide * 2f;
        nestBox = new Rectangle(Main.arenaWidth/2 - nestSide/2, Main.arenaHeight/2 - nestSide/2, nestSide, nestSide);
        crasherZone = new Rectangle(Main.arenaWidth/2 - crasherSide/2, Main.arenaHeight/2 - crasherSide/2, crasherSide, crasherSide);

        polygonAmount = (int) (Main.arenaWidth*Main.arenaWidth/(200*Main.GRID_SIZE*Main.GRID_SIZE));
        pentagonNestAmount = (int) (nestSide*nestSide/(200*Main.GRID_SIZE*Main.GRID_SIZE));
        crasherAmount = (int) (pentagonNestAmount * 1.5f);
        System.out.println("Polygon amount: " + polygonAmount + ", Nest amount: " + pentagonNestAmount);
        count = 0;
        nestCount = 0;
        crasherCount = 0;
    }

    public static void spawnRandomPolygon() {
        double rand = Math.random();
        Vector2 pos;

        do {  // Ensure the polygon does not spawn inside the nest
            pos = new Vector2((float) (Math.random() * (Main.arenaWidth)), (float) (Math.random() * (Main.arenaHeight)));
        } while (Graphics.isIntersecting(pos, crasherZone));

        String shape;
        if (rand < 0.04) {
            shape = Polygon.PENTAGON;
        } else if (rand < 0.20) {
            shape = Polygon.TRIANGLE;
        } else {
            shape = Polygon.SQUARE;
        }
        new Polygon(pos, shape, false);
        count++;  // Increment the count of polygons
    }

    public static void spawnNestPolygon() {
        Vector2 randPos;
        boolean isOnPlayer;

        // Ensure nest polygons do not spawn on top of players
        do {
            randPos = new Vector2(Graphics.randf(nestBox.x, nestBox.x + nestBox.width), Graphics.randf(nestBox.y, nestBox.y + nestBox.height));
            if (Leaderboard.tankList == null) {
                isOnPlayer = false;
            } else {
                isOnPlayer = false;
                for (Tank tank : Leaderboard.tankList) {
                    if (Graphics.distance(randPos, tank.pos) < 173 + tank.getRadiusScaled()) {  // Alpha pentagon radius + tank radius
                        isOnPlayer = true;
                        break;
                    }
                }
            }
        } while (isOnPlayer);

        new Polygon(randPos, Math.random() < 0.02 ? Polygon.ALPHA_PENTAGON : Polygon.PENTAGON, true);
        nestCount++;  // Increment the count of nest polygons
    }

    // Spawn between the crasher zone, or rectangle with a rectangular hole (pentagon nest) in the middle
    public static void spawnCrasher() {
        double rand = Math.random();
        float innerShift = nestBox.width * 0.25f;
        Vector2 pos;
        if (rand < 0.25) {
            pos = new Vector2(Graphics.randf(crasherZone.x + innerShift, nestBox.x + innerShift), Graphics.randf(crasherZone.y + innerShift, crasherZone.y + crasherZone.height - innerShift));
        } else if (rand < 0.5){
            pos = new Vector2(Graphics.randf(nestBox.x + nestBox.width - innerShift, crasherZone.x + crasherZone.width - innerShift), Graphics.randf(crasherZone.y + innerShift, crasherZone.y + crasherZone.height - innerShift));
        } else if (rand < 0.75) {
            pos = new Vector2(Graphics.randf(crasherZone.x + innerShift, crasherZone.x + crasherZone.width - innerShift), Graphics.randf(crasherZone.y + innerShift, nestBox.y + innerShift));
        } else {
            pos = new Vector2(Graphics.randf(crasherZone.x + innerShift, crasherZone.x + crasherZone.width - innerShift), Graphics.randf(nestBox.y + nestBox.height - innerShift, crasherZone.y + crasherZone.height - innerShift));
        }
        
        new Crasher(pos, Math.random() < 0.2);
        crasherCount++;  // Increment the count of crashers
    }
}
