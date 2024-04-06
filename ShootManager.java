import java.util.HashMap;
import java.util.Map;

public class ShootManager {
    Map<Integer, Integer> fireIndex;  // fireIndex[count] is turret index to fire at count
    int totalLength;
    int counter;

    public ShootManager(int[] fireCount, int totalLength) {
        fireIndex = new HashMap<>();
        this.totalLength = totalLength;
        for (int i = 0; i < fireCount.length; i++) {
            fireIndex.put(fireCount[i], i);  // Turret i fires when count == fireCount[i]
        }
        counter = 0;
    }

    public void reset() {
        counter = 0;
    }

    public void update() {
        counter++;
        if (counter >= totalLength) {
            counter = 0;
        }
    }

    public int getFireIndex() {
        return fireIndex.getOrDefault(counter, -1);
    }
}
