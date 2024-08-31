package com.ojo.passkeydemo.repositories;

import com.ojo.passkeydemo.entities.AuthUser;
import com.ojo.passkeydemo.entities.Authenticator;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthenticatorRepo extends JpaRepository<Authenticator, Long> {
    List<Authenticator> findAllByUser(AuthUser user);
    Optional<Authenticator> findAuthenticatorByName(String name);
    List<Authenticator> findAllByCredentialId(ByteArray credentialId);
}
