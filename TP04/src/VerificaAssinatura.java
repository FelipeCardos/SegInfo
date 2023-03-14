import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;

public class VerificaAssinatura {

	public static void main(String[] args) throws Exception {
		//chave privada de quem assina ---> keystore
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
