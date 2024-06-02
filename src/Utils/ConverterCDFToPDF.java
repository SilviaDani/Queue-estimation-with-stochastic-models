package Utils;

import java.util.HashMap;

public class ConverterCDFToPDF {

    public static HashMap<Double, Double> convertCDFToPDF(HashMap<Double, Double> cdf) {
        HashMap<Double, Double> pdf = new HashMap<>();
        // sort the keys
        var keys = cdf.keySet().stream().sorted().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (i == 0) {
                pdf.put((double) keys[i], cdf.get(keys[i]));
            } else {
                pdf.put((double) keys[i], Math.abs(cdf.get(keys[i]) - cdf.get(keys[i - 1])) / ((double) keys[i] - (double) keys[i - 1]));
            }
        }
        return pdf;
    }
}
