package lab3;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;
import javax.crypto.SecretKey;

/**
 * Automated Cryptographic Verification Test Suite.
 * Validates DES/AES symmetric ciphers, RSA asymmetric ciphers, nonces, and SHA-256 digests.
 *
 * @author Kalp Shah
 */
public class CryptoTestSuite {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("     RUNNING CRYPTOGRAPHIC INTEGRITY TEST SUITE       ");
        System.out.println("======================================================");

        testDESRoundTrip();
        testAESRoundTrip();
        testRSAKeyPairAndCipher();
        testNonceGenerationAndVerification();
        testSHA256Checksum();
        testHexFormatting();

        System.out.println("------------------------------------------------------");
        System.out.println("Results: " + passedTests + "/" + totalTests + " tests passed.");
        if (passedTests == totalTests) {
            System.out.println("✔ ALL CRYPTOGRAPHIC TESTS PASSED SUCCESSFULLY!");
            System.exit(0);
        } else {
            System.err.println("✘ SOME TESTS FAILED.");
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testDESRoundTrip() {
        String testKey = "KALPSHAHKALPSHAH";
        String plaintext = "Senior Engineer Confidential Message 2026!";
        SecretKey desKey = KeyFunctions.getKey(testKey);

        assertTrue("DES: Key derived successfully", desKey != null);

        byte[] cipher = KeyFunctions.getDESCipher(desKey, plaintext.getBytes(StandardCharsets.UTF_8));
        assertTrue("DES: Cipher output is non-null and not equal to plaintext",
                cipher != null && !Arrays.equals(cipher, plaintext.getBytes(StandardCharsets.UTF_8)));

        byte[] decrypted = KeyFunctions.getPlainBytesDES(desKey, cipher);
        String decryptedStr = new String(decrypted, StandardCharsets.UTF_8);
        assertTrue("DES: Decrypted output matches original plaintext", plaintext.equals(decryptedStr));
    }

    private static void testAESRoundTrip() {
        String testKey = "SuperSecretAESKey123";
        String plaintext = "High throughput AES-128 payload validation test.";
        SecretKey aesKey = KeyFunctions.getAESKey(testKey);

        assertTrue("AES: 128-bit key generated", aesKey != null && "AES".equals(aesKey.getAlgorithm()));

        byte[] cipher = KeyFunctions.getAESCipher(aesKey, plaintext.getBytes(StandardCharsets.UTF_8));
        assertTrue("AES: Cipher output generated", cipher != null && cipher.length > 0);

        byte[] decrypted = KeyFunctions.getPlainBytesAES(aesKey, cipher);
        String decryptedStr = new String(decrypted, StandardCharsets.UTF_8);
        assertTrue("AES: Decrypted output matches original plaintext", plaintext.equals(decryptedStr));
    }

    private static void testRSAKeyPairAndCipher() {
        KeyPair keyPair = KeyFunctions.getKeyPair();
        assertTrue("RSA: KeyPair generated", keyPair != null && keyPair.getPublic() != null && keyPair.getPrivate() != null);

        String secretData = "Asymmetric RSA Handshake Challenge";
        byte[] cipher = KeyFunctions.getRSACipher(keyPair.getPublic(), secretData.getBytes(StandardCharsets.UTF_8));
        assertTrue("RSA: Cipher created with public key", cipher != null && cipher.length > 0);

        byte[] decrypted = KeyFunctions.getPlainTextRSA(keyPair.getPrivate(), cipher);
        String decryptedStr = new String(decrypted, StandardCharsets.UTF_8);
        assertTrue("RSA: Decrypted with private key matches original", secretData.equals(decryptedStr));
    }

    private static void testNonceGenerationAndVerification() {
        int nonce1 = KeyFunctions.getNonce();
        assertTrue("Nonce: Nonce within valid bound [0, 100)", nonce1 >= 0 && nonce1 < 100);

        boolean confirmPass = KeyFunctions.confirmNonce(nonce1, nonce1);
        assertTrue("Nonce: Matching nonces confirm positive", confirmPass);

        boolean confirmFail = KeyFunctions.confirmNonce(nonce1, nonce1 + 1);
        assertTrue("Nonce: Mismatched nonces rejected", !confirmFail);

        int secureNonce = KeyFunctions.getSecureNonce();
        assertTrue("Nonce: Secure random nonce generated", secureNonce >= 0);
    }

    private static void testSHA256Checksum() {
        String data = "Cryptographic integrity verification test data.";
        String hash1 = KeyFunctions.getSHA256(data.getBytes(StandardCharsets.UTF_8));
        String hash2 = KeyFunctions.getSHA256(data.getBytes(StandardCharsets.UTF_8));

        assertTrue("SHA-256: Digest is non-null and 64 hex characters", hash1 != null && hash1.length() == 64);
        assertTrue("SHA-256: Digests are deterministic", hash1.equals(hash2));

        String modifiedData = data + "x";
        String hashModified = KeyFunctions.getSHA256(modifiedData.getBytes(StandardCharsets.UTF_8));
        assertTrue("SHA-256: Avalanche effect ensures distinct hash for modified input", !hash1.equals(hashModified));
    }

    private static void testHexFormatting() {
        byte[] testBytes = new byte[]{0x0A, 0x1B, (byte) 0xFF};
        String hexPreview = KeyFunctions.bytesToHex(testBytes);
        assertTrue("Hex: Formatted uppercase preview matches", "0A 1B FF".equals(hexPreview));

        String fullHex = KeyFunctions.bytesToFullHex(testBytes);
        assertTrue("Hex: Full lowercase hex matches", "0a1bff".equals(fullHex));
    }
}
