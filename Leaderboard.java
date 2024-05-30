import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.text.rText;
import com.raylib.java.textures.Texture2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Leaderboard {
    private static final int LEADERBOARD_SIZE = 10;
    public static HashSet<Integer> tankIds = new HashSet<>();
    public static ArrayList<Tank> tankList = new ArrayList<>();
    private static Bar[] scoreBars = new Bar[LEADERBOARD_SIZE];
    private static Texture2D[] tankBuilds = new Texture2D[LEADERBOARD_SIZE];
    static final float leaderboardGap = 25, leaderboardWidth = 240, leaderboardHeight = 21;
    static final float cornerY = leaderboardHeight;
    static final float cornerX = Graphics.cameraWidth - leaderboardWidth - cornerY * 0.5f;

    // Scoreboard title variables
    static final int titleFontSize = 30;
    static final float titleSpacing = -18f*titleFontSize / Graphics.outlineFont.getBaseSize();
    static final Vector2 titleDimensions = rText.MeasureTextEx(Graphics.outlineFont, "Scoreboard", titleFontSize, titleSpacing);
    static final Vector2 titlePos = new Vector2(cornerX + (leaderboardWidth - titleDimensions.x) * 0.5f, cornerY/2);

    public static void clear() {
        tankIds.clear();
        tankList.clear();
        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            scoreBars[i] = new Bar(leaderboardWidth, leaderboardHeight, 2, Graphics.SCORE_GREEN, Graphics.BAR_GREY, 1f, 0);
        }
    }
    public static void addTank(Tank tank) {
        tankIds.add(tank.getId());
    }
    public static void removeTank(Tank tank) {
        tankIds.remove(tank.getId());
    }


    public static void update() {
        if (Main.counter % Graphics.FPS != 0) return;  // Only update every second
        tankList.clear();  // Clear tankList
        tankIds.forEach(id -> tankList.add((Tank) Main.gameObjectPool.getObj(id)));  // Fill tankList with tanks from tankIds
        tankList.sort((tankA, tankB) -> Float.compare(tankB.score, tankA.score));  // Sort by greatest to least score

        float maxScore = tankList.get(0).score;

        // Clear leaderboard
        Arrays.fill(tankBuilds, null);

        for (int i = 0; i < Math.min(tankIds.size(), LEADERBOARD_SIZE); i++) {
            Tank tank = tankList.get(i);
            tankBuilds[i] = TextureLoader.getIconTexture(tank.tankBuild.name, tank.fillCol);
            scoreBars[i].update(new Vector2(cornerX, (cornerY/2 + titleDimensions.y + 10) + (i - 0.5f) * leaderboardGap), tankList.get(i).score/maxScore);
            scoreBars[i].setText(tank.username + " - " + formatScoreShort(tank.score), 21);

            final int finalI = i;
            scoreBars[i].setCustomDraw((Rectangle rect) -> Graphics.drawTextureCentered(tankBuilds[finalI], new Vector2(rect.x + 11, rect.y + rect.height * 0.5f), 0, 1f, Color.WHITE));
        }
    }

    public static void draw() {
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
        Graphics.drawTextOutline("Scoreboard", titlePos, titleFontSize, titleSpacing, Color.WHITE);
        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            if (tankBuilds[i] != null) {
                scoreBars[i].draw();
            }
        }
    }

    public static Tank getTankRank(int rank) {
        if (rank >= tankList.size() || rank < 0) return null;
        return tankList.get(rank);
    }

    // Utility

    /**
     * Formats a score to the thousands, e.g. 1,100 -> 1.1k
     * @param score The score to format
     * @return The formatted score
     */
    public static String formatScoreShort(float score) {
        if (score < 1000) {
            return String.valueOf((int) score);
        } else {
            return Graphics.round(score / 1000, 1) + "k";
        }
    }
}
