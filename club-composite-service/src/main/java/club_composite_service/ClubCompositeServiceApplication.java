package club_composite_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@ComponentScan(basePackages = {"club_composite_service", "util"})
public class ClubCompositeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClubCompositeServiceApplication.class, args);
    }

    @Bean
    //@LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}