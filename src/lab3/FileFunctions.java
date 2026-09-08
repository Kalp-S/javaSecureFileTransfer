package lab3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * File I/O and integrity verification helper functions.
 *
 * @author Kalp Shah
 */
public class FileFunctions {

    /**
     * Reads the entire content of a file into a byte array safely.
     */
    public static byte[] getFile(String filepath) {
        if (filepath == null) return new byte[0];
        File file = new File(filepath).getAbsoluteFile();
        if (!file.exists() || !file.isFile()) {
            System.err.println("[WARN] File not found or is not a regular file: " + filepath);
            return new byte[0];
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int totalBytesRead = 0;
            while (totalBytesRead < data.length) {
                int read = fis.read(data, totalBytesRead, data.length - totalBytesRead);
                if (read == -1) break;
                totalBytesRead += read;
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Verifies that a file matches an expected SHA-256 hash.
     */
    public static boolean verifyChecksum(File file, String expectedHash) {
        if (file == null || expectedHash == null) return false;
        String actualHash = KeyFunctions.getSHA256(file);
        boolean matches = expectedHash.equalsIgnoreCase(actualHash);
        if (matches) {
            System.out.println("[VERIFICATION] SHA-256 matches expected checksum: " + actualHash);
        } else {
            System.err.println("[VERIFICATION FAILED] Expected: " + expectedHash + ", Got: " + actualHash);
        }
        return matches;
    }
}
