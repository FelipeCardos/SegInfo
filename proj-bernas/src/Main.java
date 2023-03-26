import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeySpecException {
        Key k = CryptoUtils.generateSymmetricKey();
        String s1 = "123hello";
        Key kK = CryptoUtils.generateKeyFromPassword(s1);
        byte[] s = CryptoUtils.encryptKey(k, kK);
        Key kk = CryptoUtils.decryptKey(s, kK);
        System.out.println(k.equals(kk));


    }
}