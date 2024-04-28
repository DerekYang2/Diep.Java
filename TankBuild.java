import com.raylib.java.raymath.Vector2;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TankBuild
{
    Barrel[] barrels;
    BulletStats[] bulletStats;
    FireManager fireManager;
    float fieldFactor;
    Tank host;
    Vector2[] pendingRecoil;  // Recoil to be applied after fire

    public TankBuild(Barrel[] barrels, FireManager fireManager, BulletStats[] bulletStats, float fieldFactor) {
        this.barrels = barrels;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;
        this.fieldFactor = fieldFactor;
        pendingRecoil = new Vector2[barrels.length];

        // Set whether barrels are drone barrels
        for (int idx = 0; idx < barrels.length; idx++) {
            if (barrels[idx].getMaxDrones() > 0) {
                fireManager.setDroneBarrel(idx);
            }
        }
    }

    public void setHost(Tank host) {
        this.host = host;
        for (Barrel b : barrels) {
            b.setHost(host);
        }
        fireManager.setHost(host);
    }

    public void update() {
        for (int i = 0; i < barrels.length; i++) {
            barrels[i].update(host.pos.x, host.pos.y, host.direction);
            if (barrels[i].recoilFrames == barrels[i].recoilTime/2) {
                host.addForce(pendingRecoil[i]);
            }
        }
    }

    public void updateFire(boolean isFiring) {
        fireManager.setFiring(isFiring);

        for (int i : fireManager.getFireIndices()) {
            pendingRecoil[i] = barrels[i].shoot(bulletStats[i]);
        }
    }

    public void draw() {
        for (Barrel barrel : barrels) {
            barrel.draw();
        }
    }

    // Static creation methods
    public static HashMap<String, JSONObject> tankDefinitions;

    public static void loadTankDefinitions() {
        tankDefinitions = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(TankBuild.readFile("config/TankDefinitions.json", Charset.defaultCharset()));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                tankDefinitions.put(jsonObject.getString("name").trim().toLowerCase(), jsonObject);
            }
        } catch (IOException e) {
            System.out.println("Error reading TankDefinitions.json: " + e.getMessage());
        }
    }
    private static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Create a TankBuild object from a tank definition in TankDefinitions.json
     * Hierarchy: Tank -> Barrels -> Bullet
     * @param name Name of the tank definition
     * @return TankBuild object
     */
    public static TankBuild createTankBuild(String name) {
        name = name.trim().toLowerCase();
        JSONObject jsonTank = tankDefinitions.get(name);
        if (jsonTank == null) {
            System.out.println("Tank definition not found: " + name);
            jsonTank = tankDefinitions.get("tank");  // Default to tank
        }

        JSONArray jsonBarrels = jsonTank.getJSONArray("barrels");

        Barrel[] barrels = new Barrel[jsonBarrels.length()];
        BulletStats[] bulletStats = new BulletStats[jsonBarrels.length()];
        double[][] reloadData = new double[jsonBarrels.length()][2];  // array of {delay percent, reload percent}

        for (int i = 0; i < jsonBarrels.length(); i++) {  // Loop through each barrel object
            JSONObject jsonBarrel = jsonBarrels.getJSONObject(i);
            barrels[i] = new Barrel(jsonBarrel.getFloat("width"), jsonBarrel.getFloat("size"), jsonBarrel.getFloat("offset"), jsonBarrel.getDouble("angle"), jsonBarrel.getBoolean("isTrapezoid"), jsonBarrel.getDouble("trapezoidDirection") != 0, !jsonBarrel.isNull("addon") && jsonBarrel.getString("addon").equals("trapLauncher"));

            reloadData[i] = new double[] {jsonBarrel.getDouble("delay"), jsonBarrel.getDouble("reload")};

            JSONObject jsonBullet = jsonBarrel.getJSONObject("bullet");

            bulletStats[i] = new BulletStats(jsonBullet.getString("type"), jsonBullet.getFloat("sizeRatio"), jsonBullet.getFloat("health"), jsonBullet.getFloat("damage"), jsonBullet.getFloat("speed"), jsonBullet.getFloat("scatterRate"), jsonBullet.getFloat("lifeLength"), jsonBullet.getFloat("absorbtionFactor"), jsonBarrel.getFloat("recoil"));

            // If drone
            if (bulletStats[i].type.equals("drone")) {
                barrels[i].initializeDrones(jsonBarrel.getInt("droneCount"), jsonBarrel.getBoolean("canControlDrones"));
            }
        }

        FireManager fireManager = new FireManager(reloadData);
        float fieldFactor = jsonTank.getFloat("fieldFactor");

        return new TankBuild(barrels, fireManager, bulletStats, fieldFactor);
    }

    /**
     * Create a TankBuild object from a random tank definition in TankDefinitions.json
     * @return
     */
    public static TankBuild createRandomBuild() {
        Set<String> keys = tankDefinitions.keySet();
        ArrayList<String> keyList = new ArrayList<>(keys);
        // Generate a random build until a valid one is found
        TankBuild randBuild;
        do {
            String randomKey = keyList.get((int)(Math.random() * keyList.size()));
            randBuild = createTankBuild(randomKey);
        } while (randBuild == null);
        return randBuild;
    }

    public static float getRand(float maxV) {
        return (float)Math.random() * maxV;
    }

    public static TankBuild william() {
        int barrelAmount = (int)(Math.random() * 20);

        Barrel[] barrels = new Barrel[barrelAmount];
        BulletStats[] bulletStats = new BulletStats[barrelAmount];
        double[][] reloadData = new double[barrelAmount][2];  // array of {delay percent, reload percent}

        for (int i = 0; i < barrelAmount; i++) {  // Loop through each barrel object
            
            barrels[i] = new Barrel(getRand(100), getRand(200), getRand(60), getRand((float)(2*Math.PI)), false, false, false);

            reloadData[i] = new double[] {getRand(1), getRand(1.5f)+0.5};

            bulletStats[i] = new BulletStats("bullet", getRand(1)+0.5f, 1, 1, getRand(3.f), getRand(10), getRand(2), 1, getRand(15));
        }

        FireManager fireManager = new FireManager(reloadData);
        float fieldFactor = 1f;

        return new TankBuild(barrels, fireManager, bulletStats, fieldFactor);
    }
    
    public static TankBuild createNewRandomBuild() {
        int barrelAmount = (int)(Math.random() * 10);

        Barrel[] barrels = new Barrel[barrelAmount];
        BulletStats[] bulletStats = new BulletStats[barrelAmount];
        double[][] reloadData = new double[barrelAmount][2];  // array of {delay percent, reload percent}

        for (int i = 0; i < barrelAmount; i++) {  // Loop through each barrel object
            
            barrels[i] = new Barrel(getRand(100), getRand(200), getRand(60), getRand((float)(2*Math.PI)), false, false, false);

            reloadData[i] = new double[] {getRand(1), getRand(1.5f)+0.5};

            bulletStats[i] = new BulletStats("bullet", getRand(1)+0.5f, 1, 1, getRand(3.f), getRand(10), getRand(2), 1, getRand(15));
        }

        FireManager fireManager = new FireManager(reloadData);
        float fieldFactor = 1f;

        return new TankBuild(barrels, fireManager, bulletStats, fieldFactor);
    }

 /*   // Static creation methods
    public static TankBuild tank() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(42, 95, 0, 0)
        };
        FireManager fireManager = new FireManager(new double[][]{{0, 1}});
        return new TankBuild(barrels, fireManager, new BulletStats[]{
                new BulletStats(1, 1, 1,1 , 1, 1, 1, 1)
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
                        new BulletStats(1, 0.7f, 0.6f, 1, 1, 1, 1, 0.5f),
                        new BulletStats(1, 0.7f, 0.6f, 1, 1, 1, 1, 0.5f),
                        new BulletStats(1, 0.7f, 0.6f, 1, 1, 1, 1, 0.5f)
                }, 1);
    }

    // Pentashot
    public static TankBuild pentaShot() {
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
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1, 0.7f),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1, 0.7f),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1, 0.7f),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1, 0.7f),
                        new BulletStats(1, 1, 0.55f, 1, 1, 1, 1, 0.7f)
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
                        new BulletStats(0.7f, 1, 0.75f, 1.4f, 0.3f, 1, 1, 0.3f),
                        new BulletStats(0.7f, 1, 0.75f, 1.4f, 0.3f, 1, 1, 0.3f),
                        new BulletStats(0.7f, 1, 0.75f, 1.4f, 0.3f, 1, 1, 0.3f)
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
                        new BulletStats(1, 2, 3, 0.7f, 1, 1, 0.1f, 15)
                }, 1);
    }

    // OctoTank
    public static TankBuild octoTank() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(42, 95, 0, -0.7853981633974483),
                new Barrel(42, 95, 0, 0.7853981633974483),
                new Barrel(42, 95, 0, -2.356194490192345),
                new Barrel(42, 95, 0, 2.356194490192345),
                new Barrel(42, 95, 0, 3.141592653589793),
                new Barrel(42, 95, 0, -1.5707963267948966),
                new Barrel(42, 95, 0, 1.5707963267948966),
                new Barrel(42, 95, 0, 0)
        };
        FireManager fireManager = new FireManager(new double[][]{{0.5, 1}, {0.5, 1}, {0.5, 1}, {0.5, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}});
        return new TankBuild(barrels, fireManager,
                new BulletStats[]{
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1),
                        new BulletStats(1, 1, 0.65f, 1, 1, 1, 1, 1)
                }, 1);
    }

    // Booster
    public static TankBuild booster() {
        Barrel[] barrels = new Barrel[]{
                new Barrel(42, 95, 0, 0),
                new Barrel(42, 70, 0, 3.9269908169872414),
                new Barrel(42, 70, 0, 2.356194490192345),
                new Barrel(42, 80, 0, 3.665191429188092),
                new Barrel(42, 80, 0, 2.6179938779914944),
        };
        FireManager fireManager = new FireManager(new double[][]{{0, 1}, {2.f / 3, 1}, {2.f / 3, 1}, {1.f / 3, 1}, {1.f / 3, 1}});
        return new TankBuild(barrels, fireManager,
                new BulletStats[]{
                        new BulletStats(1, 1, 0.6f, 1, 1, 1, 1, 0.2f),
                        new BulletStats(1, 1, 0.6f, 1, 1, 1, 1, 0.2f),
                        new BulletStats(1, 1, 0.6f, 1, 1, 1, 1, 0.2f),
                        new BulletStats(1, 1, 0.6f, 1, 1, 1, 1, 2.5f),
                        new BulletStats(1, 1, 0.6f, 1, 1, 1, 1, 2.5f)
                }, 1);
    }*/
}

