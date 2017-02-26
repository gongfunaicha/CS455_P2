package cs455.scaling.client;

import cs455.scaling.util.timeStamp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client {

    private String serverHost = null;
    private int serverPort = 0;
    private int messageRate = 1;
    private Selector selector= null;

    public Client(String[] args)
    {
        // Check arguments, exit on fail
        checkArguments(args);
    }

    public static void main(String[] args)
    {
        // Command line check
        if (args.length != 3)
        {
            // Wrong number of arguments, display error message
            timeStamp.printWithTimestamp("Invalid number of arguments");
            timeStamp.printWithTimestamp("Usage: java cs455.scaling.client.Client server-host server-port message-rate");
            System.exit(1);
        }

        // Right number of arguments, create instance of Messaging Node
        Client client = new Client(args);

        client.initializeSelector();
        client.connectToServer();
        client.startSelect();
        // TODO: Connect to server, start statistics collector and sender thread
    }

    private void startSelect()
    {
        while (true)
        {
            // Start select, block until at least one key is ready for select
            try {
                selector.select();
            } catch (IOException e) {
                timeStamp.printWithTimestamp("Failed to do select. Program will now exit.");
                System.exit(1);
            }

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext())
            {
                SelectionKey key = keys.next();
                if (key.isConnectable())
                {
                    this.connect(key);
                }
                else if (key.isReadable())
                {
                    this.read(key);
                }
                // Remove from the collection, or we'll process it in next while(true) iteration
                keys.remove();
            }

        }
    }

    private void connect(SelectionKey key)
    {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        // Finish connection
        boolean status = false;
        try {
             status = socketChannel.finishConnect();
        } catch (Exception e) {
            timeStamp.printWithTimestamp("Failed to finish connect. Program will now exit.");
            System.exit(1);
        }
        if (status)
        {
            // Connect success, change intention
            timeStamp.printWithTimestamp("Successfully connected to server.");
            key.interestOps(SelectionKey.OP_READ);
        }
        else
        {
            // Connect failed
            timeStamp.printWithTimestamp("Failed to connect to server. Program will now exit.");
            System.exit(1);
        }

        // TODO: Finished connection, start sender thread and collector thread
    }

    private void read(SelectionKey key)
    {
        // TODO: read from channel, remember to SYNCHRONIZE selection key
    }

    private void initializeSelector()
    {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            timeStamp.printWithTimestamp("Failed to open selector. Program will now exit.");
            System.exit(1);
        }
    }

    private void connectToServer()
    {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            // Configure non blocking
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
        } catch (IOException e) {
            timeStamp.printWithTimestamp("Failed to connect to server. Program will now exit.");
            System.exit(1);
        }

    }

    private void checkArguments(String[] args)
    {
        // Check hostname
        if (!validateIP(args[0]))
        {
            timeStamp.printWithTimestamp("Error: Inputted IP not valid. Please check the server-host. Program will now exit.");
            System.exit(1);
        }

        serverHost = args[0];

        // Check port number
        try
        {
            serverPort = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            timeStamp.printWithTimestamp("Error: Inputted port number not int. Please check the server-port. Program will now exit.");
            System.exit(1);
        }

        // Check port number between 0 and 65535
        if (serverPort < 0 || serverPort > 65535)
        {
            timeStamp.printWithTimestamp("Error: Invalid port number. Port number should be between 0 and 65535. Program will now exit.");
            System.exit(1);
        }

        // Check message rate
        try
        {
            messageRate = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e)
        {
            timeStamp.printWithTimestamp("Error: Inputted message rate not int. Please check the message-rate. Program will now exit.");
            System.exit(1);
        }

        if (messageRate <= 0)
        {
            timeStamp.printWithTimestamp("Error: Invalid message rate. Message rate should be greater than 0. Program will now exit.");
            System.exit(1);
        }

    }

    private boolean validateIP(String IP)
    {
        // Cut based on "."
        String[] splitted = IP.split("\\.");

        // If not four parts, not a valid IP
        if (splitted.length != 4)
            return false;

        for (String substr: splitted)
        {
            int number = 0;
            try
            {
                number = Integer.parseInt(substr);
            }
            catch (NumberFormatException e)
            {
                // Not valid integer, not valid IP
                return false;
            }
            // Not between 0 and 255, not valid IP
            if (number < 0 || number > 255)
                return false;
        }

        // All passed, is valid IP
        return true;
    }

}