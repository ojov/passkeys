package com.ojo.passkeydemo.services.impls;

import com.ojo.passkeydemo.entities.Authenticator;
import com.yubico.webauthn.RegisteredCredential;

@Repository
public class RegistrationService implements CredentialRepository {

    @Autowired
    private AuthUserRepo authUserRepo;

    @Autowired
    private AuthSupportRepo authSupportRepo;

    @Autowired
    private AuthenticatorRepository authRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        AuthUser user = authUserRepo.findByUserName(username);
        List<Authenticator> auth = authRepository.findAllByUser(user);
        return auth.stream()
                .map(credential -> PublicKeyCredentialDescriptor.builder().id(credential.getCredentialId()).build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        AuthUser user = authUserRepo.findByUserName(username);
        return Optional.of(user.getHandle());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        AuthUser user = authUserRepo.findByHandle(userHandle);
        return Optional.of(user.getUserName());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        AuthSupport authSupport = authSupportRepo.findByCredId(credentialId.getBase64Url());
        Optional<Authenticator> auth = authRepository.findByName(authSupport.getUserName());

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