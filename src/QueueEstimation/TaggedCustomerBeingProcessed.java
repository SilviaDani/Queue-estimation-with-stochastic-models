package QueueEstimation;

public class TaggedCustomerBeingProcessed extends Event{
    public TaggedCustomerBeingProcessed(double eventTime, String serverID, String clientID) {
        this.eventTime = eventTime;
        this.serverID = serverID;
        this.clientID = clientID;
    }

    @Override
    public String toString() {
        return "TaggedCustomerStartService{" +
                "eventTime=" + eventTime +
                ", serverID='" + serverID + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}
