import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyCloudServer {

    private ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(1234);
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
/*                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();*/

            }
        } catch (IOException e) {
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
    
    public static void VerificaAssinatura(){
        FileInputStream kfile = new FileInputStream("keystore.alice");
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
	}

    
}
