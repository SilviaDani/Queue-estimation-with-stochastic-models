package Utils;

import QueueEstimation.Approximation.ModelApproximation;
import QueueEstimation.Approximation.ModelApproximator;
import QueueEstimation.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ApproxParser{

    private static HashMap<String, String[]> transitionPrefices = null;

    public ApproxParser(){
        if (transitionPrefices == null){
            transitionPrefices = new HashMap<>();
            transitionPrefices.put("EXP", new String[]{"Service"});
            transitionPrefices.put("HYPOEXP", new String[]{"ServiceERL", "ServiceEXP"});
            transitionPrefices.put("HYPEREXP", new String[]{"Service0", "Service1"});
        }
    }

    private static boolean isTransitionLine(String line, String modelType){
        for(String prefix : transitionPrefices.get(modelType)){
            if(line.startsWith(prefix)){
                return true;
            }
        }
        return false;
    }

    public static double getApproximatedETA (String filename, ModelApproximator approximator){
        double currentTime = 0.0;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            boolean isParentSection = false;
            HashMap<String, String> currentETAs = new HashMap<>();
            while(line != null){
                if(line.trim().equals("-- Parent --")) {
                    isParentSection = true;
                } else if (isParentSection && isTransitionLine(line.trim(), approximator.getModelType())){
                    currentETAs.put(line.trim().split(" ")[0], line.trim().split(" ")[2]);
                } else if(line.trim().equals("-- Event --")){
                    isParentSection = false;
                    line = reader.readLine();
                    currentTime += Double.parseDouble(currentETAs.get(line.trim()));
                    currentETAs.clear();
                }
                line = reader.readLine();
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return currentTime;
    }
}
