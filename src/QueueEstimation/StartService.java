package QueueEstimation;

public class StartService extends Event{
    public StartService(double eventTime, String serverID, String clientID) {
        this.eventTime = eventTime;
        this.serverID = serverID;
        this.clientID = clientID; //TODO check if this is necessary
    }
    @Override
    public String toString() {
        return "StartService{" +
                "eventTime=" + eventTime +
                ", serverID='" + serverID + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}
