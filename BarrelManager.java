import java.util.ArrayList;

public class BarrelManager
{
    Turret[] turrets;
    ShootManager shootManager;
    Tank host;

    public BarrelManager(Turret[] turrets, ShootManager shootManager) {
        this.turrets = turrets;
        this.shootManager = shootManager;
    }

    public void setHost(Tank host) {
        this.host = host;
        for (Turret t : turrets) {
            t.setHost(host);
        }
        shootManager.setHost(host);
    }

    public void update() {
        for (Turret t : turrets) {
            t.update(host.pos.x, host.pos.y, host.direction);
        }
    }

    public void draw() {
        for (Turret t : turrets) {
            t.draw();
        }
    }

    public void reset() {
        shootManager.reset();
    }

    public void fire() {
        ArrayList<Integer> fireIndices = shootManager.getFireIndices();
        if (fireIndices != null) {
            for (int i : fireIndices) {
                host.addForce(turrets[i].shoot());
            }
        }
    }
}

