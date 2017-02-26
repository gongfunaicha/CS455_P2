package cs455.scaling.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class timeStamp {
    public static String getCurrentTimeStamp()
    {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.S");
        return simpleDateFormat.format(date);
    }

    public static void printWithTimestamp(String data)
    {
        System.out.println("[" + getCurrentTimeStamp() + "] " + data);
    }
}
