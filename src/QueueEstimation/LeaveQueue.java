package QueueEstimation;

public class LeaveQueue extends Event {
    public LeaveQueue(double eventTime, String serverID, String clientID) {
        this.eventTime = eventTime;
        this.serverID = serverID;
        this.clientID = clientID; //TODO check if this is necessary
    }

    @Override
    public String toString() {
        return "LeaveQueue{" +
                "eventTime=" + eventTime +
                ", serverID='" + serverID + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}