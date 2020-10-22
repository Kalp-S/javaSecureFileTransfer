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
import java.io.*;
import java.net.*;
import javax.crypto.*;

public class Client {
     public static void main(String[] args)
    {
        byte[]  cipherS, cipherR = null, 
                plainOutput = null;
        String id = "INITIATOR A";
        String host = "localhost";
        String PU_b = "NETWORK SECURITY";
        String K_s = "KALPSHAHKALPSHAH";
        String filePath = "C:\\Users\\kalps\\Desktop\\Courses\\COE 817\\lab3\\coe817lab3\\src\\coe817lab3\\client_files\\"
                + "input.jpg";
        int port = 8080;
        SecretKey sessionKey;
        SecretKey PUb;
        String plainText;
        int nonceS, nonceR, nonceTemp;
        byte[] plainBytes;
        
        try {
             System.out.println("CLIENT");
            // Create key out of the string "NETWORK SECURITY".
            // This uses the DESKeySpec to create a key from text.
            PUb = KeyFunctions.getKey(PU_b);
            // Generate Nonce.
            nonceS = KeyFunctions.getNonce();
            // notify client of attempt to connect
            System.out.println("Connecting to " + host
                    + " on port " + port);
            // attempt the connection to socket.
            Socket client = new Socket(host, port);
            //report connection success to client.
            System.out.println("Connected to "
                    + client.getRemoteSocketAddress() +" success!" + "\n" );
            // print ID message sent.
            System.out.println("Sending client ID and nonce encrypted with B's"
                    + " public key PU_b " + "to host: \n"
                    + "Nonce Generated: " + nonceS + "\n"
                            + "Client Id: " + id + " \n");
            // send ID to host.
            DataOutputStream out =
                    new DataOutputStream(client.getOutputStream());
            plainText = nonceS + "|" + id;
            cipherS = KeyFunctions.getDESCipher(PUb, 
                    plainText.getBytes());
            // send initial message.
            KeyFunctions.printMessageSent(plainText, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);
            
//-------------RECIEVE CIPHER FROM HOST THAT CONTAINS Nonces---------------
            //Recieve cipher from host.
            DataInputStream in =
                        new DataInputStream(client.getInputStream());
            // Read in length of incoming bytes.
            int duration = in.readInt();
            // initialize byte array to contain incoming byte stream.
            if(duration > 0) cipherR = new byte[duration];
            in.read(cipherR, 0, duration);
            
            // print encrypted cipher message to standardout.
            KeyFunctions.printRecievedCipher(cipherR);
            // print decrypted message to standardout
            plainOutput = KeyFunctions.getPlainBytesDES(PUb, cipherR);
            KeyFunctions.printRecievedDecryption(plainOutput);
            
//-----------Using recieved Nonce to send back for authorization ---------------
            // parse incoming ciphertext for session key using regex.
            plainText = new String(plainOutput);
            String[] decryptedArray = plainText.split("\\|");

            // get host's nonce from the decrpyted text.
            nonceR = Integer.parseInt(decryptedArray[1]);
            // get authorization nonce;
            nonceTemp = Integer.parseInt(decryptedArray[0]);
            if(KeyFunctions.confirmNonce(nonceS, nonceTemp)){
                // send to client the length of cipher in bytes, then the cipher
                System.out.println("Send Nonce recieved from host for "
                    + "idenitifaction encrypted using PU_b\n");
                cipherS = KeyFunctions.getDESCipher(PUb, 
                        decryptedArray[1].getBytes());
                KeyFunctions.printMessageSent((String)decryptedArray[1], 
                        cipherS);
                out.writeInt(cipherS.length);
                out.write(cipherS);
            }else { return;}     
////-----------------Send the encrypted secret key. ----------------------------
            
            // Create key out of the string recieved session key.
            // This uses the DESKeySpec to create a key from text.
            sessionKey = KeyFunctions.getKey((K_s));
            cipherS = KeyFunctions.getDESCipher(PUb, K_s.getBytes());
            // send to client the length of cipher in bytes, then the cipher.
            System.out.println("Send secret session key which is a DES key"
                    + " created\n");
            KeyFunctions.printMessageSent(K_s, 
                        cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);
//----------------------session chat messages example --------------------------
            String greetingMessage = "Hello how are you?";
            nonceS = KeyFunctions.getNonce();
            greetingMessage = greetingMessage + "|" + nonceS;
            cipherS = KeyFunctions.getDESCipher(sessionKey, 
                    greetingMessage.getBytes());
            KeyFunctions.printMessageSent(greetingMessage, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);
//-------------------- Chat instances example-----------------------------------
            duration = in.readInt();
            // initialize byte array to contain incoming byte stream.
            if(duration > 0) cipherR = new byte[duration];
            in.read(cipherR, 0, duration);
            // print encrypted cipher recieved.
            KeyFunctions.printRecievedCipher(cipherR);
            plainBytes = KeyFunctions.getPlainBytesDES(sessionKey, 
                    cipherR);
            KeyFunctions.printRecievedDecryption(plainBytes);
            
            //parse incoming ciphertext for session key using regex.
            plainText = new String(plainBytes);
            decryptedArray = plainText.split("\\|");
            // Create key out of the string recieved session key.
            // This uses the DESKeySpec to create a key from text.
            System.out.println("Extracted Host Nonce: " + 
                    decryptedArray[1] + "\n" + "Extracted authorization Nonce:"
                            + decryptedArray[2]);
            System.out.println("Extracted Client Message: " + decryptedArray[0] 
                    + "\n");
            nonceR = Integer.parseInt(decryptedArray[1]);
            nonceTemp = Integer.parseInt(decryptedArray[2]);
            String hostMessage = decryptedArray[0];
            KeyFunctions.confirmNonce(nonceTemp, nonceS);
//-------------------------- Send encrypted image ------------------------------
            File file = new File(filePath);
            FileInputStream stream = new FileInputStream(file);
            byte[] b = new byte[(int)file.length()];
            stream.read(b);
            cipherS = KeyFunctions.getDESCipher(sessionKey,b);
            out.writeInt(cipherS.length);
            out.write(cipherS);
//-------------------------------close all connections--------------------------
            in.close();
            out.close();
            client.close(); 
//-------------------------------close all connections--------------------------

         } catch(IOException ex) {
            ex.printStackTrace();
        } 
    }
}
