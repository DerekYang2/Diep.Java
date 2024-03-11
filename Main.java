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

    public static void initialize() {
        globalClock.start();
        drawablePool = new Pool<>();
        updatablePool = new Pool<>();
        idServer = new IdServer();
        new TestObj();
        new Square();
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

        System.out.println("Running");

        window.add(gamePanel);
        window.setVisible(true);
        gamePanel.startGameThread();
    }

    public static Graphics2D drawRectCustom(Graphics g, double x, double y, int width, int length, double theta) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);

        int rectWidth = width;
        int rectHeight = length;

        // Create an AffineTransform object
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        // Rotate to the cursor
        at.rotate(theta);
        // Move the transform back
        at.translate(0, -rectHeight / 2.f);

        // Apply the transform to the Graphics2D object
        g2d.setTransform(at);

        // Draw the rectangle
        g2d.fillRect(0, 0, rectWidth, rectHeight);

        // Reset the transformations
        g2d.setTransform(new AffineTransform());
        return g2d;
    }
}
