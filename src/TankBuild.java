

import com.raylib.java.raymath.Vector2;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

    // Predator-like zoom
    boolean zoomAbility;

    // Invisibility variables
    float min_movement = 0.4f;  // Minimum movement speed to be visible
    boolean isInvisible;  // Whether the tank can turn invisible
    float visibilityRateShooting;  // Rate of visibility increase when shooting
    float visibilityRateMoving;  // Rate of visibility increase when moving
    float invisibilityRate;  // Constant rate of invisibility decrease

    protected boolean isDeleted = false;  // Whether the tank build has been deleted

    // Max stats
    HashMap<String, Integer> maxStats;

    // Upgrading variables
    int levelRequirement;


    public TankBuild(String name, AddOn addOn, Barrel[] barrels, FireManager fireManager, BulletStats[] bulletStats, float fieldFactor) {
        this.name = name;
        this.barrels = barrels;
        this.fireManager = fireManager;
        this.bulletStats = bulletStats;
        this.fieldFactor = fieldFactor;
        this.addOn = addOn;
        pendingRecoil = new Vector2[barrels.length];
        maxStats = new HashMap<>();

        // Set whether barrels are drone barrels, which are always on fire regardless of controller
        for (int idx = 0; idx < barrels.length; idx++) {
            if (bulletStats[idx].type.equals("drone")) {
                fireManager.setDroneBarrel(idx);
            }
        }
    }

    public void setMaxStat(String stat, int value) {
        maxStats.put(stat, value);
    }

    /**
     * Receives the stat name in terms of bullets and returns the proper equivalent (could be bullet or drone or null)
     * @param stat The stat name in terms of bullet ("bullet penetration")
     * @return The proper stat name ("drone health")
     */
    public String getProperStat(String stat) {
        String droneEquivalent = switch (stat) {
            case "Bullet Penetration" -> "Drone Health";
            case "Bullet Damage" -> "Drone Damage";
            case "Bullet Speed" -> "Drone Speed";
            default -> stat;
        };
        if (maxStats.containsKey(droneEquivalent)) {  // If the drone equivalent is in the max stats
            return droneEquivalent;
        } else {  // If the bullet stat is in the max stats
            return stat;
        }
    }

    public int getMaxStat(String stat) {
        if (maxStats.containsKey(stat)) {
            return maxStats.get(stat);
        } else {  // TODO: crashes caused by drone turrets having different stat names
            throw new IllegalArgumentException("Stat " + stat + " not found in maxStats, name = " + name);
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

    public void setFlags(boolean isInvisible, float visibilityRateShooting, float visibilityRateMoving, float invisibilityRate, boolean zoomAbility) {
        this.isInvisible = isInvisible;
        this.visibilityRateShooting = visibilityRateShooting;
        this.visibilityRateMoving = visibilityRateMoving;
        this.invisibilityRate = invisibilityRate;
        this.zoomAbility = zoomAbility;
    }

    public void resetFireManagerDelay() {
        fireManager.resetDelay();
    }

    public void delete() {
        if (!isDeleted) {
            isDeleted = true;
            for (Barrel barrel : barrels) {
                barrel.delete();
            }
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
        ArrayList<Integer> fireIndices = fireManager.getFireIndices();
        for (int i : fireIndices) {
            pendingRecoil[i] = barrels[i].shoot();
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

    public void addOnDrawMiddle() {
        if (addOn != null) {
            addOn.drawMiddle();
        }
    }

    public void addOnDrawAfter() {
        if (addOn != null) {
            addOn.drawAfter();
        }
    }

    public void setPos(Vector2 pos) {
        for (Barrel barrel : barrels) {
            barrel.setPos(pos);
        }
        if (addOn != null) {
            addOn.setPos(pos);
        }
    }

    public Barrel getBarrel(int idx) {
        return barrels[idx];
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

    public BulletStats getBarrelBulletStats(Barrel barrel) {
        for (int i = 0; i < barrels.length; i++) {
            if (barrels[i] == barrel) {
                return bulletStats[i];
            }
        }
        return null;
    }

    public float maxBarrelLength() {
        float maxLen = 0;
        for (Barrel barrel : barrels) {
            if (barrel.getTurretLength() > maxLen) {
                maxLen = barrel.getTurretLength();
            }
        }
        return maxLen;
    }

    // Static creation methods
    public static HashMap<String, JSONObject> tankDefinitions;
    public static String[] buildName = new String[100];  // The build name for a given ID
    public static final String DEFINITIONS_PATH = "config/TankDefinitions.json";
    public static final HashMap<String, ArrayList<String>> prerequisite = new HashMap<>();
    public static final ArrayList<String> finalUpgrades = new ArrayList<>();

    public static void loadTankDefinitions() {
        tankDefinitions = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(TankBuild.readFile(DEFINITIONS_PATH, Charset.defaultCharset()));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTank = jsonArray.getJSONObject(i);
                String nameStr = jsonTank.getString("name").trim().toLowerCase();
                tankDefinitions.put(nameStr, jsonTank);
                buildName[jsonTank.getInt("id")] = nameStr;  // Store the build name for a given ID
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTank = jsonArray.getJSONObject(i);
                String nameStr = jsonTank.getString("name").trim().toLowerCase();

                if (jsonTank.getInt("levelRequirement") == 45) {  // If the tank is a final upgrade
                    finalUpgrades.add(nameStr);
                }

                JSONArray jsonUpgrades = jsonTank.getJSONArray("upgrades");
                for (int j = 0; j < jsonUpgrades.length(); j++) {
                    String upgradeName = buildName[jsonUpgrades.getInt(j)];  // Get the upgrade name
                    if (!prerequisite.containsKey(upgradeName)) {  // If the upgrade name is not in the map, add it
                        prerequisite.put(upgradeName, new ArrayList<>());
                    }
                    prerequisite.get(upgradeName).add(nameStr);  // Add the prerequisite to the upgrade name
                }
            }

            // TODO: when tank functionalities added, remove these lines
            finalUpgrades.remove("necromancer");
            finalUpgrades.remove("factory");
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
     * @param buildName Name of the tank definition
     * @return TankBuild object
     */
    public static TankBuild createTankBuild(String buildName) {
        buildName = buildName.trim().toLowerCase();
        JSONObject jsonTank = tankDefinitions.get(buildName);
        if (jsonTank == null) {
            System.out.println("Tank definition not found: " + buildName);
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
            JSONObject jsonBullet = jsonBarrel.getJSONObject("bullet");

            bulletStats[i] = new BulletStats(jsonBullet.getString("type"), jsonBullet.getFloat("sizeRatio"), jsonBullet.getFloat("health"), jsonBullet.getFloat("damage"), jsonBullet.getFloat("speed"), jsonBullet.getFloat("scatterRate"), jsonBullet.getFloat("lifeLength"), jsonBullet.getFloat("absorbtionFactor"), jsonBarrel.getFloat("recoil"));
            reloadData[i] = new double[] {jsonBarrel.getDouble("delay"), jsonBarrel.getDouble("reload")};
            barrels[i] = new Barrel(bulletStats[i], jsonBarrel.getFloat("width"), jsonBarrel.getFloat("size"), (float) reloadData[i][1], jsonBarrel.getFloat("offset"), jsonBarrel.getDouble("angle"), jsonBarrel.getBoolean("isTrapezoid"), jsonBarrel.getDouble("trapezoidDirection") != 0, !jsonBarrel.isNull("addon") && jsonBarrel.getString("addon").equals("trapLauncher"));

            // If drone
            if (bulletStats[i].type.equals("drone") || bulletStats[i].type.equals("swarm")) {
                barrels[i].initializeDrones(jsonBarrel.getInt("droneCount"), jsonBarrel.getBoolean("canControlDrones"));
            }
        }

        FireManager fireManager = new FireManager(reloadData);
        float fieldFactor = jsonTank.getFloat("fieldFactor");

        TankBuild build = new TankBuild(buildName, addOn, barrels, fireManager, bulletStats, fieldFactor);

        JSONObject jsonFlags = jsonTank.getJSONObject("flags");
        build.setFlags(jsonFlags.getBoolean("invisibility"), jsonTank.getFloat("visibilityRateShooting"), jsonTank.getFloat("visibilityRateMoving"), jsonTank.getFloat("invisibilityRate"), jsonFlags.getBoolean("zoomAbility"));

        JSONArray statObject = jsonTank.getJSONArray("stats");
        for (Object obj : statObject) {
            JSONObject stat = (JSONObject) obj;
            build.setMaxStat(stat.getString("name"), stat.getInt("max"));
        }

        build.levelRequirement = jsonTank.getInt("levelRequirement");

        return build;
    }

    /**
     * Returns a random tank definition name
     * @return Random tank definition name
     */
    public static String getRandomBuildName() {
        Set<String> keys = tankDefinitions.keySet();
        ArrayList<String> keyList = new ArrayList<>(keys);
        // Generate a random build until a valid one is found
        String buildName;
        do {
            buildName = keyList.get((int)(Math.random() * keyList.size()));
        } while (buildName.contains("dominator") || buildName.equals("arena closer") || buildName.equals("mothership"));
        return buildName;
    }

    public static Queue<Pair<String, Integer>> getRandomUpgradePath() {
        ArrayList<Pair<String, Integer>> upgradePath = new ArrayList<>();

        String randFinal = finalUpgrades.get(Graphics.randInt(0, finalUpgrades.size()));
        Pair<String, Integer> currentTank = new Pair<>(randFinal, 45);
        upgradePath.add(currentTank);

        while (!currentTank.first.equals("tank")) {
            ArrayList<String> preBuilds = prerequisite.get(currentTank.first);
            String preBuild = preBuilds.get(Graphics.randInt(0, preBuilds.size()));

            currentTank = new Pair<>(preBuild, getLevelRequirement(preBuild));
            upgradePath.add(currentTank);
        }

        Queue<Pair<String, Integer>> upgradeQueue  = new LinkedList<>();
        for (int i = upgradePath.size() - 1; i >= 0; i--) {  // Add the upgrade path in reverse order
            upgradeQueue.add(upgradePath.get(i));
        }
        return upgradeQueue;
    }

    public static int getLevelRequirement(String name) {
        return tankDefinitions.get(name).getInt("levelRequirement");
    }

    public static Set<String> getTankDefinitions() {
        return tankDefinitions.keySet();
    }
}

