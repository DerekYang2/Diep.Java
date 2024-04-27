import com.raylib.java.raymath.Vector2;

public class BotController implements Controller {
    Tank host;

    float moveDir;

    boolean shouldFire = false;

    public BotController() {
        moveDir = (float) (Math.random() * 2 * Math.PI);
    }

    @Override
    public void setHost(Tank host) {
        this.host = host;
    }

    @Override
    public float barrelDirection() {
        // Bot will always aim at the player
        Vector2 PlayerPos = Main.player.pos;
        return (float) Math.atan2(PlayerPos.y - host.pos.y, PlayerPos.x - host.pos.x);
    }

    @Override
    public Vector2 getTarget() {
        return Main.player.pos;
    }

    @Override
    public float moveDirection() {
        // Bot will bounce around the arena
        float xComp = (float) Math.cos(moveDir);
        float yComp = (float) Math.sin(moveDir);
        if (host.pos.x <= 0) {
            xComp = Math.abs(xComp);
        }
        if (host.pos.x >= Main.arenaWidth) {
            xComp = -Math.abs(xComp);
        }
        if (host.pos.y <= 0) {
            yComp = Math.abs(yComp);
        }
        if (host.pos.y >= Main.arenaHeight) {
            yComp = -Math.abs(yComp);
        }
        moveDir = (float) Math.atan2(yComp, xComp);
        moveDir = -1;
        return moveDir;
    }

    @Override
    public boolean fire() {
        if (Math.random() < 1) {
            shouldFire = true;
        }
        //shouldFire = false;
        return shouldFire;
    }

    @Override
    public boolean special() {
        return false;
    }
}
