package com.ojo.passkeydemo.entities;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@Entity
@Getter
@NoArgsConstructor
@Builder
public class AuthUser {

    @Id
    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_created_ts")
    private Long userCreatedTimeStamp;

    @Lob
    @Column(nullable = false, length = 64)
    private ByteArray handle;

    public AuthUser(UserIdentity userIdentity) {
        this.handle = userIdentity.getId();
        this.userName = userIdentity.getName();
        this.userCreatedTimeStamp = Instant.now().getEpochSecond();
    }

    public UserIdentity toUserIdentity() {
        return UserIdentity.builder()
                .name(getUserName())
                .displayName(getUserName())
                .id(getHandle())
                .build();
    }
}
