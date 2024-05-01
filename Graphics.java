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
    public static int ANTIALIASING = 0, PERFORMANCE_MODE = 0;  // Constant environment variables
    public static float strokeWidth = 7f;
    public static int FPS = 60 * (2 - PERFORMANCE_MODE);
    final public static int cameraWidth = 1920;
    final public static int cameraHeight = 1080;
    public static int screenWidth, screenHeight;
    private static float screenScale;  // Scale of render texture to screen

    public static Raylib rlj;
    public static Camera2D camera;
    private static RenderTexture target;

    private static Vector2 mouse = new Vector2(), virtualMouse = new Vector2();

    // Custom textures
    public static Texture2D circle, sharpRectangle, circleNoAA, roundedRectangle, roundedTrapezoid, trapezoidNoAA, roundedTriangle, sharpTriangle, trapperHead, innerTrapperHead, roundedTrap, sharpTrap, sharpRoundTriangle, roundHexagon;

    // Colors
    public static Color RED = rgb(241, 78, 84),
            RED_STROKE = rgb(180, 58, 63),
            BLUE = rgb(0, 178, 225),
            BLUE_STROKE = rgb(0, 133, 168),
            GREY_STROKE = rgb(114, 114, 114),
            GREY = rgb(153, 153, 153),
            GRID = rgb(205, 205, 205),
            GRID_STROKE = rgba(0, 0, 0, 6),
            BOUNDARY = rgba(0, 0, 0, 15),
            HEALTH_BAR = rgb(133, 227, 125),
            HEALTH_BAR_STROKE = rgb(85, 85, 85),
            DARK_GREY = rgb(85, 85, 85),
            DARK_GREY_STROKE = rgb(63, 63, 63);


    public static Color getColor(String hexStr) {
        return rlj.textures.GetColor(Integer.parseInt(hexStr, 16));
    }

    public static void initialize(String title) {
        // First get environment setup
        getEnvironmentVariables();

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode monitor = gd.getDisplayMode();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        if (monitor.getRefreshRate() < 120) {
            PERFORMANCE_MODE = 1;
        }

        FPS = 60 * (2 - PERFORMANCE_MODE);
        // Screen dimensions (actual monitor pixels)
        screenWidth = monitor.getWidth() - insets.left - insets.right;
        screenHeight = monitor.getHeight() - insets.top - insets.bottom;

        // Raylib window
        rlj = new Raylib();
        rCore.SetConfigFlags(Config.ConfigFlag.FLAG_MSAA_4X_HINT | Config.ConfigFlag.FLAG_WINDOW_RESIZABLE | Config.ConfigFlag.FLAG_WINDOW_MAXIMIZED);
        rlj.core.SetExitKey(0);  // Disable exit key (not working in raylib java?)

        rlj.core.InitWindow(screenWidth, screenHeight, title);
        rlj.core.MaximizeWindow();
        rlj.core.SetWindowMinSize(320, 240);
        // rlj.core.SetWindowPosition(0, 0);

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
        circle = loadTexture("assets/Circle.png");
        circleNoAA = loadTexture("assets/Circle.png");
        sharpRectangle = loadTexture("assets/SharpRectangle.png");
        roundedRectangle = loadTexture("assets/RoundedRectangle.png");
        roundedTrapezoid = loadTexture("assets/RoundedTrapezoid.png");
        trapezoidNoAA = loadTexture("assets/RoundedTrapezoid.png");
        roundedTriangle = loadTexture("assets/RoundedTriangle.png");
        sharpTriangle = loadTexture("assets/SharpTriangle.png");
        trapperHead = loadTexture("assets/TrapperHead.png");
        innerTrapperHead = loadTexture("assets/InnerTrapperHead.png");
        sharpTrap = loadTexture("assets/SharpTrap.png");
        roundedTrap = loadTexture("assets/RoundedTrap.png");
        sharpRoundTriangle = loadTexture("assets/SharpRoundTriangle.png");
        roundHexagon = loadTexture("assets/RoundHexagon.png");

        if (ANTIALIASING == 1) {
            setTextureAntiAliasing(circle);
            //setTextureAntiAliasing(sharpRectangle);
            setTextureAntiAliasing(roundedRectangle);
            setTextureAntiAliasing(roundedTrapezoid);
            setTextureAntiAliasing(roundedTriangle);
            //setTextureAntiAliasing(sharpTriangle);
            setTextureAntiAliasing(trapperHead);
            // setTextureAntiAliasing(innerTrapperHead);
            // setTextureAntiAliasing(sharpTrap);
            setTextureAntiAliasing(roundedTrap);
            setTextureAntiAliasing(sharpRoundTriangle);
            setTextureAntiAliasing(roundHexagon);
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
        rlj.textures.SetTextureWrap(texture, RLGL.RL_TEXTURE_WRAP_CLAMP);
    }

    public static void getEnvironmentVariables() {
        try {
            File file = new File("config/.env");

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
        return camera.getZoom();
    }

    public static Vector2 getCameraTarget() {
        return camera.target;
    }

    /**
     * Set the zoom of the camera
     *
     * @param fieldFactor Field factor of the turret
     * @param level       Level of the player
     */
    public static void setZoom(float fieldFactor, int level) {
        camera.setZoom((float) ((.55f * fieldFactor) / Math.pow(1.01, (level - 1) * 0.5f)));
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

    // Get color with brightness correction, brightness factor goes from -1.0f to 1.0f
    public static Color lerpColor(Color color1, Color color2, float factor) {
        return new Color((int) (color1.r + (color2.r - color1.r) * factor),
                (int) (color1.g + (color2.g - color1.g) * factor),
                (int) (color1.b + (color2.b - color1.b) * factor),
                (int) (color1.a + (color2.a - color1.a) * factor));
    }

    public static Color colAlpha(Color color, float opacity) {
        //return lerpColor(color, GRID, 1-opacity);
        return rTextures.Fade(color, opacity);
    }

    public static Color lerpColorGrid(Color color, float opacity) {
        return lerpColor(color, GRID, 1 - opacity);
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
        rlj.shapes.DrawRectangleRounded(new Rectangle(x, y, width, height), roundness, 7, color);
    }

    // https://www.desmos.com/calculator/odkmazouws
    private static float shiftFactor(float radius) {
        double a = 0.000155254, b = 0.000221842, c = 0.800486;
        double u = 0.01, v = 0.07;
        return (float) (a * radius * radius + b * radius + c);  // Quadratic
    }


    public static void drawTriangleRounded(Vector2 centerPos, float radius, float radians, float strokeWidth, Color color, Color strokeCol) {
        // Height of triangle is 3/2 * diameter or 3 * radius
        if (radius > 40) {
            drawTriangleRounded2(centerPos, radius, radians, strokeWidth, color, strokeCol);
            return;
        }
        strokeWidth *= 1.12f;
        float height = 3 * radius;  // Height of the triangle
        float sideLen = (float) (2.0 / Math.sqrt(3) * height);  // Width of the triangle
        // Note on texture, texture height is sideLen and texture width is height (since sideways is 0 radians)
        rTextures.DrawTexturePro(roundedTriangle, new Rectangle(0, 0, roundedTriangle.width, roundedTriangle.height), new Rectangle(centerPos.x, centerPos.y, height, sideLen), new Vector2(height / 3 * 1.07f, sideLen / 2.f), (float) (radians * 180 / Math.PI), strokeCol);
        rTextures.DrawTexturePro(sharpTriangle, new Rectangle(0, 0, sharpTriangle.width, sharpTriangle.height), new Rectangle(centerPos.x, centerPos.y, height - 2 * strokeWidth, sideLen - 2 * strokeWidth), new Vector2(height / 3 * 1.07f - strokeWidth * shiftFactor(radius), (sideLen - 2 * strokeWidth) * 0.5f), (float) (radians * 180 / Math.PI), color);
    }


    private static void drawTriangleRounded2(Vector2 centerPos, float radius, float radians, float strokeWidth, Color color, Color strokeCol) {
        // Height of triangle is 3/2 * diameter or 3 * radius
        float height = 3 * radius;  // Height of the triangle
        float sideLen = (float) (2.0 / Math.sqrt(3) * height);  // Width of the triangle
        rTextures.DrawTexturePro(sharpRoundTriangle, new Rectangle(0, 0, sharpRoundTriangle.width, sharpRoundTriangle.height), new Rectangle(centerPos.x, centerPos.y, height, sideLen), new Vector2(height / 3 * 1.04f, sideLen / 2.f), (float) (radians * 180 / Math.PI), strokeCol);
        float k = (sideLen - 2 * strokeWidth * (float) Math.sqrt(3) / 2) / sideLen;
        float innerSideLen = sideLen * k;
        float innerHeight = height * k;
        rTextures.DrawTexturePro(sharpTriangle, new Rectangle(0, 0, sharpTriangle.width, sharpTriangle.height), new Rectangle(centerPos.x, centerPos.y, innerHeight, innerSideLen), new Vector2(height / 3 * 1.04f - strokeWidth * (float) Math.sqrt(3) / 2, innerSideLen * 0.5f), (float) (radians * 180 / Math.PI), color);
    }


    public static void drawHexagon(Vector2 centerPos, float sideLen, float radians, Color color) {
        float height = (float) (Math.sqrt(3) * sideLen);  // Height of the hexagon
        float width = 2 * sideLen;  // Width of the hexagon

        rTextures.DrawTexturePro(roundHexagon, new Rectangle(0, 0, roundHexagon.width, roundHexagon.height), new Rectangle(centerPos.x, centerPos.y, width, height), new Vector2(width / 2, height / 2), (float) (radians * 180 / Math.PI), color);
    }

    public static void drawTrap(Vector2 centerPos, float radius, float radians, float strokeWidth, Color color, Color strokeCol) {
        strokeWidth *= 1.4f;
        // Height of triangle is 3/2 * diameter or 3 * radius
        float height = 3 * radius;  // Height of the triangle
        float sideLen = (float) (2.0/Math.sqrt(3) * height);  // Width of the triangle
        // Note on texture, texture height is sideLen and texture width is height (since sideways is 0 radians)
        rTextures.DrawTexturePro(roundedTrap, new Rectangle(0, 0, roundedTrap.width, roundedTrap.height), new Rectangle(centerPos.x, centerPos.y, height, sideLen), new Vector2(height/3, sideLen/2.f), (float)(radians * 180/Math.PI), strokeCol);
        rTextures.DrawTexturePro(sharpTrap, new Rectangle(0, 0, sharpTrap.width, sharpTrap.height), new Rectangle(centerPos.x, centerPos.y, height - 2 * strokeWidth, sideLen - 2 * strokeWidth), new Vector2(height/3 - strokeWidth * .705f, (sideLen - 2*strokeWidth) * 0.5f), (float)(radians * 180/Math.PI), color);
    }

    public static void drawTurret(float xleft, float ycenter, float length, float height, double radians, float stroke, Color color, Color strokeCol, float opacity) {
        // Set opacity of colors
        color = colAlpha(color, opacity);
        strokeCol = colAlpha(strokeCol, opacity);
        float aspectRatio = length/height;
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height), new Vector2(0, height/2.f), (float)radians, strokeCol);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height - 2 * stroke), new Vector2(stroke, (height - 2 * stroke)/2.f), (float)radians, color);

        Texture2D rectTexture = (height < 75) ? roundedRectangle : sharpRectangle;
        Rectangle srcRect = new Rectangle(rectTexture.width - rectTexture.height * aspectRatio, 0, rectTexture.height * aspectRatio, rectTexture.height);
        rTextures.DrawTexturePro(rectTexture, srcRect, new Rectangle(xleft, ycenter, length, height), new Vector2(0, height/2.f), (float)(radians * 180/Math.PI), strokeCol);
        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height - 2 * stroke), new Vector2(stroke, (height - 2 * stroke)/2.f), (float)radians, color);
    }
    public static void drawTrapperTurret(float xleft, float ycenter, float length, float height, double radians, float stroke, Color color, Color strokeCol, float opacity) {
        // Set opacity of colors
        color = colAlpha(color, opacity);
        strokeCol = colAlpha(strokeCol, opacity);
        float trapperHeight = height * (3.f/1.81f);  // Longer side of the trapezoid
        float trapperLength = trapperHead.width * (trapperHeight / trapperHead.height);  // Length of the trapper head, maintain texture aspect ratio
        //length -= trapperLength;  // Subtract the length of the trapper head
        float aspectRatio = length/height;

        rTextures.DrawTexturePro(trapperHead, new Rectangle(0, 0, trapperHead.width, trapperHead.height), new Rectangle(xleft, ycenter, trapperLength, trapperHeight), new Vector2(-(length-strokeWidth), trapperHeight/2), (float)(radians * 180/Math.PI), strokeCol);
        float k = (trapperLength - strokeWidth)/trapperLength;
        trapperHeight *= k * 0.97f;
        trapperLength *= k;
        rTextures.DrawTexturePro(innerTrapperHead, new Rectangle(0, 0, innerTrapperHead.width, innerTrapperHead.height), new Rectangle(xleft, ycenter, trapperLength, trapperHeight), new Vector2(-(length-strokeWidth), trapperHeight/2), (float)(radians * 180/Math.PI), color);

        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height), new Vector2(0, height/2.f), (float)radians, strokeCol);
        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, height - 2 * stroke), new Vector2(stroke, (height - 2 * stroke)/2.f), (float)radians, color);
    }


    public static void drawTurretTrapezoid(float xleft, float ycenter, float length, float height, double radians, float stroke, Color color, Color strokeCol, float opacity, boolean isFlipped) {
        stroke *= 1.1f;
        color = colAlpha(color, opacity);
        strokeCol = colAlpha(strokeCol, opacity);

        float textureWidth = roundedTrapezoid.getWidth();
        float textureHeight = roundedTrapezoid.getHeight();

        height *= 2.19f/1.32f;
        rTextures.DrawTexturePro(roundedTrapezoid, new Rectangle(0, 0, (isFlipped?-1:1)*textureWidth, textureHeight), new Rectangle(xleft, ycenter, length, height), new Vector2(0, height/2.f), (float)(radians * 180/Math.PI), strokeCol);
        //rTextures.DrawTexturePro(whiteTrapezoid, new Rectangle(0, 0, (isFlipped?-1:1)*textureWidth, textureHeight), new Rectangle(xleft, ycenter, length - 2*stroke, height - 2 * stroke), new Vector2(-stroke, (height - 2 * stroke)/2.f), (float)(radians * 180/Math.PI), color);

        length *= (length - 2 * stroke)/length;
        height *= (height - 2 * stroke)/height;
        rTextures.DrawTexturePro(trapezoidNoAA, new Rectangle(0, 0, (isFlipped?-1:1)*textureWidth, textureHeight), new Rectangle(xleft, ycenter, length, height), new Vector2(-stroke-1, height/2.f), (float)(radians * 180/Math.PI), color);
    }

    public static void drawRectangleLines(Rectangle rect, float stroke, Color color) {
        rlj.shapes.DrawRectangleLinesEx(rect, stroke, color);
    }

    public static void drawCircle(float x, float y, float radius, Color color, float opacity) {
        rlj.shapes.DrawCircle((int) x, (int) y, radius, colAlpha(color, opacity));
    }
    public static void drawCircle(float x, float y, float radius, float stroke, Color color, Color strokeColor, float opacity) {
        drawCircle(x, y, radius, strokeColor, opacity);
        drawCircle(x, y, radius - stroke, color, opacity);
    }

    public static void drawCircleTexture(float x, float y, float radius, float stroke, Color color, Color strokeColor, float opacity) {
        if (ANTIALIASING == 1) {
            Graphics.drawTextureCentered(circle, new Vector2(x, y), (radius) * 2, (radius) * 2, colAlpha(strokeColor, opacity));
            Graphics.drawTextureCentered(circleNoAA, new Vector2(x, y), (radius) * 2 - 2 * Graphics.strokeWidth, (radius) * 2 - 2 * Graphics.strokeWidth, colAlpha(color, opacity));
        } else {
            // drawCircle(x, y, radius, stroke, lerpColorGrid(color, opacity), lerpColorGrid(strokeColor, opacity), 1);  // For lerp
            drawCircle(x, y, radius, stroke, color, strokeColor, opacity);
        }
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

    public static Vector2 rotatePoint(Vector2 point, Vector2 origin, double radians) {
        float cosTheta = (float) Math.cos(radians);
        float sinTheta = (float) Math.sin(radians);
        return new Vector2(cosTheta * (point.x - origin.x) - sinTheta * (point.y - origin.y) + origin.x, sinTheta * (point.x - origin.x) + cosTheta * (point.y - origin.y) + origin.y);
    }

    /**
     * Is the rotation clockwise from fromRadians to toRadians
     * @param fromRadians The starting angle
     * @param toRadians The ending angle
     * @return True if the rotation is clockwise, false otherwise
     */
    public static boolean isClockwise(double fromRadians, double toRadians)
    {
        // normalize from and to
        fromRadians = normalizeAngle(fromRadians);
        toRadians = normalizeAngle(toRadians);

        if (fromRadians < toRadians) {
            return toRadians - fromRadians >= Math.PI;
        } else {
            return fromRadians - toRadians < Math.PI;
        }
    }

    /**
     * Normalize the angle to be between 0 and 2PI
     * @param angle The angle to normalize
     * @return The normalized angle between 0 and 2PI
     */
    public static double normalizeAngle(double angle)
    {
        if (angle < 0)
            angle += 2 * Math.PI;
        if (angle > 2 * Math.PI)
            angle -= 2 * Math.PI;
        return angle;
    }

    /**
     * Linearly interpolate between two angles
     * @param fromRadians The starting angle
     * @param toRadians The ending angle
     * @param percentage The percentage to interpolate
     * @return The interpolated angle
     */
    public static double angle_lerp(double fromRadians, double toRadians, double percentage) {
        // Normalize from and to
        fromRadians = normalizeAngle(fromRadians);
        toRadians = normalizeAngle(toRadians);

        if (isClockwise(fromRadians, toRadians)) {
            if (fromRadians < toRadians)
                toRadians -= 2 * Math.PI;
        } else {
            if (fromRadians > toRadians)
                toRadians += 2 * Math.PI;
        }

        // Return normalized angle
        return normalizeAngle(fromRadians + (toRadians - fromRadians) * percentage);
    }

    public static float randf(double min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static float distance(Vector2 vec1, Vector2 vec2) {
        return Raymath.Vector2Distance(vec1, vec2);
    }

    public static float distanceSq(Vector2 vec1, Vector2 vec2) {
        return (vec1.x - vec2.x) * (vec1.x - vec2.x)  + (vec1.y - vec2.y) * (vec1.y - vec2.y);
    }

    /**
     * Is angle between the angles start and end (going ccw)
     * @param angle The angle to check
     * @param start The starting angle
     * @param end The ending angle after going ccw
     * @return True if the angle is between start and end, false otherwise
     */
    public static boolean isAngleBetween(double angle, double start, double end) {
        // Normalize angles
        angle = normalizeAngle(angle);
        start = normalizeAngle(start);
        end = normalizeAngle(end);

        if (end >= start) {
            return start <= angle && angle <= end;
        } else {
            return (start <= angle && angle <= 2*Math.PI) || (0 <= angle && angle <= end);
        }
    }

    /**
     * Returns the length (magnitude) of the vector
     * @param vec The vector to get the length of
     * @return The length of the vector
     */
    public static double length(Vector2 vec) {
        return Math.sqrt(vec.x * vec.x + vec.y * vec.y);
    }

    /**
     * Angle between two vectors in Radians
     * @param vec1 The first vector
     * @param vec2 The second vector
     * @return The inner angle between the two vectors in Radians
     */
    public static double angle(Vector2 vec1, Vector2 vec2) {
        return Math.toRadians(Raymath.Vector2Angle(vec1, vec2));
    }

    /**
     * Scale a vector by a factor
     * @param vec The vector to scale
     * @param scale The constant factor to scale by
     * @return The scaled vector
     */
    public static Vector2 scale(Vector2 vec, float scale) {
        return new Vector2(vec.x * scale, vec.y * scale);
    }

}
