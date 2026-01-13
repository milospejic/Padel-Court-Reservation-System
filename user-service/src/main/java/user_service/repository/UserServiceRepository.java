package user_service.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import user_service.model.UserModel;
import reactor.core.publisher.Mono;

public interface UserServiceRepository extends ReactiveCrudRepository<UserModel, Integer> {

    Mono<UserModel> findByEmail(String email);
    Mono<UserModel> findById(int id);
    Mono<Boolean> existsByRole(String role);

    @Modifying
    @Query("UPDATE user_model SET password = :password, role = :role WHERE email = :email")
    Mono<Integer> updateUser(String email, String password, String role);

    Mono<Boolean> existsById(int id);
    Mono<Void> deleteById(int id);
    Mono<Boolean> existsByEmailAndRole(String email, String role);
    Mono<Boolean> existsByEmail(String email);
}