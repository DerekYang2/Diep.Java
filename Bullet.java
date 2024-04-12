import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Bullet extends GameObject {
    protected float acceleration = 0.2f;
    float direction;
    int lifeFrames = 120 * 3;

    Color fillCol;
    Color strokeCol;

    // The bullet trajectory will be determined based on the position where it spawns
    public Bullet(float centerX, float centerY, float direction, float cannonLength, float diameter, Color fillCol, Color strokeCol) {
        super(new Vector2(centerX + cannonLength * (float) Math.cos(direction), centerY + cannonLength * (float) Math.sin(direction)), (int) (diameter * 0.5f), 1, 1.4f);
        this.fillCol = fillCol;
        this.strokeCol = strokeCol;
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
        if (pos.x < Main.cameraBox.x || pos.x > Main.cameraBox.x + Main.cameraBox.width || pos.y < Main.cameraBox.y || pos.y > Main.cameraBox.y + Main.cameraBox.height) {
            return;
        }
        Graphics.drawCircle((int) pos.x, (int) pos.y, radius, Graphics.strokeWidth, fillCol, strokeCol);
    }

  @Override
  public void createId() {
    this.id = Main.idServer.getIdFront();
  }
}
