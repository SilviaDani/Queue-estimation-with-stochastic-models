package QueueEstimation;
//make abstract class
public abstract class Event {
    protected double eventTime;
    protected  double relativeEventTime; // time spent in service
    protected String serverID;
    protected String clientID; //TODO check if this is necessary
    @Override
    public abstract String toString();
}
