import java.util.ArrayList;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Keyboard.KEY_D;

public class PlayerController implements Controller {
    ArrayList<Integer> keyQueue;
    Tank host;

    public PlayerController() {
        keyQueue = new ArrayList<>();
    }

    public void setHost(Tank host) {
        this.host = host;
    }

    @Override
    public float barrelDirection() {
        return (float) Math.atan2(Graphics.getVirtualMouse().y - host.pos.y, Graphics.getVirtualMouse().x - host.pos.x);
    }

    /**
     * Method is required so that if a user holds more than two keys, only the most recent two are used
     * @param key
     * @return
     */
    private boolean isKeyDown(int key) {
        // Only check the latest two keys in the queue
        if (keyQueue.size() > 1) {
            return keyQueue.get(keyQueue.size() - 1) == key || keyQueue.get(keyQueue.size() - 2) == key;
        }
        if (keyQueue.size() == 1) {
            return keyQueue.get(0) == key;
        }
        return false;
    }

    @Override
    public float moveDirection() {
        // Push key to top of queue if pressed
        // Issue when performance mode on, added to queue twice
        if (Graphics.isKeyPressed(KEY_W)) {
            keyQueue.add(KEY_W);
        }
        if (Graphics.isKeyPressed(KEY_A)) {
            keyQueue.add(KEY_A);
        }
        if (Graphics.isKeyPressed(KEY_S)) {
            keyQueue.add(KEY_S);
        }
        if (Graphics.isKeyPressed(KEY_D)) {
            keyQueue.add(KEY_D);
        }

        // Remove any duplicates
        if (keyQueue.size() > 1) {
            if (keyQueue.getLast() == keyQueue.get(keyQueue.size() - 2)) {
                keyQueue.removeLast();
            }
        }

        // Remove key from queue if released
        if (Graphics.isKeyReleased(KEY_W)) {
            keyQueue.remove((Integer) KEY_W);
        }
        if (Graphics.isKeyReleased(KEY_A)) {
            keyQueue.remove((Integer) KEY_A);
        }
        if (Graphics.isKeyReleased(KEY_S)) {
            keyQueue.remove((Integer) KEY_S);
        }
        if (Graphics.isKeyReleased(KEY_D)) {
            keyQueue.remove((Integer) KEY_D);
        }

        if (Main.counter % 120 == 0) {
            // Output the key queue
            System.out.println(keyQueue);
        }

        // Calculate move direction
        float moveDirection = -1;
        if (isKeyDown(KEY_S) ) {
            moveDirection = (float) (Math.PI * 0.5);
        } else if (isKeyDown(KEY_W)) {
            moveDirection = (float) (Math.PI * 1.5);
        } else if (isKeyDown(KEY_A)) {
            moveDirection = (float) Math.PI;
        } else if (isKeyDown(KEY_D)) {
            moveDirection = 0;
        }
        // Two are held
        if (isKeyDown(KEY_W) && isKeyDown(KEY_A)) {
            moveDirection = (float) (Math.PI * 1.25);
        } else if (isKeyDown(KEY_W) && isKeyDown(KEY_D)) {
            moveDirection = (float) (Math.PI * 1.75);
        } else if (isKeyDown(KEY_S) && isKeyDown(KEY_A)) {
            moveDirection = (float) (Math.PI * 0.75);
        } else if (isKeyDown(KEY_S) && isKeyDown(KEY_D)) {
            moveDirection = (float) (Math.PI * 0.25);
        }
        return moveDirection;
    }

    @Override
    public boolean fire() {
        return Graphics.isLeftMouseDown();
    }

    @Override
    public boolean unload() {
        return Graphics.isLeftMouseReleased();
    }
}
