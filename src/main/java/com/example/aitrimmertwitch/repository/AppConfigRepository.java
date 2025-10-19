package com.example.aitrimmertwitch.repository;

import com.example.aitrimmertwitch.entity.AppConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    Optional<AppConfig> findByKey(String key);
}
