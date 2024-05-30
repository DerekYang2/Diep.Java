import com.raylib.java.raymath.Vector2;

public class LeaderPointer {
    public static double direction;
    public static void update() {
        Tank leader = Leaderboard.getTankRank(0);
        if (leader != null) {
            if (leader == Main.player)
                direction = 0;
            else
                direction = Math.atan2(leader.pos.y - Main.player.pos.y, leader.pos.x - Main.player.pos.x);
        }
        direction = Graphics.normalizeAngle(direction);
    }
    // https://www.desmos.com/calculator/topmpimq9v
    // TODO: if leader is on screen, do not draw
    public static void draw() {
            //Graphics.drawTriangle(Main.player.pos, 20F, 1.5F, (float) direction, Graphics.RED);
            if (direction != 0) {
    /*            theta_i = arctan(H / W) @{ color: "#c74440" }

                r(theta) = W / cos(theta) @{
                            domain: @{ theta: @{ min: -theta_i, max: theta_i } },
                }

                r(theta) = H / sin(theta) @{
                            domain: @{ theta: @{ min: theta_i, max: pi - theta_i } },
                }

                r(theta) = -W / cos(theta) @{
                            domain: @{ theta: @{ min: pi - theta_i, max: pi + theta_i } },
                }

                r(theta) = -H / sin(theta) @{
                            domain: @{ theta: @{ min: pi + theta_i, max: 2 * pi - theta_i } },
                }*/
                float inverseZoom = 1 / Graphics.getCameraZoom();
                float H = Graphics.screenHeight * 0.5f, W = Graphics.screenWidth * 0.5f;
                float theta_i = (float) Math.atan(H / W);
                float r = 0;
                if (Graphics.isAngleBetween(direction, -theta_i, theta_i)) {
                    r = W / (float) Math.cos(direction);
                } else if (direction > theta_i && direction <= Math.PI - theta_i) {
                    r = H / (float) Math.sin(direction);
                } else if (direction > Math.PI - theta_i && direction <= Math.PI + theta_i) {
                    r = -W / (float) Math.cos(direction);
                } else if (direction > Math.PI + theta_i && direction <= 2 * Math.PI - theta_i) {
                    r = -H / (float) Math.sin(direction);
                }
                r -= 40;
                Vector2 pos = new Vector2(W + r * (float) Math.cos(direction), H + r * (float) Math.sin(direction));
                Graphics.drawTriangle(Graphics.getScreenToWorld2D(pos, Graphics.camera), 12 * inverseZoom, 1, (float) direction, Graphics.rgba(0, 0, 0, 50));
            }
    }
}
