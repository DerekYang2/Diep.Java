import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.RenderTexture;
import com.raylib.java.textures.rTextures;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Insets;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;

public class Main{
    public static Pool<Drawable> drawablePool;
    public static Pool<Updatable> updatablePool;
    public static IdServer idServer;
    public static int windowWidth, windowHeight;  // Temporary

    public static String environment = "development";

    public static Stopwatch globalClock = new Stopwatch();

    public static InputInfo inputInfo = new InputInfo();

    // Called in GamePanel.java to initialize game
    public static void initialize() {
        globalClock.start();
        drawablePool = new Pool<>();
        updatablePool = new Pool<>();
        idServer = new IdServer();
        // new TestObj();
        new TestTwin();
        // new Square();
    }

    /*******************************************************************************************
     *
     *   raylib-j [core] example - window scale letterbox (and virtual mouse)
     *
     *   This example has been created using raylib-j (version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example contributed by Anata (@anatagawa) and reviewed by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019 Anata (@anatagawa)
     *
     ********************************************************************************************/


    // Clamp Vector2 value with min and max and return a new vector2
    // NOTE: Required for virtual mouse, to clamp inside virtual game size
    static Vector2 ClampValue(Vector2 value, Vector2 min, Vector2 max)
    {
        value.x = Math.min(value.x, max.x);
        value.x = Math.max(value.x, min.x);
        value.y = Math.min(value.y, max.y);
        value.y = Math.max(value.y, min.y);
        return value;
    }

    static int getRandomValue(int min, int max)
    {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public static void main(String[] args){

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        Raylib rlj = new Raylib();
        // Enable config flags for resizable window and vertical synchro
        rCore.SetConfigFlags(Config.ConfigFlag.FLAG_VSYNC_HINT | Config.ConfigFlag.FLAG_MSAA_4X_HINT);
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib-j [core] example - window scale letterbox");
        rlj.core.SetWindowMinSize(320, 240);
        rlj.core.SetWindowPosition(0, 32);
        int gameScreenWidth = 1920;
        int gameScreenHeight = 1080;

        // Render texture initialization, used to hold the rendering result so we can easily resize it
        RenderTexture target = rlj.textures.LoadRenderTexture(gameScreenWidth, gameScreenHeight);
        rTextures.SetTextureFilter(target.texture, RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR);  // Texture scale
        // filter to use

        Color[] colors = new Color[10];
        for (int i = 0; i < 10; i++)
            colors[i] = new Color(getRandomValue(100, 250), getRandomValue(50, 150),
                    getRandomValue(10, 100), 255 );

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Compute required framebuffer scaling

            float scale = Math.min((float) screenWidth/gameScreenWidth,
                    (float) screenHeight/gameScreenHeight);

            if (rlj.core.IsKeyPressed(KEY_SPACE))
            {
                // Recalculate random colors for the bars
                for (int i = 0; i < 10; i++)
                    colors[i] = new Color(getRandomValue(100, 250), getRandomValue(50, 150),
                            getRandomValue(10, 100), 255);
            }

            // Update virtual mouse (clamped mouse value behind game screen)
            Vector2 mouse = rCore.GetMousePosition();
            Vector2 virtualMouse = new Vector2();
            virtualMouse.x = (mouse.x - (screenWidth - (gameScreenWidth*scale))*0.5f)/scale;
            virtualMouse.y = (mouse.y - (screenHeight - (gameScreenHeight*scale))*0.5f)/scale;
            ClampValue(virtualMouse, new Vector2(), new Vector2((float) gameScreenWidth, (float) gameScreenHeight));

            // Apply the same transformation as the virtual mouse to the real mouse (i.e. to work with raygui)
            //SetMouseOffset(-(GetScreenWidth() - (gameScreenWidth*scale))*0.5f, -(GetScreenHeight() - (gameScreenHeight*scale))*0.5f);
            //SetMouseScale(1/scale, 1/scale);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.BLACK);

            // Draw everything in the render texture, note this will not be rendered on screen, yet
            rlj.core.BeginTextureMode(target);

            rlj.core.ClearBackground(Color.RAYWHITE);         // Clear render texture background color

            for(int i = 0; i < 10; i++)
                rlj.shapes.DrawRectangle(0, (gameScreenHeight/10)*i, gameScreenWidth, gameScreenHeight/10, colors[i]);

            rlj.text.DrawText("If executed inside a window,\nyou can resize the window,\nand see the screen scaling!",
                    10, 25, 20, Color.WHITE);

            rlj.text.DrawText("Default Mouse: ["+(int)mouse.x+", "+(int)mouse.y+"]", 350, 25, 20, Color.GREEN);
            rlj.text.DrawText("Virtual Mouse: ["+(int)virtualMouse.x+ ", "+(int)virtualMouse.y+"]", 350, 55,
                    20, Color.YELLOW);
            rlj.text.DrawText("Test: " + String.valueOf(scale), 350, 85, 20, Color.BLACK);
            rlj.core.EndTextureMode();

            // Draw RenderTexture2D to window, properly scaled
            rTextures.DrawTexturePro(target.texture, new Rectangle(0.0f, 0.0f, (float)target.texture.width, (float)-target.texture.height),
                    new Rectangle((screenWidth - ((float)gameScreenWidth*scale))*0.5f, (screenHeight - ((float)gameScreenHeight*scale))*0.5f,
                            (float)gameScreenWidth*scale, (float)gameScreenHeight*scale), new Vector2(), 0.0f, Color.WHITE);

            rlj.core.EndDrawing();
            //--------------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadRenderTexture(target);    // Unload render texture
        //--------------------------------------------------------------------------------------
    }

}