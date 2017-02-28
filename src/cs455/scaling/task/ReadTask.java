package cs455.scaling.task;

import java.nio.channels.SelectionKey;

public class ReadTask implements Task{
    private SelectionKey key = null;

    public ReadTask(SelectionKey key)
    {
        this.key = key;
    }

    @Override
    public String getType() {
        return "Read";
    }
}
