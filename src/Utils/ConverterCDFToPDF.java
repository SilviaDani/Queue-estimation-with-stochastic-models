package Utils;

import org.oristool.lello.exception.ValueException;

import java.util.HashMap;

public class ConverterCDFToPDF {

    public static HashMap<Double, Double> convertCDFToPDF(HashMap<Double, Double> cdf) {
        HashMap<Double, Double> pdf = new HashMap<>();
        // sort the keys
        var keys = cdf.keySet().stream().sorted().toArray();
        for (int i = 0; i < keys.length - 1; i++) {
            double x1 = (double) keys[i];
            double y1 = cdf.get(x1);
            double x2 = (double) keys[i + 1];
            double y2 = cdf.get(x2);

            double pdfValue = (y2 - y1) / (x2 - x1);
            double midPoint = (x1 + x2) / 2.0;
            pdf.put(midPoint, pdfValue);
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
