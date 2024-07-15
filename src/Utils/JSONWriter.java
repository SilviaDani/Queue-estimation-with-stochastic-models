package Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class JSONWriter {

    public static void writeData(String path, int run, String key, ArrayList<Double> JSD) {
        try {
            File file = new File(path);

            JSONObject root;
            JSONArray experiments;

            if (file.exists()) {
                // Read the existing file content
                String content = new String(Files.readAllBytes(Paths.get(path)));
                root = new JSONObject(content);
                experiments = root.getJSONArray("experiments");
            } else {
                // Create new JSON structure
                root = new JSONObject();
                experiments = new JSONArray();
                root.put("experiments", experiments);
            }

            // Create new experiment JSON object
            JSONObject experiment = new JSONObject();
            experiment.put("run", run);
            experiment.put("key", key);
            experiment.put("JSD", new JSONArray(JSD));

            // Append new experiment to the experiments array
            experiments.put(experiment);

            // Write the updated JSON structure back to the file
            try (FileWriter fileWriter = new FileWriter(path)) {
                fileWriter.write(root.toString(4)); // 4 is the number of spaces for indentation
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Test the method
        ArrayList<Double> jsdList = new ArrayList<>();
        jsdList.add(0.16);
        jsdList.add(0.26);
        jsdList.add(0.36);

        writeData("experiment.json", 1, "testKey", jsdList);
    }
}