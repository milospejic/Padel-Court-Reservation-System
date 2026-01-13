package club_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import club_service.model.ClubModel;
import reactor.core.publisher.Mono;

public interface ClubRepository extends ReactiveCrudRepository<ClubModel, Integer> {
    Mono<Boolean> existsByName(String name);
    Mono<ClubModel> findByName(String name);
}