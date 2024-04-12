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
    public static Pool<Drawable> drawablePool;
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
        drawablePool = new Pool<>();
        updatablePool = new Pool<>();
        gameObjectPool = new Pool<>();
        idServer = new IdServer();
        // new TestObj();
        player = new Player(new Vector2(0,0));
        for (int i = 0; i < 2; i++)
            new Tank(new Vector2((float) (Math.random() * arenaWidth), (float) (Math.random() * arenaHeight)), new BotController());
        Graphics.setCameraTarget(player.pos);
        counter = 0;
    }

    private static void updateCamera() {
        counter++;
        Vector2 difference = Raymath.Vector2Subtract(player.pos, Graphics.getCameraTarget());
        Graphics.shiftCameraTarget(Raymath.Vector2Scale(difference, 0.055f));
        if (Graphics.isKeyDown(Keyboard.KEY_DOWN)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() - 0.005f);
        }
        if (Graphics.isKeyDown(Keyboard.KEY_UP)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() + 0.005f);
        }

    }

    private static void update() {
        float xLeft = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, 0), Graphics.camera).x;
        float xRight = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(Graphics.cameraWidth, 0), Graphics.camera).x;
        float yTop = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, 0), Graphics.camera).y;
        float yBottom = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, Graphics.cameraHeight), Graphics.camera).y;
        cameraBox = new Rectangle(xLeft, yTop, xRight - xLeft, yBottom - yTop);

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
        float xLeft = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, 0), Graphics.camera).x;
        float xRight = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(Graphics.cameraWidth, 0), Graphics.camera).x;
        float yTop = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, 0), Graphics.camera).y;
        float yBottom = Graphics.rlj.core.GetScreenToWorld2D(new Vector2(0, Graphics.cameraHeight), Graphics.camera).y;

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
        // Draw circle at mouse pos
        Graphics.drawRectangle(Graphics.getVirtualMouse().x, Graphics.getVirtualMouse().y, 5, 5, Color.WHITE);

        // Draw all the drawable objects
        for (Drawable drawable : Main.drawablePool.getObjects()) {
            drawable.draw();
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

            Graphics.drawFPS(10, 10, 20, Color.BLACK);
            Graphics.drawText("Number of objects: " + drawablePool.getObjects().size(), 10, 25, 20, Color.WHITE);

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