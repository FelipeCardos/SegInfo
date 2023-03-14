import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;

public class Assina {

	public static void main(String[] args) throws Exception {
		//chave privada de quem assina ---> keystore
    	FileInputStream kfile = new FileInputStream("keystore.alice");
    	KeyStore kstore = KeyStore.getInstance("PKCS12");
    	kstore.load(kfile,"123456".toCharArray());
    	Key myPrivateKey = kstore.getKey("alice", "123456".toCharArray());

    	
    	
    	FileInputStream file = new FileInputStream("a.txt");
    	byte[] buffer = new byte[16];
    	Signature s = Signature.getInstance("SHA256withRSA");
    	s.initSign((PrivateKey) myPrivateKey);
    	int n;
    	while((n = file.read(buffer))!=-1) {
    		s.update(buffer,0,n);
    	}
    	FileOutputStream fileAssinatura = new FileOutputStream("a.assinatura");
    	fileAssinatura.write(s.sign());
    	fileAssinatura.close();
    	file.close();
	}

}
