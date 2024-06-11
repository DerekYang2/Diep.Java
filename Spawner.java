import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.LinkedList;
import java.util.Queue;

public class Spawner {
    public static int count = 0, nestCount = 0, crasherCount = 0, alphaCount = 0;  // Current amount of object
    public static int[] enemyCount = {0, 0, 0, 0}, enemyAmount = {11, 12, 12, 12};  // Team blue (0) has one less tank
    public static int polygonAmount = 0, pentagonNestAmount = 0, crasherAmount = 0;  // Target amount of polygons
    public static Rectangle nestBox, crasherZone;
    private static Queue<SpawnQuery> respawnQueue = new LinkedList<>();

    // Static polygon spawning methods ---------------------------------------------------------------------------------
    public static void addRespawn(String username, int group, int level) {
        respawnQueue.add(new SpawnQuery(username, group, level));
    }

    public static void spawnEnemiesInitial() {
        switch (GameModeManager.getMode()) {
            case FFA -> {
                int spawnAmount = getSpawnAmount();
                for (int i = 0; i < spawnAmount; i++)
                    spawnRandomEnemy(-1);  // FFA team does not matter
            }
            case TWO_TEAM -> {
                int spawnAmount = getSpawnAmount();
                for (int i = 1; i <= spawnAmount; i++) {
                    spawnRandomEnemy(i % 2);
                }
            }
            case SOLO -> {
                int spawnAmount = getSpawnAmount();
                for (int i = 0; i < spawnAmount; i++)
                    spawnRandomEnemy(1);  // Spawn on team red (while player is on team blue)
            }
            default -> {
                for (int team = 0; team < enemyAmount.length; team++) {
                    while (enemyCount[team] < enemyAmount[team]) {
                        spawnRandomEnemy(team);
                    }
                }
            }
        }
/*            if (GameModeManager.getMode() == GameMode.TWO_TEAM) {
                int spawnAmount = getSpawnAmount();
                for (int i = 0; i < spawnAmount; i++) {
                    spawnRandomEnemy(i % 2);
                }
            } else if ()
            else {
                for (int team = 0; team < enemyAmount.length; team++) {
                    while (enemyCount[team] < enemyAmount[team]) {
                        spawnRandomEnemy(team);
                    }
                }
            }*/
    }

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
        // Handle respawn queue
        while (!respawnQueue.isEmpty()) {
            spawnEnemy(respawnQueue.poll());
        }
    }

    public static void reset() {
        respawnQueue.clear();
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

    public static Color teamFillCol(int group) {
        if (GameModeManager.getMode() == GameMode.FFA) {  // All tanks are red in FFA
            if (Main.player != null && group == Main.player.group) {
                return Graphics.BLUE;
            } else {
                return Graphics.RED;
            }
        }
        switch (group) {
            case 0 -> {
                return Graphics.BLUE;
            }
            case 1 -> {
                return Graphics.RED;
            }
            case 2 -> {
                return Graphics.GREEN;
            }
            default -> {
                return Graphics.PURPLE;
            }
        }
    }

    public static Color teamStrokeCol(int group) {
        if (GameModeManager.getMode() == GameMode.FFA) {  // All tanks are red in FFA
            if (Main.player != null && group == Main.player.group) {
                return Graphics.BLUE_STROKE;
            } else {
                return Graphics.RED_STROKE;
            }
        }
        switch (group) {
            case 0 -> {
                return Graphics.BLUE_STROKE;
            }
            case 1 -> {
                return Graphics.RED_STROKE;
            }
            case 2 -> {
                return Graphics.GREEN_STROKE;
            }
            default -> {
                return Graphics.PURPLE_STROKE;
            }
        }
    }

    public static Tank spawnRandomEnemy(int team) {
        Color fillCol = teamFillCol(team), strokeCol = teamStrokeCol(team);

        // Generate position outside of crasher zone
        Vector2 randPos;
        do {
            randPos = new Vector2(Graphics.randf(0, Main.arenaWidth), Graphics.randf(0, Main.arenaHeight));
        } while (Graphics.isIntersecting(randPos, Spawner.crasherZone));

        int spawnLevel = GameModeManager.getMode() == GameMode.MENU ? Graphics.randInt(1, 44) : 1;  // Random level between 1 and 44 for menu game
        Tank t = new EnemyTank(randPos, "tank", fillCol, strokeCol, spawnLevel);
        
        if (GameModeManager.getMode() != GameMode.FFA) {  // Set team for team modes
            t.group = team;
            enemyCount[team]++;
        }
        
        return t;
    }

    private static Tank spawnEnemy(SpawnQuery query) {
        Vector2 randPos;
        do {
            randPos = new Vector2(Graphics.randf(0, Main.arenaWidth), Graphics.randf(0, Main.arenaHeight));
        } while (Graphics.isIntersecting(randPos, Spawner.crasherZone));

        Color fillCol = teamFillCol(query.group), strokeCol = teamStrokeCol(query.group);

        Tank t = new EnemyTank(randPos, "tank", fillCol, strokeCol, query.level);
        t.username = query.username;
        
        if (GameModeManager.getMode() != GameMode.FFA) {  // Set team for team modes
            t.group = query.group;
            enemyCount[query.group]++;
        }
        return t;
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

    public static int getSpawnAmount() {
        switch (GameModeManager.getMode()) {
            case FFA -> {
                return 49;
            }
            case TWO_TEAM -> {
                return 39;
            }
            case SOLO -> {
                return 30;
            }
            default -> {
                int sum = 0;
                for (int a : enemyAmount) {
                    sum += a;
                }
                return sum;
            }
        }
    }

    private static class SpawnQuery {
        public String username;
        public int group;
        public int level;
        public SpawnQuery(String username, int group, int level) {
            this.username = username;
            this.group = group;
            this.level = level;
        }
    }
}
