package com.ojo.passkeydemo.security;

import com.ojo.passkeydemo.config.WebAuthnConfig;
import com.ojo.passkeydemo.services.impls.RegistrationService;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
@Configuration
public class SecurityConfig {

    @Bean
    @Autowired
    public RelyingParty relyingParty(RegistrationService registrationService, WebAuthnConfig properties) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(properties.getHostname())
                .name(properties.getDisplay())
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(registrationService)
                .origins(Collections.singleton(properties.getOrigin()))
                .build();
    }

//    @Bean
//    public CacheManager cacheManager() {
//        return new ConcurrentMapCacheManager("yourCacheName");
//    }
    
}
