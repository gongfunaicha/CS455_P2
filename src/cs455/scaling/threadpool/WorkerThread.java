package cs455.scaling.threadpool;

import cs455.scaling.serverThread.ServerStatisticsCollector;
import cs455.scaling.task.Task;
import cs455.scaling.util.Attachment;
import cs455.scaling.util.DigestUtil;
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

        TimeStamp.printWithTimestamp("Finished read task.");
    }

    private void hash(SelectionKey key)
    {
        Attachment attachment = (Attachment)key.attachment();
        ByteBuffer dataBuffer = attachment.getDataBuffer();
        ByteBuffer digestBuffer = attachment.getDigestBuffer();

        // Get data from data buffer and clear the buffer
        byte[] data = new byte[dataBuffer.remaining()];
        dataBuffer.get(data);
        dataBuffer.clear();

        // Do SHA1
        String digest = DigestUtil.SHA1FromBytes(data);

        // Put bytes into digest buffer
        digestBuffer.put(digest.getBytes());
        digestBuffer.flip();
        taskQueue.putTask(new Task('W', key));

        TimeStamp.printWithTimestamp("Finished hash task.");
    }

    private void write(SelectionKey key)
    {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        ByteBuffer digestBuffer = ((Attachment)key.attachment()).getDigestBuffer();

        try {
            socketChannel.write(digestBuffer);
        } catch (IOException e) {
            TimeStamp.printWithTimestamp("Failed to write to channel. Terminating connection.");
            try {
                socketChannel.close();
            } catch (IOException e1) {
                TimeStamp.printWithTimestamp("Failed to close connection.");
            }
            serverStatisticsCollector.decrementActiveConnectionCount();
            return;
        }

        if (digestBuffer.hasRemaining())
        {
            // Not fully written, compact and acquire OP_WRITE
            digestBuffer.compact();
            key.interestOps(SelectionKey.OP_WRITE);
        }
        else
        {
            // Fully written, task completed, set not in use
            digestBuffer.clear();
            serverStatisticsCollector.incrementThroughputCount();
            ((Attachment)key.attachment()).setNotInUse();
        }
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
