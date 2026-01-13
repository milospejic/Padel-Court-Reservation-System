package user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@SpringBootApplication
@ComponentScan(basePackages = {"user_service", "util"})
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
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
}
