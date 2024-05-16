package QueueEstimation;

import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;

public class Server {
    protected ServiceDistribution serviceDistribution;
    protected double distributionParameter; // At the moment is the lambda for the exponential distribution. In the future who knows?

    public Server(ServiceDistribution serviceDistribution, double distributionParameter) {
        serviceDistribution = ServiceDistribution.EXPONENTIAL;
        distributionParameter = 1.0;
    }

    public Server(double distributionParameter) {
        serviceDistribution = ServiceDistribution.EXPONENTIAL; // FIXME: is it useful?
        this.distributionParameter = distributionParameter;
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net) {
        return null;
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(PetriNet net, double clockRate) {
        return null;
    }

}
