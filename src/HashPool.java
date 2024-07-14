

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

public class HashPool <T extends Deletable> {
    private HashMap<Integer, T> objects;  // id -> pointer to object
    private Stack<T> pendingAdd; // stack of ids waiting to be added
    private Stack<Integer> pendingDeletes;  // stack of ids to waiting to be deleted

    public HashPool() {
        objects = new HashMap<>();
        pendingAdd = new Stack<>();
        pendingDeletes = new Stack<>();
    }

    public void clear() {
        objects.clear();
        pendingAdd.clear();
        pendingDeletes.clear();
    }

    public void addObj(T obj) {
        pendingAdd.push(obj);
    }

    public void deleteObj(int id) {
        pendingDeletes.push(id);
    }

    // Handles the pending deletes
    public void refresh() {
        // Update pending adds
        while (!pendingAdd.isEmpty()) {
            T obj = pendingAdd.pop();
            objects.put(obj.getId(), obj);  // add the object to the pool
        }
        // Update pending deletes
        while (!pendingDeletes.isEmpty()) {
            int id = pendingDeletes.pop();
            objects.remove(id);  // remove the object from the pool
        }
    }

    // Return hashmap.values()
    public Collection<T> getObjects() {
        return objects.values();
    }

    public T getObj(int id) {
        return objects.get(id);
    }
}
