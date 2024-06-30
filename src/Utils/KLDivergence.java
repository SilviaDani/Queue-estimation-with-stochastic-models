package Utils;

import java.util.HashMap;

public class KLDivergence {

    public static double computeKLDivergence(HashMap<Double, Double> p1, HashMap<Double, Double> p2) {
        // check if p1 and p2 have the same size
        if (p1.size() != p2.size()) {
            throw new IllegalArgumentException("Input distributions have different sizes");
        }
        double klDivergence = 0.0;
        // sort the keys
        var keys = p1.keySet().stream().sorted().toArray();
        for (var key : keys) {
            if (p1.get((Double)key) > 0 && p2.get((Double)key) > 0){
                klDivergence += p1.get((Double)key) * Math.log(p1.get((Double)key) / (p2.get((Double)key) + 1e-8));
            }
        }
        Logger.debug("KL Divergence: " + klDivergence);
        return klDivergence / Math.log(2.0);
    }
}
