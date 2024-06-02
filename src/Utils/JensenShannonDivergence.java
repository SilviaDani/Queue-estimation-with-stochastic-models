package Utils;

import java.util.HashMap;

public class JensenShannonDivergence {
    public static double computeJensenShannonDivergence(HashMap<Double, Double> p1, HashMap<Double, Double> p2) {
        // check if p1 and p2 have the same size
        if (p1.size() != p2.size()) {
            throw new IllegalArgumentException("Input distributions have different sizes");
        }
        HashMap<Double, Double> m = new HashMap<>();
        // sort the keys
        var keys = p1.keySet().stream().sorted().toArray();
        for (var key : keys) {
            m.put((Double) key, (p1.get(key) + p2.get(key)) / 2);
        }
        double klPM = KLDivergence.computeKLDivergence(p1, m);
        double klQM = KLDivergence.computeKLDivergence(p2, m);
        return (klPM + klQM) / 2;
    }
}
