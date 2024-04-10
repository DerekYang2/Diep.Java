import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class Main {
    public static long counter;
    public static Pool<Drawable> drawablePool;
    public static Pool<Updatable> updatablePool;
    public static Pool<GameObject> gameObjectPool;

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
        gameObjectPool = new Pool<>();
        idServer = new IdServer();
        // new TestObj();
        player = new TestTwin();
        counter = 0;
    }

    private static void updateCamera() {
        counter++;
        Graphics.setCameraTarget(new Vector2((float) player.x, (float) player.y));
        if (Graphics.isKeyDown(Keyboard.KEY_DOWN)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() - 0.005f);
        }
        if (Graphics.isKeyDown(Keyboard.KEY_UP)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() + 0.005f);
        }

    }

    private static void draw() {

        // Draw circle at mouse pos
        Graphics.drawRectangle(Graphics.getVirtualMouse().x, Graphics.getVirtualMouse().y, 5, 5, Color.WHITE);
        // Draw fps
        Graphics.drawFPS(10, 10, 20, Color.BLACK);

        // Number of objects
        Graphics.drawText("Number of objects: " + drawablePool.getObjects().size(), 10, 25, 20, Color.WHITE);

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
        Main.gameObjectPool.refresh();
        updateCamera();

        // Update all the updatable objects
        for (Updatable updatable : Main.updatablePool.getObjects()) {
            updatable.update();
        }

        // Collide all the game objects
        for (GameObject gameObject : Main.gameObjectPool.getObjects()) {
            for (GameObject other : Main.gameObjectPool.getObjects()) {
                if (gameObject != other) {
                    if (gameObject.checkCollision(other)) {
                        gameObject.receiveKnockback(other);
                    }
                }
            }
        }
    }

    final private static float GRID_SIZE = 50;
    private static void drawCamera() {
        Rectangle cameraBounds = Graphics.getCameraBounds();
        Graphics.drawRectangleLines(cameraBounds, 3, Color.RED);
    }
    private static void drawGrid() {
        float zoom = Graphics.getCameraZoom();
        Rectangle cameraBounds = Graphics.getCameraBounds();
        double scaledGrid = GRID_SIZE * zoom;
        Vector2 originScreen = Graphics.rlj.core.GetWorldToScreen2D(new Vector2(0, 0), Graphics.camera);

        double firstX = originScreen.x;
        if (firstX > 0) {
            firstX %= (scaledGrid);
        } else {
            firstX = -Math.abs(firstX) % (scaledGrid);
        }

        for (float xi = (float) firstX; xi < Graphics.cameraWidth; xi += scaledGrid) {
            Graphics.drawLine(xi, 0, xi, Graphics.cameraHeight, 1, Graphics.GRID_STROKE);
        }

        double firstY = originScreen.y;
        if (firstY > 0) {
            firstY %= (scaledGrid);
        } else {
            firstY = -Math.abs(firstY) % (scaledGrid);
        }

        for (float yi = (float) firstY; yi < Graphics.cameraHeight; yi += scaledGrid) {
            Graphics.drawLine(0, yi, Graphics.cameraWidth, yi, 1, Graphics.GRID_STROKE);
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
            for (int i = 0; i <= Graphics.PERFORMANCE_MODE; i++) {

                // Compute required framebuffer scaling
                Graphics.updateMouse();
                update();
            }
            // Draw
            //----------------------------------------------------------------------------------
            Graphics.beginDrawMode();
            Graphics.drawBackground(Graphics.GRID);
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