

public class BulletStats
{
    public String type;
    public float sizeRatio, health, damage, speed, scatterRate, lifeLength, absorbtionFactor, recoil;
    public BulletStats(String type, float sizeRatio, float health, float damage, float speed, float scatterRate, float lifeLength, float absorbtionFactor, float recoil) {
        this.type = type;
        this.sizeRatio = sizeRatio;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.scatterRate = scatterRate;
        this.lifeLength = lifeLength;
        this.absorbtionFactor = absorbtionFactor;
        this.recoil = recoil;
    }
}
