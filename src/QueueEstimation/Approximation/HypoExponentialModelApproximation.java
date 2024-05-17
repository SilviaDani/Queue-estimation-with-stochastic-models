package QueueEstimation.Approximation;

import Utils.Logger;
import Utils.WorkingPrintStreamLogger;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.RewardEvaluator;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

public class HypoExponentialModelApproximation implements ModelApproximation{
    static PetriNet net = null; // We make this static because the approximant model is the same for every hyperexp model, the only things we change are the parameters
    static Marking marking = null;

    // Parameters
    private int nErl = 1;
    private double lambdaErl = 1.0;
    private double lambdaExp = 1.0;

    private double skip = 0.1;

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

    public HypoExponentialModelApproximation(double mean, double variance, int initialTokens, int nServers, double skip){
        this(mean, variance, initialTokens, nServers);
        this.skip = skip;
    }
    @Override
    public String getModelType() {
        return "HYPOEXP";
    }
    @Override
    public void approximateModel() {
        File f = new File("log_approx.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(f);
            WorkingPrintStreamLogger l = new WorkingPrintStreamLogger(new PrintStream(fos), true);
            Sequencer s = new Sequencer(net, marking, new STPNSimulatorComponentsFactory(), l);

            BigDecimal timeLimit = new BigDecimal(1000);
            BigDecimal timeStep = new BigDecimal("0.1");
            int timePoints = (timeLimit.divide(timeStep)).intValue() + 1;

            TransientMarkingConditionProbability r1 =
                    new TransientMarkingConditionProbability(s,
                            new ContinuousRewardTime(timeStep), timePoints,
                            MarkingCondition.fromString("Done"));
            RewardEvaluator re1 = new RewardEvaluator(r1, 1);
            s.simulate();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private PetriNet createNet(){
        Place Done = net.addPlace("Done");
        Place Intermediate = net.addPlace("Intermediate");
        Place Start = net.addPlace("Start");
        Place Queue = net.addPlace("Queue");
        Transition Call = net.addTransition("Call");
        Transition Skip = net.addTransition("Skip");
        Transition ServiceERL = net.addTransition("ServiceERL");
        Transition ServiceEXP = net.addTransition("ServiceEXP");

        //Generating Connectors
        net.addPrecondition(Queue, Call);
        net.addPostcondition(Call, Start);
        net.addPrecondition(Queue, Skip);
        net.addPostcondition(Skip, Done);

        net.addPrecondition(Start, ServiceERL);
        net.addPostcondition(ServiceEXP, Done);
        net.addPostcondition(ServiceERL, Intermediate);
        net.addPrecondition(Intermediate, ServiceEXP);

        return net;
    }

    public void computeParameters(double mean, double variance) {
        /*
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
        Logger.debug("lambdaExp: " + this.lambdaExp);*/
        double cv = Math.sqrt(variance)/mean;
        this.nErl = 1;
        this.lambdaErl = (2/mean) / (1 + Math.sqrt(1 + 2 * (cv * cv - 1)));
        this.lambdaExp = (2/mean) / (1 - Math.sqrt(1 + 2 * (cv * cv - 1)));
        Logger.debug("nErl: " + this.nErl);
        Logger.debug("lambdaErl: " + this.lambdaErl);
        Logger.debug("lambdaExp: " + this.lambdaExp);
    }

    private void updateModel(int nServers){

        net.getTransition("Call").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Call").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(1.0-this.skip), net)));
        net.getTransition("Skip").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Skip").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(this.skip), net)));

        net.getTransition("ServiceERL").removeFeature(EnablingFunction.class);
        net.getTransition("ServiceERL").addFeature(new EnablingFunction("Intermediate < "+ nServers)); // FIXME: check if it's correct
        net.getTransition("ServiceERL").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("ServiceERL").addFeature(StochasticTransitionFeature.newErlangInstance(nErl, new BigDecimal(lambdaErl)));
        net.getTransition("ServiceEXP").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("ServiceEXP").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambdaExp), MarkingExpr.from("1", net)));

    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Intermediate"), 0);
        marking.setTokens(net.getPlace("Start"), 0);
        marking.setTokens(net.getPlace("Queue"), initialTokens);
    }
}
