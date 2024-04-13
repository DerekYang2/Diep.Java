public class Stats {
    public final static int MOVEMENT_SPEED = 0, RELOAD = 1, BULLET_DAMAGE = 2, BULLET_PENETRATION = 3, BULLET_SPEED = 4, BODY_DAMAGE = 5, MAX_HEALTH = 6, HEALTH_REGEN = 7;
    private int[] stats;
    public Stats() {
        stats = new int[8];
        // Fill with 0
        for (int i = 0; i < stats.length; i++) {
            stats[i] = 0;
        }
    }

    public void setStat(int stat, int value) {
        stats[stat] = value;
    }

    public int getStat(int stat) {
        return stats[stat];
    }
}
