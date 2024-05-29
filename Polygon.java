import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

public class Polygon extends GameObject {
    final static String SQUARE = "square", TRIANGLE = "triangle", PENTAGON = "pentagon", ALPHA_PENTAGON = "alpha pentagon";
    final static float BASE_ROTATION = 0.5f*0.01f * 25.f/120;
    /** Used to calculate the speed at which the shape orbits. Radians Per Tick. */
    final static float BASE_ORBIT = 0.5f*0.005f * 25.f/120;
    /** The velocity of the shape's orbits. */
    final static float BASE_VELOCITY = 0.5f*1 * 25.f/120;
    public static int count = 0, nestCount = 0;

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
        group = Integer.MIN_VALUE;  // Set to impossible group
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
                setMaxHealth(100);
                setDamage(12 * 25.f/120);
                scoreReward = 130;
            }
            case Polygon.ALPHA_PENTAGON -> {
                setCollisionFactors(0.05f, 11);
                setMaxHealth(3000);
                setDamage(20 * 25.f/120);
                scoreReward = 3000;
            }
        }
        baseAcceleration = BASE_VELOCITY * (1-friction);
        healthBar.setWidth(radius * scale * 2);
    }

    @Override
    protected void setFlags() {
        noInternalCollision = false;
        keepInArena = true;
        isProjectile = false;
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
                float sideLength = radius * scale * 2;
                if (Main.onScreen(pos, sideLength)) {
                    Graphics.drawTextureCentered(Graphics.squarePolygon, pos, sideLength, sideLength, rotation, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
            }
            case Polygon.TRIANGLE -> {
                if (Main.onScreen(pos, radius * scale * 2/3)) {
                    Graphics.drawTriangleRounded(pos, radius * scale * 2/3, rotation, Graphics.strokeWidth, Graphics.colAlpha(getDamageLerpColor(Graphics.TRIANGLE), opacity), Graphics.colAlpha(getDamageLerpColor(Graphics.TRIANGLE_STROKE), opacity));
                }
            }
            case Polygon.PENTAGON -> {
                float height = (float) (radius * scale * 2.2360679775);
                if (Main.onScreen(pos, radius * scale)) {
                    Graphics.drawTextureCentered(Graphics.pentagonPolygon, pos, rotation,height/Graphics.pentagonPolygon.height, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
                //Graphics.drawCircle(pos, radius * scale, Graphics.RED, 1);
            }
            case Polygon.ALPHA_PENTAGON -> {
                float height = (float) (radius * scale * 2.2360679775);
                if (Main.onScreen(pos, radius * scale)) {
                    Graphics.drawTextureCentered(Graphics.alphaPentagon, pos, rotation, height/Graphics.alphaPentagon.height, Graphics.colAlpha(getDamageLerpColor(Color.WHITE), opacity));
                }
            }
        }
    }

    @Override
    protected float getScoreReward() {
        return scoreReward;
    }

    public static Polygon spawnRandomPolygon() {
        double rand = Math.random();
        Vector2 pos;

        do {  // Ensure the polygon does not spawn inside the nest
            pos = new Vector2((float) (Math.random() * (Main.arenaWidth)), (float) (Math.random() * (Main.arenaHeight)));
        } while (Graphics.isIntersecting(new Rectangle(pos.x-100, pos.y-100, 200, 200), Main.crasherZone));

        count++;

        String shape;
        if (rand < 0.04) {
            shape = Polygon.PENTAGON;
        } else if (rand < 0.20) {
            shape = Polygon.TRIANGLE;
        } else {
            shape = Polygon.SQUARE;
        }
        return new Polygon(pos, shape, false);
    }

    public static Polygon spawnNestPolygon() {
        Vector2 pos = new Vector2(Graphics.randf(Main.nestBox.x, Main.nestBox.x + Main.nestBox.width), Graphics.randf(Main.nestBox.y, Main.nestBox.y + Main.nestBox.height));

        nestCount++;

        double rand = Math.random();
        if (rand < 0.02) {
            return new Polygon(pos, Polygon.ALPHA_PENTAGON, true);
        } else {
            return new Polygon(pos, Polygon.PENTAGON, true);
        }
    }

    @Override
    public void delete() {
        super.delete();
        if (isNestPolygon) {
            nestCount--;
        } else {
            count--;
        }
    }
}
