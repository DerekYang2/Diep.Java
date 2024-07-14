

import java.util.ArrayList;

public class DrawPool
{
    final public static int BOTTOM = 0, MIDDLE = 1, TOP = 2, TOP_UI = 3;
    private ArrayList<Pool<Drawable>> drawPools;

    public DrawPool() {
        drawPools = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            drawPools.add(new Pool<>());
        }
    }

    public void clear() {
        drawPools.clear();
        for (int i = 0; i <= 3; i++) {
            drawPools.add(new Pool<>());
        }
    }

    public void addObj(Drawable obj, int layer) {
        drawPools.get(layer).addObj(obj);
    }

    public void refresh() {
        for (Pool<Drawable> pool : drawPools) {
            pool.refresh();
        }
    }

    public void deleteObj(int id, int layer) {
        drawPools.get(layer).deleteObj(id);
    }

    public void drawAll() {
        for (Pool<Drawable> pool : drawPools) {
            for (Drawable obj : pool.getObjects()) {
                obj.draw();
            }
        }
    }
}
