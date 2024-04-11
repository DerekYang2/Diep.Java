import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages collisions between objects
 * Spatial hashing is used to reduce the number of collision checks
 */
public class CollisionManager {
    final public static float sectorSize = 125;
    public static int sectorsX = (int) Math.ceil(Main.arenaWidth/sectorSize), sectorsY = (int) Math.ceil(Main.arenaHeight/sectorSize);
    private static HashMap<Integer, ArrayList<Integer>> collisionGroups = new HashMap<>();
    private static HashSet<Integer> collidedPairs = new HashSet<>();

    public static int hash(Vector2 pos) {
        return (int) (pos.x / sectorSize) + (int) (pos.y / sectorSize) * sectorsX;
    }

    public static void updateSectors() {
        collisionGroups.clear();
        for (GameObject object : Main.gameObjectPool.getObjects()) {
            Rectangle boundingBox = object.boundingBox();
            int xiMin = (int) (boundingBox.x / sectorSize), xiMax = (int) ((boundingBox.x + boundingBox.width) / sectorSize);
            int yiMin = (int) (boundingBox.y / sectorSize), yiMax = (int) ((boundingBox.y + boundingBox.height) / sectorSize);

            for (int xi = xiMin; xi <= xiMax; xi++) {
                for (int yi = yiMin; yi <= yiMax; yi++) {
                    int sector = xi + sectorsX * yi;
                    if (!collisionGroups.containsKey(sector)) {
                        collisionGroups.put(sector, new ArrayList<>());
                    }
                    collisionGroups.get(sector).add(object.id);
                }
            }
        }
    }

    public static void updateCollision() {
        updateSectors();
        collidedPairs.clear();
        int minId = Main.idServer.peekFrontId();
        int numObjects = Main.gameObjectPool.getObjects().size();

        for (ArrayList<Integer> group : collisionGroups.values()) {
            if (group.size() == 1) continue;
            for (int id : group) {
                for (int id2 : group) {
                    if (id != id2) {
                        GameObject obj1 = Main.gameObjectPool.getObj(id), obj2 = Main.gameObjectPool.getObj(id2);
                        if (obj1.group != obj2.group) {  // Collision only if groups are different
                            int objHash = (obj1.id - minId) * (numObjects + 1) + (obj2.id - minId);  // Unique hash for pair
                            if (obj1.checkCollision(obj2) && !collidedPairs.contains(objHash)) {
                                obj1.receiveKnockback(obj2);
                                collidedPairs.add(objHash);
                            }
                        }
                    }
                }
            }
        }
    }
}
