import java.util.Stack;

public class IdServer {
    final int MAX_IDS = 10000;
    Stack<Integer> pendingReturn;
    int[] idList;
    private int lastIdx;

    public IdServer() {
        pendingReturn = new Stack<>();
        idList = new int[MAX_IDS];
        for (int i = 0; i < idList.length; i++) {
            idList[i] = i;
        }
        lastIdx = idList.length - 1;
    }

    public int getId() {
        if (lastIdx == -1) {
            throw new RuntimeException("IdServer has run out of ids");
        }
        return idList[lastIdx--];
    }

    public void returnId(int id) {
        pendingReturn.push(id);
    }

    public void refresh() {
        while (!pendingReturn.isEmpty()) {
            idList[++lastIdx] = pendingReturn.pop();
        }
    }

    public int maxIds() {
        return MAX_IDS;
    }
}
