package cs455.scaling.clientThread;

import cs455.scaling.util.DigestUtil;
import cs455.scaling.util.HashStorage;
import cs455.scaling.util.TimeStamp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Random;

// responsible for generating 8KB data and send out
public class ClientSenderThread extends Thread{

    SelectionKey key = null;
    HashStorage hashStorage = null;
    int frequency = 1; // Number of messages per second
    ClientStatisticCollector clientStatisticCollector = null;

    public ClientSenderThread(SelectionKey key, HashStorage hashStorage, int frequency, ClientStatisticCollector clientStatisticCollector)
    {
        this.key = key;
        this.hashStorage = hashStorage;
        this.frequency = frequency;
        this.clientStatisticCollector = clientStatisticCollector;
    }

    @Override
    public void run() {
        Random random = new Random();
        // Buffer size 8KB
        byte[] data = new byte[8192];
        ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
        byteBuffer.clear();

        while (true)
        {
            // generate byte array, calculate hash, add to hash storage
            random.nextBytes(data);
            hashStorage.put(DigestUtil.SHA1FromBytes(data));
            // Synchronize on key to ensure Client is not reading
            synchronized (key)
            {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                byteBuffer.put(data);
                byteBuffer.rewind();
                try {
                    // Currently ignore send buffer full on client side
                    socketChannel.write(byteBuffer);
                } catch (IOException e) {
                    TimeStamp.printWithTimestamp("Failed to write data to socket channel. Program will now exit.");
                    System.exit(1);
                }
            }
            clientStatisticCollector.incrementSendCount();
            byteBuffer.clear();
            try {
                sleep(1000/frequency);
            } catch (InterruptedException e) {
                TimeStamp.printWithTimestamp("Interrupted when waiting to send next message");
            }
        }
    }
}
