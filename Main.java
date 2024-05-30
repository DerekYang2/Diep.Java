import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class Main {
    final public static float GRID_SIZE = 50;
    static Rectangle cameraBox;
    public static float arenaWidth = GRID_SIZE * 100, arenaHeight = GRID_SIZE * 100;
    public static Rectangle nestBox, crasherZone;
    public final static float ARENA_PADDING = GRID_SIZE * 4;

    public static int polygonAmount = 0, pentagonNestAmount = 0;
    public static long counter = 0;
    public static DrawPool drawablePool;
    public static HashPool<GameObject> gameObjectPool;
    public static IdServer idServer;
    public static Stopwatch globalClock;
    static Player player;

    // Variables for game reset
    static Stopwatch lastReset = new Stopwatch();  // For resetting the game
    static boolean pendingReset = false;
    public static void resetGame() {
        pendingReset = true;
        lastReset.start();
    }

    // TEMP: Debugging/analysis
    static float percentage;
    static Stopwatch stopwatch = new Stopwatch();

    // Called in GamePanel.java to initialize game

    public static void initialize() {
        Graphics.initialize("DiepJava");
        TankBuild.loadTankDefinitions();  // Load tank definitions from TankDefinitions.json
        NameGenerator.initialize();
        ScoreHandler.initialize();
        globalClock = new Stopwatch();
        drawablePool = new DrawPool();
        gameObjectPool = new HashPool<>();
        idServer = new IdServer();
        lastReset.start();
        startGame();
    }

    public static void startGame() {
        // Game initialization
        globalClock.start();
        drawablePool.clear();
        gameObjectPool.clear();
        idServer.reset();
        TextureLoader.clear();
        Leaderboard.clear();

        int spawn = 2;

        polygonAmount = spawn * 30;
        pentagonNestAmount = spawn;
        Polygon.count = 0;
        Polygon.nestCount = 0;

        // Set arena size
        arenaWidth = arenaHeight = (float) (Math.floor(40 * Math.sqrt(spawn + 1)) * GRID_SIZE * 2) + ARENA_PADDING * 2;
        float nestSide = arenaWidth / 5, crasherSide = nestSide * 2f;
        nestBox = new Rectangle(arenaWidth/2 - nestSide/2, arenaHeight/2 - nestSide/2, nestSide, nestSide);
        crasherZone = new Rectangle(arenaWidth/2 - crasherSide/2, arenaHeight/2 - crasherSide/2, crasherSide, crasherSide);
        // new TestObj();
        player = new Player(new Vector2(0,0), "tank");
        for (int i = 0; i < spawn; i++) {
            String buildName;
            //buildName = TankBuild.getRandomBuildName();
            buildName = "tank";
            int group = -Graphics.randInt(0, 4);
            Color fillCol, strokeCol;
            switch (group) {
                case 0 -> {
                    fillCol = Graphics.BLUE;
                    strokeCol = Graphics.BLUE_STROKE;
                }
                case -1 -> {
                    fillCol = Graphics.RED;
                    strokeCol = Graphics.RED_STROKE;
                }
                case -2 -> {
                    fillCol = Graphics.GREEN;
                    strokeCol = Graphics.GREEN_STROKE;
                }
                default -> {
                    fillCol = Graphics.PURPLE;
                    strokeCol = Graphics.PURPLE_STROKE;
                }
            }
            Tank t = new EnemyTank(new Vector2((float) Math.random() * arenaWidth, (float) Math.random() * arenaHeight), buildName, fillCol, strokeCol);
            t.group = group;
            if (t.group == 0) t.group = player.group;
        }

        Graphics.setCameraTarget(player.pos);
        cameraBox = Graphics.getCameraWorld();
        //counter = 0;
    }

    private static void update() {
        // Reset game was called, only once per second
        if (Graphics.isKeyDown(Keyboard.KEY_O) && lastReset.s() > 1) {
            resetGame();
        }

        counter++;

        if (Main.counter % 120 == 0) {
            stopwatch.start();
        }

        // Handle the pending operations
        Main.drawablePool.refresh();
        Main.gameObjectPool.refresh();
        Main.idServer.refresh();

        // Create polygons
        while (Polygon.count < polygonAmount) {
            Polygon.spawnRandomPolygon();
        }
        while (Polygon.nestCount < pentagonNestAmount) {
            Polygon.spawnNestPolygon();
        }

        CollisionManager.updateCollision();

        // Update all the game objects
        for (Updatable gameObject : gameObjectPool.getObjects()) {
            gameObject.update();
        }

        if (Main.counter % 120 == 0) {
            percentage = 100 * (float) stopwatch.ms() / (1000.f/120);  // Time taken / max time allowed
        }

        cameraBox = Graphics.getCameraWorld();
    }

    private static void drawGrid() {
        // If too zoomed out, don't draw grid
        if (Graphics.getCameraZoom() < 1.f/5) {
            return;
        }

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

    public static boolean onScreen(Vector2 pos, float radius) {
        return pos.x + radius > cameraBox.x && pos.x - radius < cameraBox.x + cameraBox.width && pos.y + radius > cameraBox.y && pos.y - radius < cameraBox.y + cameraBox.height;
    }

    static float xt = 0;
    private static void draw() {
        //Graphics.drawCircle(xt, 100, 10, Color.RED, 1);
        //xt += 6.25f;
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
            if (pendingReset) {
                startGame();
                pendingReset = false;
            }
            for (int i = 0; i <= Graphics.PERFORMANCE_MODE; i++) {
                // Compute required framebuffer scaling
                Graphics.updateMouse();
                update();
                Minimap.update();
                player.updateUpgradeBars();
            }
            Leaderboard.update();
            player.updateBars();

            TextureLoader.refreshTankTextures();
            // Draw
            //----------------------------------------------------------------------------------
            Graphics.beginDrawMode();
            Graphics.drawBackground(Graphics.GRID);
            // Graphics.drawText("Number of objects: " + gameObjectPool.getObjects().size(), 10, 25, 20, Color.BLACK);
            //Graphics.drawText(String.format("Percentage %.2f", percentage), 10, 40, 20, Color.BLACK);
            //Graphics.drawText(String.format("Score: %d\tLevel: %d", (int)player.score, (int)player.level), 10, 60, 20, Color.BLACK);
            drawGrid();
            //Graphics.drawText(String.format("Percentage %.2f", percentage), 10, 40, 20, Color.BLACK);
            Graphics.beginCameraMode();
            drawBounds();
            draw();  // Main draw function
            //Graphics.drawTextureCentered(tankTextures.get(Graphics.BLUE).get("auto 5"), new Vector2(the0, 0), Math.PI/4, 1, Color.WHITE);
            Minimap.draw();
            player.drawKillQueue();
            Graphics.drawFPS(Graphics.getScreenToWorld2D(new Vector2(10, 10), Graphics.camera), (int)(20/Graphics.getCameraZoom()), Color.WHITE);
            Graphics.endCameraMode();
            player.drawUpgradeBars();
            Graphics.endTextureMode();

            if (Main.counter % (2 - Graphics.PERFORMANCE_MODE) == 0) {  // Every other frame
                Graphics.beginUITexture();
                if (Main.counter % Graphics.FPS == 0) {
                    Graphics.rlj.core.ClearBackground(Graphics.rgba(0, 0, 0, 0));  // Only clear on leaderboard refresh
                    Leaderboard.draw();
                    player.drawUsername();
                }
                player.drawLevelBar();
                Graphics.endTextureMode();
            }

            Graphics.endDrawMode();
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        TextureLoader.clear();
        Graphics.close();
    }
}