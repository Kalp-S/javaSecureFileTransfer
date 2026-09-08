package lab3;

import java.io.*;
import java.net.*;
import javax.crypto.*;

/**
 * Initiator (Client A) for the Authenticated Cryptographic Protocol.
 * Performs mutual challenge-response authentication, session key exchange,
 * encrypted messaging, and secure binary file transfer.
 *
 * @author Kalp Shah
 */
public class Client {

    public static void main(String[] args) {
        int port = 8080;
        String host = "localhost";
        String defaultWinPath = "C:\\Users\\kalps\\Desktop\\Courses\\COE 817\\lab3\\coe817lab3\\src\\coe817lab3\\client_files\\input.jpg";
        String filePath = defaultWinPath;

        // Parse CLI arguments: [input_file] [host] [port]
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            filePath = args[0].trim();
        } else if (!new File(defaultWinPath).exists()) {
            File rel1 = new File("src/lab3/client_files/input.jpg");
            File rel2 = new File("client_files/input.jpg");
            if (rel1.exists()) {
                filePath = rel1.getPath();
            } else if (rel2.exists()) {
                filePath = rel2.getPath();
            }
        }

        if (args.length > 1 && !args[1].trim().isEmpty()) {
            host = args[1].trim();
        }
        if (args.length > 2 && !args[2].trim().isEmpty()) {
            try {
                port = Integer.parseInt(args[2].trim());
            } catch (NumberFormatException ignored) {}
        }

        byte[] cipherS, cipherR;
        byte[] plainOutput;
        String id = "INITIATOR A";
        String PU_b = "NETWORK SECURITY";
        String K_s = "KALPSHAHKALPSHAH";
        SecretKey sessionKey;
        SecretKey PUb;
        String plainText;
        int nonceS, nonceR, nonceTemp;
        byte[] plainBytes;

        System.out.println("======================================================");
        System.out.println("   SECURE AUTHENTICATED SOCKET CLIENT (INITIATOR A)   ");
        System.out.println("======================================================");

        try {
            // Step 1: Setup Public Key & Nonce
            PUb = KeyFunctions.getKey(PU_b);
            nonceS = KeyFunctions.getNonce();

            System.out.println("[CONNECTING] Connecting to " + host + " on port " + port + "...");
            Socket client = new Socket(host, port);
            System.out.println("[CONNECTED] Remote socket: " + client.getRemoteSocketAddress() + "\n");

            // Step 2: Send Client ID and Nonce encrypted under Responder's public key PU_b
            System.out.println("--- [PHASE 1: AUTHENTICATION & CHALLENGE] ---");
            System.out.println("Sending Client ID & Nonce encrypted with PU_b to host:");
            System.out.println("  Client ID: " + id);
            System.out.println("  Generated Nonce A: " + nonceS + "\n");

            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            plainText = nonceS + "|" + id;
            cipherS = KeyFunctions.getDESCipher(PUb, plainText.getBytes());
            KeyFunctions.printMessageSent(plainText, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);

            // Step 3: Receive Host's Response containing both Nonces
            System.out.println("--- [PHASE 2: MUTUAL CHALLENGE VERIFICATION] ---");
            DataInputStream in = new DataInputStream(client.getInputStream());
            int duration = in.readInt();
            cipherR = new byte[duration];
            in.readFully(cipherR);

            KeyFunctions.printRecievedCipher(cipherR);
            plainOutput = KeyFunctions.getPlainBytesDES(PUb, cipherR);
            KeyFunctions.printRecievedDecryption(plainOutput);

            plainText = new String(plainOutput);
            String[] decryptedArray = plainText.split("\\|");

            nonceR = Integer.parseInt(decryptedArray[1]);
            nonceTemp = Integer.parseInt(decryptedArray[0]);

            if (KeyFunctions.confirmNonce(nonceS, nonceTemp)) {
                System.out.println("Sending back Host Nonce for mutual identity verification (encrypted with PU_b)...");
                cipherS = KeyFunctions.getDESCipher(PUb, decryptedArray[1].getBytes());
                KeyFunctions.printMessageSent(decryptedArray[1], cipherS);
                out.writeInt(cipherS.length);
                out.write(cipherS);
            } else {
                System.err.println("[SECURITY ERROR] Host failed nonce challenge! Aborting.");
                client.close();
                return;
            }

            // Step 4: Transmit Encrypted Symmetric Session Key K_s
            System.out.println("--- [PHASE 3: SESSION KEY AGREEMENT] ---");
            sessionKey = KeyFunctions.getKey(K_s);
            cipherS = KeyFunctions.getDESCipher(PUb, K_s.getBytes());
            System.out.println("Sending DES secret session key (encrypted with PU_b)...");
            KeyFunctions.printMessageSent(K_s, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);

            // Step 5: Authenticated Chat Exchange using Session Key
            System.out.println("--- [PHASE 4: ENCRYPTED CHAT COMMUNICATION] ---");
            String greetingMessage = "Hello how are you?";
            nonceS = KeyFunctions.getNonce();
            String chatPayload = greetingMessage + "|" + nonceS;
            cipherS = KeyFunctions.getDESCipher(sessionKey, chatPayload.getBytes());
            KeyFunctions.printMessageSent(chatPayload, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);

            duration = in.readInt();
            cipherR = new byte[duration];
            in.readFully(cipherR);

            KeyFunctions.printRecievedCipher(cipherR);
            plainBytes = KeyFunctions.getPlainBytesDES(sessionKey, cipherR);
            KeyFunctions.printRecievedDecryption(plainBytes);

            plainText = new String(plainBytes);
            decryptedArray = plainText.split("\\|");
            System.out.println("  Extracted Host Message: " + decryptedArray[0]);
            System.out.println("  Extracted Host Nonce: " + decryptedArray[1]);
            System.out.println("  Extracted Verification Nonce: " + decryptedArray[2]);

            nonceR = Integer.parseInt(decryptedArray[1]);
            nonceTemp = Integer.parseInt(decryptedArray[2]);
            KeyFunctions.confirmNonce(nonceTemp, nonceS);

            // Step 6: Transmit Encrypted Binary File
            System.out.println("--- [PHASE 5: ENCRYPTED FILE TRANSFER] ---");
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                System.err.println("[ERROR] Payload file not found: " + file.getAbsolutePath());
                client.close();
                return;
            }

            byte[] b = FileFunctions.getFile(filePath);
            String inputSha256 = KeyFunctions.getSHA256(b);
            System.out.println("Transmitting file: " + file.getName() + " (" + b.length + " bytes)");
            System.out.println("Source File SHA-256: " + inputSha256);

            cipherS = KeyFunctions.getDESCipher(sessionKey, b);
            System.out.println("Encrypted Payload Size: " + cipherS.length + " bytes");
            out.writeInt(cipherS.length);
            out.write(cipherS);
            out.flush();

            System.out.println("[SUCCESS] Encrypted file transmitted successfully!\n");

            // Teardown
            in.close();
            out.close();
            client.close();
            System.out.println("Session completed. Connection closed cleanly.");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
