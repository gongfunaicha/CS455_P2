package cs455.scaling.clientThread;

import cs455.scaling.util.TimeStamp;

import java.sql.Time;

public class ClientStatisticCollector extends Thread{

    private int sendCount = 0;
    private int receiveCount = 0;

    public ClientStatisticCollector()
    {
        this.sendCount = 0;
        this.receiveCount = 0;
    }

    @Override
    public void run() {
        while (true)
        {
            // Collect statistics every ten seconds
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                TimeStamp.printWithTimestamp("Interrupted when trying to wait for ten seconds.");
            }

            synchronized (this)
            {
                TimeStamp.printWithTimestamp("Total Sent Count: " + String.valueOf(sendCount) + ", Total Received Count: " + String.valueOf(receiveCount));

                // Reset sendCount and receiveCount
                sendCount = 0;
                receiveCount = 0;
            }
        }
    }

    public synchronized void incrementSendCount()
    {
        sendCount++;
    }
    public synchronized void incrementReceiveCount()
    {
        receiveCount++;
    }
}
