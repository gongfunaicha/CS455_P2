package cs455.scaling.testserver;

import cs455.scaling.util.DigestUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {

    public static void main(String[] args)
    {
        Socket socket = null;
        try {
            ServerSocket serverSocket = new ServerSocket(5555);
            socket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("Failed to bind to 5555 or accept incoming connection.");
        }

        OutputStream os = null;
        DataInputStream dataInputStream = null;
        try {
             os = socket.getOutputStream();
             dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = new byte[8192];

        while (true)
        {
            try {
                dataInputStream.read(data);
            } catch (IOException e) {
                System.out.println("Failed to read data from input stream.");
                System.exit(1);
            }
            String hash = DigestUtil.SHA1FromBytes(data);
            try {
                os.write(hash.getBytes());
            } catch (IOException e) {
                System.out.println("Failed to write data to output stream.");
                System.exit(1);
            }
        }


    }
}
