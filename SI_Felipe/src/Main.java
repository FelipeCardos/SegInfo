import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchProviderException {
        Key k = CryptoUtils.generateSymmetricKey();
        KeyPair kp = CryptoUtils.generateKeyPair();
        Key priv = kp.getPrivate();
        Key pub = kp.getPublic();
        byte[] s = CryptoUtils.encryptKey(k, pub);
        Key kk = CryptoUtils.decryptKey(s, priv);
        System.out.println(k.equals(kk));

    }
}