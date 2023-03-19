import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MyCloudServer {

    private ServerSocket serverSocket;

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static ObjectOutputStream objectOutputStream = null;



    private KeyPair keyPair;





    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(23456);
        MyCloudServer server = new MyCloudServer(serverSocket);
        server.startServer();

    }

    public MyCloudServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer(){

        try{
            System.out.println("Starting Server");
            while (!serverSocket.isClosed()) {


                Socket socket = serverSocket.accept();
                System.out.println("New Client Connected");

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());


                String type = dataInputStream.readUTF();
                switch (type) {
                    case "-c":
                        cServerFunction();
                }

/*                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();*/

            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(1024);
        this.keyPair = kpGen.generateKeyPair();
    }

    private void cServerFunction() throws NoSuchAlgorithmException, IOException {
        if (this.keyPair == null) {
            generateKeyPair();
        }

        objectOutputStream.writeObject(this.keyPair.getPublic());
        objectOutputStream.flush();






    }
}
