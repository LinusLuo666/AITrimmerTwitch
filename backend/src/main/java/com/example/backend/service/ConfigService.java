package com.example.backend.service;

import com.example.backend.dto.ConfigDto;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    public ConfigDto loadConfig() {
        return new ConfigDto("en-US", 3, true);
    }
}
