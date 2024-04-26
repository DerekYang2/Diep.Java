import com.raylib.java.raymath.Vector2;

public interface Controller {
    public void setHost(Tank host);
    public float barrelDirection();

    Vector2 getTarget();

    public float moveDirection();
    public boolean fire();
}
