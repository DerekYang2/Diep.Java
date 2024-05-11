import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.textures.Texture2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Leaderboard {
    private static final int LEADERBOARD_SIZE = 10;
    static HashSet<Integer> tankIds = new HashSet<>();
    private static String[] leaderboard = new String[LEADERBOARD_SIZE];
    private static Texture2D[] tankBuilds = new Texture2D[LEADERBOARD_SIZE];
    public static void clear() {
        tankIds.clear();
    }
    public static void addTank(Tank tank) {
        tankIds.add(tank.getId());
    }
    public static void removeTank(Tank tank) {
        tankIds.remove(tank.getId());
    }

    public static void draw() {
        if (Main.counter % 10 == 0) {
            ArrayList<Tank> tankList = new ArrayList<>();
            tankIds.forEach(id -> tankList.add((Tank) Main.gameObjectPool.getObj(id)));
            tankList.sort((tankA, tankB) -> Float.compare(tankB.score, tankA.score));

            // Clear leaderboard
            Arrays.fill(leaderboard, "");
            Arrays.fill(tankBuilds, null);

            for (int i = 0; i < Math.min(tankIds.size(), LEADERBOARD_SIZE); i++) {
                Tank tank = tankList.get(i);
                tankBuilds[i] = TextureLoader.getTankTexture(tank.tankBuild.name, tank.fillCol);
                leaderboard[i] = String.format("%s : %.1fk", tank.tankBuild.name, tank.score/1000);
            }
        }

        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            if (leaderboard[i] != null && tankBuilds[i] != null) {
                Graphics.drawTextureCentered(tankBuilds[i], new Vector2(1675, 20 + i * 20), 0, 0.1f, Color.WHITE);
                Graphics.drawText(leaderboard[i], 1700, 10 + i * 20, 20, Color.BLACK);
            }
        }
    }
}
