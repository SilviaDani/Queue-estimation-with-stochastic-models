package QueueEstimation;

public class LeaveQueue extends Event {
    public LeaveQueue(double eventTime, double relativeEventTime, String serverID, String clientID) {
        this.eventTime = eventTime;
        this.relativeEventTime = relativeEventTime;
        this.serverID = serverID;
        this.clientID = clientID; //TODO check if this is necessary
    }

    @Override
    public String toString() {
        return "LeaveQueue{" +
                "eventTime=" + eventTime +
                ", relativeEventTime=" + relativeEventTime + '\'' +
                ", serverID='" + serverID + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}