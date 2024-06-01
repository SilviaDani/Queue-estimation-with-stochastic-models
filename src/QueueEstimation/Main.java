package QueueEstimation;

import QueueEstimation.Approximation.*;
import Utils.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;

public class Main {
    final static int REPETITIONS = 100;
    public static void main(String[] args) {
        int numServers = 2;
        int numClients = 6; // Tagged Customer included!

        // Create the servers
        ArrayList<Server> servers = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            servers.add(new ExpServer(2));
            //servers.add(new UniServer(1, 10));
        }

        // Create the STPN model
        STPN stpn = new STPN(servers, numClients);
        ApproxParser approxParser = new ApproxParser();
        HashMap<Double, Double> trueTransient = null;
        try {
            trueTransient = stpn.makeModel();
        } catch (Exception e) {
            System.out.println("Error creating the model");
        }
        ModelApproximator modelApproximator = new ModelApproximator();
        ArrayList<Event> events = Parser.parse("log.txt", numServers);

        // Troviamo il reale tempo di attesa e il tempo di ciascun evento per il plot
        ArrayList<Event> filteredEvents = new ArrayList(); // arraylist di soli eventi fine servizio e skip
        for (int currentEvent = 0; currentEvent < events.size(); currentEvent++) {
            Event curEvent = events.get(currentEvent);
            if (curEvent instanceof EndService || curEvent instanceof LeaveQueue) {
                filteredEvents.add(curEvent);
            }
        }

        double realWaitingTime = filteredEvents.getLast().eventTime;
        // TODO da modificare in base alla risposta di Riccardo
        // TODO io direi numClients


        // Stimiamo il tempo di attesa con la rete approssimata ad ogni evento di fine o skip
        ArrayList<Double> obsTimes = new ArrayList(); // X axis of the plot
        ArrayList<Double> estimations = new ArrayList();
        ArrayList<Double> stds = new ArrayList();

        ArrayList<Double> JSDs = new ArrayList(); // it starts from 1 because we need at least 2 events to compute the mean and variance

        for (int currentEvent = 0; currentEvent < filteredEvents.size(); currentEvent++) {
            // Get current event
            Event curEvent = filteredEvents.get(currentEvent);
            obsTimes.add(curEvent.eventTime);
            // Compute mean and variance based on the simulated events up to the current one IF THERE ARE AT LEAST 2 EVENTS
            if (currentEvent >= 1){
                DescriptiveStatistics serviceStats = new DescriptiveStatistics();
                double skipProb = 0.0;
                for (int i = 0; i <= currentEvent; i++) {
                    Event event = filteredEvents.get(i);
                    if (event instanceof EndService) {
                        serviceStats.addValue(event.relativeEventTime);
                    } else if (event instanceof LeaveQueue) {
                        skipProb += 1.0;
                    }
                }
                double mean = serviceStats.getMean() / numServers;
                double variance = serviceStats.getVariance() / (numServers * numServers);
                double cv = Math.sqrt(variance) / mean;
                skipProb /= (currentEvent + 1);
                Logger.debug("Mean: " + mean + "\nVariance: " + variance + "\nCV: " + cv + "\nSkip probability: " + skipProb);
                // Compute approximation
                int queueSize = numClients - (currentEvent + 1);
                if (cv - 1 > 1E-6) {
                    modelApproximator.setModelApproximation(new HyperExponentialModelApproximation(mean, variance, queueSize, numServers, skipProb));
                } else if (Math.abs(cv - 1) <= 1E-6) {
                    modelApproximator.setModelApproximation(new ExponentialModelApproximation(mean, variance, queueSize, skipProb));
                } else if (cv < 1 && cv * cv > 0.5) {
                    modelApproximator.setModelApproximation(new HypoExponentialModelApproximation(mean, variance, queueSize, numServers, skipProb));
                } else {
                    modelApproximator.setModelApproximation(new LowCVHypoExponentialModelApproximation(mean, variance, queueSize, numServers, skipProb));
                }
                HashMap<Double, Double> approxTransient = modelApproximator.analyzeModel();
                // Measure the distance between the real distribution and the approximated
                double jsd = JensenShannonDistance.computeJensenShannonDistance(trueTransient, approxTransient); // TODO implement this
                JSDs.add(jsd);
            }
        }
        //print JSDs
        for (int i = 0; i < JSDs.size(); i++) {
            Logger.debug("JSD " + i + ": " + JSDs.get(i));
        }

        if (false) {
            ChartPlotter chartPlotter = new ChartPlotter("Ground Truth vs Approximation", obsTimes, estimations, stds);
            chartPlotter.setSize(800, 800);
            chartPlotter.setLocationRelativeTo(null);
            chartPlotter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            chartPlotter.setVisible(true);
        }
    }
}
