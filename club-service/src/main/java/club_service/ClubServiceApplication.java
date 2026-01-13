package club_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@SpringBootApplication
@ComponentScan(basePackages = {"club_service", "util"})
public class ClubServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClubServiceApplication.class, args);
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
