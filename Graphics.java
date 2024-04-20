import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.camera.Camera2D;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.shapes.rShapes;
import com.raylib.java.textures.Image;
import com.raylib.java.textures.RenderTexture;
import com.raylib.java.textures.Texture2D;
import com.raylib.java.textures.rTextures;

import java.awt.*;
import java.io.*;

import static com.raylib.java.core.input.Mouse.MouseButton.*;

public class Graphics extends Raylib {
    public static int ANTIALIASING = 1, PERFORMANCE_MODE = 0;  // Constant environment variables
    public static float strokeWidth = 7f;
    public static int FPS = 60 * (2 - PERFORMANCE_MODE);
    final static int TASKBAR_HEIGHT = 48, TITLEBAR_HEIGHT = 32;
    final public static int cameraWidth = 1920;
    final public static int cameraHeight = 1080;
    public static int screenWidth, screenHeight;
    private static float screenScale;  // Scale of render texture to screen

    public static Raylib rlj;
    public static Camera2D camera;
    private static RenderTexture target;

    private static Vector2 mouse = new Vector2(), virtualMouse = new Vector2();

    // Custom textures
    public static Texture2D whiteCirc, whiteRect, whiteCircNoAA, whiteRectRounder;

    // Colors
    public static Color RED = rgb(241, 78, 84),
            RED_STROKE = rgb(180, 58, 63),
            BLUE = rgb(0, 178, 225),
            BLUE_STROKE = rgb(0, 133, 168),
            GREY_STROKE = rgb(114, 114, 114),
            GREY = rgb(153, 153, 153),
            GRID = rgb(205, 205, 205),
            GRID_STROKE = rgba(0, 0, 0, 8),
            BOUNDARY = rgba(0, 0, 0, 15),
            HEALTH_BAR = rgb(133, 227, 125),
            HEALTH_BAR_STROKE = rgb(85, 85, 85);

    public static Color getColor(String hexStr) {
        return rlj.textures.GetColor(Integer.parseInt(hexStr, 16));
    }

    public static void initialize(String title) {
        // First get environment setup
        getEnvironmentVariables();
        DisplayMode monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        if (monitor.getRefreshRate() < 120) {
            PERFORMANCE_MODE = 1;
        }

        FPS = 60 * (2 - PERFORMANCE_MODE);
        // Screen dimensions (actual monitor pixels)
        screenWidth = monitor.getWidth();
        screenHeight = monitor.getHeight() - TASKBAR_HEIGHT - TITLEBAR_HEIGHT;

        // Raylib window
        rlj = new Raylib();
        rCore.SetConfigFlags(Config.ConfigFlag.FLAG_MSAA_4X_HINT | Config.ConfigFlag.FLAG_WINDOW_RESIZABLE | Config.ConfigFlag.FLAG_WINDOW_MAXIMIZED);

        rlj.core.InitWindow(screenWidth, screenHeight, title);
        rlj.core.MaximizeWindow();
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
        // this.cameraData.FOV = (.55 * fieldFactor) / Math.pow(1.01, (this.cameraData.values.level - 1) / 2);
        camera.setRotation(0.0f);

        screenScale = Math.min((float) screenWidth / cameraWidth, (float) screenHeight / cameraHeight);

        initializeTextures();
    }

    public static void initializeTextures() {
        whiteCirc = loadTexture("whiteCircle.png");
        whiteCircNoAA = loadTexture("whiteCircle.png");
        whiteRect = loadTexture("whiteRect.png");
        whiteRectRounder = loadTexture("whiteRect2.png");

        if (ANTIALIASING == 1) {
            setTextureAntiAliasing(whiteCirc);
            setTextureAntiAliasing(whiteRect);
            setTextureAntiAliasing(whiteRectRounder);
        }
    }

    private static Texture2D loadTexture(String path) {
        Image img = rTextures.LoadImage(path);
        Texture2D texture = rTextures.LoadTextureFromImage(img);
        rTextures.UnloadImage(img);
        return texture;
    }

    private static void setTextureAntiAliasing(Texture2D texture) {
        rlj.textures.GenTextureMipmaps(texture);
        rTextures.SetTextureFilter(texture, RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR);
    }

