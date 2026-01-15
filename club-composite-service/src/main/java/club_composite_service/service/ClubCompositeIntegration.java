package club_composite_service.service;

import api.core.club.Club;
import api.core.club.ClubService;
import api.core.review.Review;
import api.core.review.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@Component
public class ClubCompositeIntegration implements ClubService, ReviewService {

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String clubServiceUrl = "http://club-service";
    private final String reviewServiceUrl = "http://review-service";

    @Autowired
    public ClubCompositeIntegration(WebClient.Builder webClientBuilder, ObjectMapper mapper) {
        this.webClient = webClientBuilder.build();
        this.mapper = mapper;
    }


    @Override
    public Mono<Club> getClub(int id) {
        return webClient.get()
                .uri(clubServiceUrl + "/club/" + id)
                .retrieve()
                .bodyToMono(Club.class)
                .onErrorMap(WebClientResponseException.NotFound.class, ex -> new NoDataFoundException("Club not found: " + id));
    }

    @Override
    public Flux<Club> getClubs() {
        return webClient.get()
                .uri(clubServiceUrl + "/club")
                .retrieve()
                .bodyToFlux(Club.class);
    }

    @Override
    public Mono<Club> createClub(Club body) {
        return webClient.post()
                .uri(clubServiceUrl + "/club")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Club.class)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> deleteClub(int id) {
        return webClient.delete()
                .uri(clubServiceUrl + "/club/" + id)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }


    @Override
    public Flux<Review> getReviews(int clubId) {
        return webClient.get()
                .uri(reviewServiceUrl + "/review?clubId=" + clubId)
                .retrieve()
                .bodyToFlux(Review.class);
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return webClient.post()
                .uri(reviewServiceUrl + "/review")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Review.class)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> deleteReview(int id) {
        return webClient.delete()
                .uri(reviewServiceUrl + "/review/" + id)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    private Throwable handleException(WebClientResponseException ex) {
        switch (ex.getStatusCode().value()) {
            case 404:
                return new NoDataFoundException(ex.getResponseBodyAsString());
            case 422:
            case 400:
                return new InvalidRequestException(ex.getResponseBodyAsString());
            default:
                return ex;
        }
    }
}