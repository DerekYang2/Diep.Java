package other;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class TestMain {
    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    static HashMap<String, JSONObject> tankDefinitions = new HashMap<>();

    public static void main(String[] args) throws IOException {
        JSONArray jsonArray = new JSONArray(readFile("config/TankDefinitions.json", Charset.defaultCharset()));
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
        }

    }
}
