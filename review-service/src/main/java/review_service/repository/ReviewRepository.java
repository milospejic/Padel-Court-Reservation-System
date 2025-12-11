package review_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import review_service.model.ReviewModel;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewModel, Integer> {
    List<ReviewModel> findByClubId(int clubId);
    List<ReviewModel> findByUserEmail(String userEmail);
}