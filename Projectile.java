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

    /**
     * Creates a projectile game object and spawns right away
     * @param hostBarrel The pointer to the barrel that fired this projectile
     * @param spawnPos The position where the projectile spawns (center position of projectile)
     * @param direction The direction the projectile is fired (radians)
     * @param diameter The diameter of the projectile
     * @param bulletStats The bullet stats of the projectile
     * @param fillCol The fill color of the projectile
     * @param strokeCol The stroke color of the projectile
     * @param drawLayer The draw layer of the projectile
     */
    public Projectile(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer, float friction) {
        super(spawnPos, (int) (diameter * 0.5f), 1f, drawLayer);
        this.direction = direction;  // Calculate direction (scatter angle already applied by Barrel.java)
        this.bulletStats = bulletStats;
        this.host = hostBarrel.host;
        this.group = host.group;  // Set group to host group (TODO: make a collision and damage group)
        this.friction = friction;
        updateStats();

        // Drawing variables
        this.fillCol = fillCol;
        
        this.strokeCol = strokeCol;
        radius = diameter * 0.5f * bulletStats.sizeRatio;  // Multiply radius by bullet stats size ratio
    }

    public Projectile(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol, int drawLayer) {
        super(spawnPos, (int) (diameter * 0.5f), 1f, drawLayer);
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

    public abstract float getMaxSpeed();

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

    @Override
    protected float getScoreReward() {
        return 0;
    }
}
