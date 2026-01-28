package api_gateway.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers; // Note the 's'

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthetication {

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private JwtAuthenticationManager authenticationManager;

    @Autowired
    private CustomAuthenticationHandler customAuthenticationHandler;
    
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange(exchange -> exchange
            	.pathMatchers("/actuator/**").permitAll()
            	.pathMatchers("/auth/**").permitAll()
            		
            	.pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
            	.pathMatchers("/*/v3/api-docs").permitAll()
            	.pathMatchers("/swagger-ui/index.html").permitAll()

                .pathMatchers("/*/openapi/v3/api-docs").permitAll()
                // Public Endpoints (View Clubs, View Reviews)
                .pathMatchers(HttpMethod.GET, "/club/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/review/**").permitAll()
                
                .pathMatchers(HttpMethod.GET, "/club-composite/**").permitAll()
                // User & Admin Operations (Bookings, Writing Reviews)
                .pathMatchers("/reservation/**").hasAnyRole("USER", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/review/**").hasAnyRole("USER", "ADMIN")
                
                // Admin Only Operations (Manage Clubs, Delete Reviews, Notifications)
                .pathMatchers("/club/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/review/**").hasRole("ADMIN")
                .pathMatchers("/notification/**").hasRole("ADMIN")

                // User Management (Strictly Admin/Owner)
                .pathMatchers(HttpMethod.POST, "/user").hasAnyRole("ADMIN", "OWNER")
                .pathMatchers(HttpMethod.PUT, "/user").hasAnyRole("ADMIN", "OWNER")
                .pathMatchers(HttpMethod.DELETE, "/user/**").hasRole("OWNER")
                .pathMatchers(HttpMethod.GET, "/user/**").hasAnyRole("ADMIN", "OWNER")

                .anyExchange().authenticated() 
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler(customAuthenticationHandler)
                .authenticationEntryPoint(customAuthenticationHandler)
            );

        return http.build();
    }
}