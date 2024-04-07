import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShootManager {
    ArrayList<ArrayList<Integer>> turretGroup;  // Stores indices of turrets at group i
    int[] delayFrames;  // Stores the delay between each group
    float scale = 1.0f;
    int frameCounter, groupCounter;

    /**
     * Constructor for ShootManager
     * fireOrder = {0, 0, 1, 2} means turret 0 and 1 fire first, then turret 2, then turret 3
     * delays = {10, 20, 30}  means after 10 frames, turret 0 and 1 fire, after 20 frames, turret 2 fires, after 30 frames, turret 3 fires
     * @param fireOrder integer array where ith element is the order of the ith turret
     * @param delays The frame delay time for the ith order group
     */
    public ShootManager(int[] fireOrder, int[] delays) {
        // initialize turret group
        turretGroup = new ArrayList<>();
        for (int i = 0; i < fireOrder.length; i++) {
            turretGroup.add(new ArrayList<>());
        }

        for (int tIndex = 0; tIndex < fireOrder.length; tIndex++) {
            int orderNumber = fireOrder[tIndex];
            turretGroup.get(orderNumber).add(tIndex);
        }

        assert(turretGroup.size() == delays.length);  // Make sure the number of groups is the same as the number of delays

        delayFrames = new int[delays.length];
        for (int i = 0; i < delays.length; i++) {  // Make a true clone, not just change pointer
            delayFrames[i] = delays[i];
        }

        groupCounter = 0;
        frameCounter = delayFrames[0]-1;  // Preload the first fire
    }

    public void reset() {
        // Preload the next first fire
        frameCounter = delayFrames[groupCounter]-1;
    }

    public ArrayList<Integer> getFireIndices() {
        ArrayList<Integer> retList = new ArrayList<>();  // Empty list
        frameCounter++;
        if (frameCounter >= delayFrames[groupCounter]) {  // Fire
            retList = turretGroup.get(groupCounter);  // Get the list of turret indices for this group
            frameCounter = 0;
            groupCounter = (groupCounter + 1) % turretGroup.size();  // Cycle through groups
        }
        return retList;
    }
}
