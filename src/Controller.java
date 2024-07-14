

import com.raylib.java.raymath.Vector2;

public interface Controller {
    public void setHost(Tank host);
    public void update();  // Updates the key strokes
    public void updateTankBuild();  // Call when tank changes or initializes build
    public boolean autoFire();
    public float barrelDirection();
    Vector2 getTarget();
    public float moveDirection();
    public boolean fire();
    public boolean holdSpecial();  // Right click (repel drones, zoom in, etc.)
    public boolean pressSpecial();
}
