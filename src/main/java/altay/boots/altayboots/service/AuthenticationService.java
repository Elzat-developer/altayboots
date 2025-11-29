package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.auth.ChangePasswordDto;
import altay.boots.altayboots.dto.auth.JwtAuthenticationResponce;
import altay.boots.altayboots.dto.auth.SignInRequest;

public interface AuthenticationService {
    JwtAuthenticationResponce signIn(SignInRequest signInRequest);
    void changePassword(ChangePasswordDto dto);
}
