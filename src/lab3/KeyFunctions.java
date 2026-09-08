package lab3;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Core cryptographic utility suite.
 * Supports DES, AES, RSA key generation, ciphers, SHA-256 integrity digests, and nonce generation.
 *
 * @author Kalp Shah
 */
public class KeyFunctions {

    /**
     * Converts a byte array to an uppercase formatted hex preview string (up to 32 bytes).
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        int max = Math.min(bytes.length, 32);
        for (int i = 0; i < max; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        if (bytes.length > max) {
            sb.append(String.format("... (%d bytes total)", bytes.length));
        }
        return sb.toString().trim();
    }

    /**
     * Converts a byte array to a full lowercase hexadecimal string.
     */
    public static String bytesToFullHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Computes the SHA-256 hexadecimal hash digest of a byte array.
     */
    public static String getSHA256(byte[] data) {
        if (data == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return bytesToFullHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Computes the SHA-256 hexadecimal hash digest of a file.
     */
    public static String getSHA256(File file) {
        if (file == null || !file.exists() || !file.isFile()) return null;
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return bytesToFullHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // DES Cryptography
    // -------------------------------------------------------------------------

    public static SecretKey getKey(String key) {
        byte[] keyB = key.getBytes(StandardCharsets.UTF_8);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            SecretKey desKey = factory.generateSecret(new DESKeySpec(keyB));
            return desKey;
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] getPlainBytesDES(SecretKey key, byte[] encryptedInput) {
        if (key == null || encryptedInput == null) return null;
        try {
            Cipher desCipherObj = Cipher.getInstance("DES");
            desCipherObj.init(Cipher.DECRYPT_MODE, key);
            return desCipherObj.doFinal(encryptedInput);
        } catch (InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] getDESCipher(SecretKey key, byte[] input) {
        if (key == null || input == null) return null;
        try {
            Cipher desCipherObj = Cipher.getInstance("DES");
            desCipherObj.init(Cipher.ENCRYPT_MODE, key);
            return desCipherObj.doFinal(input);
        } catch (InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // AES Cryptography (Modern Symmetric Standard)
    // -------------------------------------------------------------------------

    public static SecretKey getAESKey(String key) {
        if (key == null) return null;
        byte[] keyBytes = Arrays.copyOf(key.getBytes(StandardCharsets.UTF_8), 16); // 128-bit key
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] getAESCipher(SecretKey key, byte[] input) {
        if (key == null || input == null) return null;
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, key);
            return aesCipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getPlainBytesAES(SecretKey key, byte[] encryptedInput) {
        if (key == null || encryptedInput == null) return null;
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, key);
            return aesCipher.doFinal(encryptedInput);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Nonce Handling
    // -------------------------------------------------------------------------

    public static int getNonce() {
        Random rand = new Random();
        return rand.nextInt(100);
    }

    public static int getSecureNonce() {
        SecureRandom rand = new SecureRandom();
        return rand.nextInt(1000000);
    }

    public static boolean confirmNonce(int correctNonce, int recievedNonce) {
        if (correctNonce == recievedNonce) {
            System.out.println("The nonce is correct and confirmed: [" + correctNonce + "]\n");
            return true;
        } else {
            System.out.println("The nonce is incorrect (" + correctNonce + " != " + recievedNonce + "), disconnecting...\n");
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // RSA Cryptography (Asymmetric Key Exchange)
    // -------------------------------------------------------------------------

    public static KeyPair getKeyPair() {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);
            return keyGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getRSACipher(Key rsaKey, byte[] plainBytes) {
        if (rsaKey == null || plainBytes == null) return null;
        try {
            Cipher rsaCipherObj = Cipher.getInstance("RSA");
            rsaCipherObj.init(Cipher.ENCRYPT_MODE, rsaKey);
            return rsaCipherObj.doFinal(plainBytes);
        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static byte[] getPlainTextRSA(Key rsaKey, byte[] plainBytes) {
        if (rsaKey == null || plainBytes == null) return null;
        try {
            Cipher rsaCipherObj = Cipher.getInstance("RSA");
            rsaCipherObj.init(Cipher.DECRYPT_MODE, rsaKey);
            return rsaCipherObj.doFinal(plainBytes);
        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Debug & Formatted Console Output
    // -------------------------------------------------------------------------

    public static void printRecievedCipher(byte[] recievedCipher) {
        System.out.println("The following cipher was received: ");
        System.out.println("  Cipher hex preview: " + bytesToHex(recievedCipher));
        System.out.println("  Cipher payload size: " + (recievedCipher != null ? recievedCipher.length : 0) + " bytes\n");
    }

    public static void printRecievedDecryption(byte[] decryptedOutput) {
        System.out.println("Decrypting Cipher ...");
        System.out.println("  Decrypted bytes (hex): " + bytesToHex(decryptedOutput));
        System.out.println("  Decrypted string format: " + (decryptedOutput != null ? new String(decryptedOutput) : "null") + "\n");
    }

    public static void printMessageSent(String plainText, byte[] cipherSent) {
        System.out.println("Encrypting and sending the following: " + plainText);
        System.out.println("  Encrypted bytes (hex): " + bytesToHex(cipherSent));
        System.out.println("  Encrypted payload size: " + (cipherSent != null ? cipherSent.length : 0) + " bytes\n");
    }

    // -------------------------------------------------------------------------
    // Stream Utilities
    // -------------------------------------------------------------------------

    public static void sendCipherImageDES(SecretKey key, String filePath, Socket socket) {
        try (FileInputStream in = new FileInputStream(filePath);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            Cipher enc = Cipher.getInstance("DES");
            enc.init(Cipher.ENCRYPT_MODE, key);
            CipherOutputStream outstream = new CipherOutputStream(out, enc);
            byte[] buffer = new byte[1024];
            int duration;
            while ((duration = in.read(buffer)) != -1) {
                outstream.write(buffer, 0, duration);
            }
            outstream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void recieveCipherImageDES(SecretKey key, String filePath) {
        try (FileInputStream in = new FileInputStream(filePath);
             FileOutputStream out = new FileOutputStream("output1.jpg")) {
            Cipher enc = Cipher.getInstance("DES");
            enc.init(Cipher.DECRYPT_MODE, key);
            CipherOutputStream outstream = new CipherOutputStream(out, enc);
            byte[] buffer = new byte[1024];
            int duration;
            while ((duration = in.read(buffer)) != -1) {
                outstream.write(buffer, 0, duration);
            }
            outstream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
