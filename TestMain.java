/*
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TestMain extends JPanel {
    private BufferedImage buffer;
    static int bufferWidth = 1920; // Initial width
    static int bufferHeight = 1080; // Initial height
    public TestMain() {
        // Create the buffer with initial dimensions (adjust as needed)
        buffer = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);

        // Draw shapes onto the buffer (using relative coordinates)
        Graphics2D g2d = buffer.createGraphics();
        // Set the background color
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, bufferWidth, bufferHeight);
        g2d.setColor(Color.RED);
        g2d.fillRect(100, 100, 50, 40); // Example: Draw a red rectangle
        g2d.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Scale the buffer to match the panel's size
        Graphics2D g2d = (Graphics2D) g;
        // Height of the current panel
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        // Maintain image aspect ratio

        // Ratio that the image needs to be scaled down by, find the dimension that scaled down the most from the original. 
        float scaleFactor = Math.min((float) panelWidth / buffer.getWidth(), (float) panelHeight / buffer.getHeight());
        int scaledWidth, scaledHeight;
        // Change the height and widht by the same ratio
        scaledWidth = (int) (buffer.getWidth() * scaleFactor);
        scaledHeight = (int) (buffer.getHeight() * scaleFactor);
        System.out.println("scaledHeight: " + scaledHeight + " " +  "scaledWidth: " + scaledWidth);
        // The difference between the panel and the actual size of the screen / 2 splits the empty space (black bars) evenly 
        int x = (panelWidth - scaledWidth) / 2;
        int y = (panelHeight - scaledHeight) / 2;
        // x, y are the corners of the frame, redraw the image with this scale at x, y
        g2d.drawImage(buffer, x, y, scaledWidth, scaledHeight, null);

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Scalable Panel with Buffer Example");
        JPanel panel = new TestMain();
        frame.add(panel);
        // set color
        panel.setBackground(Color.BLACK);
        // maximize window
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // set size to be the same as the buffer
        panel.setPreferredSize(new Dimension(bufferWidth, bufferHeight));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}*/
