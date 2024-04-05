import javax.swing.JPanel;
import java.awt.*;

class FPSDraw {
    private long lastFpsTime;
    int cur_fps, frame_count;

    public FPSDraw() {
        lastFpsTime = System.nanoTime();
        cur_fps = 0;
        frame_count = 0;
    }

    public void draw(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        frame_count++;
        // If the current time and the last time the fps was caluclated is greater than 1s, print the amount of frames that were rendered in that second.
        if (System.nanoTime() - lastFpsTime > 1e9) {
            lastFpsTime = System.nanoTime();
            cur_fps = frame_count;
            frame_count = 0;
        }
        g.drawString("FPS: " + cur_fps, x, y);
    }
}

public class GamePanel extends JPanel implements Runnable {
    final int FPS = 60;
    FPSDraw fpsDraw = new FPSDraw();
    int width, height;
    Thread gameThread;

    public static double scaleFactor;
    public static int cameraX, cameraY;
    public static int scaledWidth, scaledHeight;

    public GamePanel(int width, int height) {
        this.width = width;
        this.height = height;
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
    }

    private void update() {
        // Handle the pending operations
        Main.drawablePool.refresh();
        Main.updatablePool.refresh();

        // Update all the updatable objects
        for (Updatable updatable : Main.updatablePool.getObjects()) {
            updatable.update();
        }


        // Dimensions of current game panel
        double panelWidth = getWidth();
        double panelHeight = getHeight();
        // Calculate how much it needs to be scaled down by
        scaleFactor = Math.min(panelWidth / 1920.0, panelHeight / 1080.0);

        // Calculate new dimensions for the board
        scaledWidth = (int) (1920 * scaleFactor);
        scaledHeight = (int) (1080 * scaleFactor);


        // Difference between window width and the size of the game panel, then move the top left corner to center the camera
        cameraX = (int) (panelWidth - scaledWidth) / 2; 
        cameraY = (int) (panelHeight - scaledHeight) / 2;
    }
    private void draw(Graphics g) {
        // Draw circle at mouse pos
        g.setColor(Color.WHITE);
        g.fillRect(Main.inputInfo.mouseX, Main.inputInfo.mouseY, 5, 5);
        // Click test
        if (Main.inputInfo.attackPressed) {
            g.setColor(Color.RED);
            g.fillOval(Main.inputInfo.mouseX - 10, Main.inputInfo.mouseY - 10, 20, 20);
        }
        // Draw fps
        fpsDraw.draw(g, 10, 10);
        // Number of objects
        g.setColor(Color.WHITE);
        g.drawString("Number of objects: " + Main.drawablePool.getObjects().size(), 10, 20);
        // Draw all the drawable objects
        for (Drawable drawable : Main.drawablePool.getObjects()) {
            drawable.draw(g);
        }
    }

    public void startGameThread() {
        // Initialize
        Main.initialize();
        gameThread = new Thread(this);
        // Start the game thread
        gameThread.start();
    }

    @Override
    // run is called when the thread is started 
    public void run() {
        // Update the game every 1s / FPS nano seconds 
        double updateInterval = 1e9 / FPS;
        double nextTime = System.nanoTime() + updateInterval;
        // Run the game loop
        while (gameThread != null) {
            if (System.nanoTime() >= nextTime) {
                // On every frame update all updatables 
                update();
                // On every frame call paintComponent 
                repaint();
                nextTime += updateInterval;
            }
            double remainingTime = Math.max(0, nextTime - System.nanoTime());
            remainingTime = remainingTime / 1e6;  // convert to milliseconds
            try {
                Thread.sleep((long)remainingTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Drawing all drawables
        draw(g);
    }
}