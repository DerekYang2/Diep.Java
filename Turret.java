import java.awt.*;
import java.awt.geom.AffineTransform;


public class Turret {
  double x, y;
  double xOriginal, yOriginal;
  double xAbsolute, yAbsolute;
  double thetaOriginal;
  double rotatedAngle;
  int id;

  Stopwatch stopwatch;

  int width, length;
  double offset;


  // Debug
  double lastPrint;
  int timesPrinted = 0;

  Turret(int length, int width, int offset, double theta) {
    this.width = width;
    this.length = length;

    this.offset = offset;

    this.xOriginal = 0;
    this.yOriginal = 0 + offset;
    thetaOriginal = theta;

    stopwatch = new Stopwatch();
    stopwatch.start();

    // Default spawn point, along the x axis with an offset from the origin 
    x = xOriginal;
    y = yOriginal;

    lastPrint = stopwatch.ms();
    // prevAngle = -Math.PI / 2; // spawn pointing upward
  }  

  public void draw(Graphics g) {
    drawRect(g, (int) (x + xAbsolute), (int) (y + yAbsolute), width, length, rotatedAngle + thetaOriginal, Color.GRAY);
    g.setColor(Color.GREEN);
    g.fillOval((int) (x + xAbsolute) - 4, (int) (y + yAbsolute) - 4, 8, 8);
  }

  private static void drawRect(Graphics g, int xleft, int ycenter, int width, int height, double theta, Color color) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(color);

      // Create an AffineTransform object
      AffineTransform at = new AffineTransform();
      at.translate(xleft, ycenter);
      // Rotate to the cursor
      at.rotate(theta);
      // Centering graphic draw origin
      at.translate(0, -height / 2.f);

      // Apply the transform to the Graphics2D object
      g2d.setTransform(at);

      // Draw the rectangle
      g2d.fillRect(0, 0, width, height);

      // Reset the transformations
      g2d.setTransform(new AffineTransform());
  }

  public void update(double xAbs, double yAbs, double tankAngle) {
    // Redraw Turret in new position
    xAbsolute = xAbs;
    yAbsolute = yAbs;
    rotatedAngle = tankAngle;
    // Calculate new position by rotating xOriginal and yOriginal around 0, 0
    x = xOriginal * Math.cos(rotatedAngle) - yOriginal * Math.sin(rotatedAngle);
    y = xOriginal * Math.sin(rotatedAngle) + yOriginal * Math.cos(rotatedAngle);
  }

  public void shoot() {
    // Spawn at the end of the turret
    new Bullet(x + xAbsolute, y + yAbsolute, rotatedAngle + thetaOriginal, width);
  }
}

