package QueueEstimation;

import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;

import java.math.BigDecimal;

public class UniServer extends Server{

    private final double lowerBound;
    private final double upperBound;
    public UniServer(double lowerBound, double upperBound) {
        super();
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net){
        return StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(lowerBound), BigDecimal.valueOf(upperBound));
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net, double clockRate){
        return StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(lowerBound), BigDecimal.valueOf(upperBound), MarkingExpr.from(String.valueOf(clockRate), net));
    }
}
