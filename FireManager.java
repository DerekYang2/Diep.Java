import java.util.ArrayList;

public class FireManager {
    private int turretCount;
    private ArrayList<float[]> turretData;  // {delay, reload} for ith turret
    private int[] frameCounter;  // Current frame for ith turret
    private Tank host;
    private boolean paused = false;
    private boolean isFiring = false;

    /**
     * Constructor for FireManager
     * @param data The data for each turret. Each element is a list of 2 integers, the delay and reload factor
     */
    public FireManager(double[][] data) {
        turretCount = data.length;
        turretData = new ArrayList<>();
        frameCounter = new int[turretCount];
        for (double[] d : data) {
            turretData.add(new float[]{(float) d[0], (float) d[1]});  // {delay, reload}
        }
    }

    public void setHost(Tank host) {
        this.host = host;
        // Initialize frame counter
        for (int i = 0; i < turretCount; i++) {
            frameCounter[i] = getDelayFrames(i);
        }
    }

    /**
     * Total reload frames for a turret = ceil((15 - reload stat points) * base reload)
     * Might be outdated: new one: 15 * 0.914^(reload stat points)
     * https://www.desmos.com/calculator/zrl5fljsxp
     * @param ti The turret index
     * @return
     */
    public int getReloadFrames(int ti) {
        return Math.round((120.f/25) * 15*(float)Math.pow(0.914, host.stats.getStat(Stats.RELOAD)) * getReloadFactor(ti));  // convert 25 fps to 120 fps
    }

    /**
     * Delay frames = total reload frames * delay
     * @param ti The turret index
     * @return
     */
    private int getDelayFrames(int ti) {
        return Math.round(getReloadFrames(ti) * getDelayFactor(ti));
    }

    private float getDelayFactor(int turretIndex) {
        return turretData.get(turretIndex)[0];
    }

    private float getReloadFactor(int turretIndex) {
        return turretData.get(turretIndex)[1];
    }

    public void setFiring(boolean isFiring) {
        this.isFiring = isFiring;
        if (isFiring)  // When firing, set paused to false
            paused = false;
    }

    public ArrayList<Integer> getFireIndices() {
        ArrayList<Integer> retList = new ArrayList<>();

        if (!paused) {
            for (int i = 0; i < turretCount; i++) {  // For each turret
                if (frameCounter[i] == 0) {
                    if (isFiring) {  // Fire this turret
                        retList.add(i);
                        frameCounter[i] = getReloadFrames(i);
                    } else {  // Put fire on hold
                        paused = true;  // Pause everything starting from next frame
                    }
                } else {
                    frameCounter[i]--;
                }
            }
        }

        return retList;
    }

}
