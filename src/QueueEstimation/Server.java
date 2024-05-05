package QueueEstimation;

public class Server {
    protected ServiceDistribution serviceDistribution;
    protected int distributionParameter; // At the moment is the lambda for the exponential distribution. In the future who knows?

    public Server(ServiceDistribution serviceDistribution, int distributionParameter) {
        serviceDistribution = ServiceDistribution.EXPONENTIAL;
        distributionParameter = 1;
    }

}
