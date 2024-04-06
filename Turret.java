import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.shapes.rShapes;

public class Turret {
  double x, y;
  double xOriginal, yOriginal;
  double xAbsolute, yAbsolute;
  double thetaOriginal;
  double rotatedAngle;
  int id;

  float turretWidth, turretLength;  // renamed variables
  double offset;

  Turret(float width, float length, float offset, double theta) {  // renamed parameters
    this.turretWidth = width;  // TODO: swapped assignments RENAME!!
    this.turretLength = length;

    this.offset = offset;

    this.xOriginal = 0;
    this.yOriginal = 0 + offset;
    thetaOriginal = theta;

    // Default spawn point, along the x axis with an offset from the origin
    x = xOriginal;
    y = yOriginal;

    // prevAngle = -Math.PI / 2; // spawn pointing upward
  }

  public void draw() {
    drawRect((int) (x + xAbsolute), (int) (y + yAbsolute), (int) turretLength, (int) turretWidth, rotatedAngle + thetaOriginal, Color.GRAY);
/*    // Debug? TODO: what is this draw underneath
    g.setColor(Color.GREEN);
    g.fillOval((int) (x + xAbsolute) - 4, (int) (y + yAbsolute) - 4, 8, 8);*/
  }

  private static void drawRect(int xleft, int ycenter, int length, int width, double theta, Color color) {  // renamed parameters
/*
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(color);

    // Create an AffineTransform object
    AffineTransform at = new AffineTransform();
    at.translate(xleft, ycenter);
    // Rotate to the cursor
    at.rotate(theta);
    // Centering graphic draw origin
    at.translate(0, -width / 2.f);  // swapped width and height

    // Apply the transform to the Graphics2D object
    g2d.setTransform(at);

    // Draw the rectangle
    g2d.fillRect(0, 0, length, width);  // swapped width and height

    // Reset the transformations
    g2d.setTransform(new AffineTransform());
*/

    rShapes.DrawRectanglePro(new Rectangle((float) xleft, (float) ycenter, length, width), new Vector2(0, width/2.f), (float) (theta * 180.f / Math.PI), Color.GRAY);
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
    // Spawn at the end of the turret FIX THIS
    new Bullet(x + xAbsolute, y + yAbsolute, rotatedAngle + thetaOriginal, turretLength, turretWidth);  // swapped width with length
  }
}
