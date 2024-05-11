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
        STPN stpn = new STPN(numServers, 10);
        try {
            stpn.makeModel();
        }catch (Exception e){
            System.out.println("Error creating the model");
        }
        ArrayList<Event> events = Parser.parse("log.txt", numServers);

        int i = 0;
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Event event : events){
            if(event instanceof EndService) {
                if (i >= numServers) {
                    stats.addValue(event.relativeEventTime);
                    Logger.debug("EndService event: " + event.relativeEventTime);
                }
                i++;
            }
        }
        double mean = stats.getMean() / numServers;
        double variance = stats.getVariance() / (numServers * numServers);
        double cv = Math.sqrt(variance) / mean;
        Logger.debug("Mean: " + mean);
        Logger.debug("Variance: " + variance);
        Logger.debug("Standard deviation: " + Math.sqrt(variance));
        Logger.debug("CV: " + cv);


        // TODO: use real data. This is a placeholder
        if (cv > 1 + 1E-6){
            ModelApproximator modelApproximator = new ModelApproximator();
            modelApproximator.setModelApproximation(new HyperExponentialModelApproximation(mean, variance, 1, numServers));
            modelApproximator.approximateModel();
        } else if (Math.abs(cv - 1) <= 1E-6){
            ModelApproximator modelApproximator = new ModelApproximator();
            modelApproximator.setModelApproximation(new ExponentialModelApproximation(mean, variance, 1));
            modelApproximator.approximateModel();
        } else {
            ModelApproximator modelApproximator = new ModelApproximator();
            modelApproximator.setModelApproximation(new HypoExponentialModelApproximation(mean, variance, 1, numServers));
            modelApproximator.approximateModel();
        }
    }
}
