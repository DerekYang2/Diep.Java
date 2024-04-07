import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.shapes.rShapes;
import com.raylib.java.textures.Texture2D;
import com.raylib.java.textures.rTextures;

public class Turret {
  double x, y;
  double xOriginal, yOriginal;
  double xAbsolute, yAbsolute;
  double thetaOriginal;
  double rotatedAngle;
  int id;
  float scale;

  float turretWidth, turretLength;  // renamed variables
  double offset;
  public Texture2D testRect;
  public Rectangle srcRect;

  Turret(float width, float length, float offset, double theta, float scale) {  // renamed parameters
    this.scale = scale;
    this.turretWidth = width;  // TODO: swapped assignments RENAME!!
    this.turretLength = length;
    this.offset = offset;

    this.xOriginal = 0;
    this.yOriginal = 0 + offset * scale;
    thetaOriginal = theta;

    // Default spawn point, along the x axis with an offset from the origin
    x = xOriginal;
    y = yOriginal;

    // prevAngle = -Math.PI / 2; // spawn pointing upward
    testRect = rTextures.LoadTexture("RoundedRect.png");
    Graphics.rlj.textures.GenTextureMipmaps(testRect);
    rTextures.SetTextureFilter(testRect, RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR);
    float aspectRatio = length/width;
    System.out.println("Aspect Ratio: " + aspectRatio);
    srcRect = new Rectangle(testRect.width - testRect.height * aspectRatio, 0, testRect.height * aspectRatio, testRect.height);
  }

  public void draw() {
    drawRect((int) (x + xAbsolute), (int) (y + yAbsolute), (int) (turretLength * scale), (int) (turretWidth * scale), rotatedAngle + thetaOriginal, Main.greyCol);
/*    // Debug? TODO: what is this draw underneath
    g.setColor(Color.GREEN);
    g.fillOval((int) (x + xAbsolute) - 4, (int) (y + yAbsolute) - 4, 8, 8);*/
  }

  private void drawRect(int xleft, int ycenter, int length, int width, double theta, Color color) {  // renamed parameters
    //        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)radians, strokeCol);
    //        rShapes.DrawRectanglePro(rectangle, origin, radians * 180.f / (float) Math.PI, color);
    rTextures.DrawTexturePro(testRect, srcRect, new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)(theta * 180/Math.PI), Color.WHITE);
    //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)theta, color);
    Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width - 2 * Main.strokeWidth), new Vector2(Main.strokeWidth, (width - 2 * Main.strokeWidth)/2.f), (float)theta, color);
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
    new Bullet(x + xAbsolute, y + yAbsolute, rotatedAngle + thetaOriginal, (turretLength * scale), (turretWidth * scale));  // swapped width with length
  }
}
