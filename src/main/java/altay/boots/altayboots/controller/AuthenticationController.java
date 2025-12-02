package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.auth.JwtAuthenticationResponce;
import altay.boots.altayboots.dto.auth.SignInRequest;
import altay.boots.altayboots.dto.auth.SignUpRequest;
import altay.boots.altayboots.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationResponce> signIn(@RequestBody SignInRequest signInRequest){
        return new ResponseEntity<>(authenticationService.signIn(signInRequest), HttpStatus.OK);
    }
    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest signUpRequest){
        authenticationService.signUp(signUpRequest);
        return new ResponseEntity<>("Аккаунт успешно сохранен!", HttpStatus.CREATED);
    }
}