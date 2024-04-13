import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Texture2D;

public class Barrel {
    double x, y;
    double xOriginal, yOriginal;
    double xAbsolute, yAbsolute;
    double thetaOriginal;
    double rotatedAngle;
    int id;

    int recoilFrames = 0;

    float turretWidth, turretLengthOG;  // renamed variables
    float turretLength;

    public Texture2D testRect;
    public Rectangle srcRect;

    // Recoil constants, single turret should not fire in less than 8 frames for now
    final int recoilTime = 25;
    final float recoilFactor = 0.1f;
    final float recoilForceFactor = 0.03f;
    Tank host;  // For color and other things that may appear in the future

    Barrel(float width, float length, float offset, double radians) {  // renamed parameters
        this.turretWidth = width;
        this.turretLengthOG = turretLength = length;

        this.xOriginal = 0;
        this.yOriginal = offset;
        thetaOriginal = radians;

        // prevAngle = -Math.PI / 2; // spawn pointing upward
/*    testRect = rTextures.LoadTexture("whiteRect.png");
    Graphics.rlj.textures.GenTextureMipmaps(testRect);
    rTextures.SetTextureFilter(testRect, RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR);
    float aspectRatio = length/width;
    System.out.println("Aspect Ratio: " + aspectRatio);
    srcRect = new Rectangle(testRect.width - testRect.height * aspectRatio, 0, testRect.height * aspectRatio, testRect.height);*/
    }

    public void setHost(Tank host) {
        this.host = host;
        // Default spawn point, along the x axis with an offset from the origin (0, 0)
        x = xOriginal * host.scale;
        y = yOriginal * host.scale;
    }

    public void draw() {
        drawRect((int) (x + xAbsolute), (int) (y + yAbsolute), (int) (turretLength * host.scale), (int) (turretWidth * host.scale), rotatedAngle + thetaOriginal);
/*    // Debug? TODO: what is this draw underneath
    g.setColor(Color.GREEN);
    g.fillOval((int) (x + xAbsolute) - 4, (int) (y + yAbsolute) - 4, 8, 8);*/
    }

    private void drawRect(int xleft, int ycenter, int length, int width, double radians) {  // renamed parameters
        //        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)radians, strokeCol);
        //        rShapes.DrawRectanglePro(rectangle, origin, radians * 180.f / (float) Math.PI, color);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)theta, color);

        //rTextures.DrawTexturePro(testRect, srcRect, new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)(theta * 180/Math.PI), Main.strokeCol);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width - 2 * Graphics.strokeWidth), new Vector2(Graphics.strokeWidth, (width - 2 * Graphics.strokeWidth)/2.f), (float)theta, color);
        Graphics.drawRoundedRect(xleft, ycenter, length, width, radians, Graphics.strokeWidth, Graphics.GREY, Graphics.GREY_STROKE);
    }

    // https://www.desmos.com/calculator/ikwpyuj8ny
    private float lengthShift(int frame) {
/*    float dist = recoilFactor * (turretWidth * scale);  // Even though its length shift, base on width because more width = stronger turret
    if (0 <= frame && frame < retCoeff * recoilTime) {
      return -Math.abs(dist/(retCoeff * recoilTime)) * frame;
    } else {
      return -Math.abs(dist/((1-retCoeff) * recoilTime)) * (frame - retCoeff * recoilTime) + dist;
    }*/
        float dist = recoilFactor * (turretWidth * host.scale);  // Even though its length shift, base on width because more width = stronger turret
        return (float) (-Math.abs(dist) * Math.cos((Math.PI / recoilTime) * (frame - recoilTime * 0.5f)));
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
        x = (xOriginal * host.scale) * Math.cos(rotatedAngle) - (yOriginal * host.scale) * Math.sin(rotatedAngle);
        y = (xOriginal * host.scale) * Math.sin(rotatedAngle) + (yOriginal * host.scale) * Math.cos(rotatedAngle);

        recoilFrames--;
        if (recoilFrames < 0) {
            recoilFrames = 0;
        }
    }

    /**
     * TODO: attack or boost type turret shoudl have different factor
     *
     * @param turretWidth
     * @return
     */
    private float recoilForceFunction(float turretWidth) {
        float width = turretWidth * host.scale;
        return recoilForceFactor * ((width - 21) * (width - 21));
    }

    /**
     * returns recoil direction vector
     *
     * @return
     */
    public Vector2 shoot(BulletStats bulletStats) {
        recoilFrames = recoilTime;  // Set to max recoil time
        System.out.println(Main.counter);
        Bullet b = new Bullet(host, (float) (x + xAbsolute), (float) (y + yAbsolute), (float) (rotatedAngle + thetaOriginal), (turretLength * host.scale), (turretWidth * host.scale), bulletStats, host.fillCol, host.strokeCol);  // swapped width with length
        // Return recoil direction
/*    Vector2 recoilDirection = new Vector2((float) (-Math.cos(rotatedAngle + thetaOriginal)), (float) (-Math.sin(rotatedAngle + thetaOriginal)));
    return Raymath.Vector2Scale(recoilDirection, recoilForceFunction(turretWidth));  // Scale the recoil direction*/
        return new Vector2(0, 0);
    }
}
