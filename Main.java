import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Texture2D;

public class Main {
    final public static float GRID_SIZE = 50, ARENA_PADDING = GRID_SIZE * 4;
    public static Rectangle cameraBox;
    public static float arenaWidth, arenaHeight;
    public static long counter = 0;
    public static int deathScreenFrames = 0;
    public static DrawPool[] drawablePool;
    public static HashPool<GameObject> gameObjectPool;
    public static HashPool<UIObject> UIObjectPool;
    public static IdServer idServer;
    public static Player player;
    public static GameObject cameraHost;
    public static Vector2 cameraTarget;
    public static Texture2D deathTexture;
    public static String killerName, deathBuild, deathScore, deathLevel, aliveTime;
    public static Stopwatch menuTargetWatch;  // Stopwatch for switching menu camera target
    public static int menuFrames = 0;

    // Variables for game reset
    public static Stopwatch lastReset;
    public static boolean pendingReset = false;
    public static void resetGame() {
        pendingReset = true;
        lastReset.start();
    }
    // Debugging/analysis
    public static String debugText = "";

    // Called in GamePanel.java to initialize game

    public static void initialize() {
        Graphics.initialize("Diep.Java");
        // Draw a black background while game initializes
        Graphics.beginDrawMode();
        Graphics.beginTargetTexture();
        Graphics.drawBackground(Color.BLACK);
        Graphics.endTextureMode();
        Graphics.endDrawMode();

        TankBuild.loadTankDefinitions();  // Load tank definitions from TankDefinitions.json
        NameGenerator.initialize();
        ScoreHandler.initialize();
        lastReset = new Stopwatch();  // For resetting the game

        drawablePool = new DrawPool[Scene.values().length];
        for (int i = 0; i < drawablePool.length; i++) {
            drawablePool[i] = new DrawPool();
        }

        gameObjectPool = new HashPool<>();
        UIObjectPool = new HashPool<>();
        idServer = new IdServer();
        lastReset.start();

        SceneManager.setSceneUpdate(Scene.MENU, Main::menuUpdate);
        SceneManager.setSceneDraw(Scene.MENU, Main::menuDraw);
        SceneManager.setSceneUpdate(Scene.GAME, Main::gameUpdate);
        SceneManager.setSceneDraw(Scene.GAME, Main::gameDraw);

        startMenuGame();

        int iterations = 120 * 10 / (1 + Graphics.PERFORMANCE_MODE);
        for (int i = 0; i < iterations; i++) {  // pre-simulate 10 seconds of menu game
            menuUpdate();
        }
    }

    public static void main(String[] args) {
        initialize();
        //--------------------------------------------------------------------------------------
        // Main game loop
        while (!Graphics.shouldWindowClose())    // Detect window close button or ESC key
        {
            SceneManager.refreshScene();
            SceneManager.updateScene();
            SceneManager.drawScene();
        }
        // De-Initialization
        //--------------------------------------------------------------------------------------
        TextureLoader.clear();
        Graphics.close();
    }

    public static void startMenuGame() {
        Polygon.setRewardMultiplier(4);
        int spawn = Spawner.getSpawnAmount();
        // Set arena size
        arenaWidth = arenaHeight = (float) (Math.floor(32 * Math.sqrt(spawn + 1)) * GRID_SIZE * 2) + ARENA_PADDING * 2;
        // Game initialization
        drawablePool[SceneManager.getScene()].clear();
        gameObjectPool.clear();
        UIObjectPool.clear();
        idServer.reset();
        //TextureLoader.clear();  // TODO: should this be kept, ram will take a hit
        Leaderboard.clear();
        Spawner.reset();
        deathScreenFrames = 0;
        menuFrames = 0;
        // new TestObj();
        player = null;

        // Initialize camera
        menuTargetWatch = new Stopwatch();
        menuTargetWatch.start();

        Tank randomEnemy = Spawner.spawnRandomEnemy(0);
        CameraManager.setZoom(randomEnemy.getZoom());
        cameraHost = randomEnemy;
        cameraTarget = cameraHost.pos;
        Graphics.setCameraTarget(cameraTarget);
        cameraBox = Graphics.getCameraWorld();
    }

