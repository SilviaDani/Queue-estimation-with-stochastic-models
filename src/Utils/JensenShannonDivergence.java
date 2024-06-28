package Utils;

import java.util.HashMap;

public class JensenShannonDivergence {
    public static double computeJensenShannonDivergence(HashMap<Double, Double> p1, HashMap<Double, Double> p2) {
        // check if p1 and p2 have the same size
        if (p1.size() != p2.size()) {
            throw new IllegalArgumentException("Input distributions have different sizes");
        }
        HashMap<Double, Double> m = new HashMap<>(); //Midpoint distribution between the two distributions p1 and p2
        // sort the keys
        var keys = p1.keySet().stream().sorted().toArray();
        // check if p1.keySet().stream.sorted().toArray() is the same as p2.keySet().stream().sorted().toArray() (the elements are the same)
        var keys2 = p2.keySet().stream().sorted().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (Math.abs((Double)keys[i] - (Double)keys2[i]) > 1e-6) {
                Logger.debug("Key 1: " + keys[i] + " Key 2: " + keys2[i]);
                throw new IllegalArgumentException("Input distributions have different keys");
            }
        }
        // M = (P + Q) / 2
        for (var key : keys) {
            m.put((Double) key, (p1.get(key) + p2.get(key)) / 2.0);
        }
        double klPM = KLDivergence.computeKLDivergence(p1, m);
        double klQM = KLDivergence.computeKLDivergence(p2, m);
        return (klPM + klQM) / 2.0;
    }
}
