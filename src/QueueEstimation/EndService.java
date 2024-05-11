package QueueEstimation;

public class EndService extends Event{
    public EndService(double eventTime, double relativeEventTime, String serverID, String clientID) {
        this.eventTime = eventTime;
        this.relativeEventTime = relativeEventTime;
        this.serverID = serverID;
        this.clientID = clientID; //TODO check if this is necessary
    }
    @Override
    public String toString() {
        return "EndService{" +
                "eventTime=" + eventTime +
                ", relativeEventTime=" + relativeEventTime + '\'' +
                ", serverID='" + serverID + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}
