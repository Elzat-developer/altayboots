package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.auth.ChangePasswordDto;
import altay.boots.altayboots.dto.auth.JwtAuthenticationResponce;
import altay.boots.altayboots.dto.auth.SignInRequest;
import altay.boots.altayboots.model.entity.User;
import altay.boots.altayboots.repository.UserRepo;
import altay.boots.altayboots.service.AuthenticationService;
import altay.boots.altayboots.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.name(),
                signInRequest.password()));
        var user = userRepo.findByName(signInRequest.name())
                .orElseThrow(() -> new IllegalArgumentException("Invalid name or password"));
        if (user.isPasswordTemporary()) {
            throw new IllegalArgumentException("Необходимо сменить временный пароль");
        }
        var jwt = jwtService.generateToken(user);

        return new JwtAuthenticationResponce(jwt);
    }
    @Override
    public void changePassword(ChangePasswordDto dto) {
        User user = userRepo.findByName(dto.name())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        // Проверка: временный ли пароль
        if (!user.isPasswordTemporary()) {
            throw new IllegalStateException("Пароль уже был изменен.");
        }
        // Проверка старого пароля
        if (!passwordEncoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный текущий пароль.");
        }
        // Меняем пароль
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setPasswordTemporary(false); // больше менять нельзя

        userRepo.save(user);
    }
}