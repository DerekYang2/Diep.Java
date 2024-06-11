import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Minimap {
    static final float miniMapSize = 175;
    static Vector2 miniPos;
    static float miniDir;
    private static final float PADDING = 10;
    public static void update() {
        if (Main.cameraHost != null) {
            miniPos = Graphics.scale(Main.cameraHost.pos, miniMapSize / Main.arenaWidth);
            if (Main.cameraHost instanceof Tank) {
                miniDir = ((Tank) Main.cameraHost).direction;
            }
        }
    }

    public static void draw() {
        float inverseZoom = 1.f / Graphics.getCameraZoom();
        float strokeWidth = 5;
        float miniX = Graphics.cameraWidth - miniMapSize - strokeWidth - PADDING, miniY = Graphics.cameraHeight - strokeWidth - miniMapSize - PADDING;
        // Convert to game world coordinates
        Vector2 cornerPos = Graphics.getScreenToWorld2D(new Vector2(miniX, miniY), Graphics.camera);

        // Draw the minimap
        Graphics.drawRectangleRoundedLines(cornerPos.x, cornerPos.y, miniMapSize * inverseZoom, miniMapSize * inverseZoom, 0.001f, 3, strokeWidth * inverseZoom,  Graphics.colAlpha(Graphics.GREY_STROKE, 0.8f));
        Graphics.drawRectangleRounded(cornerPos.x, cornerPos.y, miniMapSize * inverseZoom, miniMapSize * inverseZoom, 0.05f, 3, Graphics.colAlpha(Graphics.GRID, 0.75f));

        float playerX = miniX + miniPos.x, playerY = miniY + miniPos.y;
        Vector2 playerPos = Graphics.getScreenToWorld2D(new Vector2(playerX, playerY), Graphics.camera);
        Graphics.drawTriangle(playerPos, 2.25f * inverseZoom, 1.5f, miniDir, Graphics.colAlpha(Graphics.DARK_GREY_STROKE, 0.75f));

        cornerPos.y -= 25 * inverseZoom;
        Graphics.drawTextOutline(Leaderboard.tankList.size() + " Players", cornerPos, (int) (20 * inverseZoom), -4 * inverseZoom, Graphics.colAlpha(Color.WHITE, 0.75f));
    }
}
