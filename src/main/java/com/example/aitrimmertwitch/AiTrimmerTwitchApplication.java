package com.example.aitrimmertwitch;

import com.example.aitrimmertwitch.config.EncryptionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EncryptionProperties.class)
public class AiTrimmerTwitchApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTrimmerTwitchApplication.class, args);
    }
}
