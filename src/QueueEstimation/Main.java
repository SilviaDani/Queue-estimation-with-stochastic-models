package QueueEstimation;

import Utils.Parser;

public class Main {
    public static void main(String[] args) {
        int numServers = 4;
        STPN stpn = new STPN(numServers, 10);
        try {
            stpn.makeModel();
        }catch (Exception e){
            System.out.println("Error creating the model");
        }
        Parser.parse("log.txt", numServers);
    }
}
