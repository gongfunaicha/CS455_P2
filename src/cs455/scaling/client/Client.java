package cs455.scaling.client;

import cs455.scaling.clientThread.ClientSenderThread;
import cs455.scaling.clientThread.ClientStatisticCollector;
import cs455.scaling.util.HashStorage;
import cs455.scaling.util.TimeStamp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client {

    private String serverHost = null;
    private int serverPort = 0;
    private int messageRate = 1;
    private Selector selector= null;
    private HashStorage hashStorage = null;
    private ClientSenderThread clientSenderThread = null;
    private ClientStatisticCollector clientStatisticCollector = null;

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
            System.out.println("Invalid number of arguments");
            System.out.println("Usage: java cs455.scaling.client.Client server-host server-port message-rate");
            System.exit(1);
        }

        // Right number of arguments, create instance of Messaging Node
        Client client = new Client(args);

        client.initializeSelector();
        client.connectToServer();
        client.startSelect();
    }

    private void startSelect()
    {
        while (true)
        {
            // Start select, block until at least one key is ready for select
            try {
                selector.select();
            } catch (IOException e) {
                TimeStamp.printWithTimestamp("Failed to do select. Program will now exit.");
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
            TimeStamp.printWithTimestamp("Failed to finish connect. Program will now exit.");
            System.exit(1);
        }
        if (status)
        {
            // Connect success, change intention
            TimeStamp.printWithTimestamp("Successfully connected to server.");
            key.interestOps(SelectionKey.OP_READ);
        }
        else
        {
            // Connect failed
            TimeStamp.printWithTimestamp("Failed to connect to server. Program will now exit.");
            System.exit(1);
        }

        // Initialize hash storage
        hashStorage = new HashStorage();

        // Initialize collector thread
        clientStatisticCollector = new ClientStatisticCollector();

        // Initialize sender thread
        clientSenderThread = new ClientSenderThread(key, hashStorage, messageRate, clientStatisticCollector);

        // Start both threads
        clientSenderThread.start();
        clientStatisticCollector.start();
    }

    private void read(SelectionKey key)
    {
        // Digest have 40 bytes (in hex)
        ByteBuffer buffer = ByteBuffer.allocate(40);
        buffer.clear();
        int read = 0;
        synchronized (key)
        {
            SocketChannel socketChannel  = (SocketChannel)key.channel();
            try
            {
                while (buffer.hasRemaining() && read != -1)
                    read = socketChannel.read(buffer);
            }
            catch (IOException e)
            {
                TimeStamp.printWithTimestamp("Failed to read data into buffer. Program will now exit.");
                System.exit(1);
            }
        }
        if (read == -1)
        {
            TimeStamp.printWithTimestamp("Lost connection to server. Program will now exit.");
            System.exit(1);
        }

        // Increment receive counter
        clientStatisticCollector.incrementReceiveCount();

        // Remove hash from the hash storage
        // TODO: Check whether need to rewind
        String hash = new String(buffer.array());
        if (!hashStorage.checkAndRemove(hash))
        {
            // Does not contain hash
            TimeStamp.printWithTimestamp("Recevied invalid hash.");
        }

    }

    private void initializeSelector()
    {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            TimeStamp.printWithTimestamp("Failed to open selector. Program will now exit.");
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
            TimeStamp.printWithTimestamp("Failed to connect to server. Program will now exit.");
            System.exit(1);
        }

    }

    private void checkArguments(String[] args)
    {
        // Check hostname
        if (!validateIP(args[0]))
        {
            System.out.println("Error: Inputted IP not valid. Please check the server-host. Program will now exit.");
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
            System.out.println("Error: Inputted port number not int. Please check the server-port. Program will now exit.");
            System.exit(1);
        }

        // Check port number between 0 and 65535
        if (serverPort < 0 || serverPort > 65535)
        {
            System.out.println("Error: Invalid port number. Port number should be between 0 and 65535. Program will now exit.");
            System.exit(1);
        }

        // Check message rate
        try
        {
            messageRate = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e)
        {
            System.out.println("Error: Inputted message rate not int. Please check the message-rate. Program will now exit.");
            System.exit(1);
        }

        if (messageRate <= 0)
        {
            System.out.println("Error: Invalid message rate. Message rate should be greater than 0. Program will now exit.");
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