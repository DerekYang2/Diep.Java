import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
public class Barrel {
    protected boolean deleted = false;  // Flag to delete the barrel

    Vector2 pos;
    Vector2 posOriginal;
    float offset;

    float xAbsolute, yAbsolute;
    double angleRelative;
    double angleAbsolute;
    double scatterDegrees = 0;
    float turretWidth, turretLengthOG;  // renamed variables
    float turretLength;

    // Custom to trapezoid rendered turrets
    boolean isTrapezoid, flippedTrapezoid;  // Flipped trapezoid has smaller end at the end of barrel
    // Custom to trapper rendered turrets
    boolean isTrapper;
    // Custom to drone controlling turrets
    int maxDrones = 0;  // Maximum number of drones that can be spawned
    boolean canControlDrones;
    int droneCount = 0;  // Number of drones currently spawned

    // Recoil animation constants
    float recoilDist;
    int recoilTime;  // Time in frames for recoil animation
    int recoilFrames;

    Tank host;
    Projectile hostProjectile;  // For projectile hosts
    BulletStats bulletStats;

    // Barrel colors
    Color fillCol, strokeCol;

    Barrel(BulletStats bulletStats, float width, float length, float reload, float offset, double radians, boolean isTrapezoid, boolean flippedTrapezoid, boolean isTrapper) {  // renamed parameters
        this.bulletStats = bulletStats;

        // Default barrel colors
        fillCol = Graphics.GREY;
        strokeCol = Graphics.GREY_STROKE;

        this.turretWidth = width;
        this.turretLengthOG = turretLength = length;
        this.offset = offset;

        // Calculate recoil animation variables (https://www.desmos.com/calculator/2dgfekbtcw)
        float recoilDistFactor = (float)(9.51697 * Math.pow(0.767725, bulletStats.recoil) + 0.693587);
        recoilDist = Math.max(4.5f, 0.6f * recoilDistFactor * bulletStats.recoil);  // Minimum of 4.5 pixels (for small turrets)
        recoilTime = Math.max(15, (int) Math.round(-44.9793 * Math.pow(0.111316, reload) + 35.0069));
        recoilFrames = 0;  // Start at 0 (animation done)

        // Trapezoid turrets
        this.isTrapezoid = isTrapezoid;
        this.flippedTrapezoid = flippedTrapezoid;

        // Trapper turrets
        this.isTrapper = isTrapper;

        // Rotate (0, offset) by radians around 0, 0
        posOriginal = Graphics.rotatePoint(new Vector2(0, offset), new Vector2(0, 0), radians);

        angleRelative = radians;
    }

    public void initializeDrones(int maxDrones, boolean canControlDrones) {
        this.maxDrones = maxDrones;
        this.canControlDrones = canControlDrones;
        droneCount = 0;
    }

    public void setHost(Tank host) {
        setHost(host, null);
    }

    public void setHost(Tank host, Projectile projectileHost) {
        this.host = host;
        this.hostProjectile = projectileHost;

        // Default spawn point, along the x axis with an offset from the origin (0, 0)
        pos = Graphics.scale(posOriginal, directHost().scale);
    }

    public void setColor(Color fillCol, Color strokeCol) {
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
    }

