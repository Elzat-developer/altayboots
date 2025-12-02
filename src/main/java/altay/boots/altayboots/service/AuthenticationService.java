package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.auth.JwtAuthenticationResponce;
import altay.boots.altayboots.dto.auth.SignInRequest;
import altay.boots.altayboots.dto.auth.SignUpRequest;

public interface AuthenticationService {
    JwtAuthenticationResponce signIn(SignInRequest signInRequest);

    void signUp(SignUpRequest signUpRequest);
}
