package api_gateway.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthetication {
    
    @Autowired
    private CustomAuthenticationHandler customAuthenticationHandler;

    @Value("${app.services.user-url:http://user-service:8770}")
    private String userServiceUrl;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchange -> exchange
            	.pathMatchers("/actuator/**").permitAll()
            		
            	.pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
            	.pathMatchers("/*/v3/api-docs").permitAll()
                // Public Endpoints (View Clubs, View Reviews)
                .pathMatchers(HttpMethod.GET, "/club/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/review/**").permitAll()
                
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
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
    
    @Bean
    public ReactiveUserDetailsService userDetailsService(WebClient.Builder webClientBuilder, BCryptPasswordEncoder encoder) {
        return username -> {
            return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/user/email/" + username)
                .retrieve()
                .bodyToMono(UserDto.class)
                .map(u -> User.withUsername(u.getEmail())
                    .password(encoder.encode(u.getPassword()))
                    .roles(u.getRole())
                    .build())
                .onErrorResume(e -> {
                    System.err.println("Authentication failed for user: " + username + " - " + e.getMessage());
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")));
        };
    }

    @Bean
    public BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}