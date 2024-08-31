package com.ojo.passkeydemo.responses;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class AuthRegisterResponse {
    private String userName;
    private JsonNode key;
}
