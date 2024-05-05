package QueueEstimation;

import Utils.Logger;
import Utils.WorkingPrintStreamLogger;
import org.oristool.analyzer.log.AnalysisLogger;
import org.oristool.analyzer.log.NoOpLogger;
import org.oristool.analyzer.log.PrintStreamLogger;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.TransientSolutionViewer;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;
import org.oristool.simulator.*;
import org.oristool.models.stpn.trans.*;
import org.oristool.simulator.rewards.*;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;
import org.oristool.simulator.stpn.TransientMarkingProbability;
import org.oristool.simulator.stpn.TransitionAbsoluteFiringTime;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class STPN<R,S> {
    protected int servers;
    protected int clients;

    public STPN(int servers, int clients) {
        this.servers = servers;
        this.clients = clients;
    }

    public TransientSolution<R, S> makeModel() throws IOException { //TODO change the distribution of the servers
        PetriNet net = new PetriNet();
        Marking marking = new Marking();
        //Generating Queue Node
        Place Queue = net.addPlace("Queue");
        marking.setTokens(Queue, clients);
        for (int s = 0; s < servers; s++) {
            //Generating the name of the places (only the ones we need after)
            String atServiceName = "AtService" + (s + 1);
            String skipName = "Skip" + (s + 1);
            String servedName = "Served" + (s + 1);
            //Generating Nodes
            Place AtService = net.addPlace(atServiceName);
            Place Served = net.addPlace(servedName);
            Place Skip = net.addPlace(skipName);
            Place Skipped = net.addPlace("Skipped" + (s + 1));
            Transition Call = net.addTransition("Call" + (s + 1));
            Transition FirstService = net.addTransition("FirstService" + (s + 1));
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
            net.addPostcondition(SkipTransition, Skipped);
            net.addPostcondition(Service, Served);
            net.addPrecondition(AtService, FirstService);
            net.addPostcondition(FirstService, Served);
            net.addInhibitorArc(Served, FirstService);

            //Generating Properties
            marking.setTokens(AtService, 1);
            marking.setTokens(Served, 0);
            marking.setTokens(Skip, 0);
            marking.setTokens(Skipped, 0);
            Call.addFeature(new EnablingFunction(atServiceName+"==0 && "+skipName+"==0"));
            Call.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
            Call.addFeature(new Priority(0));
            FirstService.addFeature(new EnablingFunction(servedName+"==0"));
            FirstService.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("1")));
            //Service.addFeature(new EnablingFunction(servedName+"!=0"));
            Service.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("2"), new BigDecimal("3")));
            SkipTransition.addFeature(new EnablingFunction(atServiceName+"==0"));
            SkipTransition.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
            SkipTransition.addFeature(new Priority(0));
            ToBeServed.addFeature(new EnablingFunction(atServiceName+"==0"));
            ToBeServed.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("9", net)));
            ToBeServed.addFeature(new Priority(0));
        }

        File f = new File("log.txt");
        if (!f.exists()){
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        WorkingPrintStreamLogger l = new WorkingPrintStreamLogger(new PrintStream(fos), true);
        Sequencer s = new Sequencer(net, marking, new STPNSimulatorComponentsFactory(), l);

        BigDecimal timeLimit = new BigDecimal(5);
        BigDecimal timeStep = new BigDecimal("0.1");
        int timePoints = (timeLimit.divide(timeStep)).intValue() + 1;
        //TODO from here
        TransientMarkingConditionProbability r1 =
                new TransientMarkingConditionProbability(s,
                        new ContinuousRewardTime(timeStep), timePoints,
                        MarkingCondition.fromString("Served1"));
        RewardEvaluator re1 = new RewardEvaluator(r1, 1);

        TransitionAbsoluteFiringTime r2 = new TransitionAbsoluteFiringTime(s, new ContinuousRewardTime(timeStep), timePoints, "Service1");
        RewardEvaluator re2 = new RewardEvaluator(r2, 1);
        //TODO to here is it necessary?
        s.simulate();

        return null;
    }

}