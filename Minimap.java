import com.raylib.java.raymath.Vector2;

public class Minimap {
    static final float miniMapSize = 175;
    static Vector2 miniPos;
    private static final float PADDING = 10;
    public static void update() {
        miniPos = Graphics.scale(Main.player.pos, miniMapSize / Main.arenaWidth);
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
        Graphics.drawTriangle(playerPos, 2.25f * inverseZoom, 1.5f, Main.player.direction, Graphics.colAlpha(Graphics.DARK_GREY_STROKE, 0.75f));
    }
}
