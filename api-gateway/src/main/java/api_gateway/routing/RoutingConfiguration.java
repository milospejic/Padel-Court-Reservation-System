package api_gateway.routing;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfiguration {
		//lb instead of http when not in kubernetes
    @Bean
    RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p.path("/auth/**")
                             .uri("http://user-service"))
                
                .route(p -> p.path("/user/**")
                             .uri("http://user-service"))
                             
                .route(p -> p.path("/reservation/**")
                             .uri("http://reservation-service"))
                             
                .route(p -> p.path("/club/**")
                             .uri("http://club-service"))
                             
                .route(p -> p.path("/review/**")
                             .uri("http://review-service"))

                .route(p -> p.path("/club-composite/**")
                             .uri("http://club-composite-service"))

                .build();
    }
}