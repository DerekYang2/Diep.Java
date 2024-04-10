import com.raylib.java.raymath.Vector2;

public class Bullet extends GameObject {
    protected float acceleration = 0.2f;
    float direction;
    int lifeFrames = 120 * 3;

    // The bullet trajectory will be determined based on the position where it spawns
    public Bullet(float centerX, float centerY, float direction, float cannonLength, float diameter) {
        super(new Vector2(centerX + cannonLength * (float) Math.cos(direction), centerY + cannonLength * (float) Math.sin(direction)), (int) (diameter * 0.5f), 1, 1.4f);
        radius = diameter * 0.5f;
        float initialSpeed = (float) ((1.0/(1-friction)) * acceleration);
        vel = new Vector2(initialSpeed * (float) Math.cos(direction), initialSpeed * (float) Math.sin(direction));
        this.direction = direction;
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
        Graphics.drawCircle((int) pos.x, (int) pos.y, radius, Graphics.strokeWidth, Graphics.BLUE, Graphics.BLUE_STROKE);
    }

  @Override
  public void createId() {
    this.id = Main.idServer.getIdFront();
  }
}
