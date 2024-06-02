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

    public static long counter = 0;
    public static int deathScreenFrames = 0;
    public static DrawPool drawablePool;
    public static HashPool<GameObject> gameObjectPool;
    public static IdServer idServer;
    public static Stopwatch globalClock;
    public static Player player;
    public static GameObject cameraHost;
    public static Vector2 cameraTarget;
    public static String killerName;

    // Variables for game reset
    static Stopwatch lastReset = new Stopwatch();  // For resetting the game
    static boolean pendingReset = false;
    public static void resetGame() {
        pendingReset = true;
        lastReset.start();
    }
    static String debugText = "";
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
        int spawn = 1;
        // Set arena size
        arenaWidth = arenaHeight = (float) (Math.floor(32 * Math.sqrt(spawn + 1)) * GRID_SIZE * 2) + ARENA_PADDING * 2;
        System.out.println("Arena size: " + arenaWidth + "x" + arenaHeight);
        // Game initialization
        globalClock.start();
        drawablePool.clear();
        gameObjectPool.clear();
        idServer.reset();
        //TextureLoader.clear();  // TODO: should this be kept, ram will take a hit
        Leaderboard.clear();
        Spawner.reset();
        deathScreenFrames = 0;
        // new TestObj();
        player = new Player(new Vector2((float) (Main.arenaWidth * Math.random()), (float) (Main.arenaHeight * Math.random())), "tank");
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
            Tank t = new EnemyTank(new Vector2(Graphics.randf() * arenaWidth, Graphics.randf() * arenaHeight), buildName, fillCol, strokeCol);
            t.group = group;
            if (t.group == 0) t.group = player.group;
        }

        // Initialize camera
        cameraHost = player;
        cameraTarget = player.pos;
        Graphics.setCameraTarget(cameraTarget);
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
        Spawner.updateSpawn();
        // Update game collisions
        CollisionManager.updateCollision();

        // Update all the game objects
        for (Updatable gameObject : gameObjectPool.getObjects()) {
            gameObject.update();
        }

        updateCamera();

        if (Main.counter % 120 == 0) {
            percentage = 100 * (float) stopwatch.ms() / (1000.f/120);  // Time taken / max time allowed
        }

        cameraBox = Graphics.getCameraWorld();
    }

    public static void updateCamera() {
        if (cameraHost == player) {
            if (player.tankBuild.zoomAbility && player.controller.holdSpecial()) {
                if (player.controller.pressSpecial()) {  // Only update target if the button is pressed
                    // TODO: check if predator zoom amount is right
                    cameraTarget = new Vector2((float) (Math.cos(player.direction) * 1000 * player.scale + player.pos.x), (float) (Math.sin(player.direction) * 1000 * player.scale + player.pos.y));
                }
            } else {
                cameraTarget = player.pos;
            }
        } else {
            cameraTarget = cameraHost.pos;
        }

        Vector2 difference = Raymath.Vector2Subtract(cameraTarget, Graphics.getCameraTarget());
        Graphics.shiftCameraTarget(Graphics.scale(difference, 0.05f));

        // Zoom in and out feature (beta testing)
        float delta = Graphics.getCameraZoom()/100;
        if (Graphics.isKeyDown(Keyboard.KEY_DOWN)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() - delta);
        }
        if (Graphics.isKeyDown(Keyboard.KEY_UP)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() + delta);
        }
        // Cap the zoom level
        Graphics.setCameraZoom(Math.max(0.1f, Math.min(10f, Graphics.getCameraZoom())));
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

    private static void drawDeathScreen() {
        Graphics.drawRectangle(0, 0, Graphics.cameraWidth, Graphics.cameraHeight, Graphics.rgba(0, 0, 0, Math.min(100, deathScreenFrames)));
        Graphics.drawTextCenteredOutline("You were killed by:", Graphics.cameraWidth/2, Graphics.cameraHeight/2 - 35, 30, -5, Color.WHITE);
        Graphics.drawTextCenteredOutline(NameGenerator.formatNameCase(killerName), Graphics.cameraWidth/2, Graphics.cameraHeight/2, 50, -5, Color.WHITE);
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
            if (deathScreenFrames > 0) {  // If animation is playing
                deathScreenFrames++;
            }
            for (int i = 0; i <= Graphics.PERFORMANCE_MODE; i++) {
                // Compute required framebuffer scaling
                Graphics.updateMouse();
                update();
                if (deathScreenFrames == 0) {
                    Minimap.update();
                    player.updateUpgradeBars();
                }
            }
            Leaderboard.update();
            if (deathScreenFrames == 0) {
                player.updateBars();
            }

            TextureLoader.refreshTankTextures();
            // Draw
            //----------------------------------------------------------------------------------
            Graphics.beginDrawMode();
            Graphics.drawBackground(Graphics.GRID);
            // Graphics.drawText("Number of objects: " + gameObjectPool.getObjects().size(), 10, 25, 20, Color.BLACK);
            //Graphics.drawText(String.format("Percentage %.2f", percentage), 10, 40, 20, Color.BLACK);
            //Graphics.drawText(String.format("Score: %d\tLevel: %d", (int)player.score, (int)player.level), 10, 60, 20, Color.BLACK);
            drawGrid();
            if (Graphics.PERFORMANCE_MODE == 1 && deathScreenFrames == 0) player.drawUpgradeBars();
            //Graphics.drawText(String.format("Percentage %.2f", percentage), 10, 40, 20, Color.BLACK);
            if (!debugText.isEmpty()) Graphics.drawText(debugText, 10, 30, 20, Color.WHITE);
            Graphics.beginCameraMode();
            drawBounds();
            draw();  // Main draw function
            //Graphics.drawTextureCentered(tankTextures.get(Graphics.BLUE).get("auto 5"), new Vector2(the0, 0), Math.PI/4, 1, Color.WHITE);
            Minimap.draw();
            if (deathScreenFrames == 0) player.drawKillQueue();
            Graphics.drawFPS(Graphics.getScreenToWorld2D(new Vector2(10, 10), Graphics.camera), (int)(20/Graphics.getCameraZoom()), Color.WHITE);
            Graphics.endCameraMode();
            if (Graphics.PERFORMANCE_MODE == 0 && deathScreenFrames == 0) player.drawUpgradeBars();
            Graphics.endTextureMode();

            if (deathScreenFrames > 0) {
                Graphics.beginUITexture();
                Graphics.rlj.core.ClearBackground(Graphics.rgba(0, 0, 0, 0));  // Only clear on leaderboard refresh
                Leaderboard.draw();
                drawDeathScreen();
                Graphics.endTextureMode();
            } else if (Main.counter % (2 - Graphics.PERFORMANCE_MODE) == 0) {  // Every other frame
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