package QueueEstimation.Approximation;

import QueueEstimation.Approximation.ModelApproximation;
import Utils.Logger;
import Utils.WorkingPrintStreamLogger;
import org.oristool.models.gspn.GSPNTransient;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.onegen.OneGenTransient;
import org.oristool.models.stpn.trans.TreeTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
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

public class LowCVHypoExponentialModelApproximation implements ModelApproximation {
    static PetriNet net = null;
    static Marking marking = null;
    
    private double detOffset = 0.0;
    private double lambda = 1.0;

    private double skip;
    public LowCVHypoExponentialModelApproximation(double mean, double variance, int initialTokens, int nServers) {
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

    public LowCVHypoExponentialModelApproximation (double mean, double variance, int initialTokens, int nServers, double skip){
        this(mean, variance, initialTokens, nServers);
        this.skip = skip;
    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Intermediate"), 0);
        marking.setTokens(net.getPlace("Start"), 0);
        marking.setTokens(net.getPlace("Queue"), initialTokens);
    }

    private void updateModel(int nServers) {
        net.getTransition("Call").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Call").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(1.0-this.skip), net)));
        net.getTransition("Skip").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Skip").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(this.skip), net)));

        net.getTransition("ServiceDET").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("ServiceDET").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal(this.detOffset), MarkingExpr.from("1", net)));
        net.getTransition("ServiceDET").removeFeature(Priority.class);
        net.getTransition("ServiceDET").addFeature(new Priority(0));
        net.getTransition("ServiceEXP").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("ServiceEXP").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(this.lambda), MarkingExpr.from("1", net)));
    }

    private void computeParameters(double mean, double variance) {
        this.lambda = 1 / Math.sqrt(variance);
        this.detOffset = mean - 1 / this.lambda;
    }

    private PetriNet createNet() {
        //Generating Nodes
        Place Done = net.addPlace("Done");
        Place Intermediate = net.addPlace("Intermediate");
        Place Start = net.addPlace("Start");
        Place Queue = net.addPlace("Queue");
        Transition Call = net.addTransition("Call");
        Transition Skip = net.addTransition("Skip");
        Transition ServiceDET = net.addTransition("ServiceDET");
        Transition ServiceEXP = net.addTransition("ServiceEXP");

        //Generating Connectors
        net.addPrecondition(Queue, Call);
        net.addPostcondition(Call, Start);
        net.addPrecondition(Queue, Skip);
        net.addPostcondition(Skip, Done);

        net.addPrecondition(Start, ServiceDET);
        net.addPostcondition(ServiceEXP, Done);
        net.addPostcondition(ServiceDET, Intermediate);
        net.addPrecondition(Intermediate, ServiceEXP);

        return net;
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

    @Override
    public String getModelType() {
        return "LOWCVHYPOEXP";
    }

    @Override
    public HashMap<Double, Double>  analyzeModel() { // FIXME it's reeeeeaaaaalllllly slow
        double step = 0.1;

        TransientSolution<Marking, Marking> solution = TreeTransient.builder()
                .timeBound(new BigDecimal("100.0"))
                .timeStep(new BigDecimal(step))
                .build().compute(net, marking);

        TransientSolution<Marking, RewardRate> reward = TransientSolution.computeRewards(false, solution, "If(Start==0,1,0)");

        if (false) { // FIXME remove this
            double[] thresholds = {0.1, 0.2, 0.3, 0.4, 0.5};
            int t_index = 0;
            HashMap<Double, Double> ETAs = new HashMap<>();
            for (int t = 0; t < reward.getSolution().length; t++) {
                if (reward.getSolution()[t][0][0] > thresholds[t_index]) {
                    Logger.debug("Time to reach " + thresholds[t_index] + ": " + t * step);
                    ETAs.put(t * step, thresholds[t_index]);
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
                transientSolution.put(t * step, reward.getSolution()[t][0][0]);
            }
            return transientSolution;
        }
    }
}
