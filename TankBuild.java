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
    String name;
    Barrel[] barrels;
    BulletStats[] bulletStats;
    FireManager fireManager;
    float fieldFactor;
    Tank host;
    Vector2[] pendingRecoil;  // Recoil to be applied after fire

    // AddOn object
    AddOn addOn;

    public TankBuild(String name, AddOn addOn, Barrel[] barrels, FireManager fireManager, BulletStats[] bulletStats, float fieldFactor) {
        this.name = name;
        this.barrels = barrels;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;
        this.fieldFactor = fieldFactor;
        this.addOn = addOn;
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
        if (name.equalsIgnoreCase("spike")) {
            host.setDamage(host.damage * 1.5f);  // Spike tank does 50% more damage, TODO: check if right
        }

        for (Barrel b : barrels) {
            b.setHost(host);
        }
        fireManager.setHost(host);
        if (addOn != null) {
            addOn.setHost(host);
        }
    }

    public void update() {
        for (int i = 0; i < barrels.length; i++) {
            barrels[i].update(host.pos.x, host.pos.y, host.direction);
            if (barrels[i].recoilFrames == barrels[i].recoilTime/2) {
                host.addForce(pendingRecoil[i]);
            }
        }
        if (addOn != null) {
            addOn.update();
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

    public void addOnDrawBefore() {
        if (addOn != null) {
            addOn.drawBefore();
        }
    }

    public void addOnDrawAfter() {
        if (addOn != null) {
            addOn.drawAfter();
        }
    }

    /**
     * Returns the front barrel of the tank
     * @return The barrel with the minimum offset
     */
    public Barrel getFrontBarrel() {
        // Get barrel with minimum offset
        float minOffset = Float.MAX_VALUE;
        Barrel frontBarrel = null;
        for (Barrel barrel : barrels) {
            float offset = (float)Graphics.normalizeAngle(barrel.getOffset());
            if (offset < minOffset) {
                minOffset = offset;
                frontBarrel = barrel;
            }
        }
        return frontBarrel;
    }

    public float getBulletSpeedMedian() {
        ArrayList<Float> bulletSpeeds = new ArrayList<>();
        for (BulletStats bulletStat : bulletStats) {
            bulletSpeeds.add(bulletStat.speed);
        }
        if (bulletSpeeds.isEmpty()) return 0;
        bulletSpeeds.sort(Float::compareTo);
        return bulletSpeeds.get(bulletSpeeds.size() / 2);
    }

    // Static creation methods
    public static HashMap<String, JSONObject> tankDefinitions;
    public static final String DEFINITIONS_PATH = "config/TankDefinitions.json";

    public static void loadTankDefinitions() {
        tankDefinitions = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(TankBuild.readFile(DEFINITIONS_PATH, Charset.defaultCharset()));
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

        AddOn addOn = jsonTank.isNull("postAddon")? null : AddOn.createAddOn(jsonTank.getString("postAddon"));
        if (addOn == null) {
            addOn = jsonTank.isNull("preAddon")? null : AddOn.createAddOn(jsonTank.getString("preAddon"));
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

        return new TankBuild(name, addOn, barrels, fireManager, bulletStats, fieldFactor);
    }

    /**
     * Returns a random tank definition name
     * @return Random tank definition name
     */
    public static String getRandomBuildName() {
        Set<String> keys = tankDefinitions.keySet();
        ArrayList<String> keyList = new ArrayList<>(keys);
        // Generate a random build until a valid one is found
        return (keyList.get((int)(Math.random() * keyList.size())));
    }



    public static float getRand(float maxV) {
        return (float)Math.random() * maxV;
    }

    public static TankBuild customRandomBuild() {
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

        return new TankBuild("custom", null, barrels, fireManager, bulletStats, fieldFactor);
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

        return new TankBuild("custom", null, barrels, fireManager, bulletStats, fieldFactor);
    }
}

