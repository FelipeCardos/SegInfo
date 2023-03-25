import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
}
