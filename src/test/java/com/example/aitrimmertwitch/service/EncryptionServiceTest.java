package com.example.aitrimmertwitch.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aitrimmertwitch.config.EncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setSecret("test-secret-value");
        encryptionService = new EncryptionService(properties);
        encryptionService.init();
    }

    @Test
    void encryptAndDecryptRoundTrip() {
        String encrypted = encryptionService.encrypt("my-secret");
        assertThat(encrypted).isNotBlank();
        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo("my-secret");
    }

    @Test
    void encryptHandlesNullInput() {
        assertThat(encryptionService.encrypt(null)).isNull();
    }

    @Test
    void decryptHandlesNullInput() {
        assertThat(encryptionService.decrypt(null)).isNull();
    }
}
