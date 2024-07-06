package Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class KLDivergence {

    public static double computeKLDivergence(HashMap<Integer, Double> p1, HashMap<Integer, Double> p2) {
        // check if p1 and p2 have the same size
        /*
        if (p1.size() != p2.size()) {
            throw new IllegalArgumentException("Input distributions have different sizes");
        }*/
        double klDivergence = 0.0;
        // sort the keys
        int[] keys1 = p1.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
        int[] keys2 = p2.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
        // Find common elements
        Set<Integer> commonKeys = new HashSet<>();
        Set<Integer> keysSet1 = new HashSet<>();
        for (int key : keys1) {
            keysSet1.add(key);
        }
        for (int key : keys2) {
            if (keysSet1.contains(key)) {
                commonKeys.add(key);
            }
        }
        for (int key : commonKeys){
            double value1 = Math.max(p1.get(key), 1e-8);
            double value2 = Math.max(p2.get(key), 1e-8);
            klDivergence += Math.max(value1 * Math.log(value1 / value2), 1e-8);
        }
        if (klDivergence < 0.0 || klDivergence > 1.0) {
            System.err.println("KL Divergence: "+klDivergence);
        }else {
            Logger.debug("KL Divergence: " + klDivergence);
        }
        return klDivergence;
    }
}
