import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
public class Barrel {
    Vector2 pos;
    Vector2 posOriginal;

    float xAbsolute, yAbsolute;
    double angleRelative;
    double angleAbsolute;

    int recoilFrames = 0;

    float turretWidth, turretLengthOG;  // renamed variables
    float turretLength;

    boolean isTrapezoid, flippedTrapezoid;  // Flipped trapezoid has smaller end at the end of barrel

    // Recoil animation constants
    final int recoilTime = 30;  // Time in frames for recoil animation
    final float recoilLengthFactor = 0.1f;  // Percent of turret width to reduce in recoil animation
    Tank host;  // For color and other things that may appear in the future

    Barrel(float width, float length, float offset, double radians, boolean isTrapezoid, boolean flippedTrapezoid) {  // renamed parameters
        this.turretWidth = width;
        this.turretLengthOG = turretLength = length;
        this.isTrapezoid = isTrapezoid;
        this.flippedTrapezoid = flippedTrapezoid;

        // Rotate (0, offset) by radians around 0, 0
        posOriginal = Graphics.rotatePoint(new Vector2(0, offset), new Vector2(0, 0), radians);

        angleRelative = radians;

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
        pos = Raymath.Vector2Scale(posOriginal, host.scale);
    }

    public void draw() {
        drawRect((int) (pos.x + xAbsolute), (int) (pos.y + yAbsolute), (int) (turretLength * host.scale), (int) (turretWidth * host.scale), angleAbsolute + angleRelative);
    }

    private void drawRect(int xleft, int ycenter, int length, int width, double radians) {  // renamed parameters
        //        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)radians, strokeCol);
        //        rShapes.DrawRectanglePro(rectangle, origin, radians * 180.f / (float) Math.PI, color);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)theta, color);

        //rTextures.DrawTexturePro(testRect, srcRect, new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)(theta * 180/Math.PI), Main.strokeCol);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width - 2 * Graphics.strokeWidth), new Vector2(Graphics.strokeWidth, (width - 2 * Graphics.strokeWidth)/2.f), (float)theta, color);
        if (isTrapezoid) {
            Graphics.drawTurretTrapezoid(xleft, ycenter, length, width, radians, Graphics.strokeWidth, Graphics.GREY, Graphics.GREY_STROKE, (float)Math.pow(host.opacity,3), flippedTrapezoid);
        } else {
            Graphics.drawTurret(xleft, ycenter, length, width, radians, Graphics.strokeWidth, Graphics.GREY, Graphics.GREY_STROKE, (float) Math.pow(host.opacity, 3));  // Square host.opacity for a steeper curve (x^3)
        }
    }

    // https://www.desmos.com/calculator/uddosuwdt4
    private float lengthShift(int frame) {
        float dist = recoilLengthFactor * (turretWidth * host.scale);  // Even though its length shift, base on width because more width = stronger turret
        return (float) (-Math.abs(dist) * Math.cos((Math.PI / recoilTime) * (frame - recoilTime * 0.5f)));
    }

    public void update(float xAbs, float yAbs, double tankAngle) {
        // Calculate turret length
        if (recoilFrames > 0) {
            turretLength = turretLengthOG + lengthShift(recoilFrames);
        } else {
            turretLength = turretLengthOG;
        }

        // Redraw Turret in new position
        xAbsolute = xAbs;
        yAbsolute = yAbs;
        angleAbsolute = tankAngle;

        // Calculate new RELATIVE position by rotating xOriginal and yOriginal (scaled) around 0, 0
        pos = Graphics.rotatePoint(Raymath.Vector2Scale(posOriginal, host.scale), new Vector2(0, 0), angleAbsolute);

        recoilFrames--;
        if (recoilFrames < 0) {
            recoilFrames = 0;
        }
    }

    /**
     * returns recoil direction vector
     * recoil magnitude is just 2 * bullet recoil (see tankdef.json)
     * @return
     */
    public Vector2 shoot(BulletStats bulletStats) {
        recoilFrames = recoilTime;  // Set to max recoil time

        double scatterAngle = Math.toRadians(bulletStats.scatterRate * (Math.random() - 0.5) * 10);  // -5 to 5 degrees times scatter rate
        float bulletAngle = (float) (angleAbsolute + angleRelative + scatterAngle);  // Apply scatter angle to bullet angle

        Bullet b = new Bullet(host, pos.x + xAbsolute, pos.y + yAbsolute, bulletAngle, (turretLength * host.scale), (turretWidth * host.scale), bulletStats, host.fillCol, host.strokeCol);  // swapped width with length

        float recoilMagnitude = 2 * bulletStats.recoil * (1-host.friction) * 10;  // (1-host.friction)/(1-0.9) = 10 * (1-host.friction), conversion from 25 fps to 120 fps
        Vector2 recoilDirection = new Vector2((float) (-Math.cos(bulletAngle)), (float) (-Math.sin(bulletAngle))); // Return recoil direction, opposite of bullet direction
        return Raymath.Vector2Scale(recoilDirection, recoilMagnitude);  // Scale the recoil direction
    }
}
