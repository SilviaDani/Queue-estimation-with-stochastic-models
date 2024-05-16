package QueueEstimation;

import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;

import java.math.BigDecimal;

public class ExpServer extends Server{


    public ExpServer(int distributionParameter) {
        super(distributionParameter);
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net){
        return StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(distributionParameter), MarkingExpr.from("1", net));
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net, double clockRate){
        return StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(distributionParameter), MarkingExpr.from(String.valueOf(clockRate), net));
    }

}
