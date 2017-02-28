package cs455.scaling.serverThread;

import cs455.scaling.util.TimeStamp;

public class ServerStatisticsCollector extends Thread{
    private int activeConnection = 0;
    private int throughput = 0;
    private final Object activeConnectionLock;
    private final Object throughputLock;

    public ServerStatisticsCollector()
    {
        activeConnection = 0;
        throughput = 0;
        activeConnectionLock = new Object();
        throughputLock = new Object();
    }

    @Override
    public void run() {
        while (true)
        {
            // Use cache to store values
            int cacheConnection = 0;
            double cacheThroughput = 0;

            synchronized (activeConnectionLock)
            {
                cacheConnection = activeConnection;
                activeConnection = 0;
            }

            synchronized (throughputLock)
            {
                cacheThroughput += throughput;
                throughput = 0;
            }

            // Print out active connection and throughput
            TimeStamp.printWithTimestamp("Current Server Throughput: " + String.valueOf(cacheThroughput/5) + " messages/s, Active Client Connections: " + String.valueOf(cacheConnection));

            // Wait for 5 seconds
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                TimeStamp.printWithTimestamp("Interrupted while waiting to print next throughput message.");
            }
        }
    }

    public void incrementActiveConnectionCount()
    {
        synchronized (activeConnectionLock)
        {
            activeConnection++;
        }
    }

    public void decrementActiveConnectionCount()
    {
        synchronized (activeConnectionLock)
        {
            activeConnection--;
        }
    }

    public void incrementThroughputCount()
    {
        synchronized (throughputLock)
        {
            throughput++;
        }
    }
}
