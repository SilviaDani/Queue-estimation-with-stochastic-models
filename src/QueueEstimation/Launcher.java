package QueueEstimation;

import QueueEstimation.Approximation.*;
import Utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;

public class Launcher {

    public ArrayList<Double> launch(int nServers, int nClients, double skipProb, boolean to_plot) {
        int numServers = nServers;
        int numClients = nClients; // Tagged Customer included!
        double timeLimit = 50.0;
        double timeStep = 0.01;

        // Create the servers
        ArrayList<Server> servers = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            servers.add(new ExpServer(2));
            //servers.add(new UniServer(5, 10));
        }

        // Create the STPN model
        STPN stpn = new STPN(servers, numClients,timeLimit, timeStep, skipProb);
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
            if ((curEvent instanceof StartService || curEvent instanceof LeaveQueue) && Objects.equals(curEvent.clientID, String.valueOf(numClients - 1))){
                filteredEvents.add(new TaggedCustomerBeingProcessed(curEvent.eventTime, curEvent.serverID, curEvent.clientID));
            }
        }

        double realWaitingTime = filteredEvents.getLast().eventTime;


        // Stimiamo il tempo di attesa con la rete approssimata ad ogni evento di fine o skip
        ArrayList<Double> obsTimes = new ArrayList(); // X axis of the plot
        ArrayList<Double> estimations = new ArrayList();
        ArrayList<Double> stds = new ArrayList();

        HashMap<Double, Double> approxTransientBefore = null;
        ArrayList<Double> JSDs = new ArrayList(); // it starts from 1 because we need at least 2 events to compute the mean and variance
        for (int currentEvent = 0; currentEvent < filteredEvents.size(); currentEvent++) {
            /*
            String progress = "";
            for (int i = 0; i < currentEvent; i++)
                progress += "*";
            Logger.debug(progress);
            */
            Logger.debug("Event " + currentEvent + " of " + (filteredEvents.size() - 1));
            // Get current event
            Event curEvent = filteredEvents.get(currentEvent);
            obsTimes.add(curEvent.eventTime);
            if (curEvent instanceof TaggedCustomerBeingProcessed) { // Tagged customer is being served
                Logger.debug("Tagged customer is being served, exiting...");
                break;
            }
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
                double variance = (serviceStats.getVariance() + 1e-6) / (numServers * numServers);
                double cv = Math.sqrt(variance) / mean;
                skipProb /= (currentEvent + 1);
                Logger.debug("Mean: " + mean + "\nVariance: " + variance + "\nCV: " + cv + "\nSkip probability: " + skipProb);
                // Compute approximation
                //int queueSize = numClients - (currentEvent + 1);
                int queueSize = numClients;
                if (cv - 1 > 1E-6) {
                    modelApproximator.setModelApproximation(new HyperExponentialModelApproximation(mean, variance, queueSize, numServers, skipProb, timeLimit, timeStep));
                } else if (Math.abs(cv - 1) <= 1E-6) {
                    modelApproximator.setModelApproximation(new ExponentialModelApproximation(mean, variance, queueSize, skipProb, timeLimit, timeStep));
                } else if (cv < 1 && cv * cv > 0.5) {
                    modelApproximator.setModelApproximation(new HypoExponentialModelApproximation(mean, variance, queueSize, numServers, skipProb, timeLimit, timeStep));
                } else {
                    modelApproximator.setModelApproximation(new LowCVHypoExponentialModelApproximation(mean, variance, queueSize, numServers, skipProb, timeLimit, timeStep));
                }
                HashMap<Double, Double> approxTransient = modelApproximator.analyzeModel();
                if (currentEvent >= 2){ //perchè l'aggiornamento parte dopo aver contato 2 eventi TODO se so cambia l'if esterno cambiare anche questo
                    // Plot like Rogge-Solti
                    String title = "before vs after " + currentEvent;
                    ReggeSoltiPlotter reggeSoltiPlotter = new ReggeSoltiPlotter(title, approxTransientBefore, approxTransient, trueTransient, currentEvent);
                    reggeSoltiPlotter.setSize(800, 800);
                    reggeSoltiPlotter.setLocationRelativeTo(null);
                    reggeSoltiPlotter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    reggeSoltiPlotter.setVisible(true);
                }
                approxTransientBefore = approxTransient;
                // Measure the distance between the real distribution and the approximated
                double jsd = JensenShannonDivergence.computeJensenShannonDivergence(ConverterCDFToPDF.convertCDFToPDF(trueTransient), ConverterCDFToPDF.convertCDFToPDF(approxTransient));
                JSDs.add(jsd);
            }
        }
        // print the JSDs
        for (int i = 0; i < JSDs.size(); i++) {
            Logger.debug("JSD " + (i+1) + ": " + JSDs.get(i));
        }

        if (false) {
            ChartPlotter chartPlotter = new ChartPlotter("Ground Truth vs Approximation", obsTimes, estimations, stds);
            chartPlotter.setSize(800, 800);
            chartPlotter.setLocationRelativeTo(null);
            chartPlotter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            chartPlotter.setVisible(true);
        }
        return JSDs;
    }
}
