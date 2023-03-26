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
                        if (file.getName().equals(f+".cifrado")) {
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

        private void gFunction() throws IOException, ClassNotFoundException {
            ArrayList<File> filesFromClient =(ArrayList<File>) inStream.readObject();
            ArrayList<File> filesInServer = new ArrayList<>();

            File folder = new File("serverFiles/"+userName);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                for (File f: filesFromClient) {
                    if((f.getName()+".cifrado").equals(file.getName())) {

                        byte[] cifradoBytes = Files.readAllBytes(Path.of(f.getName() + ".cifrado"));
                        byte[] chaveBytes = Files.readAllBytes(Path.of(f.getName() + ".chave_secreta"));

                        outStream.writeObject(cifradoBytes);
                        outStream.writeObject(chaveBytes);

                    }
                }
            }




            
        }
                private void eFunction() throws ClassNotFoundException {
            try {
                int nrFiles = (int) this.inStream.readObject();
                System.out.println(nrFiles);

                for (int x = 0; x < nrFiles; x++) {
                    String f = (String) inStream.readObject();
                    boolean bool = true;

                    for (File file: this.files) {
                        if (file.getName().equals(f)) {
                            bool = false;
                            outStream.writeObject(false);
                            break;
                        }
                    }
                    if (bool) {
                        outStream.writeObject(true);
                    }
                }

                int nrFilesToServer = (int) inStream.readObject();;
                for (int x = 0; x < nrFilesToServer; x++) {

                    String fileName = (String) inStream.readObject();
                    File savedFile = Utils.createFile("serverFiles/"+fileName+".seguro");
                    byte[] dataInFile = (byte[]) inStream.readObject();
                    Utils.transferDataToFile(savedFile, dataInFile);


                    byte[] dataInKey = (byte[]) inStream.readObject();
                    File savedKeyFile = Utils.createFile("serverFiles/"+fileName+".chave_secreta");
                    Utils.transferDataToFile(savedKeyFile, dataInKey);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
                   private void sFunction() throws Exception{
                String fileName = (String) inStream.readObject();
                File savedFile = Utils.createFile("../serverFiles/" + fileName + ".assinatura");
                byte[] dataInFile = (byte[]) inStream.readObject();
                Utils.transferDataToFile(savedFile, dataInFile);
                
                VerificaAssinatura(fileName);
        }

        private void VerificaAssinatura(String fileName)throws Exception{
            FileInputStream kfile = new FileInputStream("keystore.si");
            KeyStore kstore = KeyStore.getInstance("PKCS12");
            kstore.load(kfile,"123456".toCharArray());
            Certificate cert= kstore.getCertificate("si");
            
            
            FileInputStream file = new FileInputStream(fileName);
            byte[] buffer = new byte[16];
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(cert);
            int n;
            while((n = file.read(buffer))!=-1) {
                s.update(buffer,0,n);
            }
            byte [] assinatura = new byte[256];
            FileInputStream fileAssinatura = new FileInputStream(fileName+".assinado");
            fileAssinatura.read(assinatura);
            boolean b = s.verify(assinatura);
            System.out.println(b);
            
            fileAssinatura.close();
            file.close();
    
        }
    }


}


