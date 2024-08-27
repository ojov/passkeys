package com.ojo.passkeydemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "authn")
public class WebAuthnConfig {
    private String hostname;
    private String display;
    private String origin;

    // Getters and Setters
}
