import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class MyCloud {

    private Socket socket;
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static ObjectOutputStream objectOutputStream = null;
    private static ObjectInputStream objectInputStream = null;
    private ArrayList<File> files = new ArrayList<>();

    public static void main(String[] args) {
        MyCloud m = new MyCloud(args);
    }



    public MyCloud(String[] args) {

        ArrayList<Object> checked = checkMyCloudArgs(args);

        if (checked.get(0).equals(false)) {
            System.out.println(checked.get(1));
            System.exit(0);
        }

        try {
            System.out.println("Client Started");

            String server = (String) checked.get(2);
            int port = Integer.parseInt((String) checked.get(3));

            this.socket = new Socket(server, port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println("Sending Data...");

            switch ((String) checked.get(4)) {
                case "-c":
                    cFunction();
                case "-s":
                    sFunction();
            }




        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    private ArrayList<Object> checkMyCloudArgs(String[] args) {


        //from (-a 127.0.0.1:5000 -_ filename) to (-a 127.0.0.1 5000 -_ filename)
        ArrayList<Object> sendToServer = new ArrayList<>();

        sendToServer.add(true);

        Set<String> argOptions = Set.of("-c", "-s", "-e", "-g");

        StringBuilder error = new StringBuilder();

        int counter = 0;


        try {
            if (!Objects.equals(args[0], "-a")) {
                counter += 1;
                error.append(" Tem de identificar o servidor ao qual quer-se conectar, -a hostname.");

            } else {
                sendToServer.add(args[0]);
            }

            String[] serverId = args[1-counter].split(":");

            if (serverId.length != 2) {
                counter += 1;
                error.append(" Verifique se o hostname está correto.");
            } else {
                sendToServer.add(serverId[0]);
                sendToServer.add(serverId[1]);
            }

            if (!argOptions.contains(args[2-counter])) {
                error.append(" Verifique os argumentos dados { -c, -s, -e, -g }.");
            } else {
                sendToServer.add(args[2-counter]);
            }

            if (error.isEmpty()) {
                for (int x = 3; x < args.length; x++) {
                    File file = new File(args[x]);
                    if (!file.exists()) {
                        error.append(" Ficheiro ").append(args[x]).append(" não existe.");
                    } else {
                        this.files.add(file);
                    }
                }
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            error.append("Não indicou argumentos");
        }

        if (error.toString().equals("")) {
            return sendToServer;
        } else {
            return new ArrayList<>(Arrays.asList(false, error.toString()));
        }

    }

    private void cFunction () throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        dataOutputStream.writeUTF("-c");
        dataOutputStream.flush();

        PublicKey publicKey = (PublicKey) objectInputStream.readObject();

        Key key = Utils.generatorSymmetricKey();

        for (File file: this.files) {
            byte[] encryptFileBytes =  Utils.encryptFile(file, key);
        }







    }

    private void sFunction() {
    }
}
