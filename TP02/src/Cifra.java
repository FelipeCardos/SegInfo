import java.io.FileInputStream;  
import java.io.FileOutputStream;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.cert.Certificate;

public class Cifra {

    public static void main(String[] args) throws Exception {

    //gerar uma chave aleatoria para utilizar com o AES
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(128);
    SecretKey key = kg.generateKey();

    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.ENCRYPT_MODE, key);

    FileInputStream fis;
    FileOutputStream fos;
    CipherOutputStream cos;
    
    fis = new FileInputStream("a.txt");
    fos = new FileOutputStream("a.cif");

    cos = new CipherOutputStream(fos, c);
    byte[] b = new byte[16];  
    int i = fis.read(b);
    while (i != -1) {
        cos.write(b, 0, i);
        i = fis.read(b);
    }
    cos.close();
    
    FileInputStream kfile = new FileInputStream("keystore.alice");
    KeyStore kstore = KeyStore.getInstance("PKC12");
    kstore.load(kfile,"alicealice".toCharArray());
    Certificate cert = kstore.getCertificate("maria");

    Cipher c2 = Cipher.getInstance("RSA");
    
    c2.init(Cipher.WRAP_MODE ,cert);
    c2.wrap(key);

    byte[] keyEncoded = key.getEncoded();
    FileOutputStream kos = new FileOutputStream("a.key");
    kos.write(keyEncoded);
    kos.close();
    fos.close();
    fis.close();

    //Dicas para decifrar
    //byte[] keyEncoded2 - lido do ficheiro
    //SecretKeySpec keySpec2 = new SecretKeySpec(keyEncoded2, "AES");
    //c.init(Cipher.DECRYPT_MODE, keySpec2);    //SecretKeySpec é subclasse de secretKey
    }
}