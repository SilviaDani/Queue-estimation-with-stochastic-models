package QueueEstimation.Approximation;

import Utils.Logger;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;

import java.math.BigDecimal;

public class HyperExponentialModelApproximation implements ModelApproximation{
    static PetriNet net = null; // We make this static because the approximant model is the same for every hyperexp model, the only things we change are the parameters
    static Marking marking = null;

    // Parameters
    private double p = 0.5;
    private double lambda0 = 1.0;
    private double lambda1 = 1.0;

    public  HyperExponentialModelApproximation(double mean, double variance, int initialTokens, int nServers){
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
        Logger.debug("Approximating hyper-exponential model");
    }

    private PetriNet createNet(){
        //Generating Nodes
        Place Done = net.addPlace("Done");
        Place Intermediate0 = net.addPlace("Intermediate0");
        Place Intermediate1 = net.addPlace("Intermediate1");
        Place Start = net.addPlace("Start");
        Transition Service0 = net.addTransition("Service0");
        Transition Service1 = net.addTransition("Service1");
        Transition Switch1_P = net.addTransition("Switch1_P");
        Transition SwitchP = net.addTransition("SwitchP");

        //Generating Connectors
        net.addPrecondition(Start, Switch1_P);
        net.addPrecondition(Intermediate1, Service1);
        net.addPostcondition(SwitchP, Intermediate0);
        net.addPrecondition(Start, SwitchP);
        net.addPostcondition(Service0, Done);
        net.addPrecondition(Intermediate0, Service0);
        net.addPostcondition(Switch1_P, Intermediate1);
        net.addPostcondition(Service1, Done);

        Switch1_P.addFeature(new Priority(0));
        SwitchP.addFeature(new Priority(0));
        return net;
    }

    public void computeParameters(double mean, double variance) {
        this.p = 0.5; //FIXME: now it is fixed
        double sqroot = Math.sqrt(2) * Math.sqrt(this.p * (this.p -1 ) * (mean * mean - variance));
        double denominator = this.p * mean * mean + this.p * variance - 2 * mean * mean;
        this.lambda0 = (2 * mean * (this.p -1) + sqroot) / denominator;
        this.lambda1 = (2 * mean * (this.p - 1) - sqroot) / denominator;
    }

    private void updateModel(int nServers){
        //Generating Properties
        net.getTransition("Service0").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Service0").addFeature( StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambda0), MarkingExpr.from("1", net)));
        net.getTransition("Service1").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Service1").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambda1), MarkingExpr.from("1", net)));
        net.getTransition("Switch1_P").removeFeature(EnablingFunction.class);
        net.getTransition("Switch1_P").addFeature(new EnablingFunction("Intermediate1 < "+nServers));
        net.getTransition("Switch1_P").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Switch1_P").addFeature( StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(1.0-this.p), net)));
        net.getTransition("SwitchP").removeFeature(EnablingFunction.class);
        net.getTransition("SwitchP").addFeature(new EnablingFunction("Intermediate0 < "+nServers));
        net.getTransition("SwitchP").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("SwitchP").addFeature( StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(this.p), net)));
    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Intermediate0"), 0);
        marking.setTokens(net.getPlace("Intermediate1"), 0);
        marking.setTokens(net.getPlace("Start"), initialTokens);
    }


}
