package cs455.scaling.task;

import java.nio.channels.SelectionKey;

public class Task {
    private char task = 0;
    private SelectionKey key = null;

    // 'R': Read, 'H': Hash, 'W': Write
    public Task(char task, SelectionKey key)
    {
        this.task = task;
        this.key = key;
    }

    public char getTask()
    {
        return task;
    }

    public SelectionKey getKey()
    {
        return key;
    }
}
