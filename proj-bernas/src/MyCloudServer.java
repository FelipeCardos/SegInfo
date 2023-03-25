import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

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
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
            this.inStream = new ObjectInputStream(socket.getInputStream());
        }

        public void run() {
            try {
                String type = (String) inStream.readObject();
                System.out.println(type);

                switch (type) {
                    case "-c":
                        cFunction();
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
    }


}


