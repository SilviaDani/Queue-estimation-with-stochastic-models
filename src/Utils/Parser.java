package Utils;

import QueueEstimation.EndService;
import QueueEstimation.Event;
import QueueEstimation.LeaveQueue;
import QueueEstimation.StartService;

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
            "Service",
            "LastClientInQueueIsCalled"
    };

    protected static boolean isTransitionLine(String line){
        for(String prefix : transitionPrefices){
            if(line.startsWith(prefix)){
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Event> parse(String filename, int nServers){
        ArrayList<Event> events = new ArrayList<>();
        HashMap<String, Integer> clientCounter = new HashMap<>();
        HashMap<String, Double> entranceTimes = new HashMap<>();
        for (int i = 0; i < nServers; i++){
            entranceTimes.put(String.valueOf(i+1), 0.0);
        }
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            double currentTime = 0.0;
            HashMap<String, String> currentETAs = new HashMap<>();
            boolean isParentSection = false;
            int firstClients = - nServers;
            int clients = 0;
            while (line != null){
                if(line.trim().equals("-- Parent --")){
                    isParentSection = true;
                }else if(isParentSection && isTransitionLine(line.trim())){
                    currentETAs.put(line.trim().split(" ")[0], line.trim().split(" ")[2]);
                } else if(line.trim().equals("-- Event --")){
                    isParentSection = false;
                    line = reader.readLine(); // the transition actually fired
                    currentTime += Double.parseDouble(currentETAs.get(line.trim()));
                    Logger.debug("Transition fired: " + line.trim() + " @ " + currentTime +"/" + Double.parseDouble(currentETAs.get(line.trim())));
                    //check if the transition is a SkipTransition transition or a [First]Service transition and creates the corresponding event
                    String serverID = line.trim().replaceAll("[^0-9]", "");
                    if(line.trim().startsWith("Call")) {
                        clientCounter.put(serverID, clients);
                        clients++;
                    }else if(line.trim().startsWith("ToBeServed")){
                        events.add(new StartService(currentTime,currentTime - entranceTimes.get(serverID), serverID, String.valueOf(clientCounter.get(serverID))));
                    }else if(line.trim().startsWith("SkipTransition")){
                        events.add(new LeaveQueue(currentTime, currentTime - entranceTimes.get(serverID), serverID, String.valueOf(clientCounter.get(serverID))));
                    }else if(line.trim().startsWith("Service")){
                        events.add(new EndService(currentTime, currentTime - entranceTimes.get(serverID), serverID, String.valueOf(clientCounter.get(serverID))));
                        entranceTimes.put(serverID, currentTime);
                    }else if(line.trim().startsWith("FirstService")){
                        events.add(new EndService(currentTime,currentTime - entranceTimes.get(serverID), serverID, String.valueOf(firstClients)));
                        entranceTimes.put(serverID, currentTime);
                        firstClients++;
                    }else if(line.trim().startsWith("LastClientInQueueIsCalled")){
                        events.add(new StartService(currentTime, -1, String.valueOf(-1), String.valueOf(clients)));
                        clients++;
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
        return events;
    }
}
