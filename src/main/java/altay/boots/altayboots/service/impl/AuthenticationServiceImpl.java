package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.auth.JwtAuthenticationResponce;
import altay.boots.altayboots.dto.auth.SignInRequest;
import altay.boots.altayboots.dto.auth.SignUpRequest;
import altay.boots.altayboots.model.entity.User;
import altay.boots.altayboots.model.role.Authorities;
import altay.boots.altayboots.repository.UserRepo;
import altay.boots.altayboots.service.AuthenticationService;
import altay.boots.altayboots.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    public JwtAuthenticationResponce signIn(SignInRequest signInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.phone(),
                        signInRequest.password()
                )
        );
        User user = userRepo.findByPhone(signInRequest.phone());
        var jwt = jwtService.generateToken(user);

        return new JwtAuthenticationResponce(jwt);
    }

    @Override
    public void signUp(SignUpRequest signUpRequest){

        User userPhone = userRepo.findByPhone(signUpRequest.phone());
        if (userPhone != null){
            throw new RuntimeException("Этот phone уже используется!");
        }
        User user = new User();

        user.setPhone(signUpRequest.phone());
        user.setName(signUpRequest.name());
        user.setAuthorities(Authorities.USER);
        user.setPassword(passwordEncoder.encode(signUpRequest.password()));

        userRepo.save(user);
    }
}