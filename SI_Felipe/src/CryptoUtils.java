import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;

public class CryptoUtils {
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(1024);
        return kpGen.generateKeyPair();
    }

    static Key generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(192);
        return generator.generateKey();

    }

    static byte[] keyToByte(Key key) {
        return key.getEncoded();
    }

    static byte[] encryptKey(Key key, Key publicKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchProviderException {
        Cipher c = Cipher.getInstance("RSA");

        c.init(Cipher.WRAP_MODE,publicKey);

        return c.wrap(key);
    }

    static Key decryptKey(byte[] b, Key publicKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchProviderException {
        Cipher c = Cipher.getInstance("RSA");
        c.init(Cipher.UNWRAP_MODE,publicKey);
        return c.unwrap(b, "AES", Cipher.SECRET_KEY);
    }

    static byte[] encryptFile(File file, Key secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        return cipher.doFinal(fileBytes);

    }

    static void encryptFileV2(File file, Key secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(file.toPath()+".cifrado");
            CipherInputStream cis = new CipherInputStream(fis, cipher);
            byte[] data = new byte[192];
            int i = cis.read(data);
            while(i != -1) {
                fos.write(data,0,i);
                i = cis.read(data);

            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
