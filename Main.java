import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class Main {
    // Globals
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
        // new TestTwin();
        new TestRectangle(200, 200);
        // new Square();
    }

    public static void main(String[] args) {
        JFrame window = new JFrame("Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // set frame to screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (environment.equals("production")) {
            windowWidth = (int) screenSize.getWidth();
            windowHeight = (int) screenSize.getHeight();
            window.setSize(windowWidth, windowHeight);

            // maximize the frame
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        else if (environment.equals("development")) {
            windowWidth = (int) (screenSize.getWidth() * 0.5);
            windowHeight = (int) (screenSize.getHeight() * 0.5);
            window.setSize(windowWidth, windowHeight);
            window.setExtendedState(windowHeight);
        }

        GamePanel gamePanel = new GamePanel(window.getWidth(), window.getHeight());

        window.addKeyListener(inputInfo);
        gamePanel.addMouseMotionListener(inputInfo);
        gamePanel.addMouseListener(inputInfo);

        window.add(gamePanel);
        window.setVisible(true);
        gamePanel.startGameThread();
    }
}
