package api_gateway.routing;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfiguration {

	@Bean
	RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p.path("/user/**").uri("lb://user-service"))
		        .route(p -> p.path("/club/**").uri("lb://club-service"))
		        .route(p -> p.path("/reservation/**").uri("lb://reservation-service"))
		        .route(p -> p.path("/review/**").uri("lb://review-service"))
		        .route(p -> p.path("/notification/**").uri("lb://notification-service"))
			.build();
	}
}

