import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.camera.Camera2D;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.shapes.rShapes;
import com.raylib.java.textures.RenderTexture;
import com.raylib.java.textures.rTextures;

import java.awt.*;

import static com.raylib.java.core.input.Mouse.MouseButton.*;

public class Graphics extends Raylib {
    final static int FPS = 60;
    final static int TASKBAR_HEIGHT = 48, TITLEBAR_HEIGHT = 32;
    final public static int cameraWidth = 1920;
    final public static int cameraHeight = 1080 - TASKBAR_HEIGHT - TITLEBAR_HEIGHT;
    public static int screenWidth, screenHeight;
    private static float screenScale;  // Scale of render texture to screen

    public static Raylib rlj;
    public static Camera2D camera;
    private static RenderTexture target;

    private static Vector2 mouse = new Vector2(), virtualMouse = new Vector2();

    public static void initialize(String title) {
        // Screen dimensions (actual monitor pixels)
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) screenSize.getWidth();
        screenHeight = (int) screenSize.getHeight() - TASKBAR_HEIGHT - TITLEBAR_HEIGHT;

        // Raylib window
        rlj = new Raylib();
        rCore.SetConfigFlags(Config.ConfigFlag.FLAG_MSAA_4X_HINT);
        rlj.core.InitWindow(screenWidth, screenHeight, title);
        rlj.core.SetWindowMinSize(320, 240);
        rlj.core.SetWindowPosition(0, TITLEBAR_HEIGHT);

        // Render texture
        target = rlj.textures.LoadRenderTexture(cameraWidth, cameraHeight);
        rTextures.SetTextureFilter(target.texture, RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR);
        rlj.core.SetTargetFPS(FPS);

        // Camera
        camera = new Camera2D();
        camera.setTarget(new Vector2(0, 0));
        camera.setOffset(new Vector2(cameraWidth / 2.f, cameraHeight / 2.f));
        camera.setZoom(1.f);
        camera.setRotation(0.0f);

        screenScale = Math.min((float) screenWidth / cameraWidth, (float) screenHeight / cameraHeight);
    }

    public static void close() {
        rlj.textures.UnloadRenderTexture(target);
    }

    public static boolean shouldWindowClose() {
        return rlj.core.WindowShouldClose();
    }

    public static void updateMouse() {
        mouse = rCore.GetMousePosition();
        virtualMouse.x = (mouse.x - (screenWidth - (cameraWidth * screenScale)) * 0.5f) / screenScale;
        virtualMouse.y = (mouse.y - (screenHeight - (cameraHeight * screenScale)) * 0.5f) / screenScale;
        ClampValue(virtualMouse, new Vector2(0, 0), new Vector2((float) cameraWidth, (float) cameraHeight));

        virtualMouse = rlj.core.GetScreenToWorld2D(virtualMouse, camera);
    }

    public static void beginDrawMode() {
        rlj.core.BeginDrawing();
        rlj.core.ClearBackground(Color.BLACK);
        rlj.core.BeginTextureMode(target);
    }

    public static void drawBackground(Color color) {
        rlj.core.ClearBackground(color);
    }

    public static void beginCameraMode() {
        rlj.core.BeginMode2D(camera);
    }

    public static void endCameraMode() {
        rlj.core.EndMode2D();
    }

    public static void endDrawMode() {
        rlj.core.EndTextureMode();

        // Draw RenderTexture2D to window, properly scaled
        rTextures.DrawTexturePro(target.texture, new Rectangle(0.0f, 0.0f, (float) target.texture.width, (float) -target.texture.height),
                new Rectangle((screenWidth - ((float) cameraWidth * screenScale)) * 0.5f, (screenHeight - ((float) cameraHeight * screenScale)) * 0.5f,
                        (float) cameraWidth * screenScale, (float) cameraHeight * screenScale), new Vector2(), 0.0f, Color.WHITE);

        rlj.core.EndDrawing();
    }

    public static void setCameraTarget(Vector2 target) {
        camera.setTarget(target);
    }

    public static void setCameraZoom(float zoom) {
        camera.setZoom(zoom);
    }

    public static Rectangle getCameraBounds() {
        return new Rectangle(camera.target.x - camera.offset.x, camera.target.y - camera.offset.y, cameraWidth, cameraHeight);
    }

    public static Vector2 getMouse() {
        return mouse;
    }

    public static Vector2 getVirtualMouse() {
        return virtualMouse;
    }

    public static float getCameraZoom() {
       return  camera.getZoom();
    }

    // IO --------------------------------------------------------------------------------------------
    public static boolean isKeyPressed(int key) {
        return rlj.core.IsKeyPressed(key);
    }

    public static boolean isKeyDown(int key) {
        return rCore.IsKeyDown(key);
    }

    public static boolean isLeftMousePressed() {
        return rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT);
    }

    public static boolean isRightMousePressed() {
        return rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_RIGHT);
    }

    public static boolean isLeftMouseDown() {
        return rCore.IsMouseButtonDown(MOUSE_BUTTON_LEFT);
    }

    public static boolean isRightMouseDown() {
        return rCore.IsMouseButtonDown(MOUSE_BUTTON_RIGHT);
    }

    public static boolean isLeftMouseReleased() {
        return rlj.core.IsMouseButtonReleased(MOUSE_BUTTON_LEFT);
    }

    // Drawing ---------------------------------------------------------------------------------------
    public static Color rgb(int r, int g, int b) {
        return new Color(r, g, b, 255);
    }

    public static Color rgba(int r, int g, int b, int a) {
        return new Color(r, g, b, a);
    }

    public static void drawFPS(int x, int y, int fontSize, Color color) {
        rlj.text.DrawText(rCore.GetFPS() + " FPS", x, y, fontSize, color);
    }

    public static void drawRectangle(Rectangle rect, Color color) {
        rlj.shapes.DrawRectangle((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height, color);
    }

    public static void drawRectangle(float x, float y, float width, float height, Color color) {
        rlj.shapes.DrawRectangle((int) x, (int) y, (int) width, (int) height, color);
    }
    public static void drawRectangle(float x, float y, float width, float height, Vector2 origin, float radians, Color color) {
        rShapes.DrawRectanglePro(new Rectangle(x, y, width, height), origin, radians * 180.f / (float) Math.PI, color);
    }

    public static void drawRectangleLines(float x, float y, float width, float height, float stroke, Color color) {
        rlj.shapes.DrawRectangleLinesEx(new Rectangle(x, y, width, height), stroke, color);
    }

    public static void drawRectangleLines(Rectangle rect, float stroke, Color color) {
        rlj.shapes.DrawRectangleLinesEx(rect, stroke, color);
    }

    public static void drawCircle(float x, float y, float radius, Color color) {
        rlj.shapes.DrawCircle((int) x, (int) y, radius, color);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float stroke, Color color) {
        rlj.shapes.DrawLineEx(new Vector2(x1, y1), new Vector2(x2, y2), stroke, color);
    }

    public static void drawText(String text, int x, int y, int fontSize, Color color) {
        rlj.text.DrawText(text, x, y, fontSize, color);
    }

    private static Vector2 ClampValue(Vector2 value, Vector2 min, Vector2 max) {
        value.x = Math.min(value.x, max.x);
        value.x = Math.max(value.x, min.x);
        value.y = Math.min(value.y, max.y);
        value.y = Math.max(value.y, min.y);
        return value;
    }
}
