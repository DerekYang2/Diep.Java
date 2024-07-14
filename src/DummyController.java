

import com.raylib.java.raymath.Vector2;

public class DummyController implements Controller {

    @Override
    public void setHost(Tank host) {

    }

    @Override
    public void update() {

    }

    @Override
    public void updateTankBuild() {

    }

    @Override
    public boolean autoFire() {
        return false;
    }

    @Override
    public float barrelDirection() {
        return 0;
    }

    @Override
    public Vector2 getTarget() {
        return null;
    }

    @Override
    public float moveDirection() {
        return 0;
    }

    @Override
    public boolean fire() {
        return false;
    }

    @Override
    public boolean holdSpecial() {
        return false;
    }

    @Override
    public boolean pressSpecial() {
        return false;
    }
}
