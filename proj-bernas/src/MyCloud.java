import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

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

        for ()





        return null;
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
