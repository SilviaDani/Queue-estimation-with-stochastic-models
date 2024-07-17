package QueueEstimation;

import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;

import java.math.BigDecimal;

public class ErlServer extends Server{

    double lambda = 0.0;
    int stages = 0;

    public ErlServer(int stages, double distributionParameter) {
        super();
        this.lambda = distributionParameter;
        this.stages = stages;
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net){
        return StochasticTransitionFeature.newErlangInstance(stages, BigDecimal.valueOf(lambda));
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net, double clockRate){
        System.err.println("Erlang distribution with clock rate is not implemented yet");
        return StochasticTransitionFeature.newErlangInstance(stages, BigDecimal.valueOf(lambda)); //TODO: to be implemented
    }

}