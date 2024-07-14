

public class ScoreHandler {
    // NOTE: start at level 1
    public static final int maxPlayerLevel = 45;
    private static float levelToScoreTable[] = new float[maxPlayerLevel];

    public static void initialize() {
        levelToScoreTable[0] = 0;
        for (int i = 1; i < maxPlayerLevel; i++) {
            levelToScoreTable[i] = levelToScoreTable[i - 1] + (40.f / 9 * (float)Math.pow(1.06, i - 1) * Math.min(31, i));
        }
    }

    /**
     * The score of a tank at a given level (level 1 is the lowest level)
     * @param level The level of the tank
     * @return The score of the tank at the given level
     */
    public static float levelToScore(int level) {
        if (level >= maxPlayerLevel) return levelToScoreTable[maxPlayerLevel - 1];
        if (level <= 0) return 0;

        return levelToScoreTable[level - 1];
    }
}
