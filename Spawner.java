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
        float nestSide = Main.arenaWidth / 6, crasherSide = nestSide * 2f;
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

    /**
     * Currently, polygons can even spawn on players, might leave this mechanic, adds additional risk to the nest.
     */
    public static void spawnNestPolygon() {
        Vector2 pos = new Vector2(Graphics.randf(nestBox.x, nestBox.x + nestBox.width), Graphics.randf(nestBox.y, nestBox.y + nestBox.height));
        new Polygon(pos, Math.random() < 0.02 ? Polygon.ALPHA_PENTAGON : Polygon.PENTAGON, true);
        nestCount++;  // Increment the count of nest polygons
    }

    // Spawn between the crasher zone, or rectangle with a rectangular hole (pentagon nest) in the middle
    public static void spawnCrasher() {
        boolean leftSide = Math.random() < 0.5, topSide = Math.random() < 0.5;
        float innerShift = nestBox.x / 2;
        float x, y;
        if (leftSide) {
            x = Graphics.randf(crasherZone.x + innerShift, nestBox.x + innerShift);
        } else {
            x = Graphics.randf(nestBox.x + nestBox.width - innerShift, crasherZone.x + crasherZone.width - innerShift);
        }
        if (topSide) {
            y = Graphics.randf(crasherZone.y + innerShift, nestBox.y + innerShift);
        } else {
            y = Graphics.randf(nestBox.y + nestBox.height - innerShift, crasherZone.y + crasherZone.height - innerShift);
        }

        new Crasher(new Vector2(x, y), Math.random() < 0.2);
        crasherCount++;  // Increment the count of crashers
    }
}
