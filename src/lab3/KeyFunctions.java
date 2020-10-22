/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab3;

/**
 *
 * @author kalps
 */

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class KeyFunctions {
    public static SecretKey getKey(String key){
         byte[] keyB =  key.getBytes();
         try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            SecretKey desKey = factory.generateSecret(new DESKeySpec(keyB));
            return desKey;
         }catch(InvalidKeyException | NoSuchAlgorithmException |
                InvalidKeySpecException ex){
         }
         return null;
    }
    
    public static byte[] getPlainBytesDES(SecretKey key, byte[] encryptedInput){
         Cipher desCipherObj;
         try {
             desCipherObj = Cipher.getInstance("DES");
             desCipherObj.init(Cipher.DECRYPT_MODE, key);
             byte[] decryptedOutput = desCipherObj.doFinal(encryptedInput);
             return decryptedOutput;
         } catch(InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException ex) {
        } 
         return null;
     }
     
    public static byte[] getDESCipher(SecretKey key, byte[] input){
         Cipher desCipherObj;
         try {
             desCipherObj = Cipher.getInstance("DES");
             desCipherObj.init(Cipher.ENCRYPT_MODE, key);
             byte[] encryptedOutput = desCipherObj.doFinal(input);
             return encryptedOutput;
         } catch(InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException ex) {
        } 
         return null;
     }
      
    public static int getNonce(){
         Random rand = new Random();
         int upperbound = 100;
         int nonce = rand.nextInt(upperbound);
         return nonce;
     }
     
    public static void printRecievedCipher(byte[] recievedCipher){
        System.out.println("The following cipher was recieved: ");
        System.out.println("Recieved cipher byte code: " + 
                    recievedCipher.toString());
        System.out.println("Recieved cipher string format: " + 
                    new String(recievedCipher) + "\n");
     }
     
    public static void printRecievedDecryption(byte[] decryptedOutput){
           System.out.println("Decrypting Cipher ...");
            // decrypt the message
            //print to screen the decrypted message byte code.
            System.out.println("Decrypted byte code " + 
                    decryptedOutput.toString());
            System.out.println("Decrypted string format: " + 
                    new String(decryptedOutput) + "\n");
     }
    public static void printMessageSent(String plainText, byte[] cipherSent){
                 //create plaintext version of message to be encrypted. 
            System.out.println("Encrypting and sending the following: " +
                    plainText);
            //Display on server side the encrypted Cipher.
            System.out.println("Sending Encrypted byte code " + 
                    cipherSent.toString() + "\n");
            System.out.println("Sending Encrypted string format: " + 
                    new String(cipherSent)+ "\n");
    }
      
    public static boolean confirmNonce(int correctNonce, int recievedNonce){
        if(correctNonce == recievedNonce){
            System.out.println("The nonce is correct and confirmed.\n");
            return true;
        }else {
            System.out.println("The nonce is incorrect disconnecting...\n");
            return false;
        }
    }   
    public static KeyPair getKeyPair(){
        // A key pair generator is used to generate private/public 
        KeyPairGenerator keyGenerator;
        KeyPair rsaKeyPair = null;
        try{ 
        //keys for assymetric algorithms.
            keyGenerator = KeyPairGenerator.getInstance("RSA");
            rsaKeyPair = keyGenerator.generateKeyPair(); return rsaKeyPair;
        }catch (NoSuchAlgorithmException e) {
	}
         return rsaKeyPair;
    }
    public static byte[] getRSACipher(Key rsaKey, byte[] plainBytes){
        
        byte[] cipherOutput = null;
        Cipher rsaCipherObj;
        
        try {
        // instantiate the Cipher object passing the algorithm used.
        rsaCipherObj = Cipher.getInstance("RSA");
        // set the cipher object to encrypt operation mode and initialize.
        rsaCipherObj.init(Cipher.ENCRYPT_MODE, rsaKey);
        // encrypt the message
        cipherOutput = rsaCipherObj.doFinal(plainBytes);   
        }catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | 
				IllegalBlockSizeException | BadPaddingException e) {
			System.out.println(e.getMessage());
        }
        return cipherOutput;
    }
     public static byte[] getPlainTextRSA(Key rsaKey, byte[] plainBytes){
        
        byte[] plainTextOutput = null;
        Cipher rsaCipherObj;
        
        try {
        // instantiate the Cipher object passing the algorithm used.
        rsaCipherObj = Cipher.getInstance("RSA");
        // set the cipher object to encrypt operation mode and initialize.
        rsaCipherObj.init(Cipher.DECRYPT_MODE, rsaKey);
        // encrypt the message
        plainTextOutput = rsaCipherObj.doFinal(plainBytes);   
        }catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | 
				IllegalBlockSizeException | BadPaddingException e) {
			System.out.println(e.getMessage());
        }
        return plainTextOutput;
    }
    
     public static void sendCipherImageDES(SecretKey key, String filePath,
             Socket socket){
        try {
            FileInputStream in = new FileInputStream(filePath);
            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());
            Cipher enc = Cipher.getInstance("DES");
            enc.init(Cipher.ENCRYPT_MODE, key);
            CipherOutputStream outstream = new CipherOutputStream(out, enc);
            byte[] buffer = new byte [1024];
            int duration;
            while((duration=in.read(buffer)) != -1) {
                outstream.write(buffer, 0, duration);
            }
            in.close();
            outstream.flush();
            out.close();
        } catch (FileNotFoundException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidKeyException ex) {
        } catch (IOException ex) {
        }
     }
     
     public static void recieveCipherImageDES(SecretKey key, String filePath){
        try {
            FileInputStream in = new FileInputStream(filePath);
            FileOutputStream out = new FileOutputStream("output1.jpg");
            Cipher enc = Cipher.getInstance("DES");
            enc.init(Cipher.DECRYPT_MODE, key);
            CipherOutputStream outstream = new CipherOutputStream(out, enc);
            byte[] buffer = new byte [1024];
            int duration;
            while((duration=in.read(buffer)) != -1) {
                outstream.write(buffer, 0, duration);
            }
            in.close();
            outstream.flush();
            out.close();
        } catch (FileNotFoundException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidKeyException ex) {
        } catch (IOException ex) {
        }
     }
}
