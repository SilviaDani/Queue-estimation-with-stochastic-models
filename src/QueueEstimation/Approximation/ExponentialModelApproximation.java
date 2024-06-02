package QueueEstimation.Approximation;

import Utils.Logger;
import Utils.WorkingPrintStreamLogger;
import org.oristool.models.gspn.GSPNTransient;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.RewardEvaluator;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;
import org.oristool.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ExponentialModelApproximation implements ModelApproximation{
    static PetriNet net = null; // We make this static because the approximant model is the same for every hyperexp model, the only things we change are the parameters
    static Marking marking = null;

    // Parameters
    private double lambda = 0.0;

    double epsilon = 1E-6;

    private double skip = 0.1;
    private double timeLimit;
    private double timeStep;

    public ExponentialModelApproximation(double mean, double variance, int initialTokens, double timeLimit, double timeStep){
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
        this.timeLimit = timeLimit;
        this.timeStep = timeStep;

    }

    public ExponentialModelApproximation(double mean, double variance, int initialTokens, double skip, double timeLimit, double timeStep){
        this(mean, variance, initialTokens, timeLimit, timeStep);
        this.skip = skip;
    }
    @Override
    public String getModelType() {
        return "EXP";
    }

    @Override
    public HashMap<Double, Double> analyzeModel() {
        Pair<Map<Marking, Integer>, double[][]> result = GSPNTransient.builder()
                .timePoints(0.0, timeLimit, timeStep)
                .build().compute(net, marking); // FIXME check if 100.0 as end time is enough (or it is too much)

        TransientSolution<Marking, Marking> solution = TransientSolution.fromArray(result.second(), timeStep, result.first(), marking);

        TransientSolution<Marking, RewardRate> reward = TransientSolution.computeRewards(false, solution, "If(Start==0,1,0)");
        if (false) {// FIXME remove this
            double[] thresholds = {0.1, 0.2, 0.3, 0.4, 0.5};
            int t_index = 0;
            HashMap<Double, Double> ETAs = new HashMap<>();
            for (int t = 0; t < reward.getSolution().length; t++) {
                if (reward.getSolution()[t][0][0] > thresholds[t_index]) {
                    Logger.debug("Time to reach " + thresholds[t_index] + ": " + t * timeStep);
                    ETAs.put(t * timeStep, thresholds[t_index]);
                    t_index++;
                    if (t_index == thresholds.length) {
                        break;
                    }
                }
            }
        return ETAs;
        }else{
            HashMap<Double, Double> transientSolution = new HashMap<>();
            for (int t = 0; t < reward.getSolution().length; t++) {
                transientSolution.put(t * timeStep, reward.getSolution()[t][0][0]);
            }
        return transientSolution;
        }
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
        //Generating Nodes
        //Generating Nodes
        Place Done = net.addPlace("Done");
        Place Start = net.addPlace("Start");
        Place Queue = net.addPlace("Queue");
        Transition Call = net.addTransition("Call");
        Transition Skip = net.addTransition("Skip");
        Transition Service = net.addTransition("Service");

        //Generating Connectors
        net.addPrecondition(Queue, Call);
        net.addPostcondition(Call, Start);
        net.addPrecondition(Queue, Skip);
        net.addPostcondition(Skip, Done);

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
        net.getTransition("Call").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Call").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(1.0-this.skip), net)));
        net.getTransition("Skip").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Skip").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(this.skip), net)));

        net.getTransition("Service").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Service").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambda), MarkingExpr.from("1", net)));
    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Start"), 0);
        marking.setTokens(net.getPlace("Queue"), initialTokens);
    }


}
