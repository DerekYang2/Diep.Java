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
        Main.idServer.refresh();
        // Update all the updatable objects
        for (Updatable updatable : Main.updatablePool.getObjects()) {
            updatable.update();
        }
    }
    private void draw(Graphics g) {
        // Draw circle at mouse pos
        g.setColor(Color.WHITE);
        g.fillRect(Main.inputInfo.mouseX, Main.inputInfo.mouseY, 5, 5);
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
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Initialize
        Main.initialize();
        double updateInterval = 1e9 / FPS;
        double nextTime = System.nanoTime() + updateInterval;
        // Run the game loop
        while (gameThread != null) {
            if (System.nanoTime() >= nextTime) {
                update();
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
        draw(g);
    }
}