import java.io.FileInputStream;  
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Decifra {
    public static void main(String[] args) throws Exception {

    	//ler chave do xx.key
    	FileInputStream fkey = new FileInputStream("a.key");
    	byte [] chave = new byte [16];
    	fkey.read(chave);

    	FileInputStream kfile = new FileInputStream("keystore.alice");
    	KeyStore kstore = KeyStore.getInstance("PKCS12");
    	kstore.load(kfile,"alicealice".toCharArray());
    	Key myPrivateKey = kstore.getKey("alice", "alicealice".toCharArray());
    	
    	Cipher c2 = Cipher.getInstance("RSA");
    	c2.init(Cipher.UNWRAP_MODE, myPrivateKey);
    	
    	SecretKey aeskey = (SecretKey)c2.unwrap(chave, "AES", Cipher.SECRET_KEY);
    	SecretKeySpec keySpec2 = new SecretKeySpec(chave,"AES");
    	
    	Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, keySpec2);
        

        FileInputStream fis;
        CipherInputStream cis;
        FileOutputStream fos;
        
        fis = new FileInputStream("a.cif");
        cis = new CipherInputStream(fis,c);
        
        fos = new FileOutputStream("ad.txt");

        byte[] b = new byte[16];  
        int i = cis.read(b);
        while (i != -1) {
            fos.write(b, 0, i);
            i = cis.read(b);
        }
        fos.close();
        fis.close();
        cis.close();
    	fkey.close();        


    }
    
}
