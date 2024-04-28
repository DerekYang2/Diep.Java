import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
public abstract class Projectile extends GameObject {
    protected float acceleration;
    float direction;
    int lifeFrames;
    Color fillCol;
    Color strokeCol;
    Tank host;
    BulletStats bulletStats;

    // The bullet trajectory will be determined based on the position where it spawns
    public Projectile(Barrel hostBarrel, float centerX, float centerY, float direction, float cannonLength, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(new Vector2(centerX + cannonLength * (float) Math.cos(direction), centerY + cannonLength * (float) Math.sin(direction)), (int) (diameter * 0.5f), 1f, drawLayer);
        this.direction = direction;  // Calculate direction (scatter angle already applied by Barrel.java)
        this.bulletStats = bulletStats;
        this.host = hostBarrel.host;
        this.group = host.group;  // Set group to host group (TODO: make a collision and damage group)

        updateStats();

        // Drawing variables
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        radius = diameter * 0.5f * bulletStats.sizeRatio;  // Multiply radius by bullet stats size ratio
    }

    /**
     * Only differs whether to keep in arena or not
     */
    @Override
    protected void setFlags() {
        super.noInternalCollision = true;
        super.isProjectile = true;
    }

    @Override
    public void update() {
        super.update();

        if (isDead) return;

        if (host.isDead) {  // If host is dead, delete bullet
            triggerDelete();
            return;
        }

        addForce(acceleration, direction);
        lifeFrames--;
        if (lifeFrames <= 0) {
            triggerDelete();
        }
    }
}
