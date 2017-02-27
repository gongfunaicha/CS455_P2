package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DigestUtil {
    public static String SHA1FromBytes(byte[] data) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            TimeStamp.printWithTimestamp("SHA1 algorithm not found. Program will now exit.");
            System.exit(1);
        }
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);

        // Making sure it returns 40 characters (full representation of SHA1), add leading 0 if not
        return String.format("%040x", hashInt);

    }
}
