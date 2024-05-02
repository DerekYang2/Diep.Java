import com.raylib.java.raymath.Vector2;

public interface Controller {
    public void setHost(Tank host);
    public void update();  // Updates the key strokes
    public void updateTankBuild();  // Call when tank changes or initializes build
    public boolean toggleAutoFire();
    public float barrelDirection();
    Vector2 getTarget();
    public float moveDirection();
    public boolean fire();
    public boolean special();  // Right click (repel drones, zoom in, etc.)
}
