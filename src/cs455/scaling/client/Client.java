package cs455.scaling.client;

public class Client {

    private String serverHost = null;
    private int serverPort = 0;
    private int messageRate = 1;

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

        // TODO: Connect to server, start statistics collector and sender thread
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