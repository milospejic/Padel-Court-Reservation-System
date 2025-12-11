package club_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import club_service.model.ClubModel;

public interface ClubRepository extends JpaRepository<ClubModel, Integer> {
    boolean existsByName(String name);
    ClubModel findByName(String name);
}