package QueueEstimation.Approximation;

import java.util.HashMap;

public class ModelApproximator {
    private ModelApproximation modelApproximation;

    public void setModelApproximation(ModelApproximation modelApproximation) {
        this.modelApproximation = modelApproximation;
    }

    public void approximateModel() {
        modelApproximation.approximateModel();
    }

    public String getModelType(){
        return modelApproximation.getModelType();
    }

    public HashMap<Integer, Double> analyzeModel(){
        return modelApproximation.analyzeModel();
    }
}
