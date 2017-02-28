package cs455.scaling.util.queue;

import cs455.scaling.threadpool.WorkerThread;
import cs455.scaling.util.TimeStamp;

import java.util.LinkedList;

public class WorkerQueue {
    private LinkedList<WorkerThread> workerQueue = null;

    public WorkerQueue()
    {
        workerQueue = new LinkedList<>();
    }

    public synchronized WorkerThread getWorker()
    {
        while (workerQueue.isEmpty())
        {
            try {
                this.wait();
            } catch (InterruptedException e) {
                TimeStamp.printWithTimestamp("Interrupted when trying to wait for new worker");
            }
        }
        return workerQueue.pollFirst();
    }

    public synchronized void addWorker(WorkerThread workerThread)
    {
        workerQueue.addLast(workerThread);
        this.notify();
    }
}