    public static void getEnvironmentVariables() {
        try {
            File file = new File(".env");

            if (file.exists()) {  // If file exists, read from it
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line.contains("PERFORMANCE_MODE")) {  // Read performance mode from file if it exists
                        PERFORMANCE_MODE = Integer.parseInt(line.split("=")[1].trim());
                    }
                    if (line.contains("ANTIALIASING")) {  // Read performance mode from file if it exists
                        ANTIALIASING = Integer.parseInt(line.split("=")[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from .env file: " + e.getMessage());
        }
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

        try {
        rlj.core.EndDrawing();
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.out.println("Unhandled input exception (middle click, etc).");
        }
    }

    // Camera -----------------------------------------------------------------------------------------
    public static void setCameraTarget(Vector2 target) {
        camera.setTarget(target);
    }

    public static void shiftCameraTarget(Vector2 shift) {
        camera.setTarget(Raymath.Vector2Add(camera.target, shift));
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

    public static Vector2 getCameraTarget() {
        return camera.target;
    }

    /**
     * Set the zoom of the camera
     * @param fieldFactor Field factor of the turret
     * @param level Level of the player
     */
    public static void setZoom(float fieldFactor, int level) {
        camera.setZoom((float) ((.55f * fieldFactor) / Math.pow(1.01, (level - 1) *0.5f)));
    }

    // Get the camera bounds
    public static Rectangle getCameraWorld() {
        float xLeft = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, 0), Graphics.camera).x;
        float xRight = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(Graphics.cameraWidth, 0), Graphics.camera).x;
        float yTop = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, 0), Graphics.camera).y;
        float yBottom = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, Graphics.cameraHeight), Graphics.camera).y;
        return new Rectangle(xLeft, yTop, xRight - xLeft, yBottom - yTop);
    }

    // IO --------------------------------------------------------------------------------------------
    public static boolean isKeyPressed(int key) {
        return rlj.core.IsKeyPressed(key);
    }

    public static boolean isKeyDown(int key) {
        return rCore.IsKeyDown(key);
    }

    public static boolean isKeyReleased(int key) {
        return rlj.core.IsKeyReleased(key);
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

    public static void drawTextureCentered(Texture2D texture, Vector2 position, float width, float height) {
        float scale = Math.min(width / texture.width, height / texture.height);
        float tWidth = texture.width * scale, tHeight = texture.height * scale;
        rlj.textures.DrawTextureEx(texture, new Vector2(position.x - tWidth * 0.5f, position.y - tHeight * 0.5f), 0, scale, Color.WHITE);
    }

    public static void drawTextureCentered(Texture2D texture, Vector2 position, float width, float height, Color tint) {
        float scale = Math.min(width / texture.width, height / texture.height);
        float tWidth = texture.width * scale, tHeight = texture.height * scale;
        rlj.textures.DrawTextureEx(texture, new Vector2(position.x - tWidth * 0.5f, position.y - tHeight * 0.5f), 0, scale, tint);
    }

    public static void drawRectangle(Rectangle rect, Color color) {
        rlj.shapes.DrawRectangle((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height, color);
    }

    public static void drawRectangle(float x, float y, float width, float height, Color color) {
        rlj.shapes.DrawRectangle((int) x, (int) y, (int) width, (int) height, color);
    }
    public static void drawRectangle(Rectangle rectangle, Vector2 origin, float radians, Color color) {
        rShapes.DrawRectanglePro(rectangle, origin, radians * 180.f / (float) Math.PI, color);
    }

    public static void drawRectangleRounded(float x, float y, float width, float height, float roundness, Color color) {
        rlj.shapes.DrawRectangleRounded(new Rectangle(x, y, width, height), roundness, 5, color);
    }

    public static void drawTurret(float xleft, float ycenter, float length, float height, double radians, float stroke, Color color, Color strokeCol) {
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height), new Vector2(0, height/2.f), (float)radians, strokeCol);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height - 2 * stroke), new Vector2(stroke, (height - 2 * stroke)/2.f), (float)radians, color);
        float aspectRatio = length/height;
        Texture2D rectTexture = (height < 75) ? whiteRectRounder : whiteRect;
        Rectangle srcRect = new Rectangle(rectTexture.width - rectTexture.height * aspectRatio, 0, rectTexture.height * aspectRatio, rectTexture.height);
        rTextures.DrawTexturePro(rectTexture, srcRect, new Rectangle(xleft, ycenter, length, height), new Vector2(0, height/2.f), (float)(radians * 180/Math.PI), Graphics.GREY_STROKE);
        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height - 2 * Graphics.strokeWidth), new Vector2(Graphics.strokeWidth, (height - 2 * Graphics.strokeWidth)/2.f), (float)radians, color);
    }

    public static void drawRectangleLines(Rectangle rect, float stroke, Color color) {
        rlj.shapes.DrawRectangleLinesEx(rect, stroke, color);
    }

    public static void drawCircle(float x, float y, float radius, Color color) {
        rlj.shapes.DrawCircle((int) x, (int) y, radius, color);
    }

    public static void drawCircleTexture(float x, float y, float radius, float stroke, Color color, Color strokeColor) {
        if (ANTIALIASING == 1) {
            Graphics.drawTextureCentered(whiteCirc, new Vector2(x, y), (radius) * 2, (radius) * 2, strokeColor);
            Graphics.drawTextureCentered(whiteCircNoAA, new Vector2(x, y), (radius) * 2 - 2 * Graphics.strokeWidth, (radius) * 2 - 2 * Graphics.strokeWidth, color);
        } else {
            drawCircle(x, y, radius, stroke, color, strokeColor);
        }
    }

    public static void drawCircle(float x, float y, float radius, float stroke, Color color, Color strokeColor) {
        drawCircle(x, y, radius, strokeColor);
        drawCircle(x, y, radius - stroke, color);
    }
    public static void drawLine(float x1, float y1, float x2, float y2, float stroke, Color color) {
        rlj.shapes.DrawLineEx(new Vector2(x1, y1), new Vector2(x2, y2), stroke, color);
    }

    public static void drawText(String text, int x, int y, int fontSize, Color color) {
        rlj.text.DrawText(text, x, y, fontSize, color);
    }

    // Math -------------------------------------------------------------------------------------------
    private static void ClampValue(Vector2 value, Vector2 min, Vector2 max) {
        value.x = Math.min(value.x, max.x);
        value.x = Math.max(value.x, min.x);
        value.y = Math.min(value.y, max.y);
        value.y = Math.max(value.y, min.y);
    }
}
