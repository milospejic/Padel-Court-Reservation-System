package api_gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders; // <--- CHANGED THIS IMPORT
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import util.JwtUtil;

@Component
public class UserHeaderFilter implements GlobalFilter {
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String userId = jwtUtil.getAllClaimsFromToken(token).getSubject();
                
                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r.header("logged-in-user-id", userId))
                    .build();
                
                return chain.filter(modifiedExchange);
            } catch (Exception e) {
            }
        }
        return chain.filter(exchange);
    }
}