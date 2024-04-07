import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class Main {
    public static Color redCol = Graphics.rgb(255, 93, 86);
    public static Color strokeRed = Graphics.rgb(162, 63, 60);
    public static Color strokeCol = Graphics.rgb(91, 90, 92);
    public static Color greyCol = Graphics.rgb(155, 151, 153);

    public static float strokeWidth = 2.7f;

    public static long counter;
    public static Pool<Drawable> drawablePool;
    public static Pool<Updatable> updatablePool;
    public static IdServer idServer;

    public static String environment = "development";

    public static Stopwatch globalClock = new Stopwatch();

    static TestTwin player;

    // Called in GamePanel.java to initialize game
    public static void initialize() {
        Graphics.initialize("DiepJava");

        // Game initialization
        globalClock.start();
        drawablePool = new Pool<>();
        updatablePool = new Pool<>();
        idServer = new IdServer();
        // new TestObj();
        player = new TestTwin();
        new Square();
        counter = 0;
    }

    private static void updateCamera() {
        counter++;
        Graphics.setCameraTarget(new Vector2((float) player.x, (float) player.y));
        if (Graphics.isKeyDown(Keyboard.KEY_DOWN)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() - 0.02f);
        }
        if (Graphics.isKeyDown(Keyboard.KEY_UP)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() + 0.02f);
        }
    }

    private static void draw() {

        // Draw circle at mouse pos
        Graphics.drawRectangle(Graphics.getVirtualMouse().x, Graphics.getVirtualMouse().y, 5, 5, Color.WHITE);
        // Draw fps
        Graphics.drawFPS(10, 10, 20, Color.BLACK);

        // Number of objects
        Graphics.drawText("Number of objects: " + drawablePool.getObjects().size(), 10, 20, 20, Color.WHITE);

        // Draw all the drawable objects
        for (Drawable drawable : Main.drawablePool.getObjects()) {
            drawable.draw();
        }
    }

    // Clamp Vector2 value with min and max and return a new vector2
    // NOTE: Required for virtual mouse, to clamp inside virtual game size
    static Vector2 ClampValue(Vector2 value, Vector2 min, Vector2 max) {
        value.x = Math.min(value.x, max.x);
        value.x = Math.max(value.x, min.x);
        value.y = Math.min(value.y, max.y);
        value.y = Math.max(value.y, min.y);
        return value;
    }
    private static void update() {
        // Handle the pending operations
        Main.drawablePool.refresh();
        Main.updatablePool.refresh();

        // Update all the updatable objects
        for (Updatable updatable : Main.updatablePool.getObjects()) {
            updatable.update();
        }

        updateCamera();
    }

    final private static float GRID_SIZE = 30;
    private static void drawCamera() {
        Rectangle cameraBounds = Graphics.getCameraBounds();
        Graphics.drawRectangleLines(cameraBounds, 3, Color.RED);
    }
    private static void drawGrid() {
        Color gridCol = Graphics.rgb(195, 203, 205);
        float zoom = Graphics.getCameraZoom();
        Rectangle cameraBounds = Graphics.getCameraBounds();

        float firstX = GRID_SIZE - (player.x - Graphics.cameraWidth/2.f) % GRID_SIZE;  // Modulo position of first x grid
        for (float xi = firstX; xi < Graphics.cameraWidth; xi += GRID_SIZE * zoom) {
            Graphics.drawLine(xi, 0, xi, Graphics.cameraHeight, 1, gridCol);
        }

        float firstY = GRID_SIZE - cameraBounds.y % GRID_SIZE;  // Modulo position of first y grid
        for (float yi = firstY; yi < Graphics.cameraHeight; yi += GRID_SIZE * zoom) {
            Graphics.drawLine(0, yi, Graphics.cameraWidth, yi, 1, gridCol);
        }
    }

    public static void main(String[] args) {
        initialize();
        //--------------------------------------------------------------------------------------
        // Main game loop
        while (!Graphics.shouldWindowClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Compute required framebuffer scaling
            Graphics.updateMouse();
            update();

            // Draw
            //----------------------------------------------------------------------------------
            Graphics.beginDrawMode();
            Graphics.drawBackground(Graphics.rgb(215, 215, 215));
            drawGrid();
            Graphics.beginCameraMode();
            drawCamera();
            draw();  // Main draw function
            Graphics.endCameraMode();
            Graphics.endDrawMode();
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        Graphics.close();
    }
}