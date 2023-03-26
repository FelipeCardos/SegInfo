import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class Utils {
     public static void transferDataToFile(File savedFile, byte[] dataInFile) {
        try {
            FileOutputStream fos = new FileOutputStream(savedFile);
            fos.write(dataInFile);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File createFile(String pathname) throws IOException {
        File savedFile = new File(pathname);
        savedFile.createNewFile();
        return savedFile;
    }

    /* GeeksForGeeks toHexString */
    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }


}
