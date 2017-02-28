package cs455.scaling.threadpool;

import cs455.scaling.task.Task;
import cs455.scaling.util.queue.TaskQueue;
import cs455.scaling.util.queue.WorkerQueue;

public class ThreadpoolManager extends Thread{
    private int numThread = 0;
    private TaskQueue taskQueue = null;
    private WorkerQueue workerQueue = null;

    public ThreadpoolManager(int numThread, TaskQueue taskQueue, WorkerQueue workerQueue)
    {
        this.numThread = numThread;
        this.taskQueue = taskQueue;
        this.workerQueue = workerQueue;
    }

    @Override
    public void run() {
        // Start all the worker threads
        for (int i = 0; i < numThread; i++)
        {
            WorkerThread workerThread = new WorkerThread(workerQueue);
            // Worker threads will add themselves to worker queue
            workerThread.start();
        }

        // Constantly assign worker to thread
        while (true)
        {
            // First retrieve a worker, block if no available
            WorkerThread workerThread = workerQueue.getWorker();

            // Next retrieve a task, block if no available
            Task task = taskQueue.getTask();

            synchronized (workerThread)
            {
                // Assign task to worker thread
                workerThread.setTask(task);

                // Notify worker to wakeup
                workerThread.notify();
            }
        }
    }
}
