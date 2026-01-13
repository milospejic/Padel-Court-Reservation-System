package reservation_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reservation_service.model.ReservationModel;
import reactor.core.publisher.Flux;

public interface ReservationRepository extends ReactiveCrudRepository<ReservationModel, Integer> {
    
    Flux<ReservationModel> findByUserEmail(String userEmail);    
    Flux<ReservationModel> findByClubId(int clubId);
}