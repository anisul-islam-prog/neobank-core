package com.neobank.cards.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encryption service for secure card data storage.
 * Uses AES-256-GCM symmetric encryption for card numbers and CVVs.
 */
@Component
class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    /**
     * 256-bit encryption key (32 characters).
     * Must be set via environment variable in production.
     */
    @Value("${encryption.key:DefaultEncryptionKey32Chars!!}")
    private String encryptionKey;

    /**
     * GCM tag length in bits.
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM IV length in bytes.
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * AES algorithm name.
     */
    private static final String AES_ALGORITHM = "AES";

    /**
     * AES/GCM/NoPadding transformation.
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * Encrypt plaintext data.
     *
     * @param plaintext the data to encrypt
     * @return base64-encoded ciphertext with IV prepended
     */
    String encrypt(String plaintext) {
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext for storage
            byte[] ciphertextWithIv = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, ciphertextWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, ciphertextWithIv, GCM_IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(ciphertextWithIv);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt ciphertext data.
     *
     * @param ciphertext base64-encoded ciphertext with IV prepended
     * @return the decrypted plaintext
     */
    String decrypt(String ciphertext) {
        try {
            byte[] ciphertextWithIv = Base64.getDecoder().decode(ciphertext);

            // Extract IV from the beginning
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(ciphertextWithIv, 0, iv, 0, GCM_IV_LENGTH);

            // Extract actual ciphertext
            byte[] encrypted = new byte[ciphertextWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(ciphertextWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), parameterSpec);

            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Get the secret key from the encryption key string.
     */
    private SecretKeySpec getSecretKey() {
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    /**
     * Generate a random IV.
     */
    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
