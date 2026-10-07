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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void signUp(SignUpRequest signUpRequest){
        if (userRepo.findByPhone(signUpRequest.phone()) != null){
            throw new IllegalArgumentException("Этот phone уже используется!");
        }
        // 2. Страховочная проверка сложности пароля (защита на уровне сервиса)
        validatePasswordComplexity(signUpRequest.password());

        User user = new User();
        user.setPhone(signUpRequest.phone());
        user.setName(signUpRequest.name());
        user.setAuthorities(Authorities.USER);
        user.setPassword(passwordEncoder.encode(signUpRequest.password()));

        userRepo.save(user);
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 8 символов");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("Пароль должен содержать как буквы, так и цифры");
        }
    }
}