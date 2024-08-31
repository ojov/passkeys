package com.ojo.passkeydemo.repositories;

import com.ojo.passkeydemo.entities.AuthSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthSupportRepo extends JpaRepository<AuthSupport, Long> {
    Optional<AuthSupport> findAuthSupportByCredId(String credId);
}
