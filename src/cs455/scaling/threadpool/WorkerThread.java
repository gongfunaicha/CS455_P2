package cs455.scaling.threadpool;

import cs455.scaling.task.Task;

// Implementation of workers
public class WorkerThread extends Thread{
    private Task task = null;

    public WorkerThread()
    {
        // TODO: Add itself back to queue
    }

    @Override
    public void run() {
        // Remember to test whether task is null (it shouldn't be)
        // TODO: Do the task and after finishing, add itself back to queue
    }

    public void setTask(Task task)
    {
        this.task = task;
    }
}
