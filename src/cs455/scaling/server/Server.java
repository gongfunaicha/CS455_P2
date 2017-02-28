package cs455.scaling.server;

import cs455.scaling.serverThread.ServerStatisticsCollector;
import cs455.scaling.task.Task;
import cs455.scaling.threadpool.ThreadpoolManager;
import cs455.scaling.util.Attachment;
import cs455.scaling.util.TimeStamp;
import cs455.scaling.util.queue.TaskQueue;
import cs455.scaling.util.queue.WorkerQueue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {

    private int port = 0;
    private int threadPoolSize = 0;
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;
    private ServerStatisticsCollector serverStatisticsCollector = null;
    private TaskQueue taskQueue = null;
    private WorkerQueue workerQueue = null;
    private ThreadpoolManager threadpoolManager = null;

    public Server(String[] args)
    {
        // Check arguments, exit on error
        checkArguments(args);
    }

    public static void main(String[] args)
    {
        Server server =  new Server(args);
        server.startSelector();
        server.bind();
        server.startThreads();
        server.select();
    }

    private void select()
    {
        // Do main select
        while (true)
        {
            try {
                selector.select();
            } catch (IOException e) {
                TimeStamp.printWithTimestamp("Failed to select. Program will now exit.");
                System.exit(1);
            }

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext())
            {
                SelectionKey key = keys.next();

                if (key.isAcceptable())
                {
                    this.accept(key);
                }
                else if (key.isReadable())
                {
                    this.read(key);
                }
                else if (key.isWritable())
                {
                    this.write(key);
                }

                keys.remove();
            }

        }
    }

    private void accept(SelectionKey key)
    {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = null;
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ, new Attachment());
        } catch (IOException e) {
            TimeStamp.printWithTimestamp("Failed to accept one incoming connection.");
            return;
        }
        serverStatisticsCollector.incrementActiveConnectionCount();
    }

    private void read(SelectionKey key)
    {
        Attachment attachment = (Attachment)key.attachment();
        if (attachment.getAndUpdateInUse())
        {
            // Currently in use, skip
            return;
        }
        // Currently not in use, add to task queue
        taskQueue.putTask(new Task('R', key));
    }

    private void write(SelectionKey key)
    {
        Attachment attachment = (Attachment)key.attachment();
        if (attachment.getAndUpdateAlreadyRewrite())
        {
            // Currently already rewrite, skip
            return;
        }
        // Attempt rewrite event
        taskQueue.putTask(new Task('A', key));
    }

    private void startSelector()
    {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            TimeStamp.printWithTimestamp("Failed to open selector. Program will now exit.");
            System.exit(1);
        }
    }

    private void bind()
    {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            TimeStamp.printWithTimestamp("Server started, listening on " + InetAddress.getLocalHost().getHostAddress() + ":" + String.valueOf(port));
        } catch (IOException e) {
            TimeStamp.printWithTimestamp("Failed to bind server to port.");
            System.exit(1);
        }
    }

    private void startThreads()
    {
        // Initialize task queue and worker queue
        taskQueue = new TaskQueue();
        workerQueue = new WorkerQueue();

        // Start statistics collector
        serverStatisticsCollector = new ServerStatisticsCollector();
        serverStatisticsCollector.start();

        // Start threadpool manager
        threadpoolManager = new ThreadpoolManager(threadPoolSize, taskQueue, workerQueue, serverStatisticsCollector);
        threadpoolManager.start();
    }

    private void checkArguments(String[] args)
    {
        if (args.length == 2)
        {
            try
            {
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                System.out.println("Error: Specified port number must be an integer. Program will now exit.");
                System.exit(1);
            }
            if (port < 0 || port > 65535)
            {
                System.out.println("Error: Specified port number must be between 0 and 65535. Program will now exit.");
                System.exit(1);
            }

            try
            {
                threadPoolSize = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                System.out.println("Error: Specified thread pool size must be an integer. Program will now exit.");
                System.exit(1);
            }

            if (threadPoolSize <= 0)
            {
                System.out.println("Error: Specified thread pool size must be greater than zero. Program will now exit.");
                System.exit(1);
            }

        }
        else
        {
            System.out.println("Invalid number of arguments");
            System.out.println("Usage: java cs455.scaling.server.Server portnum thread-pool-size");
            System.exit(1);
        }
    }
}
