package cs455.scaling.util;

import java.util.LinkedList;

// Store unacknowledged hashes
public class hashStorage {
    private LinkedList<String> storage = null;

    public hashStorage()
    {
        // Initialize hash storage
        storage = new LinkedList<>();
    }

    public synchronized void put(String hash)
    {
        storage.add(hash);
    }

    public synchronized boolean checkAndRemove(String hash)
    {
        return storage.remove(hash);
    }

}
