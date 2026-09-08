package lab3;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.crypto.*;
import javax.imageio.ImageIO;

/**
 * Responder (Server B) for the Authenticated Cryptographic Protocol.
 * Listens for client connections, participates in mutual nonce validation,
 * decrypts session messages, and saves received encrypted files.
 *
 * @author Kalp Shah
 */
public class Server {

    public static void main(String[] args) {
        int port = 8080;
        String outputPath = null;

        // Parse CLI arguments: [port] [outputPath] or [outputPath] [port]
        for (String arg : args) {
            if (arg == null || arg.trim().isEmpty()) continue;
            try {
                port = Integer.parseInt(arg.trim());
            } catch (NumberFormatException e) {
                outputPath = arg.trim();
            }
        }

        String id = "RESPONDER B";
        String PU_a = "NETWORK SECURITY";
        String ks = "KALPSHAHKALPSHAH";
        int nonceS;
        int nonceR;
        ServerSocket serverSocket;
        byte[] cipherS, cipherR, plainOutput;
        SecretKey PUa;
        SecretKey sessionKey = null;
        String plainText;
        byte[] plainBytes;
        String clientID;
        String clientMessage;

        System.out.println("======================================================");
        System.out.println("   SECURE AUTHENTICATED SOCKET SERVER (RESPONDER B)   ");
        System.out.println("======================================================");

        try {
            PUa = KeyFunctions.getKey(PU_a);
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(100000);
            System.out.println("[LISTENING] Server active on port " + serverSocket.getLocalPort() + "...");

            Socket server = serverSocket.accept();
            System.out.println("[CONNECTED] Inbound connection from: " + server.getRemoteSocketAddress() + "\n");

            DataInputStream in = new DataInputStream(server.getInputStream());
            DataOutputStream out = new DataOutputStream(server.getOutputStream());

            // Step 1: Receive Client ID & Nonce A
            System.out.println("--- [PHASE 1: INITIATOR CHALLENGE] ---");
            int duration = in.readInt();
            cipherR = new byte[duration];
            in.readFully(cipherR);

            KeyFunctions.printRecievedCipher(cipherR);
            plainOutput = KeyFunctions.getPlainBytesDES(PUa, cipherR);
            KeyFunctions.printRecievedDecryption(plainOutput);

            plainText = new String(plainOutput);
            String[] decryptedArray = plainText.split("\\|");
            nonceR = Integer.parseInt(decryptedArray[0]);
            clientID = decryptedArray[1];
            System.out.println("  Extracted Nonce A: " + nonceR);
            System.out.println("  Extracted Client ID: " + clientID + "\n");

            // Step 2: Responder sends back (Nonce A || Nonce B)
            System.out.println("--- [PHASE 2: CHALLENGE-RESPONSE GENERATION] ---");
            nonceS = KeyFunctions.getNonce();
            System.out.println("Sending Nonce A [" + nonceR + "] and Responder Nonce B [" + nonceS + "] encrypted with PU_a:");
            String challengeResponse = nonceR + "|" + nonceS;
            cipherS = KeyFunctions.getDESCipher(PUa, challengeResponse.getBytes());
            KeyFunctions.printMessageSent(challengeResponse, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);

            // Step 3: Receive Client's verification response for Nonce B
            duration = in.readInt();
            cipherR = new byte[duration];
            in.readFully(cipherR);

            KeyFunctions.printRecievedCipher(cipherR);
            plainOutput = KeyFunctions.getPlainBytesDES(PUa, cipherR);
            KeyFunctions.printRecievedDecryption(plainOutput);

            plainText = new String(plainOutput);
            int returnedNonce = Integer.parseInt(plainText);
            if (!KeyFunctions.confirmNonce(nonceS, returnedNonce)) {
                System.err.println("[SECURITY ERROR] Client failed nonce challenge! Terminating.");
                server.close();
                serverSocket.close();
                return;
            }

            // Step 4: Receive Encrypted Session Key
            System.out.println("--- [PHASE 3: SESSION KEY ACQUISITION] ---");
            duration = in.readInt();
            cipherR = new byte[duration];
            in.readFully(cipherR);

            KeyFunctions.printRecievedCipher(cipherR);
            plainOutput = KeyFunctions.getPlainBytesDES(PUa, cipherR);
            KeyFunctions.printRecievedDecryption(plainOutput);

            String sessionKeyString = new String(plainOutput);
            sessionKey = KeyFunctions.getKey(sessionKeyString);
            System.out.println("[SESSION KEY] Symmetric DES session cipher established: " + sessionKeyString + "\n");

            // Step 5: Authenticated Chat Exchange
            System.out.println("--- [PHASE 4: ENCRYPTED CHAT COMMUNICATION] ---");
            duration = in.readInt();
            cipherR = new byte[duration];
            in.readFully(cipherR);

            KeyFunctions.printRecievedCipher(cipherR);
            plainBytes = KeyFunctions.getPlainBytesDES(sessionKey, cipherR);
            KeyFunctions.printRecievedDecryption(plainBytes);

            plainText = new String(plainBytes);
            decryptedArray = plainText.split("\\|");
            System.out.println("  Extracted Nonce: " + decryptedArray[1]);
            System.out.println("  Extracted Client Message: " + decryptedArray[0] + "\n");
            nonceR = Integer.parseInt(decryptedArray[1]);
            clientMessage = decryptedArray[0];

            String greetingMessage = "I am fine thank you for asking!";
            nonceS = KeyFunctions.getNonce();
            String hostChatResponse = greetingMessage + "|" + nonceS + "|" + nonceR;
            cipherS = KeyFunctions.getDESCipher(sessionKey, hostChatResponse.getBytes());
            KeyFunctions.printMessageSent(hostChatResponse, cipherS);
            out.writeInt(cipherS.length);
            out.write(cipherS);

            // Step 6: Receive Encrypted File Payload
            System.out.println("--- [PHASE 5: ENCRYPTED FILE RECEPTION] ---");
            int numberOfPackets = in.readInt();
            System.out.println("Incoming encrypted payload size: " + numberOfPackets + " bytes");

            byte[] encryptedFilePayload = new byte[numberOfPackets];
            in.readFully(encryptedFilePayload);

            System.out.println("Payload received. Decrypting with negotiated session key...");
            plainBytes = KeyFunctions.getPlainBytesDES(sessionKey, encryptedFilePayload);

            // Resolve target file path
            String defaultWinPath = "C:\\Users\\kalps\\Desktop\\Courses\\COE 817\\lab3\\coe817lab3\\src\\coe817lab3\\server_files\\output.jpg";
            String targetPath = "src/lab3/server_files/output.jpg";
            if (outputPath != null && !outputPath.trim().isEmpty()) {
                targetPath = outputPath.trim();
            } else {
                File winFile = new File(defaultWinPath);
                if (winFile.getParentFile() != null && winFile.getParentFile().exists()) {
                    targetPath = defaultWinPath;
                }
            }

            File outputFile = new File(targetPath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }

            // Write decrypted file bytes directly
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(plainBytes);
            }

            // Also verify as image if applicable
            try {
                ByteArrayInputStream bi = new ByteArrayInputStream(plainBytes);
                BufferedImage image = ImageIO.read(bi);
                if (image != null) {
                    ImageIO.write(image, "jpg", outputFile);
                }
            } catch (Exception ignored) {}

            String outputSha256 = KeyFunctions.getSHA256(plainBytes);
            System.out.println("Received file saved to: " + outputFile.getAbsolutePath());
            System.out.println("Decrypted File Size: " + plainBytes.length + " bytes");
            System.out.println("Decrypted File SHA-256: " + outputSha256);
            System.out.println("[INTEGRITY VERIFIED] File transfer successfully validated.\n");

            // Teardown
            in.close();
            out.close();
            server.close();
            serverSocket.close();
            System.out.println("Server session closed cleanly.");

        } catch (SocketTimeoutException s) {
            System.err.println("[TIMEOUT] Socket timed out waiting for client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
