package QueueEstimation;

import Utils.Logger;
import Utils.WorkingPrintStreamLogger;
import org.oristool.analyzer.log.NoOpLogger;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trans.TreeTransient;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;
import org.oristool.simulator.*;
import org.oristool.simulator.rewards.*;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;
import org.oristool.simulator.stpn.TransitionAbsoluteFiringTime;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class STPN<R,S> {
    protected int nServers;
    protected ArrayList<Server> servers;
    protected int clients;
    private double timeLimit;
    private double timeStep;

    private double skipProb;

    public STPN(ArrayList<Server> servers, int clients, double timeLimit, double timeStep, double skipProb) {
        this.servers = servers;
        this.nServers = servers.size();
        this.clients = clients;
        this.timeLimit = timeLimit;
        this.timeStep = timeStep;
        this.skipProb = skipProb;
    }

    public HashMap<Integer, Double> makeModel() throws IOException {
        PetriNet net = new PetriNet();
        Marking marking = new Marking();
        //Generating Queue Node
        Place Queue = net.addPlace("Queue");
        marking.setTokens(Queue, clients);
        Place QueueState = net.addPlace("QueueState"); // This is a state that it is used to simulate the reward "if(Queue==0,1,0)"
        marking.setTokens(QueueState, 0);
        Transition lastClientInQueueIsCalled = net.addTransition("LastClientInQueueIsCalled");
        lastClientInQueueIsCalled.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        lastClientInQueueIsCalled.addFeature(new Priority(0));
        net.addPrecondition(Queue, lastClientInQueueIsCalled);
        net.addPostcondition(lastClientInQueueIsCalled, QueueState);
        StringBuilder lastClientInQueueIsCalledEnablingCondition = new StringBuilder("Queue==1 && ( ");
        for (int s = 0; s < nServers; s++) {
            //Generating the name of the places (only the ones we need after)
            String atServiceName = "AtService" + (s + 1);
            String skipName = "Skip" + (s + 1);
            String servedName = "Served" + (s + 1);

            //Generating Nodes
            Place AtService = net.addPlace(atServiceName);
            //Place Served = net.addPlace(servedName);
            Place Skip = net.addPlace(skipName);
            //Place Skipped = net.addPlace("Skipped" + (s + 1));
            Transition Call = net.addTransition("Call" + (s + 1));
            Transition Service = net.addTransition("Service" + (s + 1));
            Transition SkipTransition = net.addTransition("SkipTransition" + (s + 1));
            Transition ToBeServed = net.addTransition("ToBeServed" + (s + 1));

            //Generating Connectors
            //net.addInhibitorArc(AtService, Call);
            net.addPrecondition(Skip, SkipTransition);
            net.addPrecondition(AtService, Service);
            net.addPostcondition(ToBeServed, AtService);
            net.addPostcondition(Call, Skip);
            net.addPrecondition(Queue, Call);
            net.addPrecondition(Skip, ToBeServed);
            //net.addPostcondition(SkipTransition, Skipped);
            //net.addPostcondition(Service, Served);

            //Generating Properties
            marking.setTokens(AtService, 0);
            //marking.setTokens(Served, 0);
            marking.setTokens(Skip, 0);
            //marking.setTokens(Skipped, 0);
            Call.addFeature(new EnablingFunction(atServiceName+"==0 && "+skipName+"==0 && Queue>1"));
            Call.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
            Call.addFeature(new Priority(0));
            // Service.addFeature(new EnablingFunction(servedName+"!=0"));
            Service.addFeature(servers.get(s).getStochasticTransitionFeature(net));
            SkipTransition.addFeature(new EnablingFunction(atServiceName+"==0"));
            SkipTransition.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(skipProb), net)));
            SkipTransition.addFeature(new Priority(0));
            ToBeServed.addFeature(new EnablingFunction(atServiceName+"==0"));
            ToBeServed.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from(String.valueOf(1-skipProb), net)));
            ToBeServed.addFeature(new Priority(0));

            lastClientInQueueIsCalledEnablingCondition.append("(").append(skipName).append("==0 && ").append(atServiceName).append("==0 )");
            if (s < nServers - 1) {
                lastClientInQueueIsCalledEnablingCondition.append(" || ");
            }
        }
        lastClientInQueueIsCalledEnablingCondition.append(" )");
        lastClientInQueueIsCalled.addFeature(new EnablingFunction(lastClientInQueueIsCalledEnablingCondition.toString()));




        File f = new File("log.txt");
        if (!f.exists()){
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        WorkingPrintStreamLogger l = new WorkingPrintStreamLogger(new PrintStream(fos), true);
        Sequencer s = new Sequencer(net, marking, new STPNSimulatorComponentsFactory(), l);

        BigDecimal timeLimit_bigDecimal = new BigDecimal(timeLimit);
        BigDecimal timeStep_bigDecimal = new BigDecimal(timeStep);
        int timePoints = (timeLimit_bigDecimal.divide(timeStep_bigDecimal, RoundingMode.DOWN)).intValue() + 1;
        TransientMarkingConditionProbability r1 =
                new TransientMarkingConditionProbability(s,
                        new ContinuousRewardTime(timeStep_bigDecimal), timePoints,
                        MarkingCondition.fromString("Served1"));
        RewardEvaluator re1 = new RewardEvaluator(r1, 1);

        TransitionAbsoluteFiringTime r2 = new TransitionAbsoluteFiringTime(s, new ContinuousRewardTime(timeStep_bigDecimal), timePoints, "Service1");
        RewardEvaluator re2 = new RewardEvaluator(r2, 1);
        s.simulate();

        // compute the transient solution
            Sequencer seq = new Sequencer(net, marking, new STPNSimulatorComponentsFactory(), NoOpLogger.INSTANCE);
            TransientMarkingConditionProbability rqs = new TransientMarkingConditionProbability(seq,
                    new ContinuousRewardTime(timeStep_bigDecimal), timePoints,
                    MarkingCondition.fromString("QueueState"));
            RewardEvaluator re = new RewardEvaluator(rqs, 10000);
            seq.simulate();
            TimeSeriesRewardResult result = (TimeSeriesRewardResult) re.getResult();
            BigDecimal[] timeSeries = new BigDecimal[timePoints];
            for (Marking marking1 : result.getMarkings()) {
                BigDecimal[] timeSerie = result.getTimeSeries(marking1);
                for (int t = 0; t < timeSerie.length; t++) {
                    timeSeries[t] = timeSeries[t] == null ? timeSerie[t] : timeSeries[t].add(timeSerie[t]);
                }
            }
            // BigDecimal[] timeSerie = result.getTimeSeries(result.getMarkings().iterator().next());
            HashMap<Integer, Double> transientSolution = new HashMap<>();
            for (int t = 0; t < timeSeries.length; t++) {
                transientSolution.put(t, timeSeries[t].doubleValue());
            }

        return transientSolution;
    }

}