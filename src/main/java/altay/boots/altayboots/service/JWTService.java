package altay.boots.altayboots.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public interface JWTService {
    String extractUserName(String token);
    Collection<? extends GrantedAuthority> extractAuthorities(String token);
    String generateToken(UserDetails userDetails);
    boolean isTokenValid(String token, UserDetails userDetails);
}
