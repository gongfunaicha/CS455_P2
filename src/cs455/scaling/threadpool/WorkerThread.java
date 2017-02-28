package cs455.scaling.threadpool;

import cs455.scaling.serverThread.ServerStatisticsCollector;
import cs455.scaling.task.Task;
import cs455.scaling.util.Attachment;
import cs455.scaling.util.TimeStamp;
import cs455.scaling.util.queue.TaskQueue;
import cs455.scaling.util.queue.WorkerQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

// Implementation of workers
public class WorkerThread extends Thread{
    private Task task = null;
    private WorkerQueue workerQueue = null;
    private TaskQueue taskQueue = null;
    private ServerStatisticsCollector serverStatisticsCollector = null;

    public WorkerThread(WorkerQueue workerQueue, TaskQueue taskQueue, ServerStatisticsCollector serverStatisticsCollector)
    {
        this.workerQueue = workerQueue;
        this.taskQueue = taskQueue;
        this.serverStatisticsCollector = serverStatisticsCollector;
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

            // Get key
            SelectionKey key = task.getKey();

            // Do the task based on task type
            // 'R': Read, 'H': Hash, 'W': Write, 'A': Attempt write
            switch (task.getTask())
            {
                case 'R':
                    this.read(key);
                    break;
                case 'H':
                    this.hash(key);
                    break;
                case 'W':
                    this.write(key);
                    break;
                case 'A':
                    this.rewrite(key);
                    break;
                default:
                    TimeStamp.printWithTimestamp("Received invalid task.");
            }

            // Finished task, set task back to null
            task = null;
        }
    }

    private void read(SelectionKey key)
    {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer dataBuffer = ((Attachment)key.attachment()).getDataBuffer();

        int read = 0;

        try
        {
            while (dataBuffer.hasRemaining() && (read != -1))
            {
                read = socketChannel.read(dataBuffer);
            }
        }
        catch (IOException e)
        {
            TimeStamp.printWithTimestamp("Failed to read data from one socket. Terminating connection.");
            try {
                socketChannel.close();
            } catch (IOException e1) {
                TimeStamp.printWithTimestamp("Failed to close socket channel.");
            }
            serverStatisticsCollector.decrementActiveConnectionCount();
            return;
        }

        if (read == -1)
        {
            TimeStamp.printWithTimestamp("Lost connection to a client. Terminating connection.");
            try {
                socketChannel.close();
            } catch (IOException e1) {
                TimeStamp.printWithTimestamp("Failed to close socket channel.");
            }
            serverStatisticsCollector.decrementActiveConnectionCount();
            return;
        }

        // Flip data buffer and add hash task to task queue
        dataBuffer.flip();
        taskQueue.putTask(new Task('H', key));
    }

    private void hash(SelectionKey key)
    {
        // TODO: Perform hash task
    }

    private void write(SelectionKey key)
    {
        // TODO: Perform write task, change in use back to false
    }

    private void rewrite(SelectionKey key)
    {
        // TODO: Perform rewrite task, remember to change intent back to OP_READ, and change in use back to false
    }

    public synchronized void setTask(Task task)
    {
        this.task = task;
    }
}
