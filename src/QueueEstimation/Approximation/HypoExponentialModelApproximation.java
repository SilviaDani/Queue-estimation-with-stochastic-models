package QueueEstimation.Approximation;

import Utils.Logger;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;

import java.math.BigDecimal;

public class HypoExponentialModelApproximation implements ModelApproximation{
    static PetriNet net = null; // We make this static because the approximant model is the same for every hyperexp model, the only things we change are the parameters
    static Marking marking = null;

    // Parameters
    private int nErl = 1;
    private double lambdaErl = 1.0;
    private double lambdaExp = 1.0;

    final static double CV_THRESHOLD = 0.707106781;

    public  HypoExponentialModelApproximation(double mean, double variance, int initialTokens, int nServers){
        if (net == null){
            net = new PetriNet();
            net = createNet();
        }
        if (marking == null){
            marking = new Marking();
        }
        computeParameters(mean, variance);
        updateModel(nServers);
        setInitialMarking(initialTokens);

    }
    @Override
    public void approximateModel() {
        Logger.debug("Approximating hypo-exponential model");
    }

    private PetriNet createNet(){
        Place Done = net.addPlace("Done");
        Place Intermediate = net.addPlace("Intermediate");
        Place Start = net.addPlace("Start");
        Transition ServiceERL = net.addTransition("ServiceERL");
        Transition ServiceEXP = net.addTransition("ServiceEXP");

        //Generating Connectors
        net.addPrecondition(Start, ServiceERL);
        net.addPostcondition(ServiceEXP, Done);
        net.addPostcondition(ServiceERL, Intermediate);
        net.addPrecondition(Intermediate, ServiceEXP);

        return net;
    }

    public void computeParameters(double mean, double variance) {
        //TODO: Implement the computation of the parameters
        double cv = Math.sqrt(variance)/mean;
        this.nErl = 1;
        if (cv < CV_THRESHOLD){
            while(this.nErl * (- mean * mean + this.nErl * variance + variance) < 0) {
                this.nErl++;
            }
        }
        double sqroot = Math.sqrt(this.nErl * (- mean * mean + this.nErl * variance + variance));
        double denominator = mean * mean - this.nErl * variance;
        this.lambdaErl = (mean + sqroot) / denominator;
        this.lambdaExp = (mean - sqroot) / denominator;
        Logger.debug("nErl: " + this.nErl);
        Logger.debug("lambdaErl: " + this.lambdaErl);
        Logger.debug("lambdaExp: " + this.lambdaExp);
    }

    private void updateModel(int nServers){
        net.getTransition("ServiceERL").removeFeature(EnablingFunction.class);
        net.getTransition("ServiceERL").addFeature(new EnablingFunction("Intermediate < "+ nServers));
        net.getTransition("ServiceERL").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("ServiceERL").addFeature(StochasticTransitionFeature.newErlangInstance(nErl, new BigDecimal(lambdaErl)));
        net.getTransition("ServiceEXP").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("ServiceEXP").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambdaExp), MarkingExpr.from("1", net)));

    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Intermediate"), 0);
        marking.setTokens(net.getPlace("Start"), initialTokens);
    }
}
