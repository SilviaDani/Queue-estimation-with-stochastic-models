package QueueEstimation;

import QueueEstimation.Approximation.ExponentialModelApproximation;
import QueueEstimation.Approximation.HyperExponentialModelApproximation;
import QueueEstimation.Approximation.HypoExponentialModelApproximation;
import QueueEstimation.Approximation.ModelApproximator;
import Utils.Logger;
import Utils.Parser;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Main {
    public static void main(String[] args) {
        int numServers = 4;
        int numClients = 10;
        STPN stpn = new STPN(numServers, numClients);
        try {
            stpn.makeModel();
        }catch (Exception e){
            System.out.println("Error creating the model");
        }
        ModelApproximator modelApproximator = new ModelApproximator();
        ArrayList<Event> events = Parser.parse("log.txt", numServers);

        //Troviamo il reale tempo di attesa e il tempo di ciascun evento per il plot
        ArrayList<Event> filteredEvents = new ArrayList(); //arraylist di soli eventi fine servizio e skip
        for (int currentEvent=0; currentEvent>events.size(); currentEvent++) {
            Event curEvent = events.get(currentEvent);
            if (curEvent instanceof EndService || curEvent instanceof LeaveQueue){
                filteredEvents.add(curEvent);
            }
        }
        double realWaitingTime = filteredEvents.get(numClients+numServers-1).eventTime;
        //TODO da modificare in base alla risposta di Riccardo
        //TODO io direi numClients


        //Stimiamo il tempo di attesa con la rete approssimata ad ogni evento di fine o skip
        ArrayList<Double> obsTimes = new ArrayList();
        ArrayList<Double> estimations = new ArrayList();
        for (int currentEvent=0; currentEvent<filteredEvents.size(); currentEvent++){
            Event curEvent = filteredEvents.get(currentEvent);
            obsTimes.add(curEvent.eventTime);
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int i=0; i<=currentEvent; i++) {
                Event event = filteredEvents.get(i);
                if (event instanceof EndService) {
                    if (Integer.valueOf(event.clientID) >= 0) {
                        stats.addValue(event.relativeEventTime);
                        Logger.debug("EndService event: " + event.relativeEventTime);
                    } else {
                        //TODO gestire quelli che erano già a servizio
                    }
                } else if (event instanceof LeaveQueue) {
                    //TODO gestire gli skip
                }
                double mean = stats.getMean() / numServers;
                double variance = stats.getVariance() / (numServers * numServers);
                double cv = Math.sqrt(variance) / mean;
                Logger.debug("Mean: " + mean);
                Logger.debug("Variance: " + variance);
                Logger.debug("Standard deviation: " + Math.sqrt(variance));
                Logger.debug("CV: " + cv);
                //TODO DUBBIO MA QUELLI CHE SONO GIà DENTRO A UN SERVER LI METTO IN START o in intermediate o altro?
                if (cv > 1 + 1E-6) {
                    modelApproximator.setModelApproximation(new HyperExponentialModelApproximation(mean, variance, (numClients + numServers - currentEvent - 1), numServers));
                } else if (Math.abs(cv - 1) <= 1E-6) {
                    modelApproximator.setModelApproximation(new ExponentialModelApproximation(mean, variance, (numClients + numServers - currentEvent - 1)));
                } else {
                    modelApproximator.setModelApproximation(new HypoExponentialModelApproximation(mean, variance, (numClients + numServers - currentEvent - 1), numServers));
                }
                modelApproximator.approximateModel();
                //TODO ritornare la stima del tempo atteso (da modificare in base alla risposta di Riccardo)
                //estimations.add()
            }
        }
    }
}
