package cs455.scaling.threadpool;

import cs455.scaling.task.Task;
import cs455.scaling.util.TimeStamp;
import cs455.scaling.util.queue.WorkerQueue;

// Implementation of workers
public class WorkerThread extends Thread{
    private Task task = null;
    private WorkerQueue workerQueue = null;

    public WorkerThread(WorkerQueue workerQueue)
    {
        this.workerQueue = workerQueue;
    }

    @Override
    public void run() {
        // Avoid race condition of wait
        synchronized (this)
        {
            // Add self back to workerQueue
            workerQueue.addWorker(this);

            while (task == null)
            {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    TimeStamp.printWithTimestamp("Interrupted when waiting for new task.");
                }
            }
            // TODO: Wake up, do the task based on task type

            // Finished task, set task back to null
            task = null;
        }
    }

    public synchronized void setTask(Task task)
    {
        this.task = task;
    }
}
