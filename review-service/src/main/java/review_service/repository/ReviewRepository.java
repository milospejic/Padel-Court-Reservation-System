package review_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import review_service.model.ReviewModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewRepository extends ReactiveCrudRepository<ReviewModel, Integer> {
    
    Flux<ReviewModel> findByClubId(int clubId);    
    Flux<ReviewModel> findByUserEmail(String userEmail);
    
    Mono<Boolean> existsByClubIdAndUserEmail(int clubId, String userEmail);
    Mono<Void> deleteByClubId(int clubId);
}