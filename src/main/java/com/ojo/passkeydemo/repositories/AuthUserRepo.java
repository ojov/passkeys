package com.ojo.passkeydemo.repositories;

import com.ojo.passkeydemo.entities.AuthUser;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepo extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findAuthUserByUserName(String username);
    Optional<AuthUser> findAuthUserByHandle(ByteArray handle);
    Boolean existsByUserName(String username);
}
