package cs455.scaling.server;

public class Server {

    private int port = 0;
    private int threadPoolSize = 0;

    public Server(String[] args)
    {
        // Check arguments, exit on error
        checkArguments(args);
    }

    public static void main(String[] args)
    {
        Server server =  new Server(args);
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
