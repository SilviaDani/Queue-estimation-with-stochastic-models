package QueueEstimation;

import Utils.WorkingPrintStreamLogger;
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
import java.util.*;

public class STPN<R,S> {
    protected int nServers;
    protected ArrayList<Server> servers;
    protected int clients;
    protected  double skipProb = 0.1;

    private double timeLimit;
    private double timeStep;

    public STPN(ArrayList<Server> servers, int clients, double timeLimit, double timeStep) {
        this.servers = servers;
        this.nServers = servers.size();
        this.clients = clients;
        this.timeLimit = timeLimit;
        this.timeStep = timeStep;
    }

    public HashMap<Double, Double> makeModel() throws IOException {
        PetriNet net = new PetriNet();
        Marking marking = new Marking();
        //Generating Queue Node
        Place Queue = net.addPlace("Queue");
        marking.setTokens(Queue, clients);
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
            net.addInhibitorArc(AtService, Call);
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
            Call.addFeature(new EnablingFunction(atServiceName+"==0 && "+skipName+"==0"));
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
        }

        File f = new File("log.txt");
        if (!f.exists()){
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        WorkingPrintStreamLogger l = new WorkingPrintStreamLogger(new PrintStream(fos), true);
        Sequencer s = new Sequencer(net, marking, new STPNSimulatorComponentsFactory(), l);

        BigDecimal timeLimit_bigDecimal = new BigDecimal(timeLimit);
        BigDecimal timeStep_bigDecimal = new BigDecimal(timeStep);
        int timePoints = (timeLimit_bigDecimal.divide(timeStep_bigDecimal)).intValue() + 1;
        TransientMarkingConditionProbability r1 =
                new TransientMarkingConditionProbability(s,
                        new ContinuousRewardTime(timeStep_bigDecimal), timePoints,
                        MarkingCondition.fromString("Served1"));
        RewardEvaluator re1 = new RewardEvaluator(r1, 1);

        TransitionAbsoluteFiringTime r2 = new TransitionAbsoluteFiringTime(s, new ContinuousRewardTime(timeStep_bigDecimal), timePoints, "Service1");
        RewardEvaluator re2 = new RewardEvaluator(r2, 1);
        s.simulate();

        // compute the transient solution
        TransientSolution<Marking, Marking> solution = TreeTransient.builder()
                .timeBound(new BigDecimal(timeLimit))
                .timeStep(new BigDecimal(timeStep))
                .build().compute(net, marking);
        TransientSolution<Marking, RewardRate> reward = TransientSolution.computeRewards(false, solution, "If(Queue==0,1,0)");
        HashMap<Double, Double> transientSolution = new HashMap<>();
        for (int t = 0; t < reward.getSolution().length; t++) {
            transientSolution.put(t * timeStep, reward.getSolution()[t][0][0]);
        }
        return transientSolution;
    }

}