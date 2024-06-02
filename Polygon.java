import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;

public class Polygon extends GameObject {
    final static String SQUARE = "square", TRIANGLE = "triangle", PENTAGON = "pentagon", ALPHA_PENTAGON = "alpha pentagon";
    final static float BASE_ROTATION = 0.5f*0.01f * 25.f/120;
    /** Used to calculate the speed at which the shape orbits. Radians Per Tick. */
    final static float BASE_ORBIT = 0.5f*0.005f * 25.f/120;
    /** The velocity of the shape's orbits. */
    final static float BASE_VELOCITY = 0.5f*1 * 25.f/120;
    public static int multiplier = 3;
    public static int polyGroup = Integer.MIN_VALUE;
    float rotation;
    float direction;
    float baseAcceleration;
    float orbitRate;
    float rotationRate;
    boolean isNestPolygon = false;

    private float scoreReward;
    public Polygon(Vector2 pos, String name, boolean inNest) {
        super(pos, (int)(switch (name) {
            case Polygon.SQUARE, Polygon.TRIANGLE -> 38.890872;
            case Polygon.PENTAGON -> 145.0/2.2360679775;  // (1+1/Math.cos(0.5f*(2*Math.PI)/5))
            case Polygon.ALPHA_PENTAGON ->  2.666666 * 145.0/2.2360679775;
            default -> 0;
        }), 1, DrawPool.BOTTOM);
        initSpawnAnimation(40);
        group = polyGroup;
        isNestPolygon = inNest;

        orbitRate = (Math.random() < 0.5 ? 1 : -1) * BASE_ORBIT * Graphics.randf(0.75f, 1.25f);
        rotationRate = (Math.random() < 0.5 ? 1 : -1) * BASE_ROTATION;

        rotation = (float) (Math.random() * Math.PI);
        direction = (float) (Math.random() * Math.PI * 2);

        username = name;
        initHealthBar();
        updateStats();
    }

    @Override
    public void updateStats() {
        switch(username) {
            case Polygon.SQUARE -> {
                setCollisionFactors(1, 8);
                setMaxHealth(10);
                setDamage(8 * 25.f/120);
                scoreReward = 10;
            }
            case Polygon.TRIANGLE -> {
                setCollisionFactors(1, 8);
                setMaxHealth(30);
                setDamage(8 * 25.f/120);
                scoreReward = 25;
            }
            case Polygon.PENTAGON -> {
                setCollisionFactors(0.5f, 11);
                setMaxHealth(130);  // 100, buffed to 130
                setDamage(12 * 25.f/120);
                scoreReward = 130;
            }
            case Polygon.ALPHA_PENTAGON -> {
                setCollisionFactors(0.05f, 11);
                setMaxHealth(4000);  // 3000, but buffed
                setDamage(20 * 25.f/120);
                scoreReward = 3000;
            }
        }
        baseAcceleration = BASE_VELOCITY * (1-friction);
        healthBar.setWidth(getRadiusScaled() * 2);
    }

    @Override
    protected void setFlags() {
        noInternalCollision = false;
        keepInArena = true;
        isProjectile = false;
        isPolygon = true;
        spawnProtection = false;
    }


    @Override
    public void update() {
        super.update();

        rotation += rotationRate;
        direction += orbitRate;

        if (pos.x < 400) {
            direction = 0;
        } else if (pos.x > Main.arenaWidth - 400) {
            direction = (float)Math.PI;
        } else if (pos.y < 400) {
            direction = (float)Math.PI/2;
        } else if (pos.y > Main.arenaHeight - 400) {
            direction = (float)Math.PI * 1.5f;
        }

        addForce(baseAcceleration, direction);
    }

    @Override
    public void draw() {
        switch (username) {
            case Polygon.SQUARE -> {
                float sideLength = getRadiusScaled() * 2;
                if (Main.onScreen(pos, sideLength)) {
                    Graphics.drawTextureCentered(Graphics.squarePolygon, pos, sideLength, sideLength, rotation, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
            }
            case Polygon.TRIANGLE -> {
                if (Main.onScreen(pos, getRadiusScaled() * 2/3)) {
                    Graphics.drawTriangleRounded(pos, getRadiusScaled() * 2/3, rotation, Graphics.strokeWidth, Graphics.colAlpha(getDamageLerpColor(Graphics.TRIANGLE), opacity), Graphics.colAlpha(getDamageLerpColor(Graphics.TRIANGLE_STROKE), opacity));
                }
            }
            case Polygon.PENTAGON -> {
                float height = (float) (getRadiusScaled() * 2.2360679775);
                if (Main.onScreen(pos, getRadiusScaled())) {
                    Graphics.drawTextureCentered(Graphics.pentagonPolygon, pos, rotation,height/Graphics.pentagonPolygon.height, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
                //Graphics.drawCircle(pos, getRadiusScaled(), Graphics.RED, 1);
            }
            case Polygon.ALPHA_PENTAGON -> {
                float height = (float) (getRadiusScaled() * 2.2360679775);
                if (Main.onScreen(pos, getRadiusScaled())) {
                    Graphics.drawTextureCentered(Graphics.alphaPentagon, pos, rotation, height/Graphics.alphaPentagon.height, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
            }
        }
    }

    @Override
    protected float getScoreReward() {
        return scoreReward * multiplier;
    }

    @Override
    public void delete() {
        super.delete();
        if (isNestPolygon) {
            Spawner.nestCount--;
            if (username.equals(Polygon.ALPHA_PENTAGON)) {
                Spawner.alphaCount--;
            }
        } else {
            Spawner.count--;
        }
    }

    @Override
    public void updateVictim(GameObject victim) {
        super.updateVictim(victim);
        if (victim == Main.player) {  // If killed player
            Main.cameraHost = this;
            Main.killerName = username;
        }
    }
}
