package com.identityforge.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class EncryptionConfig {

    @Value("${app.encryption.key-file:config/aes-key.txt}")
    private String keyFilePath;
}
