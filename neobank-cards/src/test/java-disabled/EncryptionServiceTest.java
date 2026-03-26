package com.neobank.cards.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for EncryptionService using JUnit 5 and Mockito.
 * Tests AES-256-GCM encryption and decryption operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EncryptionService Unit Tests")
class EncryptionServiceTest {

    @InjectMocks
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        // Set test encryption key (exactly 32 characters for AES-256)
        ReflectionTestUtils.setField(encryptionService, "encryptionKey", "12345678901234567890123456789012");
    }

    @Nested
    @DisplayName("Encryption Operations")
    class EncryptionOperationsTests {

        @Test
        @DisplayName("Should encrypt card number successfully")
        void shouldEncryptCardNumberSuccessfully() {
            // Given
            String cardNumber = "4532015112830366";

            // When
            String encrypted = encryptionService.encrypt(cardNumber);

            // Then
            assertThat(encrypted).isNotNull();
            assertThat(encrypted).isNotEqualTo(cardNumber);
            assertThat(encrypted).isNotEmpty();
        }

        @Test
        @DisplayName("Should encrypt CVV successfully")
        void shouldEncryptCvvSuccessfully() {
            // Given
            String cvv = "123";

            // When
            String encrypted = encryptionService.encrypt(cvv);

            // Then
            assertThat(encrypted).isNotNull();
            assertThat(encrypted).isNotEqualTo(cvv);
        }

        @Test
        @DisplayName("Should produce different ciphertext for same plaintext")
        void shouldProduceDifferentCiphertextForSamePlaintext() {
            // Given
            String plaintext = "4532015112830366";

            // When
            String encrypted1 = encryptionService.encrypt(plaintext);
            String encrypted2 = encryptionService.encrypt(plaintext);

            // Then
            assertThat(encrypted1).isNotNull();
            assertThat(encrypted2).isNotNull();
            // GCM mode uses random IV, so ciphertext should be different
            assertThat(encrypted1).isNotEqualTo(encrypted2);
        }

        @Test
        @DisplayName("Should encrypt empty string")
        void shouldEncryptEmptyString() {
            // Given
            String emptyString = "";

            // When
            String encrypted = encryptionService.encrypt(emptyString);

            // Then
            assertThat(encrypted).isNotNull();
            assertThat(encrypted).isNotEmpty(); // IV is always present
        }

        @Test
        @DisplayName("Should encrypt string with special characters")
        void shouldEncryptStringWithSpecialCharacters() {
            // Given
            String specialString = "4532-0151-1283-0366";

            // When
            String encrypted = encryptionService.encrypt(specialString);

            // Then
            assertThat(encrypted).isNotNull();
        }

        @Test
        @DisplayName("Should encrypt string with unicode characters")
        void shouldEncryptStringWithUnicodeCharacters() {
            // Given
            String unicodeString = "用户测试";

            // When
            String encrypted = encryptionService.encrypt(unicodeString);

            // Then
            assertThat(encrypted).isNotNull();
        }

        @Test
        @DisplayName("Should encrypt long card number string")
        void shouldEncryptLongCardNumberString() {
            // Given
            String longString = "45320151128303661234567890123456";

            // When
            String encrypted = encryptionService.encrypt(longString);

            // Then
            assertThat(encrypted).isNotNull();
        }

        @Test
        @DisplayName("Should encrypt numeric string")
        void shouldEncryptNumericString() {
            // Given
            String numericString = "1234567890123456";

            // When
            String encrypted = encryptionService.encrypt(numericString);

            // Then
            assertThat(encrypted).isNotNull();
        }

        @Test
        @DisplayName("Should encrypt alphanumeric string")
        void shouldEncryptAlphanumericString() {
            // Given
            String alphanumericString = "CARD4532015112830366EXP1225";

            // When
            String encrypted = encryptionService.encrypt(alphanumericString);

            // Then
            assertThat(encrypted).isNotNull();
        }
    }

    @Nested
    @DisplayName("Decryption Operations")
    class DecryptionOperationsTests {

        @Test
        @DisplayName("Should decrypt card number successfully")
        void shouldDecryptCardNumberSuccessfully() {
            // Given
            String cardNumber = "4532015112830366";
            String encrypted = encryptionService.encrypt(cardNumber);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(cardNumber);
        }

        @Test
        @DisplayName("Should decrypt CVV successfully")
        void shouldDecryptCvvSuccessfully() {
            // Given
            String cvv = "123";
            String encrypted = encryptionService.encrypt(cvv);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(cvv);
        }

        @Test
        @DisplayName("Should decrypt string with special characters")
        void shouldDecryptStringWithSpecialCharacters() {
            // Given
            String specialString = "4532-0151-1283-0366";
            String encrypted = encryptionService.encrypt(specialString);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(specialString);
        }

        @Test
        @DisplayName("Should decrypt string with unicode characters")
        void shouldDecryptStringWithUnicodeCharacters() {
            // Given
            String unicodeString = "用户测试";
            String encrypted = encryptionService.encrypt(unicodeString);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(unicodeString);
        }

        @Test
        @DisplayName("Should decrypt long card number string")
        void shouldDecryptLongCardNumberString() {
            // Given
            String longString = "45320151128303661234567890123456";
            String encrypted = encryptionService.encrypt(longString);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(longString);
        }

        @Test
        @DisplayName("Should decrypt numeric string")
        void shouldDecryptNumericString() {
            // Given
            String numericString = "1234567890123456";
            String encrypted = encryptionService.encrypt(numericString);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(numericString);
        }

        @Test
        @DisplayName("Should decrypt alphanumeric string")
        void shouldDecryptAlphanumericString() {
            // Given
            String alphanumericString = "CARD4532015112830366EXP1225";
            String encrypted = encryptionService.encrypt(alphanumericString);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(alphanumericString);
        }
    }

    @Nested
    @DisplayName("Round-Trip Encryption/Decryption")
    class RoundTripEncryptionDecryptionTests {

        @Test
        @DisplayName("Should encrypt and decrypt card number in round-trip")
        void shouldEncryptAndDecryptCardNumberInRoundTrip() {
            // Given
            String cardNumber = "4532015112830366";

            // When
            String encrypted = encryptionService.encrypt(cardNumber);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(cardNumber);
        }

        @Test
        @DisplayName("Should encrypt and decrypt CVV in round-trip")
        void shouldEncryptAndDecryptCvvInRoundTrip() {
            // Given
            String cvv = "456";

            // When
            String encrypted = encryptionService.encrypt(cvv);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(cvv);
        }

        @Test
        @DisplayName("Should encrypt and decrypt multiple values independently")
        void shouldEncryptAndDecryptMultipleValuesIndependently() {
            // Given
            String cardNumber1 = "4532015112830366";
            String cardNumber2 = "4532015112830367";
            String cardNumber3 = "4532015112830368";

            // When
            String encrypted1 = encryptionService.encrypt(cardNumber1);
            String encrypted2 = encryptionService.encrypt(cardNumber2);
            String encrypted3 = encryptionService.encrypt(cardNumber3);

            String decrypted1 = encryptionService.decrypt(encrypted1);
            String decrypted2 = encryptionService.decrypt(encrypted2);
            String decrypted3 = encryptionService.decrypt(encrypted3);

            // Then
            assertThat(decrypted1).isEqualTo(cardNumber1);
            assertThat(decrypted2).isEqualTo(cardNumber2);
            assertThat(decrypted3).isEqualTo(cardNumber3);
        }

        @Test
        @DisplayName("Should encrypt and decrypt empty string in round-trip")
        void shouldEncryptAndDecryptEmptyStringInRoundTrip() {
            // Given
            String emptyString = "";

            // When
            String encrypted = encryptionService.encrypt(emptyString);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(emptyString);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RuntimeException when decrypting invalid ciphertext")
        void shouldThrowRuntimeExceptionWhenDecryptingInvalidCiphertext() {
            // Given
            String invalidCiphertext = "invalid_base64_!!!";

            // When/Then
            assertThatThrownBy(() -> encryptionService.decrypt(invalidCiphertext))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Decryption failed");
        }

        @Test
        @DisplayName("Should throw RuntimeException when decrypting tampered ciphertext")
        void shouldThrowRuntimeExceptionWhenDecryptingTamperedCiphertext() {
            // Given
            String plaintext = "4532015112830366";
            String encrypted = encryptionService.encrypt(plaintext);
            String tamperedCiphertext = encrypted.substring(0, encrypted.length() - 5) + "XXXXX";

            // When/Then
            assertThatThrownBy(() -> encryptionService.decrypt(tamperedCiphertext))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw RuntimeException when decrypting empty ciphertext")
        void shouldThrowRuntimeExceptionWhenDecryptingEmptyCiphertext() {
            // Given
            String emptyCiphertext = "";

            // When/Then
            assertThatThrownBy(() -> encryptionService.decrypt(emptyCiphertext))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw RuntimeException when decrypting null ciphertext")
        void shouldThrowRuntimeExceptionWhenDecryptingNullCiphertext() {
            // When/Then
            assertThatThrownBy(() -> encryptionService.decrypt(null))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw RuntimeException when encrypting null plaintext")
        void shouldThrowRuntimeExceptionWhenEncryptingNullPlaintext() {
            // When/Then
            assertThatThrownBy(() -> encryptionService.encrypt(null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long plaintext")
        void shouldHandleVeryLongPlaintext() {
            // Given
            String longPlaintext = "a".repeat(1000);

            // When
            String encrypted = encryptionService.encrypt(longPlaintext);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(longPlaintext);
        }

        @Test
        @DisplayName("Should handle plaintext with newlines")
        void shouldHandlePlaintextWithNewlines() {
            // Given
            String plaintext = "line1\nline2\nline3";

            // When
            String encrypted = encryptionService.encrypt(plaintext);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should handle plaintext with tabs")
        void shouldHandlePlaintextWithTabs() {
            // Given
            String plaintext = "col1\tcol2\tcol3";

            // When
            String encrypted = encryptionService.encrypt(plaintext);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should handle plaintext with all ASCII characters")
        void shouldHandlePlaintextWithAllAsciiCharacters() {
            // Given
            String plaintext = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";

            // When
            String encrypted = encryptionService.encrypt(plaintext);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should handle binary-like data as string")
        void shouldHandleBinaryLikeDataAsString() {
            // Given
            String binaryLike = "\u0000\u0001\u0002\u0003\u0004\u0005";

            // When
            String encrypted = encryptionService.encrypt(binaryLike);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(binaryLike);
        }

        @Test
        @DisplayName("Should handle emoji characters")
        void shouldHandleEmojiCharacters() {
            // Given
            String emojiString = "💳🔐🔑";

            // When
            String encrypted = encryptionService.encrypt(emojiString);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(emojiString);
        }
    }

    @Nested
    @DisplayName("Encryption Key Configuration")
    class EncryptionKeyConfigurationTests {

        @Test
        @DisplayName("Should use configured encryption key")
        void shouldUseConfiguredEncryptionKey() {
            // Given
            String customKey = "CustomEncryptionKey32Characters!";
            ReflectionTestUtils.setField(encryptionService, "encryptionKey", customKey);
            String plaintext = "4532015112830366";

            // When
            String encrypted = encryptionService.encrypt(plaintext);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should produce different ciphertext with different keys")
        void shouldProduceDifferentCiphertextWithDifferentKeys() {
            // Given
            String plaintext = "4532015112830366";
            String key1 = "TestEncryptionKey32CharsLong!!";
            String key2 = "DifferentEncryptionKey32Chars!";

            ReflectionTestUtils.setField(encryptionService, "encryptionKey", key1);
            String encrypted1 = encryptionService.encrypt(plaintext);

            ReflectionTestUtils.setField(encryptionService, "encryptionKey", key2);
            String encrypted2 = encryptionService.encrypt(plaintext);

            // Then
            assertThat(encrypted1).isNotNull();
            assertThat(encrypted2).isNotNull();
            assertThat(encrypted1).isNotEqualTo(encrypted2);
        }

        @Test
        @DisplayName("Should decrypt with same key used for encryption")
        void shouldDecryptWithSameKeyUsedForEncryption() {
            // Given
            String plaintext = "4532015112830366";
            String key = "TestEncryptionKey32CharsLong!!";
            ReflectionTestUtils.setField(encryptionService, "encryptionKey", key);

            // When
            String encrypted = encryptionService.encrypt(plaintext);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should fail to decrypt with different key")
        void shouldFailToDecryptWithDifferentKey() {
            // Given
            String plaintext = "4532015112830366";
            String key1 = "TestEncryptionKey32CharsLong!!";
            String key2 = "DifferentEncryptionKey32Chars!";

            ReflectionTestUtils.setField(encryptionService, "encryptionKey", key1);
            String encrypted = encryptionService.encrypt(plaintext);

            ReflectionTestUtils.setField(encryptionService, "encryptionKey", key2);

            // When/Then
            assertThatThrownBy(() -> encryptionService.decrypt(encrypted))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
