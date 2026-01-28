package api_gateway.authentication;

import util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        try {
            jwtUtil.validateToken(authToken);

            Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
            String username = claims.getSubject();
            String role = claims.get("role", String.class); // "ADMIN", "USER", etc.

       
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
            );

            return Mono.just(new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                authorities
            ));

        } catch (Exception e) {
            return Mono.empty();
        }
    }
}