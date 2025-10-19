package com.example.aitrimmertwitch.service;

import com.example.aitrimmertwitch.entity.AppConfig;
import com.example.aitrimmertwitch.repository.AppConfigRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurationService {

    private final AppConfigRepository appConfigRepository;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    public ConfigurationService(AppConfigRepository appConfigRepository,
                                EncryptionService encryptionService,
                                AuditLogService auditLogService) {
        this.appConfigRepository = appConfigRepository;
        this.encryptionService = encryptionService;
        this.auditLogService = auditLogService;
    }

    public Optional<String> readConfig(String key) {
        validateKey(key);
        return appConfigRepository.findByKey(key)
            .map(config -> config.isSensitive()
                ? encryptionService.decrypt(config.getValue())
                : config.getValue());
    }

    @Transactional
    public void writeConfig(String key, String value, boolean sensitive, String actor) {
        validateKey(key);
        validateValue(value);

        AppConfig config = appConfigRepository.findByKey(key).orElseGet(AppConfig::new);
        config.setKey(key);
        config.setSensitive(sensitive);
        config.setValue(sensitive ? encryptionService.encrypt(value) : value);
        appConfigRepository.save(config);

        String auditActor = (actor == null || actor.isBlank()) ? "system" : actor;
        auditLogService.record(auditActor, "CONFIG_WRITE", String.format("key=%s, sensitive=%s", key, sensitive));
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Configuration key must not be blank");
        }
        if (key.length() > 120) {
            throw new IllegalArgumentException("Configuration key exceeds maximum length of 120 characters");
        }
    }

    private void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Configuration value must not be blank");
        }
        if (value.length() > 2048) {
            throw new IllegalArgumentException("Configuration value exceeds maximum length of 2048 characters");
        }
    }
}
