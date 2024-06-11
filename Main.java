import com.raylib.java.core.Color;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Texture2D;

/**
 * The Main class represents the entry point of the game and contains the main game loop.
 */
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
    public static String playerUsername;
    public static GameObject cameraHost;
    public static Vector2 cameraTarget;
    public static Texture2D deathTexture;
    public static String killerName, deathBuild, deathScore, deathLevel, aliveTime;
    public static Stopwatch menuTargetWatch;  // Stopwatch for switching menu camera target
    public static int menuFrames = 0;

    // Variables for game reset
    public static Stopwatch lastReset;
    public static boolean pendingReset = false, pendingMenuReset = false;

    /**
     * Resets the game by clearing pools, resetting variables, and starting the menu game.
     */
    public static void resetGame() {
        pendingReset = true;
        lastReset.start();
    }

    public static void respawn() {
        deathScreenFrames = 0;
        int deathLevelInt = Integer.parseInt(deathLevel);
        int respawnLevel = (int) Math.min(Math.max(deathLevelInt - 1, 1), Math.floor(Math.sqrt(deathLevelInt) * 3.2796));

        killerName = deathBuild = deathScore = deathLevel = aliveTime = "";
        pendingReset = false;
        lastReset.start();
        player = new Player(new Vector2((float) (Main.arenaWidth * Math.random()), (float) (Main.arenaHeight * Math.random())), "tank", respawnLevel);
        player.group = 0;  // Blue team
        // Initialize camera
        cameraHost = player;
        cameraTarget = player.pos;
        Graphics.setCameraTarget(cameraTarget);
        cameraBox = Graphics.getCameraWorld();

        // Draw UI on respawn
        Graphics.beginUITexture();
        Graphics.rlj.core.ClearBackground(Graphics.CLEAR);  // Only clear on leaderboard refresh
        Leaderboard.draw();
        player.drawUsername();
        Graphics.endTextureMode();
    }

    public static void returnToMenu() {
        SceneManager.setScene(Scene.MENU);
        pendingMenuReset = true;
        menuFrames = 0;
    }

    // Debugging/analysis
    public static String debugText = "";

    /**
     * Initializes the game by setting up graphics, loading tank definitions, and setting up scenes.
     */
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

        // Create persistent random name for player
        playerUsername = NameGenerator.generateUsername();

        SceneManager.setSceneUpdate(Scene.MENU, Main::menuUpdate);
        SceneManager.setSceneDraw(Scene.MENU, Main::menuDraw);
        SceneManager.setSceneUpdate(Scene.GAME, Main::gameUpdate);
        SceneManager.setSceneDraw(Scene.GAME, Main::gameDraw);

        startMenuGame();

        for (int i = 0; i < 120 * 10; i++) {  // pre-simulate 10 seconds of menu game
            menuUpdate();
            menuFrames--;  // Undo menu frame increment for simulation
        }

        new Button(Scene.MENU, new Vector2(Graphics.cameraWidth * 0.5f, Graphics.cameraHeight * 0.5f + 75), "Play", 35, Graphics.PLAY_BUTTON, () -> {
            SceneManager.setScene(Scene.GAME);
            pendingReset = true;
        });
        new ButtonGroup(Scene.GAME, new Vector2(Graphics.cameraWidth * 0.5f, Graphics.cameraHeight * 0.5f + 250), new String[]{"Quit", "Continue"}, 25, Graphics.PLAY_BUTTON, new Runnable[]{Main::returnToMenu, Main::respawn});
    }

    /**
     * The entry point of the game.
     *
     * @param args The command line arguments.
     */
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


    /**
     * Starts the menu game by setting up initial game state and camera.
     */
    public static void startMenuGame() {
        Polygon.setRewardMultiplier(5);
        int spawn = Spawner.getSpawnAmount();
        // Set arena size
        arenaWidth = arenaHeight = (float) (Math.floor(32 * Math.sqrt(spawn + 1)) * GRID_SIZE * 2) + ARENA_PADDING * 2;
        // Game initialization
        drawablePool[SceneManager.getScene()].clear();
        gameObjectPool.clear();
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

        // Random initial target
        Tank randomEnemy = Spawner.spawnRandomEnemy(0);
        CameraManager.setZoom(randomEnemy.getZoom());
        cameraHost = randomEnemy;
        cameraTarget = cameraHost.pos;
        Graphics.setCameraTarget(cameraTarget);
        cameraBox = Graphics.getCameraWorld();

        // Spawn enemies
        Spawner.spawnEnemiesInitial();
    }

    /**
     * Starts the game by setting up initial game state and camera.
     */
    public static void startGame() {
        Polygon.setRewardMultiplier(1);
        int spawn = Spawner.getSpawnAmount() + 1;
        // Set arena size
        arenaWidth = arenaHeight = (float) (Math.floor(32 * Math.sqrt(spawn + 1)) * GRID_SIZE * 2) + ARENA_PADDING * 2;
        System.out.println("Arena size: " + arenaWidth + "x" + arenaHeight);
        // Game initialization
        drawablePool[SceneManager.getScene()].clear();
        gameObjectPool.clear();
        idServer.reset();
        //TextureLoader.clear();  // TODO: should this be kept, ram will take a hit
        Leaderboard.clear();
        Spawner.reset();
        deathScreenFrames = 0;
        // new TestObj();
        player = new Player(new Vector2((float) (Main.arenaWidth * Math.random()), (float) (Main.arenaHeight * Math.random())), "tank", 1);
        player.group = 0;  // Blue team
        // Initialize camera
        cameraHost = player;
        cameraTarget = player.pos;
        Graphics.setCameraTarget(cameraTarget);
        cameraBox = Graphics.getCameraWorld();
        //counter = 0;

        // Spawn enemies
        Spawner.spawnEnemiesInitial();
    }

    /**
     * Updates the game state.
     */
    private static void update() {
        // Reset game was called, only once per second
        if (Graphics.isKeyDown(Keyboard.KEY_O) && lastReset.s() > 1) {
            resetGame();
        }

        counter++;

        // Handle the pending operations
        for (int i = 0; i < drawablePool.length; i++) {
            drawablePool[i].refresh();
        }
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

        CameraManager.update();

        cameraBox = Graphics.getCameraWorld();
    }

    /**
     * Draws the grid on the screen.
     */
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

    /**
     * Draws the boundaries of the game arena.
     */
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

    /**
     * Checks if a position is within the camera view.
     *
     * @param pos The position to check.
     * @param radius The radius of the object.
     * @return True if the position is within the camera view, false otherwise.
     */
    public static boolean onScreen(Vector2 pos, float radius) {
        return pos.x + radius > cameraBox.x && pos.x - radius < cameraBox.x + cameraBox.width && pos.y + radius > cameraBox.y && pos.y - radius < cameraBox.y + cameraBox.height;
    }

    /**
     * Draws the death screen.
     */
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

    /**
     * Updates the game state during the menu scene.
     */
    public static void menuUpdate() {
        if (pendingMenuReset) {
            startMenuGame();
            pendingMenuReset = false;
        }
        menuFrames++;
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
        for (UIObject uiObject : UIObjectPool.getObjects()) {
            if (uiObject.active()) {
                uiObject.update();
            }
        }
        Leaderboard.update();
        TextureLoader.refreshTankTextures();
    }

    /**
     * Draws the menu scene.
     */
    public static void menuDraw() {
        Graphics.beginDrawMode();
        Graphics.beginTargetTexture();
        Graphics.drawBackground(Graphics.GRID);
        drawGrid();
        Graphics.beginCameraMode();
        drawBounds();
        drawablePool[SceneManager.getScene()].drawAll();  // Draw all objects
        Graphics.endCameraMode();
        Graphics.drawRectangle(0, 0, Graphics.cameraWidth, Graphics.cameraHeight, Graphics.rgba(0, 0, 0, (int) Math.max(150, 255 - 0.5f * menuFrames)));
        Graphics.drawTextCenteredOutline("DIEP.JAVA", Graphics.cameraWidth/2, Graphics.cameraHeight/2, 60, -6, Color.WHITE);
        for (UIObject uiObject : UIObjectPool.getObjects()) {
            if (uiObject.active()) {
                uiObject.draw();
            }
        }

        if (menuFrames == 1) {
            Graphics.beginUITexture();
            Graphics.rlj.core.ClearBackground(Graphics.CLEAR);  // Clear the UI texture
            Graphics.endTextureMode();
        }

        Graphics.endTextureMode();
        Graphics.endDrawMode();
    }

    /**
     * Updates the game state during the game scene.
     */
    public static void gameUpdate() {
        if (pendingReset) {
            startGame();
            pendingReset = false;
        }
        if (deathScreenFrames > 0) {  // If animation is playing
            deathScreenFrames++;
            for (UIObject uiObject : UIObjectPool.getObjects()) {  // Game scene UI only updates on death screen
                if (uiObject.active()) {
                    uiObject.update();
                }
            }
        }
        for (int i = 0; i <= Graphics.PERFORMANCE_MODE; i++) {
            // Compute required framebuffer scaling
            Graphics.updateMouse();
            update();
            Minimap.update();

            if (deathScreenFrames == 0) {
                player.updateUpgradeBars();
            }
        }

        Leaderboard.update();
        if (deathScreenFrames == 0) {  // If death screen is not active
            player.updateBars();
        }
        TextureLoader.refreshTankTextures();
    }

    /**
     * Draws the game scene.
     */
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
            for (UIObject uiObject : UIObjectPool.getObjects()) {
                if (uiObject.active()) {
                    uiObject.draw();
                }
            }
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