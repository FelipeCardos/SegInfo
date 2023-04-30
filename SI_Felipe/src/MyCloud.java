import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MyCloud {

    private Socket clientSocket;
    private ArrayList<File> files = new ArrayList<>();

    private KeyPair clientKeyPair;
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
            System.out.println("Client Started");

            String server = (String) checked.get(2);
            int port = Integer.parseInt((String) checked.get(3));

            try {
                this.clientSocket = new Socket(server, port);
                System.out.println("Client connected");
                this.objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
                this.objectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());
                this.objectOutputStream.writeObject(args[2]);

                this.objectOutputStream.reset();

            } catch (IOException e) {
                System.out.println("Server não esta ativo.");
                throw new RuntimeException(e);
            }


            System.out.println("Sending Data...");

            System.out.println(checked.get(4));
            List<File> listOfFiles = new ArrayList<File>(files);
            switch ((String) checked.get(4)) {
                case "-c":
                    cFunction();
                case "-s":
                    List<File> filesToSend = sFunction(listOfFiles);
                    sendToServer(filesToSend);

            }
        } catch (IOException | ClassNotFoundException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendToServer(List<File> filesToSend){
        try{
            this.objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.objectOutputStream.writeObject(filesToSend);
            this.objectOutputStream.flush();
            this.objectOutputStream.reset();

        }catch(Exception e){e.printStackTrace();}
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
                for (int x = 3; x < args.length; x++) {
                    File file = new File(args[x]);
                    if (!file.exists()) {
                        System.out.println("Ficheiro "+ args[x] + " nao existe");
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

    private void cFunction () throws IOException, NoSuchAlgorithmException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        ArrayList<File> filesToServer = new ArrayList<>();

        objectOutputStream.writeObject("-c");

        if (this.clientKeyPair == null) {
            this.clientKeyPair = CryptoUtils.generateKeyPair();
        }

        Key symKey = CryptoUtils.generateSymmetricKey();

        objectOutputStream.writeObject(files.size());
        for (File f: this.files) {
            objectOutputStream.writeObject(f.getName());
            boolean boolFileInServer = (boolean) objectInputStream.readObject();
            if (boolFileInServer) {
                filesToServer.add(f);

            }
        }

        byte[] wrappedKey = CryptoUtils.encryptKey(symKey, this.clientKeyPair.getPublic());

        objectOutputStream.writeObject(filesToServer.size());
        for (File f: filesToServer) {
            byte[] dataToSend = CryptoUtils.encryptFile(f, symKey);
            objectOutputStream.writeObject(f.getName());
            objectOutputStream.writeObject(dataToSend);
            objectOutputStream.writeObject(wrappedKey);
        }

    }

    public static List<File> sFunction(List<File> listOfFiles){
        List<File> filesToSend = new ArrayList<>();
        try{
            FileInputStream keyFile = new FileInputStream("keystore.si");
            try{
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(keyFile, "123456".toCharArray());
                Key myPrivateKey = keyStore.getKey("si", "123456".toCharArray());
                FileInputStream file = null;
                for (File fileName : listOfFiles) {
                    try {
                        file = new FileInputStream(fileName);
                    }catch(Exception e){String error = "File not found";}

                    byte[] buffer = new byte[16];

                    Signature signature = Signature.getInstance("SHA256withRSA");
                    signature.initSign((PrivateKey) myPrivateKey);
                    int n;
                    while ((n = file.read(buffer)) != -1) {
                        signature.update(buffer, 0, n);
                    }

                    // cria um novo arquivo para a assinatura
                    FileOutputStream signatureFile = new FileOutputStream(fileName + ".assinatura");

                    // escreve a assinatura no arquivo
                    signatureFile.write(signature.sign());

                    // fecha o arquivo
                    signatureFile.close();

                    // cria um novo arquivo para o arquivo assinado
                    FileOutputStream signedFile = new FileOutputStream(fileName + ".assinado");

                    // escreve o conteúdo do arquivo original no arquivo assinado
                    FileInputStream originalFile = new FileInputStream(fileName);
                    while ((n = originalFile.read(buffer)) != -1) {
                        signedFile.write(buffer, 0, n);
                    }

                    // escreve a assinatura no final do arquivo assinado
                    signedFile.write(signature.sign());

                    // fecha o arquivo
                    signedFile.close();
                    originalFile.close();
                    file.close();
                    File f = new File(fileName+".assinado");
                    File f2 = new File(fileName+".assinatura");
                    filesToSend.add(f);
                    filesToSend.add(f2);
                }
            }catch (Exception e){System.out.print("Keystore not found: " + e.getMessage());}
        }catch(FileNotFoundException  e){System.out.print("File not found: " + e.getMessage());}
        return filesToSend;
    }
}