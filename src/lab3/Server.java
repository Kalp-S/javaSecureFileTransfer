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

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.*;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Server {
    public static void main(String [] args)
    {
        int port = 8080;
        String id = "RESPONDER B";
        String PU_a = "NETWORK SECURITY";
        String ks = "KALPSHAHKALPSHAH";
        int nonceS;
        int nonceR;
        ServerSocket serverSocket;
        byte[] cipherS, cipherR = null, plainOutput;
        SecretKey PUa;
        SecretKey sessionKey;
        String plainText;
        byte[] plainBytes;
        FileFunctions fu = new FileFunctions();
        String clientID;
        String clientMessage;
        try { 
// Setup initial socket connection and recieve requests from clients, 
// recieve ID and nonce.
            System.out.println("SERVER");
            // reserve socket and set timeout to ensure socket is closed.
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(100000);
            // display waiting message for server side.
            System.out.println("Waiting for client on port " +
            serverSocket.getLocalPort() + "...");

            // Print when client connects to socket.
            Socket server = serverSocket.accept();
            // Print when client connects to socket.
            System.out.println("Connected to "
                    + server.getRemoteSocketAddress()+ "\n");

            // Print recieved input stream from socket.
            DataInputStream in =
                    new DataInputStream(server.getInputStream());
             int duration = in.readInt();
            // initialize byte array to contain incoming byte stream.
            if(duration > 0) cipherR = new byte[duration];
            in.read(cipherR, 0, duration);
            // print encrypted cipher recieved.
            KeyFunctions.printRecievedCipher(cipherR);
            PUa = KeyFunctions.getKey(PU_a);
            plainBytes = KeyFunctions.getPlainBytesDES(PUa, 
                    cipherR);
            KeyFunctions.printRecievedDecryption(plainBytes);
            
            //parse incoming ciphertext for session key using regex.
            plainText = new String(plainBytes);
            String[] decryptedArray = plainText.split("\\|");
            // Create key out of the string recieved session key.
            // This uses the DESKeySpec to create a key from text.
            System.out.println("Extracted Nonce: " + decryptedArray[0]);
            System.out.println("Extracted Client ID: " + decryptedArray[1] + 
                    "\n");
            nonceR = Integer.parseInt(decryptedArray[0]);
            clientID = decryptedArray[1];
            
 // Generate Nonce N2, and send to client the encrypted output from step 2.
            nonceS = KeyFunctions.getNonce();
            plainText = nonceR + "|" + nonceS;
            System.out.println("Sending client's nonce and host's nonce"
                    + " encrypted with public key PU_a " + "to client: \n"
                    + "Nonce Generated: " + nonceS + "\n");
            plainBytes = plainText.getBytes();
            
            cipherS = KeyFunctions.getDESCipher(PUa, plainBytes);
            KeyFunctions.printMessageSent(plainText, cipherS);
            
             DataOutputStream out =
                new DataOutputStream(server.getOutputStream());
            out.writeInt(cipherS.length);
            out.write(cipherS);

// Recieve an authorization nonce from the client which confirms identity-------
            duration = in.readInt();
            // initialize byte array to contain incoming byte stream.
            if(duration > 0) cipherR = new byte[duration];
            in.read(cipherR, 0, duration);
            // print encrypted cipher recieved.
            KeyFunctions.printRecievedCipher(cipherR);
            plainBytes = KeyFunctions.getPlainBytesDES(PUa, 
                    cipherR);
            KeyFunctions.printRecievedDecryption(plainBytes);
            nonceR = Integer.parseInt(new String (plainBytes));
            if(KeyFunctions.confirmNonce(nonceS, nonceR)){
                // send to client the session key.
            }else { return;}
//-----------Using recieved Nonce to send back for authorization ---------------
            duration = in.readInt();
            // initialize byte array to contain incoming byte stream.
            if(duration > 0) cipherR = new byte[duration];
            in.read(cipherR, 0, duration);
            // print encrypted cipher recieved.
            KeyFunctions.printRecievedCipher(cipherR);
            plainBytes = KeyFunctions.getPlainBytesDES(PUa, 
                    cipherR);
            plainText = new String(plainBytes);
            sessionKey = KeyFunctions.getKey(plainText);
            KeyFunctions.printRecievedDecryption(plainBytes);
            System.out.println("The secret session key has been created.");
            System.out.println("Ready for communication....\n");
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
            System.out.println("Extracted Nonce: " + decryptedArray[1]);
            System.out.println("Extracted Client Message: " + decryptedArray[0] 
                    + "\n");
            nonceR = Integer.parseInt(decryptedArray[1]);
            clientMessage = decryptedArray[0];
//----------------------session chat response messages example ----------------
            String greetingMessage = "I am fine thank you for asking!";
            nonceS = KeyFunctions.getNonce();
            greetingMessage = greetingMessage + "|" + nonceS + "|" + 
                    nonceR;
            cipherS = KeyFunctions.getDESCipher(sessionKey, 
                    greetingMessage.getBytes());
            KeyFunctions.printMessageSent(greetingMessage, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);
// ---------------------------------------------------------------------------//
            int numberOfPackets = in.readInt();
            System.out.println("The number of incoming packets are " + 
                    numberOfPackets);
            int i = 0;
            //int buffersize = 0;
            byte[] temp = new byte[numberOfPackets];
                in.read(temp, 0, temp.length);
                // print encrypted cipher recieved.
                //KeyFunctions.printRecievedCipher(cipherR);
                plainBytes = KeyFunctions.getPlainBytesDES(sessionKey, 
                    temp);
                ByteArrayInputStream bi = new ByteArrayInputStream(plainBytes);
                BufferedImage image = ImageIO.read(bi);
                File file = new File("C:\\Users\\kalps\\Desktop\\Courses\\COE 817\\lab3\\coe817lab3\\src\\coe817lab3\\server_files\\"
                + "output.jpg");
                ImageIO.write(image,"jpg",file);
            in.close();
            out.close();
            server.close();
        }catch(SocketTimeoutException s){
        }catch (IOException e) {  
           e.printStackTrace();
        }
    }
}
