package com.ojo.passkeydemo.services;
public interface ReverseAuthService {
    AuthRegisterResponse registerAuthUser(String userName);
    boolean finishRegisterAuthUser(String username, String credential);
    AuthVerifyResponseDTO startLogin(String userName);
    boolean finishLogin(String userName, String credential);
}