package reservation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reservation_service.model.ReservationModel;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationModel, Integer> {
    List<ReservationModel> findByUserEmail(String userEmail);
    List<ReservationModel> findByClubId(int clubId);
}