import java.util.Stack;

public class IdServer {
    private int rightCounter, leftCounter;

    public IdServer() {
        rightCounter = 1;
        leftCounter = -1;
    }

    public int getId() {
        if (rightCounter == Integer.MAX_VALUE) {
            throw new RuntimeException("IdServer has reached the maximum number of ids");
        }
        return rightCounter++;
    }

    /**
     * In order to get a small id to run in update loop first (e.g. draw in the front)
     * @return
     */
    public int getIdFront() {
        if (leftCounter == Integer.MIN_VALUE) {
            throw new RuntimeException("IdServer has reached the minimum number of ids");
        }
        return leftCounter--;
    }

    public int peekFrontId() {
        return leftCounter;
    }

    public int peekBackId() {
        return rightCounter;
    }
}
