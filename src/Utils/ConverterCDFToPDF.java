package Utils;

import org.oristool.lello.exception.ValueException;

import java.util.HashMap;

public class ConverterCDFToPDF {

    public static HashMap<Integer, Double> convertCDFToPDF(HashMap<Integer, Double> cdf) {
        HashMap<Integer, Double> pdf = new HashMap<>();
        // sort the keys
        int[] keys = cdf.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
        for (int i = 0; i < keys.length - 1; i++) {
            int x1 = keys[i];
            double y1 = cdf.get(x1);
            int x2 = keys[i + 1];
            double y2 = cdf.get(x2);

            double pdfValue = Math.max((y2 - y1), 1e-8) / ((double)(x2 - x1)); //we use max because y2-y1 is always non-negative (CDF is a non-decreasing function)
            pdf.put(x1, pdfValue);
        }

        // Check if the pdf is normalized
        double sum = 0.0;
        for (var value : pdf.values()) {
            sum += value;
        }
        if (Math.abs(sum - 1.0) > 1e-6){
            System.err.println("The PDF is not normalized");
            for (var key : pdf.keySet()) {
                pdf.put(key, pdf.get(key) / sum);
            }
        }
        return pdf;
    }
}
