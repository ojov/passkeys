package com.ojo.passkeydemo.services.impls;

import com.ojo.passkeydemo.entities.AuthSupport;
import com.ojo.passkeydemo.entities.AuthUser;
import com.ojo.passkeydemo.entities.Authenticator;
import com.ojo.passkeydemo.repositories.AuthSupportRepo;
import com.ojo.passkeydemo.repositories.AuthUserRepo;
import com.ojo.passkeydemo.repositories.AuthenticatorRepo;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class RegistrationService implements CredentialRepository {

    @Autowired
    private AuthUserRepo authUserRepo;

    @Autowired
    private AuthSupportRepo authSupportRepo;

    @Autowired
    private AuthenticatorRepo authRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        AuthUser user = authUserRepo.findAuthUserByUserName(username).orElse(null);
        List<Authenticator> auth = authRepository.findAllByUser(user);
        return auth.stream()
                .map(credential -> PublicKeyCredentialDescriptor.builder().id(credential.getCredentialId()).build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        AuthUser user = authUserRepo.findAuthUserByUserName(username).orElse(null);
        assert user != null;
        return Optional.of(user.getHandle());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        AuthUser user = authUserRepo.findAuthUserByHandle(userHandle).orElseThrow(()
                ->new RuntimeException("User not found"));
        return Optional.of(user.getUserName());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        AuthSupport authSupport = authSupportRepo.findAuthSupportByCredId(credentialId.getBase64Url())
                .orElseThrow(()-> new RuntimeException("AuthSupport not found"));
        Optional<Authenticator> auth = authRepository.findAuthenticatorByName(authSupport.getUserName());

        if (auth.isPresent()) {
            Authenticator credential = auth.get();
            RegisteredCredential registeredCredential = RegisteredCredential.builder()
                    .credentialId(credential.getCredentialId())
                    .userHandle(credential.getUser().getHandle())
                    .publicKeyCose(credential.getPublicKey())
                    .signatureCount(credential.getCount())
                    .build();
            return Optional.of(registeredCredential);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        List<Authenticator> auth = authRepository.findAllByCredentialId(credentialId);
        return auth.stream()
                .map(credential -> RegisteredCredential.builder()
                        .credentialId(credential.getCredentialId())
                        .userHandle(credential.getUser().getHandle())
                        .publicKeyCose(credential.getPublicKey())
                        .signatureCount(credential.getCount())
                        .build())
                .collect(Collectors.toSet());
    }
}