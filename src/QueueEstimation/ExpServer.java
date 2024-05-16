package QueueEstimation;

import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;

import java.math.BigDecimal;

public class ExpServer extends Server{

    double lambda = 0.0;

    public ExpServer(double distributionParameter) {
        super();
        this.lambda = distributionParameter;
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net){
        return StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(lambda), MarkingExpr.from("1", net));
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net, double clockRate){
        return StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(lambda), MarkingExpr.from(String.valueOf(clockRate), net));
    }

}
