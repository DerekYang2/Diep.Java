import javax.swing.*;
import java.awt.*;

public class Main {
    // Globals
    public static Pool<Drawable> drawablePool;
    public static Pool<Updatable> updatablePool;
    public static IdServer idServer;
    public static int windowWidth, windowHeight;  // Temporary

    public static InputInfo inputInfo = new InputInfo();

    public static void initialize() {
        drawablePool = new Pool<>();
        updatablePool = new Pool<>();
        idServer = new IdServer();
        new TestObj();
    }

    public static void main(String[] args) {
        JFrame window = new JFrame("Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // set frame to screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        windowWidth = (int) screenSize.getWidth();
        windowHeight = (int) screenSize.getHeight();
        window.setSize(windowWidth, windowHeight);
        // maximize the frame
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);

        GamePanel gamePanel = new GamePanel(window.getWidth(), window.getHeight());

        window.addKeyListener(inputInfo);
        gamePanel.addMouseMotionListener(inputInfo);

        window.add(gamePanel);
        window.setVisible(true);
        gamePanel.startGameThread();
    }
}
