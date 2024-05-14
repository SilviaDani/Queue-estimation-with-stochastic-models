package QueueEstimation.Approximation;

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
}