    public void delete() {
        deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Returns the direct host of the barrel
     * If host is a projectile, return the projectile host
     * Otherwise, return the tank host
     * @return The host of the barrel
     */
    protected GameObject directHost() {
        return hostProjectile != null ? hostProjectile : host;
    }

    // https://www.desmos.com/calculator/uddosuwdt4
    private float lengthShift(int frame) {
        recoilDist = Math.min(turretLengthOG * directHost().scale * 0.07f, recoilDist);  // Maximum of 7% of turret length
        return (float) (-Math.abs(recoilDist) * Math.cos((Math.PI / recoilTime) * (frame - recoilTime * 0.5f)));
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

        // Calculate relative position by rotating xOriginal and yOriginal (scaled) around 0, 0
        pos = Graphics.rotatePoint(Graphics.scale(posOriginal, directHost().scale), new Vector2(0, 0), tankAngle);

        // Update absolute tank angle
        angleAbsolute = tankAngle + angleRelative;
        scatterDegrees = Graphics.randf(-5, 5);  // -5 to 5 degrees times scatter rate, not multiplied by bullet stats scatter rate yet

        recoilFrames--;
        if (recoilFrames < 0) {
            recoilFrames = 0;
        }
    }

    public void setDirection(double tankAngle) {
        angleAbsolute = tankAngle + angleRelative;
    }

    public void draw() {
        final int widthScaled = (int) getTurretWidth(), lengthScaled = (int) getTurretLength();
        if (host.cullingOff || Main.onScreen(new Vector2(pos.x + xAbsolute, pos.y + yAbsolute), lengthScaled)) {  // Culling
            drawTurret((int) (pos.x + xAbsolute), (int) (pos.y + yAbsolute), lengthScaled, widthScaled, angleAbsolute);
        }
    }

    private void drawTurret(int xleft, int ycenter, int length, int width, double radians) {  // renamed parameters
        //        Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)radians, strokeCol);
        //        rShapes.DrawRectanglePro(rectangle, origin, radians * 180.f / (float) Math.PI, color);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)theta, color);

        //rTextures.DrawTexturePro(testRect, srcRect, new Rectangle(xleft, ycenter, length, width), new Vector2(0, width/2.f), (float)(theta * 180/Math.PI), Main.strokeCol);
        //Graphics.drawRectangle(new Rectangle(xleft, ycenter, length, width - 2 * Graphics.strokeWidth), new Vector2(Graphics.strokeWidth, (width - 2 * Graphics.strokeWidth)/2.f), (float)theta, color);
        final float opacity = directHost().opacity;
        if (isTrapezoid) {
            Graphics.drawTurretTrapezoid(xleft, ycenter, length, width, radians, Graphics.strokeWidth, directHost().getDamageLerpColor(this.fillCol), directHost().getDamageLerpColor(this.strokeCol), opacity * opacity, flippedTrapezoid);
        } else if (isTrapper) {
            Graphics.drawTrapperTurret(xleft, ycenter, length, width, radians, Graphics.strokeWidth, directHost().getDamageLerpColor(this.fillCol), directHost().getDamageLerpColor(this.strokeCol), opacity * opacity);
        } else {
            Graphics.drawTurret(xleft, ycenter, length, width, radians, Graphics.strokeWidth, directHost().getDamageLerpColor(this.fillCol), directHost().getDamageLerpColor(this.strokeCol), opacity * opacity);
        }
    }

    /**
     * returns recoil direction vector
     * recoil magnitude is just 2 * bullet recoil (see tankdef.json)
     * @return
     */
    public Vector2 shoot() {
        return shoot(DrawPool.BOTTOM);
    }


    public Vector2 shoot(int drawLayer) {
        double scatterRadians = Math.toRadians(bulletStats.scatterRate * scatterDegrees);  // Multiply by scatter rate and convert to radians
        float finalAngle = (float)(angleAbsolute + scatterRadians);  // Add scatter to angle

        // Enhanced switch for bullet type (runs lambda expressions)
        switch (bulletStats.type) {
            case "bullet" ->
                    new Bullet(this, getSpawnPoint(), finalAngle, getTurretWidth(), bulletStats, directHost().fillCol, directHost().strokeCol, drawLayer);  // swapped width with length
            case "drone" -> {
                if (droneCount == maxDrones) {
                    return new Vector2(0, 0);  // Do not fire if max drones are spawned
                }
                new Drone(this, getSpawnPoint(), finalAngle, getTurretWidth(), bulletStats, directHost().fillCol, directHost().strokeCol);  // swapped width with length

                incrementDroneCount();  // Increment drone count
            }
            case "swarm" ->
                new Swarm(this, getSpawnPoint(), finalAngle, getTurretWidth(), bulletStats, directHost().fillCol, directHost().strokeCol);  // swapped width with length
            case "trap" ->
                    new Trap(this, getSpawnPoint(), finalAngle, getTurretWidth(), bulletStats, directHost().fillCol, directHost().strokeCol);  // swapped width with length
            case "skimmer" ->
                    new Skimmer(this, getSpawnPoint(), finalAngle, getTurretWidth(), bulletStats, directHost().fillCol, directHost().strokeCol, drawLayer);  // swapped width with length
            case "rocket" ->
                    new Rocket(this, getSpawnPoint(), finalAngle, getTurretWidth(), bulletStats, directHost().fillCol, directHost().strokeCol, drawLayer);  // swapped width with length
            case "glider" ->
                    new Glider(this, getSpawnPoint(), finalAngle, getTurretWidth(), bulletStats, directHost().fillCol, directHost().strokeCol, drawLayer);  // swapped width with length
        }

        recoilFrames = recoilTime;  // Set to max recoil time (animation)

        final float recoilMagnitude = 2 * bulletStats.recoil * (1- directHost().friction) * 10;  // (1-getHost().friction)/(1-0.9) = 10 * (1-getHost().friction), conversion from 25 fps to 120 fps
        final Vector2 recoilDirection = new Vector2((float) (-Math.cos(finalAngle)), (float) (-Math.sin(finalAngle))); // Return recoil direction, opposite of bullet direction
        return Graphics.scale(recoilDirection, recoilMagnitude);  // Scale the recoil direction
    }

    public int getMaxDrones() {
        return maxDrones;
    }
    public void incrementDroneCount() {
        droneCount++;
    }
    public void decrementDroneCount() {
        droneCount--;
    }

    public float getOffset() {
        return offset;
    }
    public float getTurretWidth() {
        return turretWidth * directHost().scale;
    }
    public float getTurretLength() {
        return turretLength * directHost().scale;
    }
    public Vector2 getSpawnPoint() {
        float turretLength = getTurretLength();
        if (isTrapper) {
            final float trapperHeight = getTurretWidth() * (3.f/1.81f);  // Longer side of the trapezoid
            final float trapperLength = Graphics.trapperHead.width * (trapperHeight / Graphics.trapperHead.height);  // Length of the trapper head, maintain texture aspect ratio
            turretLength += trapperLength;  // Add trapper head length
        }
        return new Vector2((float) (pos.x + xAbsolute + Math.cos(angleAbsolute) * turretLength), (float) (pos.y + yAbsolute + Math.sin(angleAbsolute) * turretLength));
    }

    public void setPos(Vector2 pos) {
        // Redraw Turret in new position
        xAbsolute = pos.x;
        yAbsolute = pos.y;
    }
}
