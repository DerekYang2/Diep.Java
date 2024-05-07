package other;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class TestMain {
    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static double getCollisionFactor(int x, float friction) {
        double num = 0;
        for (int t = 0; t <= x; t++) {
            for (int i = 0; i <= t; i++) {
                num += Math.pow(0.9, i);
            }
        }

        double denom = 0;
        for (int t = 0; t <= 120*x/25; t++) {
            for (int i = 0; i <= t; i++) {
                denom += Math.pow(friction, i);
            }
        }

        return num / denom;
    }

    static HashMap<String, JSONObject> tankDefinitions = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ArrayList<Float> frictionValues = new ArrayList<>();
        ArrayList<Double> collisionFactors = new ArrayList<>();
        for (float friction = 0.95f; friction <= 0.99f; friction += 0.001f) {
            double avg = 0;
            for (int x = 150; x < 155; x++) {
                avg += (getCollisionFactor(x, friction));
            }
            frictionValues.add(friction);
            collisionFactors.add((avg / 5));
        }


        for (int i = 0; i < frictionValues.size(); i++) {
            System.out.printf("%.3f, ", frictionValues.get(i));
        }
        System.out.println();
        for (int i = 0; i < collisionFactors.size(); i++) {
            System.out.printf("%.4f, ", collisionFactors.get(i));
        }
/*        JSONArray jsonArray = new JSONArray(readFile("config/TankDefinitions.json", Charset.defaultCharset()));
        System.out.println(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            tankDefinitions.put(jsonObject.getString("name").trim().toLowerCase(), jsonObject);
        }


        JSONObject jsonObj = tankDefinitions.get("predator");
        JSONArray barrels = jsonObj.getJSONArray("barrels");
        for (int j = 0; j < barrels.length(); j++) {  // Loop through each barrel object
            JSONObject barrel = barrels.getJSONObject(j);
            // Access Barrel Object
            System.out.println(barrel.getDouble("angle"));

            JSONObject bullet = barrel.getJSONObject("bullet");
            // Access Barrel's Bullet Object
            System.out.println(bullet.getString("type"));
        }*/

    }
}
