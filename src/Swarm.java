

import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Swarm extends Drone {
    public Swarm(Barrel hostBarrel, Vector2 spawnPos, float direction, float diameter, BulletStats bulletStats, Color fillCol, Color strokeCol) {
        super(hostBarrel, spawnPos, direction, diameter, bulletStats, fillCol, strokeCol, 0.96f);
        VIEW_RADIUS *= 2;  // Increase view radius to 2 * 850
    }

    @Override
    protected void setFlags() {
        super.setFlags();
        super.keepInArena = false;
    }

    @Override
    public void updateStats() {
        setCollisionFactors(bulletStats.absorbtionFactor, 4);

        // Calculate bullet stats
        float damage = (7 + (3 * host.getStat(Stats.BULLET_DAMAGE))) * bulletStats.damage;  // src: link above
        float maxHealth = (8 + 6 * host.getStat(Stats.BULLET_PENETRATION)) * bulletStats.health;  // src: link above
        super.setDamage(damage * (25.f / 120));  // Scale down because different fps
        super.setMaxHealth(maxHealth);
        // Calculate acceleration to converge to max speed
        this.acceleration = getMaxSpeed() * (1-friction);
        float initialSpeed = getMaxSpeed() + (15 + Graphics.randf(-bulletStats.scatterRate, bulletStats.scatterRate)) * (1-friction)/(1-0.9f);
        initialSpeed /= 3;
        vel = new Vector2(initialSpeed * (float) Math.cos(this.direction), initialSpeed * (float) Math.sin(this.direction));

        // Life length
        lifeFrames = Math.round(bulletStats.lifeLength * 72 * (120.f / 25));
    }

}
