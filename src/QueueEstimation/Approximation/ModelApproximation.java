package QueueEstimation.Approximation;

import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import java.util.HashMap;

public interface ModelApproximation {
    void approximateModel();

    String getModelType();

    HashMap<Integer, Double> analyzeModel();

}
