import com.raylib.java.core.Color;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;

public class Bullet extends GameObject {
    protected float acceleration;
    float direction;
    int lifeFrames;
    Color fillCol;
    Color strokeCol;

    // The bullet trajectory will be determined based on the position where it spawns
    public Bullet(Tank host, float centerX, float centerY, float direction, float cannonLength, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        super(new Vector2(centerX + cannonLength * (float) Math.cos(direction), centerY + cannonLength * (float) Math.sin(direction)), (int) (diameter * 0.5f), bulletStats.absorbtionFactor, (7.f / 3 + host.stats.getStat(Stats.BULLET_DAMAGE)) * bulletStats.damage * bulletStats.absorbtionFactor);

        this.group = host.group;  // Set group to host group (TODO: make a collision and damage group)

        // Calculate bullet stats
        // https://github.com/ABCxFF/diepindepth/blob/b035291bd0bed436d0ffbe2eb707fb96ed5f2bf4/extras/stats.md?plain=1#L34
        final float damage = (7 + (3 * host.stats.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        final float maxHealth = (8 + 6 * host.stats.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        final float velMax = (20 + 3 * host.stats.getStat(Stats.BULLET_SPEED)) * bulletStats.speed - (float)Math.random() * bulletStats.scatterRate;  // src: not link above (check diepcustom repo)
        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);

        // Calculate direction
        double radianOffset = (Math.PI / 180) * bulletStats.scatterRate * (Math.random() - 0.5) * 12;  // -5 to 5 degrees times scatter rate
        this.direction = direction + (float) radianOffset;


        // Calculate acceleration to converge to max speed (https://www.desmos.com/calculator/9hakym7jxy)
        this.acceleration = (float) ((velMax * 25./120) * (1-this.friction));
        float initialSpeed = (float) ((velMax+30) * 25./120);
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Math.round(bulletStats.lifeLength * 72 * (120.f / 25));  // Lengthen because 25 fps -> 120 fps

        // Drawing variables
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
        radius = diameter * 0.5f * bulletStats.sizeRatio;  // Multiply radius by bullet stats size ratio
    }

    @Override
    public void update() {
        super.update();
        addForce(acceleration, direction);
        lifeFrames--;
        if (lifeFrames <= 0) {
            delete();
        }
    }

    @Override
    public void draw() {
        super.draw();
        if (pos.x + radius < Main.cameraBox.x || pos.x - radius > Main.cameraBox.x + Main.cameraBox.width || pos.y + radius < Main.cameraBox.y || pos.y - radius > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        Graphics.drawCircle((int) pos.x, (int) pos.y, radius, Graphics.strokeWidth, fillCol, strokeCol);
        if (radius > 45) drawHealthBar();
    }

    @Override
    public void addToPools() {
        super.addToPools();
        Main.drawablePool.addObj(this, DrawPool.BOTTOM);
    }

    @Override
    public void delete() {
        super.delete();
        Main.drawablePool.deleteObj(this.getId(), DrawPool.BOTTOM);
    }
}
