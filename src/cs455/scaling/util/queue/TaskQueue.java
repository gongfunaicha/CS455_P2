package cs455.scaling.util.queue;

import cs455.scaling.task.Task;
import cs455.scaling.util.TimeStamp;

import java.util.LinkedList;

public class TaskQueue {
    private LinkedList<Task> taskQueue = null;

    public TaskQueue()
    {
        taskQueue = new LinkedList<>();
    }

    public synchronized Task getTask()
    {
        // Use while to check whether task queue is empty after wakeup
        while (taskQueue.isEmpty())
        {
            // wait for task to be put in
            try {
                this.wait();
            } catch (InterruptedException e) {
                TimeStamp.printWithTimestamp("Interrupted when trying to wait for new task");
            }
        }
        return taskQueue.pollFirst();
    }

    public synchronized void putTask(Task task)
    {
        taskQueue.addLast(task);
        // Notify in case someone is waiting to get task
        this.notify();
    }

}
