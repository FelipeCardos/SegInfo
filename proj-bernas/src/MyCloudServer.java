import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class MyCloudServer {

    private ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {

        MyCloudServer server = new MyCloudServer(args);
        server.runServer();

    }
    public MyCloudServer(String[] args) throws IOException {
        this.startServer(args);
    }

    private void startServer(String[] args){
        System.out.println("Starting Server");
        try {
            int port = Integer.parseInt(args[0]);
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void runServer() throws IOException {
        System.out.println("Waiting for clients");
        while(true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Client connected");
                ServerThread serverThread = new ServerThread(clientSocket);
                serverThread.start();


            } catch (IOException e) {
                this.closeServer();
                throw new RuntimeException(e);

            }
        }
    }


    private void closeServer () throws IOException {
        this.serverSocket.close();
    }












    static class ServerThread extends Thread {

        private String userName;

        private final ObjectOutputStream outStream;
        private final ObjectInputStream inStream;
        protected ArrayList<File> files = new ArrayList<>();
        private Socket socket = null;

        ServerThread(Socket inSoc) throws IOException {
            socket = inSoc;
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
            this.inStream = new ObjectInputStream(socket.getInputStream());
        }

        public void run() {
            try {
                serverAuth();
                System.out.println("Client Authed");

                File folder = new File("serverFiles/" + userName);
                String[] filesInFolder = folder.list();
                System.out.println(Arrays.toString(filesInFolder));

                String type = (String) inStream.readObject();
                System.out.println(type);

                switch (type) {
                    case "-c":
                        cFunction();
                    case "-g":
                        gFunction();
                }
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }


        private void serverAuth() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
            userName = (String) inStream.readObject();

            if (Files.isDirectory(Path.of("serverFiles/" + userName))) {
                outStream.writeObject(false);
                int counter = 0;
                while (counter < 3) {
                    counter++;
                    String tryPassword = String.valueOf(inStream.readObject());
                    String tryPasswordHashed = Utils.toHexString(CryptoUtils.getSHA(tryPassword));
                    byte[] passwordBytes = Files.readAllBytes(Paths.get("serverFiles/"+userName+"/"+userName+".password"));
                    String password = new String(passwordBytes);
                    System.out.println(password.equals(tryPasswordHashed));
                    if (password.equals(tryPasswordHashed)){
                        outStream.writeObject(true);
                    } else {
                        outStream.writeObject(false);
                    }

                }
            } else {
                outStream.writeObject(true);
                File theDir = new File("serverFiles/"+userName);
                theDir.mkdir();
                String userPassword = (String) inStream.readObject();
                String userPasswordHashed = Utils.toHexString(CryptoUtils.getSHA(userPassword));
                byte[] userPasswordHashedBytes = userPasswordHashed.getBytes(StandardCharsets.UTF_8);
                File userpasswordFile = Utils.createFile("serverFiles/"+userName+"/"+userName+".password");
                Utils.transferDataToFile(userpasswordFile, userPasswordHashedBytes);
            }
        }

        private void cFunction() throws ClassNotFoundException {
            try {
                int nrFiles = (int) this.inStream.readObject();
                System.out.println("___________________________________");
                System.out.println(nrFiles);

                for (int x = 0; x < nrFiles; x++) {
                    String f = (String) inStream.readObject();
                    boolean bool = true;

                    for (File file: this.files) {
                        System.out.println(file.getName());
                        System.out.println(f+".cifrado");
                        if (file.getName().equals(f+".cifrado")) {
                            System.out.println(f+".cifrado");
                            System.out.println("encontra-se no servidor");
                            bool = false;
                            outStream.writeObject(false);
                            break;
                        }
                    }
                    if (bool) {
                        outStream.writeObject(true);
                    }

                }

                System.out.println("---------------------------------");

                int nrFilesToServer = (int) inStream.readObject();;
                for (int x = 0; x < nrFilesToServer; x++) {

                    String fileName = (String) inStream.readObject();
                    File savedFile = Utils.createFile("serverFiles/"+this.userName+"/"+fileName+".cifrado");
                    byte[] dataInFile = (byte[]) inStream.readObject();
                    Utils.transferDataToFile(savedFile, dataInFile);


                    byte[] dataInKey = (byte[]) inStream.readObject();
                    File savedKeyFile = Utils.createFile("serverFiles/"+this.userName+"/"+fileName+".chave_secreta");
                    Utils.transferDataToFile(savedKeyFile, dataInKey);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void gFunction() {

        }
    }


}


