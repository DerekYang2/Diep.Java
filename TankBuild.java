import java.util.ArrayList;

public class TankBuild
{
    Barrel[] barrels;
    BulletStats[] bulletStats;
    FireManager fireManager;
    float fieldFactor;
    Tank host;

    public TankBuild(Barrel[] barrels, FireManager fireManager, BulletStats[] bulletStats, float fieldFactor) {
        this.barrels = barrels;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;
        this.fieldFactor = fieldFactor;
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

    // TEMP: builds
    // Static creation methods
    public static TankBuild tank() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(42, 95, 0, 0)
        };
        FireManager fireManager = new FireManager(new double[][]{{0, 1}});
        return new TankBuild(barrels, fireManager, new BulletStats[]{
                new BulletStats(1, 1, 1,1 , 1, 1, 1)
        }, 1);
    }

    // Triplet
    public static TankBuild triplet() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(42, 80, 26, 0),
                new Barrel(42, 80, -26, 0),
                new Barrel(42, 95, 0, 0)
        };
        FireManager fireManager = new FireManager(new double[][]{{0.5, 1}, {0.5, 1}, {0, 1}});
        return new TankBuild(barrels, fireManager,
                new BulletStats[]{
                        new BulletStats(1, 0.7f, 0.6f, 1, 1, 1, 1),
                        new BulletStats(1, 0.7f, 0.6f, 1, 1, 1, 1),
                        new BulletStats(1, 0.7f, 0.6f, 1, 1, 1, 1)
                }, 1);
    }

    // Pentashot
    public static TankBuild pentashot() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(42, 80, 0, -0.7853981633974483),
                new Barrel(42, 80, 0, 0.7853981633974483),
                new Barrel(42, 95, 0, -0.39269908169872414),
                new Barrel(42, 95, 0, 0.39269908169872414),
                new Barrel(42, 110, 0, 0)
        };
        FireManager fireManager = new FireManager(new double[][]{{2.0/3, 1}, {2.0/3, 1}, {1.0/3, 1}, {1.0/3, 1}, {0, 1}});
        return new TankBuild(barrels, fireManager,
                new BulletStats[]{
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1)
                }, 1);
    }

    // Predator
    public static TankBuild predator() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(42, 110, 0, 0),
                new Barrel(56.7f, 95, 0, 0),
                new Barrel(71.4f, 80, 0, 0)
        };
        FireManager fireManager = new FireManager(new double[][]{{0, 3}, {0.2, 3}, {0.4, 3}});
        return new TankBuild(barrels, fireManager,
                new BulletStats[]{
                        new BulletStats(0.7f, 1, 0.75f, 1.4f, 0.3f, 1, 1),
                        new BulletStats(0.7f, 1, 0.75f, 1.4f, 0.3f, 1, 1),
                        new BulletStats(0.7f, 1, 0.75f, 1.4f, 0.3f, 1, 1)
                }, 0.85f);
    }

    // Destroyer
    public static TankBuild destroyer() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(71.4f, 95, 0, 0)
        };
        FireManager fireManager = new FireManager(new double[][]{{0, 4}});
        return new TankBuild(barrels, fireManager,
                new BulletStats[]{
                        new BulletStats(1, 2, 3, 0.7f, 1, 1, 0.1f)
                }, 1);
    }
}

