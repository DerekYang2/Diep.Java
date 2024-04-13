import java.util.ArrayList;

public class BarrelManager
{
    Barrel[] barrels;
    BulletStats[] bulletStats;
    FireManager fireManager;
    Tank host;

    public BarrelManager(Barrel[] barrels, FireManager fireManager, BulletStats[] bulletStats) {
        this.barrels = barrels;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;
    }

    public void setHost(Tank host) {
        this.host = host;
        for (Barrel t : barrels) {
            t.setHost(host);
        }
        fireManager.setHost(host);
    }

    public void update() {
        for (Barrel t : barrels) {
            t.update(host.pos.x, host.pos.y, host.direction);
        }
    }

    public void draw() {
        for (Barrel t : barrels) {
            t.draw();
        }
    }

    public void reset() {
        fireManager.reset();
    }

    public void fire() {
        ArrayList<Integer> fireIndices = fireManager.getFireIndices();
        for (int i : fireIndices) {
            host.addForce(barrels[i].shoot(bulletStats[i]));
        }
    }
}

