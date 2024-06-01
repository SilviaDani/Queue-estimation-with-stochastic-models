package QueueEstimation.Approximation;

import Utils.Logger;
import Utils.WorkingPrintStreamLogger;
import org.apache.commons.math3.analysis.function.Log;
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

public class HyperExponentialModelApproximation implements ModelApproximation{
    static PetriNet net = null; // We make this static because the approximant model is the same for every hyperexp model, the only things we change are the parameters
    static Marking marking = null;

    // Parameters
    private double p = 0.5;
    private double lambda0 = 1.0;
    private double lambda1 = 1.0;

    private double skip = 0.1;

    public  HyperExponentialModelApproximation(double mean, double variance, int initialTokens, int nServers){
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

    public HyperExponentialModelApproximation(double mean, double variance, int initialTokens, int nServers, double skip){
        this(mean, variance, initialTokens, nServers);
        this.skip = skip;
    }
    @Override
    public String getModelType() {
        return "HYPEREXP";
    }

    @Override
    public HashMap<Double, Double> analyzeModel() {
        double step = 0.1;
        Pair<Map<Marking, Integer>, double[][]> result = GSPNTransient.builder()
                .timePoints(0.0, 100.0, step)
                .build().compute(net, marking); // FIXME check if 100.0 as end time is enough (or it is too much)

        TransientSolution<Marking, Marking> solution = TransientSolution.fromArray(result.second(), step, result.first(), marking);

        TransientSolution<Marking, RewardRate> reward = TransientSolution.computeRewards(false, solution, "If(Start==0,1,0)");
        if (false) {// FIXME remove this
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
        Place Done = net.addPlace("Done");
        Place Intermediate0 = net.addPlace("Intermediate0");
        Place Intermediate1 = net.addPlace("Intermediate1");
        Place Start = net.addPlace("Start");
        Place Queue = net.addPlace("Queue");
        Transition Call = net.addTransition("Call");
        Transition Skip = net.addTransition("Skip");
        Transition Service0 = net.addTransition("Service0");
        Transition Service1 = net.addTransition("Service1");
        Transition Switch1_P = net.addTransition("Switch1_P");
        Transition SwitchP = net.addTransition("SwitchP");

        //Generating Connectors
        net.addPrecondition(Queue, Call);
        net.addPostcondition(Call, Start);
        net.addPrecondition(Queue, Skip);
        net.addPostcondition(Skip, Done);

        net.addPrecondition(Start, Switch1_P);
        net.addPrecondition(Intermediate1, Service1);
        net.addPostcondition(SwitchP, Intermediate0);
        net.addPrecondition(Start, SwitchP);
        net.addPostcondition(Service0, Done);
        net.addPrecondition(Intermediate0, Service0);
        net.addPostcondition(Switch1_P, Intermediate1);
        net.addPostcondition(Service1, Done);

        Switch1_P.addFeature(new Priority(0));
        SwitchP.addFeature(new Priority(0));
        return net;
    }

    public void computeParameters(double mean, double variance) {
        this.p = 0.5; //TODO p ora è preso a caso
        double cvsqaured = variance/(mean * mean);
        this.lambda0 = (1 / mean) / (1 - Math.sqrt((1 - this.p) / this.p) * (cvsqaured - 1)/2);
        this.lambda1 = (1 / mean) / (1 + Math.sqrt(this.p / (1 - this.p)) * (cvsqaured - 1)/2);
        Logger.debug("p: " + this.p);
        Logger.debug("lambda0: " + this.lambda0);
        Logger.debug("lambda1: " + this.lambda1);
    }

    private void updateModel(int nServers){
        //Generating Properties
        net.getTransition("Call").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Call").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(1.0-this.skip), net)));
        net.getTransition("Skip").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Skip").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(this.skip), net)));

        net.getTransition("Service0").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Service0").addFeature( StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambda0), MarkingExpr.from("1", net)));
        net.getTransition("Service1").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Service1").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(lambda1), MarkingExpr.from("1", net)));
        net.getTransition("Switch1_P").removeFeature(EnablingFunction.class);
        net.getTransition("Switch1_P").addFeature(new EnablingFunction("Intermediate1 < "+nServers));
        net.getTransition("Switch1_P").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("Switch1_P").addFeature( StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(1.0-this.p), net)));
        net.getTransition("SwitchP").removeFeature(EnablingFunction.class);
        net.getTransition("SwitchP").addFeature(new EnablingFunction("Intermediate0 < "+nServers));
        net.getTransition("SwitchP").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("SwitchP").addFeature( StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(this.p), net)));
    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Intermediate0"), 0);
        marking.setTokens(net.getPlace("Intermediate1"), 0);
        marking.setTokens(net.getPlace("Start"), 0);
        marking.setTokens(net.getPlace("Queue"), initialTokens);
    }


}
