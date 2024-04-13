import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class Main {
    final public static float GRID_SIZE = 50;
    static Rectangle cameraBox;
    public static float arenaWidth = GRID_SIZE * 100, arenaHeight = GRID_SIZE * 100;
    public final static float ARENA_PADDING = GRID_SIZE * 4;

    public static long counter;
    public static DrawPool drawablePool;
    public static Pool<Updatable> updatablePool;
    public static Pool<GameObject> gameObjectPool;
    public static IdServer idServer;
    public static Stopwatch globalClock = new Stopwatch();

    static Tank player;

    // Called in GamePanel.java to initialize game
    public static void initialize() {
        Graphics.initialize("DiepJava");

        // Game initialization
        globalClock.start();
        drawablePool = new DrawPool();
        updatablePool = new Pool<>();
        gameObjectPool = new Pool<>();
        idServer = new IdServer();
        // Set arena size
        arenaWidth = arenaHeight = (float) (Math.floor(25 * Math.sqrt(1 + 1/*number of players*/)) * GRID_SIZE * 2);
        // new TestObj();
        player = new Player(new Vector2(0,0));
        for (int i = 0; i < 1; i++) {
            new Tank(new Vector2((float) (Math.random() * arenaWidth), (float) (Math.random() * arenaHeight)), new BotController());
        }
        Graphics.setCameraTarget(player.pos);
        counter = 0;
    }

    private static void updateCamera() {
        Vector2 difference = Raymath.Vector2Subtract(player.pos, Graphics.getCameraTarget());
        Graphics.shiftCameraTarget(Raymath.Vector2Scale(difference, 0.055f));
        if (Graphics.isKeyDown(Keyboard.KEY_DOWN)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() - 0.005f);
        }
        if (Graphics.isKeyDown(Keyboard.KEY_UP)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() + 0.005f);
        }

    }

    // TEMP: Debugging/analysis
    static float percentage;
    static Stopwatch stopwatch = new Stopwatch();

    private static void update() {
        counter++;

        if (Main.counter % 120 == 0) {
            stopwatch.start();
        }

        cameraBox = Graphics.getCameraWorld();

        // Handle the pending operations
        Main.drawablePool.refresh();
        Main.updatablePool.refresh();
        Main.gameObjectPool.refresh();
        Main.idServer.refresh();

        updateCamera();

        // Update all the updatable objects
        for (Updatable updatable : Main.updatablePool.getObjects()) {
            updatable.update();
        }

        // Collide all the game objects
/*        for (GameObject gameObject : Main.gameObjectPool.getObjects()) {
            for (GameObject other : Main.gameObjectPool.getObjects()) {
                if (gameObject != other && gameObject.group != other.group) {
                    if (gameObject.checkCollision(other)) {
                        gameObject.receiveKnockback(other);
                    }
                }
            }
        }*/


        CollisionManager.updateCollision();

        if (Main.counter % 120 == 0) {
            percentage = 100 * (float) stopwatch.ms() / (1000.f/120);  // Time taken / max time allowed
        }
    }

    private static void drawGrid() {
        float zoom = Graphics.getCameraZoom();
        double scaledGrid = GRID_SIZE * zoom;
        Vector2 originScreen = Graphics.rlj.core.GetWorldToScreen2D(new Vector2(0, 0), Graphics.camera);

        double firstX = originScreen.x;
        if (firstX > 0) {
            firstX %= (scaledGrid);
        } else {
            firstX = -Math.abs(firstX) % (scaledGrid);
        }

        for (float xi = (float) firstX; xi < Graphics.cameraWidth; xi += (float) scaledGrid) {
            Graphics.drawLine(xi, 0, xi, Graphics.cameraHeight, 1, Graphics.GRID_STROKE);
        }

        double firstY = originScreen.y;
        if (firstY > 0) {
            firstY %= (scaledGrid);
        } else {
            firstY = -Math.abs(firstY) % (scaledGrid);
        }

        for (float yi = (float) firstY; yi < Graphics.cameraHeight; yi += (float) scaledGrid) {
            Graphics.drawLine(0, yi, Graphics.cameraWidth, yi, 1, Graphics.GRID_STROKE);
        }
    }

    public static void drawBounds() {
        // Get camera bounds as world coordinates
        float xLeft = cameraBox.x, xRight = cameraBox.x + cameraBox.width, yTop = cameraBox.y, yBottom = cameraBox.y + cameraBox.height;
        // Draw left and right boundaries
        Graphics.drawRectangle(xLeft, yTop, ARENA_PADDING-xLeft, yBottom-yTop, Graphics.BOUNDARY);  // Draw from left of the screen to ARENA_PADDING
        Graphics.drawRectangle(arenaWidth - ARENA_PADDING, yTop, xRight - (arenaWidth - ARENA_PADDING), yBottom-yTop, Graphics.BOUNDARY);  // Draw from (arena - ARENA_PADDING) to right of the screen


        // Draw top and bottom boundaries
        float xLeft2 =  Math.max(xLeft, ARENA_PADDING);
        float xRight2 = Math.min(xRight, arenaWidth - ARENA_PADDING);

        int widthFix = ((int) (arenaWidth - ARENA_PADDING) != (int) (xLeft2) + (int)(xRight2-xLeft2))? 1 : 0; // Right side: integer cast issues, add 1 pixel to fix
        int posFix = ((int) xLeft + (int)(ARENA_PADDING-xLeft) != (int) xLeft2) ? -1 : 0; // Left side: integer case issues, subtract 1 pixel to fix
        if (posFix == -1) {  // If left position was changed, increase width so right position stays the same
            widthFix++;
        }

        Graphics.drawRectangle((int)xLeft2 + posFix, yTop, (int)(xRight2-xLeft2) + widthFix, ARENA_PADDING - yTop, Graphics.BOUNDARY);
        Graphics.drawRectangle((int)xLeft2 + posFix, arenaHeight - ARENA_PADDING, (int)(xRight2-xLeft2) + widthFix, yBottom - (arenaHeight - ARENA_PADDING), Graphics.BOUNDARY);
    }

    //static float xt = 0;
    private static void draw() {
        drawBounds();
        //Graphics.drawCircle(xt, 100, 10, Color.RED);
        //xt += 6 * GRID_SIZE/120;
        // Draw all the drawable objects
        drawablePool.drawAll();
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

            Graphics.drawFPS(10, 10, 20, Color.BLACK);
            Graphics.drawText("Number of objects: " + updatablePool.getObjects().size(), 10, 25, 20, Color.BLACK);
            Graphics.drawText(String.format("Percentage %.2f", percentage), 10, 40, 20, Color.BLACK);

            drawGrid();
            Graphics.beginCameraMode();
            draw();  // Main draw function
            Graphics.endCameraMode();
            Graphics.endDrawMode();
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        Graphics.close();
    }
}