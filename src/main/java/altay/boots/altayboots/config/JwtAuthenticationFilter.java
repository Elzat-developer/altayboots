package altay.boots.altayboots.config;

import altay.boots.altayboots.service.JWTService;
import altay.boots.altayboots.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String phone;
        //if (StringUtils.isEmpty(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader, "Bearer ")) {
        //    filterChain.doFilter(request, response);
        //    return;
        //}
        if (StringUtils.isEmpty(authHeader)) {
            System.out.println("⚠️ [JWT Filter] No Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            System.out.println("✅ [JWT Filter] Token extracted WITH 'Bearer' prefix");
        } else {
            // ⚠️ если Bearer нет — принимаем как токен
            jwt = authHeader;
            System.out.println("✅ [JWT Filter] Token extracted WITH 'Bearer' prefix");
        }
        try {
        phone = jwtService.extractUserName(jwt);
        System.out.println("✅ [JWT Filter] Extracted phone: " + phone);

        if (StringUtils.isNotEmpty(phone) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(phone);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                // ✅ Берём роли из токена
                Collection<? extends GrantedAuthority> authorities = jwtService.extractAuthorities(jwt);

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities
                );

                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
                System.out.println("✅ [JWT Filter] Authorities: " + authorities);
                System.out.println("✅ [JWT Filter] Authentication successful for: " + phone);
            }
        }

        filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("❌ [JWT Filter] Error processing token: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
