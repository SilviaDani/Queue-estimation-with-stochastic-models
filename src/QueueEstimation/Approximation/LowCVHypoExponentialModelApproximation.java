package QueueEstimation.Approximation;

import QueueEstimation.Approximation.ModelApproximation;
import Utils.Logger;
import Utils.WorkingPrintStreamLogger;
import org.oristool.analyzer.log.NoOpLogger;
import org.oristool.models.gspn.GSPNTransient;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.onegen.OneGenTransient;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trans.TreeTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.TimeSeriesRewardResult;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.RewardEvaluator;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;
import org.oristool.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class LowCVHypoExponentialModelApproximation implements ModelApproximation {
    static PetriNet net = null;
    static Marking marking = null;
    
    private double detOffset = 0.0;
    private double lambda = 1.0;

    private double skip;

    private double timeLimit;
    private double timeStep;
    public LowCVHypoExponentialModelApproximation(double mean, double variance, int initialTokens, int nServers, double timeLimit, double timeStep){
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
        this.timeLimit = timeLimit;
        this.timeStep = timeStep;
    }

    public LowCVHypoExponentialModelApproximation (double mean, double variance, int initialTokens, int nServers, double skip, double timeLimit, double timeStep){
        this(mean, variance, initialTokens, nServers, timeLimit, timeStep);
        this.skip = skip;
    }

    private void setInitialMarking(int initialTokens) {
        marking.setTokens(net.getPlace("Done"), 0);
        marking.setTokens(net.getPlace("Intermediate"), 0);
        marking.setTokens(net.getPlace("Start"), 0);
        marking.setTokens(net.getPlace("Queue"), initialTokens);
        marking.setTokens(net.getPlace("Sink"), 0);
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
        net.getTransition("ServiceDET").removeFeature(EnablingFunction.class);
        net.getTransition("ServiceDET").addFeature(new EnablingFunction("Start > 1 && Intermediate == 0"));
        net.getTransition("ServiceEXP").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("ServiceEXP").addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal(this.lambda), MarkingExpr.from("1", net)));

        net.getTransition("LastClientInQueueIsProcessed").removeFeature(StochasticTransitionFeature.class);
        net.getTransition("LastClientInQueueIsProcessed").addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        net.getTransition("LastClientInQueueIsProcessed").removeFeature(Priority.class);
        net.getTransition("LastClientInQueueIsProcessed").addFeature(new Priority(0));
        net.getTransition("LastClientInQueueIsProcessed").removeFeature(EnablingFunction.class);
        net.getTransition("LastClientInQueueIsProcessed").addFeature(new EnablingFunction("Start == 1 && Queue == 0 && Intermediate == 0"));
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
        Place Sink = net.addPlace("Sink");
        Transition Call = net.addTransition("Call");
        Transition Skip = net.addTransition("Skip");
        Transition ServiceDET = net.addTransition("ServiceDET");
        Transition ServiceEXP = net.addTransition("ServiceEXP");
        Transition LastClientInQueueIsProcessed = net.addTransition("LastClientInQueueIsProcessed");

        //Generating Connectors
        net.addPrecondition(Queue, Call);
        net.addPostcondition(Call, Start);
        net.addPrecondition(Queue, Skip);
        net.addPostcondition(Skip, Done);
        net.addPrecondition(Start, LastClientInQueueIsProcessed);
        net.addPostcondition(LastClientInQueueIsProcessed, Sink);

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

            BigDecimal timeLimit = new BigDecimal(this.timeLimit);
            BigDecimal timeStep = new BigDecimal(this.timeStep);
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
    public HashMap<Double, Double>  analyzeModel() {
        BigDecimal timeLimit_bigDecimal = new BigDecimal(timeLimit);
        BigDecimal timeStep_bigDecimal = new BigDecimal(timeStep);
        int timePoints = (timeLimit_bigDecimal.divide(timeStep_bigDecimal, RoundingMode.DOWN)).intValue() + 1;
        Sequencer seq = new Sequencer(net, marking, new STPNSimulatorComponentsFactory(), NoOpLogger.INSTANCE);
        TransientMarkingConditionProbability rqs = new TransientMarkingConditionProbability(seq,
                new ContinuousRewardTime(timeStep_bigDecimal), timePoints,
                MarkingCondition.fromString("Sink"));
        RewardEvaluator re = new RewardEvaluator(rqs, 1000);
        seq.simulate();
        TimeSeriesRewardResult result = (TimeSeriesRewardResult) re.getResult();
        BigDecimal[] timeSerie = result.getTimeSeries(result.getMarkings().iterator().next());
        HashMap<Double, Double> transientSolution = new HashMap<>();
        for (int t = 0; t < timeSerie.length; t++) {
            transientSolution.put(t * timeStep, timeSerie[t].doubleValue());
        }
        return transientSolution;
    }
}
