import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class ShootManager {
    ArrayList<ArrayList<Integer>> turretGroup;  // Stores indices of turrets at group i
    float[] delayFrames;  // Stores the delay between each group
    int frameCounter, groupCounter;
    private Tank host;

    /**
     * Constructor for ShootManager
     * fireOrder = {0, 0, 1, 2} means turret 0 and 1 fire first, then turret 2, then turret 3
     * delays = {10, 20, 30}  means after 10 frames, turret 0 and 1 fire, after 20 frames, turret 2 fires, after 30 frames, turret 3 fires
     * @param fireOrder integer array where ith element is the order of the ith turret
     * @param delays The frame delay time for the ith order group
     */
    public ShootManager(int[] fireOrder, float[] delays, float baseReload) {
        // Find the number of groups
        int groupAmount = 1;
        for (int i = 0; i < fireOrder.length; i++) {
            groupAmount = Math.max(groupAmount, fireOrder[i]+1);
        }

        // initialize turret group
        turretGroup = new ArrayList<>();
        for (int i = 0; i < groupAmount; i++) {
            turretGroup.add(new ArrayList<>());
        }

        for (int tIndex = 0; tIndex < fireOrder.length; tIndex++) {
            int orderNumber = fireOrder[tIndex];
            turretGroup.get(orderNumber).add(tIndex);
        }

        assert(turretGroup.size() == delays.length);  // Make sure the number of groups is the same as the number of delays

        delayFrames = new float[delays.length];
        // Base time is 15 frames, but convert to 120 fps
        delayFrames[0] = (15 * 120.f/25) * (delays[0] + (1 - delays[delays.length-1]));  // Time before first group is sum in front + sum behind last
        for (int i = 1; i < delays.length; i++) {  // Make a true clone, not just change pointer
            delayFrames[i] = (15 * 120.f/25) * (delays[i] - delays[i-1]);
        }

        groupCounter = 0;
    }

    public void setHost(Tank host) {
        this.host = host;
        frameCounter = getDelayFrames(0) -1;  // Preload the first fire
    }

    private int getDelayFrames(int group) {
        ceil((15 - reload stat points) * base reload);

        return (int)Math.round(delayFrames[group] * Math.pow(0.914, host.stats.getStat(Stats.RELOAD)));  // Base reload time * 0.914^reload stat
    }

    public void reset() {
        // Preload the next first fire
        frameCounter = getDelayFrames(groupCounter)-1;
    }

    public ArrayList<Integer> getFireIndices() {
        ArrayList<Integer> retList = new ArrayList<>();  // Empty list
        frameCounter++;
        if (frameCounter >= getDelayFrames(groupCounter)) {  // Fire
            retList = turretGroup.get(groupCounter);  // Get the list of turret indices for this group
            frameCounter = 0;
            groupCounter = (groupCounter + 1) % turretGroup.size();  // Cycle through groups
        }
        return retList;
    }
}
