

import java.util.Arrays;

public class Stats {
    public final static int MOVEMENT_SPEED = 0, RELOAD = 1, BULLET_DAMAGE = 2, BULLET_PENETRATION = 3, BULLET_SPEED = 4, BODY_DAMAGE = 5, MAX_HEALTH = 6, HEALTH_REGEN = 7;
    private int[] stats;
    public Stats() {
        stats = new int[8];
        // Fill with 0
        Arrays.fill(stats, 0);
    }

    public Stats(int movementSpeed, int reload, int bulletDamage, int bulletPenetration, int bulletSpeed, int bodyDamage, int maxHealth, int healthRegen) {
        stats = new int[8];
        stats[MOVEMENT_SPEED] = movementSpeed;
        stats[RELOAD] = reload;
        stats[BULLET_DAMAGE] = bulletDamage;
        stats[BULLET_PENETRATION] = bulletPenetration;
        stats[BULLET_SPEED] = bulletSpeed;
        stats[BODY_DAMAGE] = bodyDamage;
        stats[MAX_HEALTH] = maxHealth;
        stats[HEALTH_REGEN] = healthRegen;
    }

    public void setStat(int stat, int value) {
        stats[stat] = value;
    }

    public int getStat(int stat) {
        return stats[stat];
    }

    /**
     * Gets the number of upgrade stats for a given level
     * @param level The level of the tank
     * @return The number of upgrade stats for the given level
     */
    public static int getStatCount(int level) {
        if (level <= 0) return 0;
        if (level <= 28) return level - 1;

        return (int) (level / 3.f) + 18;
    }
}
