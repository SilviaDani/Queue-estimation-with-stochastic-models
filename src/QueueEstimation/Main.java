package QueueEstimation;

import QueueEstimation.Approximation.ExponentialModelApproximation;
import QueueEstimation.Approximation.HyperExponentialModelApproximation;
import QueueEstimation.Approximation.HypoExponentialModelApproximation;
import QueueEstimation.Approximation.ModelApproximator;
import Utils.ApproxParser;
import Utils.ChartPlotter;
import Utils.Logger;
import Utils.Parser;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) {
        int numServers = 3;
        int numClients = 50;
        STPN stpn = new STPN(numServers, numClients);
        ApproxParser approxParser = new ApproxParser();
        try {
            stpn.makeModel();
        }catch (Exception e){
            System.out.println("Error creating the model");
        }
        ModelApproximator modelApproximator = new ModelApproximator();
        ArrayList<Event> events = Parser.parse("log.txt", numServers);

        // Troviamo il reale tempo di attesa e il tempo di ciascun evento per il plot
        ArrayList<Event> filteredEvents = new ArrayList(); // arraylist di soli eventi fine servizio e skip
        for (int currentEvent=0; currentEvent<events.size(); currentEvent++) {
            Event curEvent = events.get(currentEvent);
            if (curEvent instanceof EndService || curEvent instanceof LeaveQueue){
                filteredEvents.add(curEvent);
            }
        }

        double realWaitingTime = filteredEvents.getLast().eventTime;
        // TODO da modificare in base alla risposta di Riccardo
        // TODO io direi numClients


        // Stimiamo il tempo di attesa con la rete approssimata ad ogni evento di fine o skip
        ArrayList<Double> obsTimes = new ArrayList(); // X axis of the plot
        ArrayList<Double> estimations = new ArrayList();

        for (int currentEvent = 1; currentEvent < filteredEvents.size(); currentEvent++){
            Logger.debug("-------------------------------------------------");
            Logger.debug("Current event: " + currentEvent);
            Logger.debug(filteredEvents.get(currentEvent).toString());
            // Get the current event
            Event curEvent = filteredEvents.get(currentEvent);
            obsTimes.add(curEvent.eventTime);
            Logger.debug("Observed time: " + curEvent.eventTime);
            Logger.debug("Time left: " + (realWaitingTime - curEvent.eventTime));
            DescriptiveStatistics stats = new DescriptiveStatistics();
            // Compute mean and variance of the service time based on the events up to the current one
            for (int i = 0; i <= currentEvent; i++) {
                Event event = filteredEvents.get(i);
                if (event instanceof EndService) {
                    if (Integer.parseInt(event.clientID) >= 0) {
                        stats.addValue(event.relativeEventTime);
                        Logger.debug("EndService event: " + event.relativeEventTime);
                    } else {
                        // TODO gestire quelli che erano già a servizio
                        Logger.debug("eeeeee");
                    }
                } else if (event instanceof LeaveQueue) {
                    // TODO gestire gli skip
                    Logger.debug("hei");
                }
            }
                double mean = stats.getMean() / numServers;
                double variance = stats.getVariance() / (numServers * numServers);
                double cv = Math.sqrt(variance) / mean;
                Logger.debug("Mean: " + mean);
                Logger.debug("Variance: " + variance);
                Logger.debug("Standard deviation: " + Math.sqrt(variance));
                Logger.debug("CV: " + cv);
                // Compute approximation
            if (numClients - currentEvent - 1 > 0) {
                //TODO DUBBIO MA QUELLI CHE SONO GIà DENTRO A UN SERVER LI METTO IN START o in intermediate o altro?
                if (cv > 1 + 1E-6) {
                    modelApproximator.setModelApproximation(new HyperExponentialModelApproximation(mean, variance, (numClients  - currentEvent - 1), numServers));
                } else if (Math.abs(cv - 1) <= 1E-6) {
                    modelApproximator.setModelApproximation(new ExponentialModelApproximation(mean, variance, (numClients  - currentEvent - 1)));
                } else {
                    modelApproximator.setModelApproximation(new HypoExponentialModelApproximation(mean, variance, (numClients - currentEvent - 1), numServers));
                }
                modelApproximator.approximateModel();
                // Parse the output
                ArrayList<Event> approxEvents = ApproxParser.getApproximatedETA("log_approx.txt", modelApproximator);
                // Let's consider only the last-nServer event
                // TODO: modificare in base alla risposta di Riccardo -> double eta = approxEvents.get(approxEvents.size() - numServers).eventTime;
                double eta = approxEvents.getLast().eventTime;
                for (int i = 0; i < approxEvents.size(); i++) {
                    Logger.debug(approxEvents.get(i).toString());
                }
                Logger.debug("Estimated time: " + eta);
                estimations.add(eta);
            } else {
                estimations.add(0.0);
            }
                //TODO ritornare la stima del tempo atteso (da modificare in base alla risposta di Riccardo)
                //estimations.add()
        }
        ChartPlotter chartPlotter = new ChartPlotter("Ground Truth vs Approximation", obsTimes, estimations);
        chartPlotter.setSize(800, 800);
        chartPlotter.setLocationRelativeTo(null);
        chartPlotter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chartPlotter.setVisible(true);
    }
}
