package QueueEstimation;

import Utils.Logger;
import java.util.HashMap;
import java.util.ArrayList;

import static Utils.JSONWriter.writeData;


public class Main {
    final static int REPETITIONS = 50;

    //Parametri al variare del numero dei server e i client
    final static int[] servers = {1, 2, 4, 6};
    final static int[] clients = {8, 16, 32, 64};
    final static double skipProbability = 0.1;

    //Parametri al variare della probabilità di skip
    final static double[] skips = {0.1, 0.2, 0.4, 0.8};

    final static int [] conditionings = {1, 2, 4};
    final static int server = 2;
    final static int client = 16;

    public static void main(String[] args) {
        //Logger.debug("SEQ = " + System.getenv("SEQ"));
        //boolean seq = System.getenv("SEQ").contains("true");
        boolean seq = true;
        Logger.debug("SEQ = " + seq);
        //Al variare di clients e servers
        if (!seq) {
            HashMap<String, ArrayList<Double>> JSDs = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int s = 0; s < servers.length; s++) {
                for (int c = 0; c < clients.length; c++) {
                    if (s == 0 && c == 1)
                        continue;
                    if (s == 1 && c == 1)
                        continue;
                    String current_key = "Servers " + servers[s] + ", Clients " + clients[c];
                    for (int i = 0; i < REPETITIONS; i++) {
                        Logger.debugMode = true;
                        Logger.debug("\nLaunching experiment " + i + " with " + current_key);
                        Logger.debugMode = false;
                        boolean to_plot = false;
                        boolean alreadyComputedTrueTransient = true;
                        if (i == 0) {
                            to_plot = true;
                            alreadyComputedTrueTransient = false;
                        }
                        //to_plot = false;
                        ArrayList<Double> currentJSDs = experiment_launcher.launch(servers[s], clients[c], skipProbability, to_plot, "exp", alreadyComputedTrueTransient);
                        String filepath = "Results/s" + servers[s] + "c" + (clients[c]==8?"08":clients[c]) + ".json";
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
        if (true && !seq){
            //Al variare dello skip
            HashMap<Integer, ArrayList<Double>> JSDs_skip = new HashMap<Integer, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int skip = 0; skip < skips.length; skip++) {
                String current_key = "Skip " + skips[skip];
                for (int i = 0; i < REPETITIONS; i++){
                    Logger.debug("\nLaunching experiment " + i + " with " + current_key);
                    boolean to_plot = false;
                    boolean alreadyComputedTrueTransient = true;
                    if (i == 0) {
                        to_plot = true;
                        alreadyComputedTrueTransient = false;
                    }
                    to_plot = false;
                    ArrayList<Double> currentJSDs = experiment_launcher.launch(server, client, skips[skip], to_plot, "exp", alreadyComputedTrueTransient);
                    // JSDs_skip.put(skip, currentJSDs);
                    String filepath = "Results/s" + server + "c" + client + "skip" + skip + ".json";
                    writeData(filepath, i, "s"+server+"c"+client+" Skip " + skip, currentJSDs);
                }
            }
        }
        if (true && !seq){
            // Al variare delle distribuzioni dei tempi di servizio
            Launcher experiment_launcher = new Launcher();
            String[] distributions = {"exp", "uni", "erl"};
            // exponential lambda = 1
            // uniform [1.5, 2] <- forno della pizza
            // erlang k = 2, lambda = 1
            for (String dist : distributions) {
                for (int i = 0; i < REPETITIONS; i++) {
                    Logger.debug("\nLaunching experiment " + i + " with " + dist);
                    boolean to_plot = false;
                    boolean alreadyComputedTrueTransient = true;
                    if (i == 0) {
                        to_plot = true;
                        alreadyComputedTrueTransient = false;
                    }
                    to_plot = false;
                    ArrayList<Double> currentJSDs = experiment_launcher.launch(server, client, 0.1, to_plot, dist, alreadyComputedTrueTransient);
                    String filepath = "Results/s" + server + "c" + client + " "+dist+".json";
                    writeData(filepath, i, "s" + server + "c" + client + " " + dist, currentJSDs);
                }
            }
        }
        if (!seq) {
            //In questo esperimento mettiamo a confronto la performance del modello
            HashMap<String, ArrayList<Double>> JSDs_diff = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int c = 0; c < conditionings.length; c++) {
                for (int i = 0; i < REPETITIONS; i++) {
                    Logger.debug("\nLaunching experiment " + i + " -- conditioned t-" + conditionings[c]);
                    boolean to_plot = false;
                    boolean alreadyComputedTrueTransient = true;
                    if (i == 0) {
                        to_plot = true;
                        alreadyComputedTrueTransient = false;
                    }
                    to_plot = false;
                    ArrayList<Double> currentJSDs = experiment_launcher.launch_second_experiment(server, client, 0.1, to_plot, 50.0, alreadyComputedTrueTransient, conditionings[c]);
                    String filepath = "Results/s" + server + "c" + client + "cond" + conditionings[c] + ".json";
                    writeData(filepath, i, "s" + server + "c" + client + " Difference", currentJSDs);
                }
            }
        }
        if (!seq) {
            Launcher experiment_launcher = new Launcher();
            for (int c_index = 0; c_index < clients.length; c_index++){
                for (int i = 0; i < REPETITIONS * 2; i++){
                    Logger.debug("\nLaunching experiment " + i + " -- with skip-prob depending on queue length");
                    boolean to_plot = false;
                    boolean alreadyComputedTrueTransient = true;
                    if (i == 0) {
                        to_plot = true;
                        alreadyComputedTrueTransient = false;
                    }
                    to_plot = false;
                    ArrayList<Double> currentJSDs = experiment_launcher.launch_experiment_with_queue_dependant_skip_prob(server, clients[c_index], to_plot, "exp", alreadyComputedTrueTransient);
                    String filepath = "Results/s" + server + "c" + clients[c_index] + "_skip_prob_queue.json";
                    writeData(filepath, i, "s" + server + "c" + clients[c_index] + " skip_prob_queue", currentJSDs);
                }
            }
        }
        if(seq){
            Launcher experiment_launcher = new Launcher();
            String[] distributions = {"exp", "uni", "erl"};
            for(String dist : distributions) {
                for (int skip_index = 0; skip_index < skips.length; skip_index++) {
                    for (int c_index = 0; c_index < clients.length; c_index++) {
                        for (int s_index = 0; s_index < servers.length; s_index++) {
                            boolean to_plot = false;
                            boolean alreadyComputedTrueTransient = false;
                            for (int i = 0; i < REPETITIONS; i++) {
                                Logger.debugMode = true;
                                Logger.debug("\nLaunching experiment " + i + " -- checking the goodness of the analysis");
                                Logger.debugMode = false;
                                if (i == 0) {
                                    alreadyComputedTrueTransient = false;
                                }
                                ArrayList<Double> currentJSDs = experiment_launcher.launch_jsd_at_the_end(servers[s_index], clients[c_index], skips[skip_index], to_plot, dist, alreadyComputedTrueTransient);
                                String filepath = "Results/s" + servers[s_index] + "c" + clients[c_index] + "sp"+skips[skip_index]+dist+"_at_the_end.json";
                                writeData(filepath, i, "s" + servers[s_index] + "c" + clients[c_index] + "sp"+skips[skip_index]+dist+"_at_the_end", currentJSDs);
                            }
                        }
                    }
                }
            }
        }
        Logger.debug("Done");
    }
}
