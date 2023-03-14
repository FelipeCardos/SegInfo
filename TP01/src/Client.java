import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

	
	public static void main(String[]args) throws IOException, ClassNotFoundException {
		Socket echoSocket = new Socket("127.0.0.1", 23456); //”127.0.0.1”
		
		ObjectInputStream in = new ObjectInputStream(echoSocket.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(echoSocket.getOutputStream());
		
		out.writeObject("Felipe");
		out.writeObject("pass_felipe");
		
		Boolean fromServer = (Boolean) in.readObject();
		
		System.out.println(fromServer);
		
		
		if(fromServer) {
			File myfile = new File("aa.pdf");
			
			long myFileSize = myfile.length();
			
			FileInputStream fileStream = new FileInputStream("aa.pdf");
			
			byte buffer [] = new byte [1024];
			
		
			int n;
			
			while((n = fileStream .read(buffer, 0, 1024))>0) {
				out.write(buffer,0, n);
			}
		}
		out.close();
		in.close();
		echoSocket.close();
		
	}
	
}
