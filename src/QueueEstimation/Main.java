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
    final static int client = 16;

    public static void main(String[] args) {
        //Al variare di clients e servers
        if (false) {
            HashMap<String, ArrayList<Double>> JSDs = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int s = 0; s < servers.length; s++) {
                for (int c = 0; c < clients.length; c++) {
                    if (servers[s] != 2 || clients[c] != 16){
                        continue;
                    }
                    String current_key = "Servers " + servers[s] + ", Clients " + clients[c];
                    for (int i = 0; i < REPETITIONS; i++) {
                        Logger.debug("\nLaunching experiment " + i + " with " + current_key);
                        boolean to_plot = false;
                        if (i == 0)
                            to_plot = true;
                        ArrayList<Double> currentJSDs = experiment_launcher.launch(servers[s], clients[c], skipProbability, to_plot, "exp");
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
                String current_key = "Skip " + skips[skip];
                for (int i = 0; i < 1; i++){
                    Logger.debug("\nLaunching experiment " + i + " with " + current_key);
                    boolean to_plot = false;
                    if (i == 0){
                        to_plot = true;
                    }
                    ArrayList<Double> currentJSDs = experiment_launcher.launch(server, client, skip, to_plot, "exp");
                    // JSDs_skip.put(skip, currentJSDs);
                    String filepath = "Results/s" + server + "c" + client + "skip" + skip + ".json";
                    //writeData(filepath, i, "s"+servers+"c"+client+" Skip " + skip, currentJSDs);
                }
            }
        }
        if (false){
            // Al variare delle distribuzioni dei tempi di servizio
            Launcher experiment_launcher = new Launcher();
            String[] distributions = {"exp", "uni", "erl"};
            // exponential lambda = 1
            // uniform [1.5, 2] <- forno della pizza
            // erlang k = 2, lambda = 1
            for (String dist : distributions) {
                for (int i = 0; i < REPETITIONS; i++) {
                    boolean to_plot = false;
                    if (i == 0) {
                        to_plot = true;
                    }
                    ArrayList<Double> currentJSDs = experiment_launcher.launch(server, client, 0.1, to_plot, dist);
                    String filepath = "Results/s" + server + "c" + client + " "+dist+".json";
                    writeData(filepath, i, "s" + server + "c" + client + " " + dist, currentJSDs);
                }
            }

        }


        if (true){
            // Differenza tra X1(t|t>t2) e X2(t)
            HashMap<String, ArrayList<Double>> JSDs_diff = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int i = 0; i < REPETITIONS; i++) {
                Logger.debug("\nLaunching experiment " + i + " -- conditioned");
                boolean to_plot = false;
                if (i == 0){
                    to_plot = true;
                }
                ArrayList<Double> currentJSDs = experiment_launcher.launch_second_experiment(server, client, 0.1, to_plot, 50.0);
                String filepath = "Results/s" + server + "c" + client + "diff.json";
                writeData(filepath, i, "s"+server+"c"+client+" Difference", currentJSDs);
            }
        }
        Logger.debug("Done");
    }
}
