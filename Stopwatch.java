public class Stopwatch {
    double initialTime;
    Stopwatch() {
        initialTime = -1e9;
    }
    public void start() {
        initialTime = System.nanoTime();
    }
    public double s() {
        return (System.nanoTime() - initialTime) / 1e9;
    }
    public double ms() {
        return (System.nanoTime() - initialTime) / 1e6;
    }
}
