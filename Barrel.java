import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
public class Barrel {
    double x, y;
    double xOriginal, yOriginal;
    double xAbsolute, yAbsolute;
    double angleRelative;
    double angleAbsolute;
    int id;

    int recoilFrames = 0;

    float turretWidth, turretLengthOG;  // renamed variables
    float turretLength;

    // Recoil animation constants
    final int recoilTime = 30;  // Time in frames for recoil animation
    final float recoilLengthFactor = 0.1f;  // Percent of turret width to reduce in recoil animation
    Tank host;  // For color and other things that may appear in the future

    Barrel(float width, float length, float offset, double radians) {  // renamed parameters
        this.turretWidth = width;
        this.turretLengthOG = turretLength = length;

        this.xOriginal = 0;
        this.yOriginal = offset;
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
        x = xOriginal * host.scale;
        y = yOriginal * host.scale;
    }

    public void draw() {
        drawRect((int) (x + xAbsolute), (int) (y + yAbsolute), (int) (turretLength * host.scale), (int) (turretWidth * host.scale), angleAbsolute + angleRelative);
    }

    private void drawRect(int xleft, int ycenter, int length, int width, double radians) {  // renamed parameters
        //        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)radians, strokeCol);
        //        rShapes.DrawRectanglePro(rectangle, origin, radians * 180.f / (float) Math.PI, color);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)theta, color);

        //rTextures.DrawTexturePro(testRect, srcRect, new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)(theta * 180/Math.PI), Main.strokeCol);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width - 2 * Graphics.strokeWidth), new Vector2(Graphics.strokeWidth, (width - 2 * Graphics.strokeWidth)/2.f), (float)theta, color);
        Graphics.drawRoundedRect(xleft, ycenter, length, width, radians, Graphics.strokeWidth, Graphics.GREY, Graphics.GREY_STROKE);
    }

    // https://www.desmos.com/calculator/uddosuwdt4
    private float lengthShift(int frame) {
        float dist = recoilLengthFactor * (turretWidth * host.scale);  // Even though its length shift, base on width because more width = stronger turret
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
        angleAbsolute = tankAngle;
        // Calculate new position by rotating xOriginal and yOriginal around 0, 0
        x = (xOriginal * host.scale) * Math.cos(angleAbsolute) - (yOriginal * host.scale) * Math.sin(angleAbsolute);
        y = (xOriginal * host.scale) * Math.sin(angleAbsolute) + (yOriginal * host.scale) * Math.cos(angleAbsolute);

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

        Bullet b = new Bullet(host, (float) (x + xAbsolute), (float) (y + yAbsolute), bulletAngle, (turretLength * host.scale), (turretWidth * host.scale), bulletStats, host.fillCol, host.strokeCol);  // swapped width with length

        float recoilMagnitude = 2 * bulletStats.recoil * (1-host.friction) * 10;  // (1-host.friction)/(1-0.9) = 10 * (1-host.friction), conversion from 25 fps to 120 fps
        Vector2 recoilDirection = new Vector2((float) (-Math.cos(bulletAngle)), (float) (-Math.sin(bulletAngle))); // Return recoil direction, opposite of bullet direction
        return Raymath.Vector2Scale(recoilDirection, recoilMagnitude);  // Scale the recoil direction
    }
}
