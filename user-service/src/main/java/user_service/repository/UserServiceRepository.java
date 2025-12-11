package user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import user_service.model.UserModel;

public interface UserServiceRepository extends JpaRepository<UserModel, Integer> {

	UserModel findByEmail(String email);
	UserModel findById(int id);
	boolean existsByRole(String role);
	@Modifying
	@Transactional
	@Query("Update UserModel u set u.password = ?2, u.role = ?3 where u.email= ?1")
	void updateUser(String email, String password, String role);
//
	boolean existsById(int id);

	void deleteById(int id);

	boolean existsByEmailAndRole(String email, String string);
	boolean existsByEmail(String email);

	
}