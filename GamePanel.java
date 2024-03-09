import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    final int UPDATE_FPS = 120;
    final int DRAW_FPS = 120;
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
        double updateInterval = 1e9 / UPDATE_FPS;
        double drawInterval = 1e9 / DRAW_FPS;
        double nextUpdateTime = System.nanoTime() + updateInterval;
        double nextDrawTime = System.nanoTime() + drawInterval;
        // Run the game loop
        while (gameThread != null) {
            if (System.nanoTime() >= nextUpdateTime) {
                update();
                nextUpdateTime += updateInterval;
            }
            if (System.nanoTime() >= nextDrawTime) {
                repaint();
                nextDrawTime += drawInterval;
            }
            double remainingTime = Math.max(0, Math.min(nextUpdateTime, nextDrawTime) - System.nanoTime());
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