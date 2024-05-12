import com.raylib.java.raymath.Vector2;
import com.raylib.java.textures.Texture2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Leaderboard {
    private static final int LEADERBOARD_SIZE = 10;
    private static HashSet<Integer> tankIds = new HashSet<>();
    private static ArrayList<Tank> tankList = new ArrayList<>();
    private static Bar[] scoreBars = new Bar[LEADERBOARD_SIZE];
    private static Texture2D[] tankBuilds = new Texture2D[LEADERBOARD_SIZE];
    static final float leaderboardGap = 25, leaderboardWidth = 275, leaderboardHeight = 21;
    static final float cornerY = leaderboardHeight;
    static final float cornerX = Graphics.cameraWidth - leaderboardWidth - cornerY * 0.5f;

    public static void clear() {
        tankIds.clear();
        tankList.clear();
        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            scoreBars[i] = new Bar(leaderboardWidth, leaderboardHeight, 2, Graphics.LIGHT_GREEN, Graphics.DARK_GREY_STROKE, 1f, 0);
        }
    }
    public static void addTank(Tank tank) {
        tankIds.add(tank.getId());
    }
    public static void removeTank(Tank tank) {
        tankIds.remove(tank.getId());
    }

    public static void draw() {
        tankList.clear();  // Clear tankList
        tankIds.forEach(id -> tankList.add((Tank) Main.gameObjectPool.getObj(id)));  // Fill tankList with tanks from tankIds
        tankList.sort((tankA, tankB) -> Float.compare(tankB.score, tankA.score));  // Sort by greatest to least score

        float maxScore = tankList.get(0).score;

        // Clear leaderboard
        Arrays.fill(tankBuilds, null);

        for (int i = 0; i < Math.min(tankIds.size(), LEADERBOARD_SIZE); i++) {
            Tank tank = tankList.get(i);
            tankBuilds[i] = TextureLoader.getIconTexture(tank.tankBuild.name, tank.fillCol);
            scoreBars[i].setText(String.format("%s - %.1fk", tank.username, tank.score / 1000), 19);
            scoreBars[i].update(new Vector2(cornerX, cornerY + i * leaderboardGap - 23 * 0.5f), tankList.get(i).score/maxScore);
        }
/*        float reverseZoom = 1.f / Graphics.getCameraZoom();
        Vector2 cornerPos = Graphics.getScreenToWorld2D(new Vector2(1675, leaderboardGap), Graphics.camera);

        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            int cornerX = (int) cornerPos.x;
            int cornerY = (int) (cornerPos.y + i * leaderboardGap * reverseZoom);

            if (leaderboard[i] != null && tankBuilds[i] != null) {
                Graphics.drawTextureCentered(tankBuilds[i], new Vector2(cornerX, cornerY), 0, reverseZoom, Graphics.rgb(255, 255, 255));
                Graphics.drawTextCenteredY(leaderboard[i], (int) (cornerX + 25f * reverseZoom), cornerY, (int) (leaderboardGap * 0.75f * reverseZoom), Graphics.DARK_GREY_STROKE);
            }
        }*/

        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            if (tankBuilds[i] != null) {
                scoreBars[i].draw();
                Graphics.drawTextureCentered(tankBuilds[i], new Vector2(cornerX + 12, cornerY + i * leaderboardGap), 0, 1f, Graphics.rgb(255, 255, 255));
            }
        }
    }

    public static Tank getTankRank(int rank) {
        if (rank >= tankList.size() || rank < 0) return null;
        return tankList.get(rank);
    }
}
