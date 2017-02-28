package cs455.scaling.server;

import cs455.scaling.util.TimeStamp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Server {

    private int port = 0;
    private int threadPoolSize = 0;
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;

    public Server(String[] args)
    {
        // Check arguments, exit on error
        checkArguments(args);
    }

    public static void main(String[] args)
    {
        Server server =  new Server(args);
        // TODO: start selector and server channel
        server.startSelector();
        server.bind();

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
        } catch (IOException e) {
            TimeStamp.printWithTimestamp("Failed to bind server to port.");
            System.exit(1);
        }
        // TODO: Start threads?

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
        }
    }
}
