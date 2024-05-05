package QueueEstimation;

public class EndService extends Event{
    public EndService(double eventTime, String serverID, String clientID) {
        this.eventTime = eventTime;
        this.serverID = serverID;
        this.clientID = clientID; //TODO check if this is necessary
    }
    @Override
    public String toString() {
        return "EndService{" +
                "eventTime=" + eventTime +
                ", serverID='" + serverID + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}
