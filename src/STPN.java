import javafx.scene.chart.XYChart;
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
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class STPN<R,S> {
    protected int servers;
    protected int clients;
    protected LocalDateTime now;

    public STPN(int servers, int clients) {
        this.servers = servers;
        this.clients = clients;
        now = LocalDateTime.now();
    }

    public TransientSolution<R, S> makeModel(String fiscalCode, ArrayList<HashMap<String, Object>> arrayList) throws Exception {
        LocalDateTime now = LocalDateTime.now().minusDays(6);
        if (clients >= 0) {
            PetriNet net = new PetriNet();
            Marking marking = new Marking();
            //Generating Nodes
            Place Queue = net.addPlace("Queue");
            marking.setTokens(Queue, clients);
            for(int s=0; s<servers; s++){
                //Generating Nodes
                Place AtService = net.addPlace("AtService"+(s+1));
                Place Skip = net.addPlace("Skip"+(s+1));
                Transition Call = net.addTransition("Call"+(s+1));
                Transition Service = net.addTransition("Service"+(s+1));
                Transition SkipTransition = net.addTransition("SkipTransition"+(s+1));
                Transition ToBeServed = net.addTransition("ToBeServed"+(s+1));

                //Generating Connectors
                net.addPrecondition(Queue, Call);
                net.addPostcondition(Call, Skip);
                net.addPrecondition(Skip, SkipTransition);
                net.addPrecondition(Skip, ToBeServed);
                net.addPostcondition(ToBeServed, AtService);
                net.addPrecondition(AtService, Service);
                net.addInhibitorArc(AtService, Call);

                //Generating Properties
                //TODO adattare per i clients già dentro gli sportelli quando arriva il tagged costumer
                marking.setTokens(AtService, 0);
                marking.setTokens(Skip, 0);

                //TODO sistemare le varie transizioni (distribuzione di Service e enabling function call)
                Call.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
                Call.addFeature(new Priority(0));
                Service.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("1")));
                SkipTransition.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
                SkipTransition.addFeature(new Priority(0));
                ToBeServed.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
                ToBeServed.addFeature(new Priority(0));
            }

            //TODO estrarre i dati temporali corretti

            // 144 -> 6 giorni
            RegTransient analysis = RegTransient.builder()
                    .greedyPolicy(new BigDecimal(samples), new BigDecimal("0.0000001"))
                    .timeStep(new BigDecimal(step)).build();

            //If(Contagioso>0&&Sintomatico==0,1,0);Contagioso;Sintomatico;If(Guarito+Isolato>0,1,0)
            var rewardRates = TransientSolution.rewardRates("Contagioso");

            TransientSolution<DeterministicEnablingState, Marking> solution =
                    analysis.compute(net, marking);

            var rewardedSolution = TransientSolution.computeRewards(false, solution, rewardRates);
            return (TransientSolution<R, S>) rewardedSolution;
        } else {
            System.out.println("There are no clients in the queue before the tagged costumer");
            return makeFakeNet();
        }
    }
}