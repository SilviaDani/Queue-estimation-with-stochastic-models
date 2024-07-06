package Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class JensenShannonDivergence {
    public static double computeJensenShannonDivergence(HashMap<Integer, Double> p1, HashMap<Integer, Double> p2) {
        // check if p1 and p2 have the same size
        /*
        if (p1.size() != p2.size()) {
            throw new IllegalArgumentException("Input distributions have different sizes");
        }

         */
        HashMap<Integer, Double> m = new HashMap<>(); //Midpoint distribution between the two distributions p1 and p2
        // sort the keys
        //var keys = p1.keySet().stream().sorted().toArray();
        int[] keys1 = p1.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
        // check if p1.keySet().stream.sorted().toArray() is the same as p2.keySet().stream().sorted().toArray() (the elements are the same)
        //var keys2 = p2.keySet().stream().sorted().toArray();
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


        /*
        for (int i = 0; i < keys.length; i++) {
            if (Math.abs((Double)keys[i] - (Double)keys2[i]) > 1e-6) {
                Logger.debug("Key 1: " + keys[i] + " Key 2: " + keys2[i]);
                throw new IllegalArgumentException("Input distributions have different keys");
            }
        }
         */
        // M = (P + Q) / 2
        if (commonKeys.isEmpty()){
            Logger.debug("In JSD computeJSDivergence: Input distributions have different keys");
            throw new IllegalArgumentException("Input distributions have different keys");
        }

        for (int key : commonKeys) {
            m.put(key, (p1.get(key) + p2.get(key)) / 2.0);
        }

        double klPM = KLDivergence.computeKLDivergence(p1, m);
        double klQM = KLDivergence.computeKLDivergence(p2, m);
        return (klPM + klQM) / 2.0;
    }
}
