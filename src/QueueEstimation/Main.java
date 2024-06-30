package QueueEstimation;

import Utils.Logger;
import java.util.HashMap;
import java.util.ArrayList;

public class Main {
    final static int REPETITIONS = 100;

    //Parametri al variare del numero dei server e i client
    final static int[] servers = {1, 2, 4, 8};
    final static int[] clients = {8, 16, 32, 64};
    final static double skipProbability = 0.1;

    //Parametri al variare della probabilità di skip
    final static double[] skips = {0.1, 0.2, 0.4, 0.8};
    final static int server = 2;
    final static int client = 32;

    public static void main(String[] args) {
        //Al variare di clients e servers
        HashMap<String, Double[]> JSDs = new HashMap<String, Double[]>();
        Launcher experiment_launcher = new Launcher();
        for (int s = 0; s < servers.length; s++) {
            for (int c = 0; c < servers.length; c++) {
                String current_key = "Servers " + s + ", Clients " + c;
                for (int i = 0; i < REPETITIONS; i++) {
                    boolean to_plot = false;
                    if (i == 0)
                        to_plot = true;
                    Double[] currentJSDs = (Double[])experiment_launcher.launch(servers[s], clients[c], skipProbability, to_plot).toArray();
                    if (!JSDs.containsKey(current_key))
                        JSDs.put(current_key, currentJSDs);
                    else{
                        Double[] stored_JSDs = JSDs.get(current_key);
                        for (int l = 0; l < stored_JSDs.length; l++) {
                            stored_JSDs[l] = stored_JSDs[l] + currentJSDs[l];
                            JSDs.put(current_key, stored_JSDs);
                        }
                    }
                }
            }
        }

        //Al variare dello skip
        HashMap<Integer, Double[]> JSDs_skip = new HashMap<Integer, Double[]>();
        for (int skip = 0; skip < skips.length; skip++){
            Double[] currentJSDs = (Double[])experiment_launcher.launch(server, client, skip, true).toArray();
            JSDs_skip.put(skip, currentJSDs);
        }
    }
}
