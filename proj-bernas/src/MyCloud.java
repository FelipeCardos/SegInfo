import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;
import java.io.FileInputStream;  
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.Key;
import java.security.Signature;
import java.security.PrivateKey;


import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.cert.Certificate;
import java.util.Arrays;


public class MyCloud {

    private Socket socket;

    public static void main(String[] args) {
        checkMyCloudArgs(args);
        MyCloud m = new MyCloud();
    }

    private static ArrayList checkMyCloudArgs(String[] args) {

        //from (-a 127.0.0.1:5000 -_ filename) to (-a 127.0.0.1 5000 -_ filename)
        ArrayList sendToServer = new ArrayList<>();

        Set<String> argOptions = Set.of("-c", "-s", "-e", "-g");

        if (args[0] != "-a") {
            String error = "Tem de identificar o servidor ao qual quer-se conectar";

        } else {
            sendToServer.add(args[0]);
        }


        String[] serverId = args[1].split(":");

        if (serverId.length != 2) {
            String error = "Verifique se o hostname está correto";
        } else {
            sendToServer.add(serverId[0]);
            sendToServer.add(serverId[1]);
        }

        if (!argOptions.contains(args[2])) {
            String error = "Verifique os argumentos dados { -c, -s, -e, -g }";
        } else {
              sendToServer.add(args[2]);
        }
        String[] files = args[3].split(" ");
        ArrayList<String> listOfFiles = new ArrayList<String>(Arrays.asList(files));
        switch(args[2]){
            case "-s":
                handleS(listOfFiles);
        } 
        //for ()


        


        return null;
    }

    public static void handleS(ArrayList<String> listOfFiles) {
        for (int i = 0; i < listOfFiles.size(); i++) {
            File file = new File(listOfFiles.get(i));
            // Verifico se ficheiro existe e se é um ficheiro        
            if (file.exists() && file.isFile()) {
                try {
                    assina(listOfFiles.get(i));
                } catch (Exception e) {
                    System.err.println("Erro ao assinar arquivo " + listOfFiles.get(i) + ": " + e.getMessage());
                    break;
                }
            } else {
                System.err.println("Arquivo " + listOfFiles.get(i) + " não encontrado ou não é um arquivo.");
                break;
            }
        }
        
    }
    private static boolean assina (String fileName)throws Exception{
        FileInputStream kfile = new FileInputStream("keystore.si");
        KeyStore kstore = KeyStore.getInstance("PKCS12");
        kstore.load(kfile,"123456".toCharArray());
        Key myPrivateKey = kstore.getKey("si", "123456".toCharArray());

        FileInputStream file = null;
        try {
            file = new FileInputStream(fileName);
        } catch (Exception e) {
            String error = "File not found";
        }
        
    	byte[] buffer = new byte[16];

        Signature s = Signature.getInstance("SHA256withRSA");
    	s.initSign((PrivateKey) myPrivateKey);
    	int n;
    	while((n = file.read(buffer))!=-1) {
    		s.update(buffer,0,n);
    	}
    	FileOutputStream fileAssinatura = new FileOutputStream(fileName + ".assinatura");
    	fileAssinatura.write(s.sign());
    	fileAssinatura.close();
    	file.close();
        System.out.println("Deu certo");
        return true;

    }

    public MyCloud() {
        try {
            System.out.println("Client Started");
            this.socket = new Socket("localhost", 1234);



        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startMyCloud(){

    }
}
