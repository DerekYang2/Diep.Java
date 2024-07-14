

/**
 * Reward multiplication system:
 * FFA, 2 Teams, 4 Teams, Maze and Sandbox has an experience multiplier of x1.
 * Domination has an experience multiplier of x2.
 * Survival, Tag and Mothership has/had an experience multiplier of x3 (the Survival game mode has been removed).
 * Game Mode Manager stores the current game mode and changes multiplier accordingly.
 */

public class GameModeManager {
    private static GameMode mode;
    public static void setMode(GameMode gameMode) {
        GameModeManager.mode = gameMode;
        switch (mode) {
            case MENU -> Polygon.setRewardMultiplier(5);
            case TAG, SOLO -> Polygon.setRewardMultiplier(3);
            default -> Polygon.setRewardMultiplier(1);  // All other modes
        }
    }

    public static GameMode getMode() {
        return GameModeManager.mode;
    }
}
