package cs455.scaling.util;

import java.nio.ByteBuffer;

// Designed to use as selector attachment
public class Attachment {
    // Data buffer is 8KB in size
    private ByteBuffer dataBuffer = null;
    // Digest buffer is 40B is size
    private ByteBuffer digestBuffer = null;
    // Used to signal that the current channel is (planned to be) in use
    private boolean inUse = false;
    // Used to signal that the current channel is planned to have rewrite
    private boolean alreadyRewrite = false;

    public Attachment()
    {
        dataBuffer = ByteBuffer.allocate(8192);
        digestBuffer = ByteBuffer.allocate(40);
        inUse = false;
    }

    // Return true if currently in use, false if not and set in use to true
    public synchronized boolean getAndUpdateInUse()
    {
        if (inUse)
        {
            // Currently in use, return true
            return true;
        }
        else
        {
            // Currently not in use, set in use to true and return false
            inUse = true;
            return false;
        }
    }

    // Return true if currently already rewrite, false if not and set already rewrite to true
    public synchronized boolean getAndUpdateAlreadyRewrite()
    {
        if (alreadyRewrite)
        {
            // Currently in use, return true
            return true;
        }
        else
        {
            // Currently not in use, set in use to true and return false
            alreadyRewrite = true;
            return false;
        }
    }

    public synchronized void setNotInUse()
    {
        inUse = false;
    }

    public synchronized void setNotAlreadyRewrite()
    {
        alreadyRewrite = false;
    }

    public ByteBuffer getDataBuffer()
    {
        return dataBuffer;
    }

    public ByteBuffer getDigestBuffer()
    {
        return digestBuffer;
    }

}
