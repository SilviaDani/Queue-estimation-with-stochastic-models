package QueueEstimation;

import Utils.Logger;
import java.util.HashMap;
import java.util.ArrayList;

import static Utils.JSONWriter.writeData;


public class Main {
    final static int REPETITIONS = 100;

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
        //2
        if (!seq) {
            //In questo esperimento mettiamo a confronto la performance del modello
            HashMap<String, ArrayList<Double>> JSDs_diff = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int c = 0; c < conditionings.length; c++) {
                for (int c_index = 0; c_index < clients.length; c_index++) {
                    for (int i = 0; i < REPETITIONS; i++) {
                        Logger.debug("\nLaunching experiment " + i + " -- conditioned t-" + conditionings[c]);
                        boolean to_plot = false;
                        boolean alreadyComputedTrueTransient = true;
                        if (i == 0) {
                            to_plot = true;
                            alreadyComputedTrueTransient = false;
                        }
                        to_plot = false;
                        ArrayList<Double> currentJSDs = experiment_launcher.launch_second_experiment(servers[0], clients[c_index], 0.1, to_plot, 50.0, alreadyComputedTrueTransient, conditionings[c]);
                        String filepath = "Results/s" + servers[0] + "c" + clients[c_index] + "cond" + conditionings[c] + ".json";
                        writeData(filepath, i, "s" + servers[0] + "c" + clients[c_index] + " Difference", currentJSDs);
                    }
                }
            }
        }
        // Changing skip prob on queue length
        if (!seq) {
            Launcher experiment_launcher = new Launcher();
            for (int c_index = 0; c_index < clients.length; c_index++){
                for (int i = 0; i < REPETITIONS; i++){
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
        // Changing s and c
        if(seq){
            Launcher experiment_launcher = new Launcher();
            String[] distributions = {"exp", "uni", "erl"};
            int skip_index = 0;
            String dist = "exp";
            //for(String dist : distributions) {
              //  for (int skip_index = 0; skip_index < skips.length; skip_index++) {
                    for (int c_index = 0; c_index < clients.length; c_index++) {
                        for (int s_index = 0; s_index < servers.length; s_index++) {
                            boolean to_plot = false;
                            boolean alreadyComputedTrueTransient = false;
                            for (int i = 0; i < REPETITIONS; i++) {
                                try {
                                    Logger.debugMode = true;
                                    Logger.debug("\ns" + servers[s_index] + "c" + clients[c_index] + "skip" + skips[skip_index] + "dist" + dist);
                                    Logger.debug("\nLaunching experiment " + i + " -- checking the goodness of the analysis - changing s and c");
                                    Logger.debugMode = false;
                                    if (i == 0) {
                                        alreadyComputedTrueTransient = false;
                                    } else {
                                        alreadyComputedTrueTransient = true;
                                    }
                                    ArrayList<Double> currentJSDs = experiment_launcher.launch_jsd_at_the_end(servers[s_index], clients[c_index], skips[skip_index], to_plot, dist, alreadyComputedTrueTransient);
                                    String filepath = "Results/s" + servers[s_index] + "c" + (clients[c_index]==8?"08":clients[c_index]) + "sp" + skips[skip_index] + dist + "_at_the_end.json";
                                    writeData(filepath, i, "s" + servers[s_index] + "c" + clients[c_index] + "sp" + skips[skip_index] + dist + "_at_the_end", currentJSDs);
                                }catch (Exception e){
                                    System.err.println("error");
                                    i--;
                                }
                            }
                     //   }
                    // }
                }
            }
        }

        if (!seq) {
            HashMap<String, ArrayList<Double>> JSDs = new HashMap<String, ArrayList<Double>>();
            Launcher experiment_launcher = new Launcher();
            for (int s = 0; s < servers.length; s++) {
                for (int c = 0; c < clients.length; c++) {
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
                        ArrayList<Double> currentJSDs = experiment_launcher.launch(servers[s], clients[c], 0, to_plot, "exp", alreadyComputedTrueTransient);
                        String filepath = "Results/s" + servers[s] + "c" + (clients[c]==8?"08":clients[c]) + "skip0.json";
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

        // Prob 0
        if(seq){
            Launcher experiment_launcher = new Launcher();
                    int s_index = 0;
                    for (int c_index = 0; c_index < clients.length; c_index++) {
                       //for (int s_index = 0; s_index < servers.length; s_index++) {
                            boolean to_plot = false;
                            boolean alreadyComputedTrueTransient = false;
                            for (int i = 0; i < REPETITIONS; i++) {
                                try {
                                    Logger.debugMode = true;
                                    Logger.debug("\nLaunching experiment " + i + " -- checking the goodness of the analysis - changing s and c with fixed 0 skip prob");
                                    Logger.debugMode = false;
                                    if (i == 0) {
                                        alreadyComputedTrueTransient = false;
                                    } else {
                                        alreadyComputedTrueTransient = true;
                                    }
                                    ArrayList<Double> currentJSDs = experiment_launcher.launch_jsd_at_the_end(servers[s_index], clients[c_index], 0, to_plot, "exp", alreadyComputedTrueTransient);
                                    String filepath = "Results/s" + servers[s_index] + "c" + (clients[c_index]==8?"08":clients[c_index]) + "sp" + 0 + "exp" + "_at_the_end.json";
                                    writeData(filepath, i, "s" + servers[s_index] + "c" + clients[c_index] + "sp" + 0 + "exp" + "_at_the_end", currentJSDs);
                                }catch (Exception e){
                                    System.err.println("errore!");
                                    System.err.println("ora ci si riprova");
                                    i--;
                                }
                            }
                        //}
            }
        }
        //0
        if(!seq){
            Launcher experiment_launcher = new Launcher();
            String[] distributions = {"exp", "uni", "erl"};
            String dist = "exp";
            //for(String dist : distributions) {
                for (int skip_index = 1; skip_index < skips.length; skip_index++) {
                    for (int c_index = 0; c_index < clients.length; c_index++) {
                        //for (int s_index = 0; s_index < servers.length; s_index++) {
                            int s_index = 0;
                            boolean to_plot = false;
                            boolean alreadyComputedTrueTransient = false;
                            for (int i = 0; i < REPETITIONS; i++) {
                                try {
                                    Logger.debugMode = true;
                                    Logger.debug("\nLaunching experiment " + i + " -- checking the goodness of the analysis - changing c and skip prob");
                                    Logger.debugMode = false;
                                    if (i == 0) {
                                        alreadyComputedTrueTransient = false;
                                    } else {
                                        alreadyComputedTrueTransient = true;
                                    }
                                    ArrayList<Double> currentJSDs = experiment_launcher.launch_jsd_at_the_end(servers[s_index], clients[c_index], skips[skip_index], to_plot, dist, alreadyComputedTrueTransient);
                                    String filepath = "Results/s" + servers[s_index] + "c" + (clients[c_index]==8?"08":clients[c_index]) + "sp" + skips[skip_index] + dist + "_at_the_end.json";
                                    writeData(filepath, i, "s" + servers[s_index] + "c" + clients[c_index] + "sp" + skips[skip_index] + dist + "_at_the_end", currentJSDs);
                                    //  }
                                }catch (Exception e){
                                    System.err.println("errore!");
                                    System.err.println("ci si riprova, dai");
                                    i--;
                                }
                            }
                    }
               // }
            }
        }
        //1
        if(!seq){
            Launcher experiment_launcher = new Launcher();
            String[] distributions = {"exp2", "uni", "erl"};
            int skip_index = 0;
            for(String dist : distributions) {
            //for (int skip_index = 1; skip_index < skips.length; skip_index++) {
                for (int c_index = 0; c_index < clients.length; c_index++) {
                    //for (int s_index = 0; s_index < servers.length; s_index++) {
                    int s_index = 0;
                    boolean to_plot = false;
                    boolean alreadyComputedTrueTransient = false;
                    for (int i = 0; i < REPETITIONS; i++) {
                        try {
                            Logger.debugMode = true;
                            Logger.debug("\nLaunching experiment " + i + " -- checking the goodness of the analysis - changing distribution");
                            Logger.debugMode = false;
                            if (i == 0) {
                                alreadyComputedTrueTransient = false;
                            } else {
                                alreadyComputedTrueTransient = true;
                            }
                            ArrayList<Double> currentJSDs = experiment_launcher.launch_jsd_at_the_end(servers[s_index], clients[c_index], skips[skip_index], to_plot, dist, alreadyComputedTrueTransient);
                            String filepath = "Results/s" + servers[s_index] + "c" + ((clients[c_index]==8)?"08":clients[c_index]) + "sp" + skips[skip_index] + dist + "_at_the_end.json";
                            writeData(filepath, i, "s" + servers[s_index] + "c" + clients[c_index] + "sp" + skips[skip_index] + dist + "_at_the_end", currentJSDs);
                            //  }
                        }catch (Exception e){
                            System.err.println(e.getMessage());
                            System.err.println("Lets try again :P");
                            i--;
                        }
                    }
                }
                // }
            }
        }

        // Skip Prob Queue Dependant JSD at the end
        if(!seq){
            Launcher experiment_launcher = new Launcher();
            String[] distributions = {"exp", "uni", "erl"};
            String dist = "exp";
            //for(String dist : distributions) {
           // for (int skip_index = 1; skip_index < skips.length; skip_index++) {
                for (int c_index = 0; c_index < clients.length; c_index++) {
                    //for (int s_index = 0; s_index < servers.length; s_index++) {
                    int s_index = 0;
                    boolean to_plot = false;
                    boolean alreadyComputedTrueTransient = false;
                    for (int i = 0; i < REPETITIONS; i++) {
                        try {
                            Logger.debugMode = true;
                            Logger.debug("\nLaunching experiment " + i + " -- checking the goodness of the analysis - skip prob dep. on queue size");
                            Logger.debugMode = false;
                            if (i == 0) {
                                alreadyComputedTrueTransient = false;
                            } else {
                                alreadyComputedTrueTransient = true;
                            }
                            ArrayList<Double> currentJSDs = experiment_launcher.launch_jsd_at_the_end_skip_prob_queue_dependant(servers[s_index], clients[c_index], -1, to_plot, dist, alreadyComputedTrueTransient);
                            String filepath = "Results/s" + servers[s_index] + "c" + (clients[c_index]==8?"08":clients[c_index]) + "sp" + "_queue_" + dist + "_at_the_end.json";
                            writeData(filepath, i, "s" + servers[s_index] + "c" + clients[c_index] + "sp" + "_queue_" + dist + "_at_the_end", currentJSDs);
                            //  }
                        }catch (Exception e){
                            System.err.println("errore!");
                            System.err.println("ci si riprova, dai");
                            i--;
                        }
                    }
                //}
                // }
            }
        }
        Logger.debug("Done");
    }
}
