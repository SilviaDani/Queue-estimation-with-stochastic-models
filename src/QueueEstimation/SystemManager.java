package QueueEstimation;

public class SystemManager{
    private int nServers;
    private int nClients; // Number of clients in queue
    private Server[] servers;
    private STPN stpn;

    //TODO: check if this is necessary -> probably not
    public SystemManager(int nServers, int nClients) {
        this.nServers = nServers;
        this.nClients = nClients;
        servers = new Server[nServers];
        // Create the servers
        for (int i = 0; i < nServers; i++) {
            servers[i] = new Server(ServiceDistribution.EXPONENTIAL, 1);
        }
    }

    public void start() {
            System.out.println("Creating a new system with " + nServers + " servers and " + nClients + " clients in queue.");

        }
}
