import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyCloudServer {

    private ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {

        MyCloudServer server = new MyCloudServer();
        server.runServer();


    }
    public MyCloudServer() throws IOException {
        this.startServer();
    }

    private void startServer(){
        System.out.println("Starting Server");
        try {
            this.serverSocket = new ServerSocket(23455);
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
                serverThread.run();


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


        private final ObjectOutputStream outStream;
        private final ObjectInputStream inStream;
        protected ArrayList<File> files = new ArrayList<>();
        private Socket socket = null;

        ServerThread(Socket inSoc) throws IOException {
            socket = inSoc;
            this.inStream = new ObjectInputStream(socket.getInputStream());
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
        }

        public void run() {
            try {
                Object obj = inStream.readObject();
                System.out.println("Received object: " + obj);
                String type = (String) obj;

                switch (type) {
                    case "-c":
                        cFunction();
                        break;
                    case "-s":
                        sFunction();
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void cFunction() throws ClassNotFoundException {
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
                    File savedFile = Utils.createFile("serverFiles/"+fileName+".cifrado");
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

        public void sFunction(){
            try{
                ArrayList<File> filesFromClient = new ArrayList<>();
                filesFromClient = (ArrayList) inStream.readObject();
                FileInputStream kfile = new FileInputStream("keystore.si");
                KeyStore kstore = KeyStore.getInstance("PKCS12");
                kstore.load(kfile,"123456".toCharArray());
                Certificate cert= kstore.getCertificate("alice");


                FileInputStream file = new FileInputStream("a.txt");
                byte[] buffer = new byte[16];
                Signature s = Signature.getInstance("SHA256withRSA");
                s.initVerify(cert);
                int n;
                while((n = file.read(buffer))!=-1) {
                    s.update(buffer,0,n);
                }
                byte [] assinatura = new byte[256];
                FileInputStream fileAssinatura = new FileInputStream("a.assinatura");
                fileAssinatura.read(assinatura);
                boolean b = s.verify(assinatura);
                System.out.println(b);

                fileAssinatura.close();
                file.close();
            }catch(Exception e){System.out.print(e.getMessage());}
        }

    }
}


