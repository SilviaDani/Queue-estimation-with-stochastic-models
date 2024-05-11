package QueueEstimation.Approximation;

import Utils.Logger;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;

public class ExponentialModelApproximation implements ModelApproximation{
    static PetriNet net = null; // We make this static because the approximant model is the same for every hyperexp model, the only things we change are the parameters
    static Marking marking = null;

    // Parameters
    private double lambda = 0.0;

    double epsilon = 1E-6;

    public ExponentialModelApproximation(double mean, double variance, int initialTokens){
        if (net == null) {
            net = new PetriNet();
            net = createNet();
        }
        if (marking == null){
            marking = new Marking();
        }
        computeParameters(mean, variance);
        updateModel();

        setInitialMarking(initialTokens);

    }
    @Override
    public void approximateModel() {
        Logger.debug("Approximating exponential model");
    }

    private PetriNet createNet(){
        //Generating Nodes
        //Generating Nodes
        Place Done = net.addPlace("Done");
        Place Start = net.addPlace("Start");
        Transition Service = net.addTransition("Service");

        //Generating Connectors
        net.addPrecondition(Start, Service);
        net.addPostcondition(Service, Done);
        return net;
    }

    public void computeParameters(double mean, double variance) {
        // assert mean == variance;
        assert Math.abs(mean - variance) <= epsilon : "Mean and variance must be equal for an exponential distribution";

        this.lambda = mean;
    }

    private void updateModel(){
        //Generating Properties
        net.getTransition("Service").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Service").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambda), MarkingExpr.from("1", net)));
    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Start"), initialTokens);
    }


}
