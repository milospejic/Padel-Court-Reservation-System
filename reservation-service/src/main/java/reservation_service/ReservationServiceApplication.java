package reservation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@SpringBootApplication
@ComponentScan(basePackages = {"reservation_service", "util"})
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
    

	@Bean
	public WebFluxConfigurer corsConfigurer() { 
		return new WebFluxConfigurer() {
			@Override
			public void addCorsMappings(org.springframework.web.reactive.config.CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("*")
						.allowedMethods("*");
			}
		};
	}

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}