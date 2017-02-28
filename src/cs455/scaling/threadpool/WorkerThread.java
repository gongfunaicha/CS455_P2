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
            // Do the task based on task type
            // 'R': Read, 'H': Hash, 'W': Write
            switch (task.getTask())
            {
                case 'R':
                    this.read();
                    break;
                case 'H':
                    this.hash();
                    break;
                case 'W':
                    this.write();
                    break;
                default:
                    TimeStamp.printWithTimestamp("Received invalid task.");
            }

            // Finished task, set task back to null
            task = null;
        }
    }

    private void read()
    {
        // TODO: Perform read task
    }

    private void hash()
    {
        // TODO: Perform hash task
    }

    private void write()
    {
        // TODO: Perform write task, remember to change intent back to OP_READ, and change in use back to false
    }

    public synchronized void setTask(Task task)
    {
        this.task = task;
    }
}
