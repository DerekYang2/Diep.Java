

import com.raylib.java.core.input.Keyboard;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;

public class CameraManager {
    private static float targetZoom = 1f, currentZoom = 1f, lerpFactor = 0.05f;

    public static void update() {
        // lerp camera zoom
        if (Math.abs(targetZoom - currentZoom) > 1e-3) {
            currentZoom += (targetZoom - currentZoom) * 0.05f; //Adjusts camera
            Graphics.setZoom(currentZoom);
        }

        if (Main.cameraHost == Main.player) {
            if (Main.player.tankBuild.zoomAbility && Main.player.controller.holdSpecial()) {
                if (Main.player.controller.pressSpecial()) {  // Only update target if the button is pressed
                    // TODO: check if predator zoom amount is right
                    Main.cameraTarget = new Vector2((float) (Math.cos(Main.player.direction) * 1000 * Main.player.scale + Main.player.pos.x), (float) (Math.sin(Main.player.direction) * 1000 * Main.player.scale + Main.player.pos.y));
                }
            } else {
                Main.cameraTarget = Main.player.pos;
            }
        } else {
            Main.cameraTarget = Main.cameraHost.pos;
        }

        Vector2 difference = Raymath.Vector2Subtract(Main.cameraTarget, Graphics.getCameraTarget());
        Graphics.shiftCameraTarget(Graphics.scale(difference, lerpFactor));

        // Zoom in and out feature (beta testing)
        float delta = Graphics.getCameraZoom()/100;
        if (Graphics.isKeyDown(Keyboard.KEY_DOWN)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() - delta);
        }
        if (Graphics.isKeyDown(Keyboard.KEY_UP)) {
            Graphics.setCameraZoom(Graphics.getCameraZoom() + delta);
        }
        // Cap the zoom level
        Graphics.setCameraZoom(Math.max(0.1f, Math.min(10f, Graphics.getCameraZoom())));
    }

    public static void setZoom(float zoom) {
        targetZoom = zoom;
    }

    public static void forceSetZoom(float zoom) {
        targetZoom = currentZoom = zoom;
        Graphics.setZoom(zoom);
    }
}
