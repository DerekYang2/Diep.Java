import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Texture2D;

public class Turret {
  double x, y;
  double xOriginal, yOriginal;
  double xAbsolute, yAbsolute;
  double thetaOriginal;
  double rotatedAngle;
  int id;
  float scale;

  int recoilFrames = 0;

  float turretWidth, turretLengthOG;  // renamed variables
  float turretLength;

  double offset;
  public Texture2D testRect;
  public Rectangle srcRect;

  // Recoil constants, single turret should not fire in less than 8 frames for now
  final int recoilTime = 8;
  final float recoilFactor = 0.08f, retCoeff = 6.f/recoilTime;


  Turret(float width, float length, float offset, double degrees, float scale) {  // renamed parameters
    this.scale = scale;
    this.turretWidth = width;  // TODO: swapped assignments RENAME!!
    this.turretLengthOG = turretLength = length;
    this.offset = offset;

    this.xOriginal = 0;
    this.yOriginal = 0 + offset * scale;
    thetaOriginal = degrees * (Math.PI / 180);

    // Default spawn point, along the x axis with an offset from the origin
    x = xOriginal;
    y = yOriginal;

    // prevAngle = -Math.PI / 2; // spawn pointing upward
/*    testRect = rTextures.LoadTexture("whiteRect.png");
    Graphics.rlj.textures.GenTextureMipmaps(testRect);
    rTextures.SetTextureFilter(testRect, RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR);
    float aspectRatio = length/width;
    System.out.println("Aspect Ratio: " + aspectRatio);
    srcRect = new Rectangle(testRect.width - testRect.height * aspectRatio, 0, testRect.height * aspectRatio, testRect.height);*/
  }

  public void draw() {
    drawRect((int) (x + xAbsolute), (int) (y + yAbsolute), (int) (turretLength * scale), (int) (turretWidth * scale), rotatedAngle + thetaOriginal);
/*    // Debug? TODO: what is this draw underneath
    g.setColor(Color.GREEN);
    g.fillOval((int) (x + xAbsolute) - 4, (int) (y + yAbsolute) - 4, 8, 8);*/
  }

  private void drawRect(int xleft, int ycenter, int length, int width, double radians) {  // renamed parameters
    //        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)radians, strokeCol);
    //        rShapes.DrawRectanglePro(rectangle, origin, radians * 180.f / (float) Math.PI, color);
    //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)theta, color);

    //rTextures.DrawTexturePro(testRect, srcRect, new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)(theta * 180/Math.PI), Main.strokeCol);
    //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width - 2 * Main.strokeWidth), new Vector2(Main.strokeWidth, (width - 2 * Main.strokeWidth)/2.f), (float)theta, color);
    Graphics.drawRoundedRect(xleft, ycenter, length, width, radians, Main.strokeWidth, Main.greyCol, Main.greyStroke);
  }

  // https://www.desmos.com/calculator/ikwpyuj8ny
  private float lengthShift(int frame) {
    float dist = recoilFactor * (turretWidth * scale);  // Even though its length shift, base on width because more width = stronger turret
    if (0 <= frame && frame < retCoeff * recoilTime) {
      return -Math.abs(dist/(retCoeff * recoilTime)) * frame;
    } else {
      return -Math.abs(dist/((1-retCoeff) * recoilTime)) * (frame - retCoeff * recoilTime) + dist;
    }
  }

  public void update(double xAbs, double yAbs, double tankAngle) {
    // Calculate turret length
    if (recoilFrames > 0) {
      turretLength = turretLengthOG + lengthShift(recoilFrames);
    } else {
      turretLength = turretLengthOG;
    }

    // Redraw Turret in new position
    xAbsolute = xAbs;
    yAbsolute = yAbs;
    rotatedAngle = tankAngle;
    // Calculate new position by rotating xOriginal and yOriginal around 0, 0
    x = xOriginal * Math.cos(rotatedAngle) - yOriginal * Math.sin(rotatedAngle);
    y = xOriginal * Math.sin(rotatedAngle) + yOriginal * Math.cos(rotatedAngle);

    recoilFrames--;
    if (recoilFrames < 0) {
      recoilFrames = 0;
    }
  }

  public void shoot() {
    recoilFrames = recoilTime;  // Set to max recoil time
    // Spawn at the end of the turret FIX THIS
    new Bullet(x + xAbsolute, y + yAbsolute, rotatedAngle + thetaOriginal, (turretLength * scale), (turretWidth * scale));  // swapped width with length
  }
}
