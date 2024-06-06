import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class Spawner {
    public static int count = 0, nestCount = 0, crasherCount = 0, alphaCount = 0;  // Current amount of object
    public static int[] enemyCount = {0, 0, 0, 0}, enemyAmount = {12, 12, 12, 12};
    public static int polygonAmount = 0, pentagonNestAmount = 0, crasherAmount = 0;  // Target amount of polygons
    public static Rectangle nestBox, crasherZone;

    // Static polygon spawning methods ---------------------------------------------------------------------------------
    public static void updateSpawn() {
        for (int team = 0; team < enemyAmount.length; team++) {
            while (enemyCount[team] < enemyAmount[team]) {
                spawnRandomEnemy(team);
            }
        }
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
        float nestSide = Main.arenaWidth / 6.3f, crasherSide = nestSide * 2f;
        nestBox = new Rectangle(Main.arenaWidth/2 - nestSide/2, Main.arenaHeight/2 - nestSide/2, nestSide, nestSide);
        crasherZone = new Rectangle(Main.arenaWidth/2 - crasherSide/2, Main.arenaHeight/2 - crasherSide/2, crasherSide, crasherSide);

        polygonAmount = (int) (Main.arenaWidth*Main.arenaWidth/(180*Main.GRID_SIZE*Main.GRID_SIZE));
        pentagonNestAmount = (int) (nestSide*nestSide/(220*Main.GRID_SIZE*Main.GRID_SIZE));
        crasherAmount = pentagonNestAmount;
        System.out.println("Polygon amount: " + polygonAmount + ", Nest amount: " + pentagonNestAmount);
        count = 0;
        nestCount = 0;
        crasherCount = 0;
        alphaCount = 0;
        for (int i = 0; i < enemyAmount.length; i++) {
            enemyCount[i] = 0;
        }
    }

    public static int getTeam(int group) {
        if (group == Main.player.group) return 0;
        return -group;
    }

    public static int getGroup(int team) {
        if (team == 0) return Main.player.group;
        return -team;
    }

    public static void spawnRandomEnemy(int team) {
        Color fillCol, strokeCol;

        switch (team) {
            case 0 -> {
                fillCol = Graphics.BLUE;
                strokeCol = Graphics.BLUE_STROKE;
            }
            case 1 -> {
                fillCol = Graphics.RED;
                strokeCol = Graphics.RED_STROKE;
            }
            case 2 -> {
                fillCol = Graphics.GREEN;
                strokeCol = Graphics.GREEN_STROKE;
            }
            default -> {
                fillCol = Graphics.PURPLE;
                strokeCol = Graphics.PURPLE_STROKE;
            }
        }

        // Generate position outside of crasher zone
        Vector2 randPos;
        do {
            randPos = new Vector2(Graphics.randf(0, Main.arenaWidth), Graphics.randf(0, Main.arenaHeight));
        } while (Graphics.isIntersecting(randPos, Spawner.crasherZone));

        Tank t = new EnemyTank(randPos, "tank", fillCol, strokeCol);
        t.group = getGroup(team);
        enemyCount[team]++;
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
        boolean isAlpha = Math.random() < 0.01;
        if (alphaCount >= 5) isAlpha = false;  // Limit the amount of alpha pentagons to 5
        new Polygon(randPos, isAlpha ? Polygon.ALPHA_PENTAGON : Polygon.PENTAGON, true);
        nestCount++;  // Increment the count of nest polygons
        if (isAlpha) alphaCount++;  // Increment the count of alpha pentagons
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
