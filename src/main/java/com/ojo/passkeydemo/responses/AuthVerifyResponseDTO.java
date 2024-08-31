package com.ojo.passkeydemo.responses;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthVerifyResponseDTO {
    private String userName;
    private String token;
    private JsonNode key;
}
