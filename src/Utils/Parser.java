package Utils;

import QueueEstimation.EndService;
import QueueEstimation.Event;
import QueueEstimation.LeaveQueue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {
    private static String [] transitionPrefices = {
            "Call",
            "ToBeServed",
            "SkipTransition",
            "FirstService",
            "Service"
    };

    private static boolean isTransitionLine(String line){
        for(String prefix : transitionPrefices){
            if(line.startsWith(prefix)){
                return true;
            }
        }
        return false;
    }

    public static void parse(String filename, int nServers){ //TODO add a return type
        ArrayList<Event> events = new ArrayList<>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            double currentTime = 0.0;
            HashMap<String, String> currentETAs = new HashMap<>();
            boolean isParentSection = false;
            while (line != null){
                if(line.trim().equals("-- Parent --")){
                    isParentSection = true;
                }else if(isParentSection && isTransitionLine(line.trim())){
                    currentETAs.put(line.trim().split(" ")[0], line.trim().split(" ")[2]);
                } else if(line.trim().equals("-- Event --")){
                    isParentSection = false;
                    line = reader.readLine(); // the transition actually fired
                    currentTime += Double.parseDouble(currentETAs.get(line.trim()));
                    Logger.debug("Transition fired: " + line.trim() + " @ " + currentTime +"/" +currentETAs.get(line.trim()));
                    //check if the transition is a SkipTransition transition or a [First]Service transition and creates the corresponding event
                    String serverID = line.trim().replaceAll("[^0-9]", "");
                    int firstClients = - nServers;
                    int clients = 0;
                    if(line.trim().startsWith("ToBeServed")){
                        clients++;
                    }else if(line.trim().startsWith("SkipTransition")){
                        events.add(new LeaveQueue(currentTime, serverID, String.valueOf(clients)));
                    }else if(line.trim().startsWith("Service")){
                        events.add(new EndService(currentTime, serverID, String.valueOf(clients)));
                    }else if(line.trim().startsWith("FirstService")){
                        events.add(new EndService(currentTime, serverID, String.valueOf(firstClients)));
                        firstClients++;
                    }
                    currentETAs.clear();
                }
                line = reader.readLine();
            }
            reader.close();
        }catch (IOException e){
            System.out.println("Error parsing the file");
            e.printStackTrace();
        }
        Logger.debug("\n----\n");
        for(Event e : events){
            Logger.debug(e.toString());
        }
    }
}
