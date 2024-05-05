package QueueEstimation;

import Utils.Parser;

public class Main {
    public static void main(String[] args) {
        STPN stpn = new STPN(4, 50);
        try {
            stpn.makeModel();
        }catch (Exception e){
            System.out.println("Error creating the model");
        }
        Parser.parse("log.txt");
    }
}
