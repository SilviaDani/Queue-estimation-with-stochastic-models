package Utils;

import java.util.HashMap;

public class ConditionedPDF {
    public static HashMap<Integer, Double> computeConditionedPDF(HashMap<Integer, Double> cdf, int t0) {
        HashMap<Integer, Double> pdf = ConverterCDFToPDF.convertCDFToPDF(cdf);
        // Compute f(t | t > t0)
        HashMap<Integer, Double> conditionedPDF = new HashMap<>();
        int[] keys = pdf.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
        for (int t : keys) {
            double value;
            if (t < t0){
                value = 0.0;
            } else {
                value = pdf.get(t) / (1 - cdf.get(t0));
            }
            if (value < 0.0){
                value = Math.max(value, 1e-8);
            }
            conditionedPDF.put(t, value);
        }

        // Check if the pdf is normalized
        double sum = 0.0;
        for (var value : conditionedPDF.values()) {
            sum += value;
        }
        if (Math.abs(sum - 1.0) > 1e-6){
            System.err.println("The PDF is not normalized");
            for (var key : conditionedPDF.keySet()) {
                conditionedPDF.put(key, conditionedPDF.get(key) / sum);
            }
        }
        return conditionedPDF;
    }
}
