package com.ojo.passkeydemo.entities;

import com.yubico.webauthn.data.ByteArray;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Authenticator {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Lob
    @Column(nullable = false)
    private ByteArray credentialId;

    @Lob
    @Column(nullable = false)
    private ByteArray publicKey;

    @Column(nullable = false)
    private Long count;

    @Lob
    @Column(nullable = true)
    private ByteArray aaguid;

    @ManyToOne
    private AuthUser user;

    public Authenticator(RegistrationResult result, AuthenticatorAttestationResponse response, AuthUser user, String name) {
        Optional<AttestedCredentialData> attestationData = response.getAttestation().getAuthenticatorData().getAttestedCredentialData();
        this.credentialId = result.getKeyId().getId();
        this.publicKey = result.getPublicKeyCose();
        this.aaguid = attestationData.map(AttestedCredentialData::getAaguid).orElse(null);
        this.count = result.getSignatureCount();
        this.name = name;
        this.user = user;
    }
}
