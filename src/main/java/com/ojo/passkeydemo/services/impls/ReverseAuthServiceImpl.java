package com.ojo.passkeydemo.services.impls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojo.passkeydemo.entities.AuthSupport;
import com.ojo.passkeydemo.entities.AuthUser;
import com.ojo.passkeydemo.entities.Authenticator;
import com.ojo.passkeydemo.repositories.AuthSupportRepo;
import com.ojo.passkeydemo.repositories.AuthUserRepo;
import com.ojo.passkeydemo.repositories.AuthenticatorRepo;
import com.ojo.passkeydemo.responses.AuthRegisterResponse;
import com.ojo.passkeydemo.responses.AuthVerifyResponseDTO;
import com.ojo.passkeydemo.services.ReverseAuthService;
import com.ojo.passkeydemo.utils.GenerateRandom;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Objects;

// ReverseAuthServiceImpl.java
@Service
public class ReverseAuthServiceImpl implements ReverseAuthService {

    private final RelyingParty relyingParty;
    private final AuthUserRepo authUserRepo;
    private final AuthenticatorRepo authenticatorRepository;
    private final GenerateRandom generateRandom;
    private final ObjectMapper mapper;
    private final AuthSupportRepo authSupportRepo;
    private final CacheManager cacheManager;

    @Autowired
    public ReverseAuthServiceImpl(RelyingParty relyingParty, AuthUserRepo authUserRepo,
                                  AuthenticatorRepo authenticatorRepository, GenerateRandom generateRandom,
                                  ObjectMapper mapper, AuthSupportRepo authSupportRepo, CacheManager cacheManager) {
        this.relyingParty = relyingParty;
        this.authUserRepo = authUserRepo;
        this.authenticatorRepository = authenticatorRepository;
        this.generateRandom = generateRandom;
        this.mapper = mapper;
        this.authSupportRepo = authSupportRepo;
        this.cacheManager = cacheManager;
    }


    @Override
    public AuthRegisterResponse registerAuthUser(String userName) {
        boolean existingUser = authUserRepo.existsByUserName(userName);
        if (!existingUser) {
            UserIdentity userIdentity = UserIdentity.builder()
                    .name(userName)
                    .displayName(userName)
                    .id(generateRandom.generateRandomId(32))
                    .build();
            AuthUser saveAuthUser = new AuthUser(userIdentity);
            try {
                authUserRepo.save(saveAuthUser);
                return newAuthRegistration(saveAuthUser);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save user.", e);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username " + userName + " already exists. Choose a new name.");
        }
    }

    private AuthRegisterResponse newAuthRegistration(AuthUser authUser) {
        UserIdentity userIdentity = authUser.toUserIdentity();
        StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder().user(userIdentity).build();
        PublicKeyCredentialCreationOptions registration = relyingParty.startRegistration(registrationOptions);
        addPkcToCache(authUser.getUserName(), registration);
        try {
            return AuthRegisterResponse.builder()
                    .userName(authUser.getUserName())
                    .key(mapper.readTree(registration.toCredentialsCreateJson()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing JSON.", e);
        }
    }

    private void addPkcToCache(String userName, PublicKeyCredentialCreationOptions registration) {
        Objects.requireNonNull(cacheManager.getCache("pkc")).put(userName, registration);
    }

    @Override
    public boolean finishRegisterAuthUser(String userName, String credential) {
        try {
            AuthUser authUser = authUserRepo.findAuthUserByUserName(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            PublicKeyCredentialCreationOptions requestOptions = (PublicKeyCredentialCreationOptions) getPkcFromCache(userName);
            if (requestOptions != null) {
                PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                        PublicKeyCredential.parseRegistrationResponseJson(credential);
                FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                        .request(requestOptions)
                        .response(pkc)
                        .build();
                RegistrationResult result = relyingParty.finishRegistration(options);
                Authenticator savedAuth = new Authenticator(result, pkc.getResponse(), authUser, userName);
                authenticatorRepository.save(savedAuth);
                AuthSupport authSupport = new AuthSupport();
                authSupport.setUserName(userName);
                authSupport.setCredId(result.getKeyId().getId().getBase64Url());
                authSupportRepo.save(authSupport);
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cached request expired. Try to register again!");
            }
        } catch (RegistrationFailedException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed.", e);
        }
    }

    private Object getPkcFromCache(String userName) {
        return Objects.requireNonNull(cacheManager.getCache("pkc")).get(userName).get();
    }

    @Override
    public AuthVerifyResponseDTO startLogin(String userName) {
        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder().username(userName).build());
        try {
            addRequestToCache(userName, request);
            return AuthVerifyResponseDTO.builder()
                    .userName(userName)
                    .key(mapper.readTree(request.toCredentialsGetJson()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void addRequestToCache(String userName, AssertionRequest request) {
        Objects.requireNonNull(cacheManager.getCache("pkc-verify")).put(userName, request);
    }

    @Override
    public boolean finishLogin(String userName, String credential) {
        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(credential);
            AssertionRequest request = (AssertionRequest) getRequestFromCache(userName);
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build());
            return result.isSuccess();
        } catch (IOException | AssertionFailedException e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }

    private Object getRequestFromCache(String userName) {
        return Objects.requireNonNull(cacheManager.getCache("pkc-verify")).get(userName).get();
    }
}