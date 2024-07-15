package QueueEstimation;

import Utils.Logger;
import java.util.HashMap;
import java.util.ArrayList;

import static Utils.JSONWriter.writeData;


public class Main {
    final static int REPETITIONS = 25;

    //Parametri al variare del numero dei server e i client
    final static int[] servers = {1, 2, 4, 6};
    final static int[] clients = {8, 16, 32, 64};
    final static double skipProbability = 0.1;

    //Parametri al variare della probabilità di skip
    final static double[] skips = {0.1, 0.2, 0.4, 0.8};
    final static int server = 2;
    final static int client = 32;

    public static void main(String[] args) {
        //Al variare di clients e servers
        if (true) {
            HashMap<String, ArrayList<Double>> JSDs = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int s = 0; s < servers.length; s++) {
                for (int c = 0; c < clients.length; c++) {
                    if (servers[s] == 1){
                        continue;
                    }
                    String current_key = "Servers " + servers[s] + ", Clients " + clients[c];
                    for (int i = 0; i < REPETITIONS; i++) {
                        Logger.debug("\nLaunching experiment " + i + " with " + current_key);
                        boolean to_plot = false;
                        if (i == 0)
                            to_plot = true;
                        ArrayList<Double> currentJSDs = experiment_launcher.launch(servers[s], clients[c], skipProbability, to_plot);
                        String filepath = "Results/s" + servers[s] + "c" + clients[c] + ".json";
                        writeData(filepath, i, current_key, currentJSDs);
                        if (!JSDs.containsKey(current_key))
                            JSDs.put(current_key, currentJSDs);
                        else {
                            ArrayList<Double> stored_JSDs = JSDs.get(current_key);
                            for (int l = 0; l < stored_JSDs.size(); l++) {
                                double _stored_JSD_l = stored_JSDs.get(l) + currentJSDs.get(l);
                                stored_JSDs.set(l, _stored_JSD_l);
                                JSDs.put(current_key, stored_JSDs);
                            }
                        }
                    }
                }
            }
        }
        if (false){
            //Al variare dello skip
            HashMap<Integer, ArrayList<Double>> JSDs_skip = new HashMap<Integer, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int skip = 0; skip < skips.length; skip++) {
                ArrayList<Double> currentJSDs = experiment_launcher.launch(server, client, skip, true);
                JSDs_skip.put(skip, currentJSDs);
            }
        }
        if (false){
            // Differenza tra X1(t|t>t2) e X2(t)
            HashMap<String, ArrayList<Double>> JSDs_diff = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            experiment_launcher.launch_second_experiment(1, 32, 0.1, true, 50.0);
        }
    }
}
