import com.raylib.java.shapes.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages collisions between objects
 * Spatial hashing is used to reduce the number of collision checks
 */
public class CollisionManager {
    final public static float sectorSize = 150;
    private static int sectorsX;
    private static float minX, minY, maxX, maxY;
    private static HashMap<Integer, ArrayList<Integer>> collisionGroups = new HashMap<>();
    private static HashSet<Integer> collidedPairs = new HashSet<>();

    public static int getSectorX(float x) {
        return (int) ((x - minX) / sectorSize);
    }
    public static int getSectorY(float y) {
        return (int) ((y - minY) / sectorSize);
    }

    public static HashSet<Integer> queryBoundingBox(Rectangle boundingBox) {
        HashSet<Integer> result = new HashSet<>();
        int xiMin = getSectorX(boundingBox.x), xiMax = getSectorX(boundingBox.x + boundingBox.width);
        int yiMin = getSectorY(boundingBox.y), yiMax = getSectorY(boundingBox.y + boundingBox.height);

        for (int xi = xiMin; xi <= xiMax; xi++) {
            for (int yi = yiMin; yi <= yiMax; yi++) {
                int sectorHash = xi + sectorsX * yi;
                if (collisionGroups.containsKey(sectorHash)) {
                    result.addAll(collisionGroups.get(sectorHash));
                }
            }
        }

        return result;
    }

    public static void updateSectors() {
        collisionGroups.clear();

        minX = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        minY = Float.MAX_VALUE;
        maxY = Float.MIN_VALUE;  // Initialize to extreme values
        // Find the minimum and maximum x, y positions of all game objects
        for (GameObject object : Main.gameObjectPool.getObjects()) {
            Rectangle boundingBox = object.boundingBox();
            minX = Math.min(minX, boundingBox.x);
            minY = Math.min(minY, boundingBox.y);
            maxX = Math.max(maxX, boundingBox.x + boundingBox.width);
            maxY = Math.max(maxY, boundingBox.y + boundingBox.height);
        }
        sectorsX = (int) ((maxX - minX) / sectorSize) + 2;

        for (GameObject object : Main.gameObjectPool.getObjects()) {
            Rectangle boundingBox = object.boundingBox();
            int xiMin = getSectorX(boundingBox.x), xiMax = getSectorX(boundingBox.x + boundingBox.width);
            int yiMin = getSectorY(boundingBox.y), yiMax = getSectorY(boundingBox.y + boundingBox.height);

            for (int xi = xiMin; xi <= xiMax; xi++) {
                for (int yi = yiMin; yi <= yiMax; yi++) {
                    int sectorHash = xi + sectorsX * yi;
                    if (!collisionGroups.containsKey(sectorHash)) {  // Initialize if current sector group does not exist
                        collisionGroups.put(sectorHash, new ArrayList<>());
                    }
                    collisionGroups.get(sectorHash).add(object.id);  // Add object id to the sector group
                }
            }
        }
    }

    public static void updateCollision() {
        updateSectors();
        collidedPairs.clear();
        int maxId = Main.idServer.maxIds();
        //HashSet<String> testPairs = new HashSet<>();

        for (ArrayList<Integer> group : collisionGroups.values()) {
            if (group.size() <= 1) continue;
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    int idSmall = group.get(i), idLarge = group.get(j);  // Get the two ids ordered
                    if (idSmall != idLarge) {
                        if (idSmall > idLarge) {  // Swap if idSmall > idLarge
                            int temp = idSmall;
                            idSmall = idLarge;
                            idLarge = temp;
                        }

                        GameObject obj1 = Main.gameObjectPool.getObj(idSmall), obj2 = Main.gameObjectPool.getObj(idLarge);
                        int objHash = obj1.id * maxId + obj2.id;  // Unique hash for a pair of ids

                        if (!collidedPairs.contains(objHash) && obj1.checkCollision(obj2)) {
                            GameObject.receiveKnockback(obj1, obj2);
                            GameObject.receiveDamage(obj1, obj2);
                            collidedPairs.add(objHash);
                        }

                        /*
                        Tests for duplicate pairs:
                        String collideStr = obj1.id + " " + obj2.id;
                        assert(!testPairs.contains(collideStr)); // Make sure no duplicate pairs
                        testPairs.add(collideStr);
                        */
                    }
                }
            }
        }
    }
}
