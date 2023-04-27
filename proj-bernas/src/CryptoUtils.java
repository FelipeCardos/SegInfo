import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

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

    static byte[] encryptKeyRSA(Key key, Key publicKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchProviderException {
        Cipher c = Cipher.getInstance("RSA");

        c.init(Cipher.WRAP_MODE,publicKey);

        return c.wrap(key);
    }

    static Key decryptKeyRSA(byte[] b, Key publicKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchProviderException {
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

    /* GeeksForGeeks getSha */
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static Key generateKeyFromPassword(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray( ));
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        return kf.generateSecret(keySpec);
    }

    static byte[] encryptKey(Key key, Key mainKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher c = Cipher.getInstance("PBEWithMD5AndDES");
        byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52,
                (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 20);
        c.init(Cipher.WRAP_MODE,mainKey, paramSpec);

        return c.wrap(key);
    }

    static Key decryptKey(byte[] b, Key mainKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Cipher c = Cipher.getInstance("PBEWithMD5AndDES");
        byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52,
                (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 20);
        c.init(Cipher.UNWRAP_MODE,mainKey, paramSpec);
        return c.unwrap(b, "AES", Cipher.SECRET_KEY);
    }

    static void decryptFile(byte[] encryptedData, File outputFile, Key secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        Files.write(outputFile.toPath(), decryptedData);
    }




}