    public static void startGame() {
        Polygon.setRewardMultiplier(1);
        int spawn = Spawner.getSpawnAmount() + 1;
        // Set arena size
        arenaWidth = arenaHeight = (float) (Math.floor(32 * Math.sqrt(spawn + 1)) * GRID_SIZE * 2) + ARENA_PADDING * 2;
        System.out.println("Arena size: " + arenaWidth + "x" + arenaHeight);
        // Game initialization
        drawablePool[SceneManager.getScene()].clear();
        gameObjectPool.clear();
        UIObjectPool.clear();
        idServer.reset();
        //TextureLoader.clear();  // TODO: should this be kept, ram will take a hit
        Leaderboard.clear();
        Spawner.reset();
        deathScreenFrames = 0;
        // new TestObj();
        player = new Player(new Vector2((float) (Main.arenaWidth * Math.random()), (float) (Main.arenaHeight * Math.random())), "tank");
        player.group = 0;  // Blue team
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

        // Handle the pending operations
        drawablePool[SceneManager.getScene()].refresh();
        Main.gameObjectPool.refresh();
        Main.UIObjectPool.refresh();
        Main.idServer.refresh();

        // Create polygons
        Spawner.updateSpawn();
        // Update game collisions
        CollisionManager.updateCollision();

        // Update all the game objects
        for (Updatable gameObject : gameObjectPool.getObjects()) {
            gameObject.update();
        }
        for (UIObject uiObject : UIObjectPool.getObjects()) {
            if (uiObject.getScene() == SceneManager.scene) {
                uiObject.update();
            }
        }

        CameraManager.update();

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

    private static void drawDeathScreen() {
        Graphics.drawRectangle(0, 0, Graphics.cameraWidth, Graphics.cameraHeight, Graphics.rgba(0, 0, 0, Math.min(100, deathScreenFrames)));
        float totHeight = 400;
        float yPos = (Graphics.cameraHeight - totHeight) / 2;
        Graphics.drawTextCenteredOutline("You were killed by:", Graphics.cameraWidth/2, (int) yPos, 28, -5, Color.WHITE);
        yPos += 40;

        Graphics.drawTextCenteredOutline(NameGenerator.formatNameCase(killerName), Graphics.cameraWidth/2, (int) yPos, 50, -5, Color.WHITE);
        yPos += 50;

        // Text
        float gap = 38;
        float padding = (250F - gap*3)/2;
        float yPos2 = yPos + padding + 10, xPos = Graphics.cameraWidth/2 - 250F;
        Graphics.drawTextOutline("Score:  " + deathScore, new Vector2(xPos, yPos2), 28, -5, Color.WHITE);
        yPos2 += gap;
        Graphics.drawTextOutline("Level:  " + deathLevel, new Vector2(xPos, yPos2), 28, -5, Color.WHITE);
        yPos2 += gap;
        Graphics.drawTextOutline("Time Alive:  " + aliveTime, new Vector2(xPos, yPos2), 28, -5, Color.WHITE);
        // Tank texture
        Graphics.drawTextureCentered(deathTexture, new Vector2(Graphics.cameraWidth/2 + 250F/2, yPos + 250F/2), 250, 250, -Math.PI/4, Color.WHITE);
        yPos += 250;

        Graphics.drawTextCenteredOutline(deathBuild, (int)(Graphics.cameraWidth/2 + 250F/2), (int) yPos, 30, -5, Color.WHITE);
    }

    public static void menuUpdate() {
        if (menuTargetWatch.s() > 5 || cameraHost == null || cameraHost.isDead) {  // Switch camera target every 5 seconds or if the current target is dead
            Tank leaderTank = Leaderboard.getTankRank(0);  // Set camera to top player
            if (leaderTank != null) {
                cameraHost = leaderTank;
                CameraManager.setZoom(leaderTank.getZoom());
            }
            menuTargetWatch.start();
        }
        for (int i = 0; i <= Graphics.PERFORMANCE_MODE; i++) {
            if (Graphics.isKeyDown(Keyboard.KEY_SPACE)) {
                SceneManager.setScene(Scene.GAME);
                pendingReset = true;
            }
            // Compute required framebuffer scaling
            Graphics.updateMouse();
            update();
        }
        Leaderboard.update();
        TextureLoader.refreshTankTextures();
    }

    public static void menuDraw() {
        menuFrames++;
        Graphics.beginDrawMode();
        Graphics.beginTargetTexture();
        Graphics.drawBackground(Graphics.GRID);
        drawGrid();
        Graphics.beginCameraMode();
        drawBounds();
        drawablePool[SceneManager.getScene()].drawAll();  // Draw all objects
        Graphics.endCameraMode();
        Graphics.drawRectangle(0, 0, Graphics.cameraWidth, Graphics.cameraHeight, Graphics.rgba(0, 0, 0, (int) Math.max(150, 255 - 0.5f * menuFrames)));
        Graphics.drawTextCenteredOutline("DIEP.JAVA", Graphics.cameraWidth/2, Graphics.cameraHeight/2, 80, -6, Color.WHITE);
        Graphics.endTextureMode();
        Graphics.endDrawMode();
    }

    public static void gameUpdate() {
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
    }

    public static void gameDraw() {
        // Draw
        //----------------------------------------------------------------------------------
        Graphics.beginDrawMode();
            Graphics.beginTargetTexture();
                Graphics.drawBackground(Graphics.GRID);
                drawGrid();
                if (Graphics.PERFORMANCE_MODE == 1 && deathScreenFrames == 0) player.drawUpgradeBars();
                if (!debugText.isEmpty()) Graphics.drawText(debugText, 10, 30, 20, Color.WHITE);
                Graphics.beginCameraMode();
                    drawBounds();
                    drawablePool[SceneManager.getScene()].drawAll();  // Draw all objects
                    for (int id : Leaderboard.tankIds) {
                        Tank tank = (Tank) gameObjectPool.getObj(id);
                        if (tank != null) tank.drawText();
                    }
                    Minimap.draw();
                    if (deathScreenFrames == 0) player.drawKillQueue();
                    Graphics.drawFPS(Graphics.getScreenToWorld2D(new Vector2(10, 10), Graphics.camera), (int) (20 / Graphics.getCameraZoom()), Color.WHITE);
                Graphics.endCameraMode();
                if (Graphics.PERFORMANCE_MODE == 0 && deathScreenFrames == 0) player.drawUpgradeBars();
            Graphics.endTextureMode();

            if (deathScreenFrames > 0) {  // If death screen is active
                Graphics.beginUITexture();
                    Graphics.rlj.core.ClearBackground(Graphics.CLEAR);  // Only clear on leaderboard refresh
                    Leaderboard.draw();
                    drawDeathScreen();
                Graphics.endTextureMode();
            } else if (Main.counter % (2 - Graphics.PERFORMANCE_MODE) == 0) {  // Normal Game UI, every other frame
                Graphics.beginUITexture();
                    if (Main.counter % Graphics.FPS == 0) {
                        Graphics.rlj.core.ClearBackground(Graphics.CLEAR);  // Only clear on leaderboard refresh
                        Leaderboard.draw();
                        player.drawUsername();
                    }
                    player.drawLevelBar();
                Graphics.endTextureMode();
            }
        Graphics.endDrawMode();
    }
}
