package QueueEstimation;
//make abstract class
public abstract class Event {
    protected double eventTime;
    protected String serverID;
    protected String clientID; //TODO check if this is necessary
    @Override
    public abstract String toString();
}
