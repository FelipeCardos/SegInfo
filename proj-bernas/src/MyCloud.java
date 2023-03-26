import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MyCloud {


    private String userName;
    private String password;

    private Socket clientSocket;
    private ArrayList<File> files = new ArrayList<>();


    private static ObjectOutputStream objectOutputStream = null;
    private static ObjectInputStream objectInputStream = null;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        MyCloud m = new MyCloud(args);
    }

    public MyCloud(String[] args) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {

        ArrayList<Object> checked = checkMyCloudArgs(args);

        if (checked.get(0).equals(false)) {
            System.out.println(checked.get(1));
            System.exit(0);
        }

        try {
            System.out.println("Inicio do Programa");

            String server = (String) checked.get(2);
            int port = Integer.parseInt((String) checked.get(3));

            try {
                this.clientSocket = new Socket(server, port);
                System.out.println("Cliente Conectado");
                objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());

            } catch (IOException e) {
                System.out.println("Server não esta ativo.");
                throw new RuntimeException(e);
            }

            boolean auth = this.auth();
            if (!auth) {
                System.out.println("Cliente Nao Autenticado, Resete o Cliente");
                System.exit(0);
            }

            System.out.println("Cliente Autenticado");


            System.out.println("Transferir Dados...");

            System.out.println(checked.get(4));

            switch ((String) checked.get(4)) {
                case "-c":
                    cFunction();
                case "-g":
                    gFunction();
            }
        } catch (IOException | ClassNotFoundException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | NoSuchProviderException | InvalidKeySpecException e) {
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
            this.files.clear();

            if (error.isEmpty()) {
                if (args.length > 3) {
                    for (int x = 3; x < args.length; x++) {
                        File file = new File(args[x]);
                        if (!file.exists()) {
                            System.out.println("Ficheiro " + args[x] + " nao existe");
                        } else {
                            this.files.add(file);
                        }
                    }
                } else {
                    error.append(" Nenhum ficheiro foi adicionado.");
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






    private boolean auth() throws IOException, ClassNotFoundException {
        boolean verified = false;
        Scanner myObj = new Scanner(System.in);
        System.out.println("Enter username:");
        String userName = myObj.nextLine();
        objectOutputStream.writeObject(userName);
        boolean firstTime = (boolean) objectInputStream.readObject();
        this.userName = userName;
        if (firstTime) {
            System.out.println("Enter password:");
            String userPassword = myObj.nextLine();
            objectOutputStream.writeObject(userPassword);
            this.password = userPassword;
            verified = true;
        }
        else {
            int counter = 0;
            while (counter < 3) {
                counter++;
                System.out.println("Enter password:");
                String userPassword = myObj.nextLine();
                objectOutputStream.writeObject(userPassword);
                boolean correctPassword = (boolean) objectInputStream.readObject();
                if (correctPassword) {
                    this.password = userPassword;
                    verified = true;
                    break;
                }

            }
        }

        return verified;
    }







    private void cFunction () throws IOException, NoSuchAlgorithmException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, InvalidKeySpecException {
        ArrayList<File> filesToServer = new ArrayList<>();

        objectOutputStream.writeObject("-c");

        Key userKey = CryptoUtils.generateKeyFromPassword(this.password);

        Key symKey = CryptoUtils.generateSymmetricKey();

        objectOutputStream.writeObject(files.size());
        for (File f: this.files) {
            objectOutputStream.writeObject(f.getName());
            boolean boolFileInServer = (boolean) objectInputStream.readObject();
            if (boolFileInServer) {
                filesToServer.add(f);

            }
        }

        byte[] wrappedKey = CryptoUtils.encryptKey(symKey, userKey);

        objectOutputStream.writeObject(filesToServer.size());
        for (File f: filesToServer) {
            byte[] dataToSend = CryptoUtils.encryptFile(f, symKey);
            objectOutputStream.writeObject(f.getName());
            objectOutputStream.writeObject(dataToSend);
            objectOutputStream.writeObject(wrappedKey);


        }










    }


    private void gFunction() {

    }
}
