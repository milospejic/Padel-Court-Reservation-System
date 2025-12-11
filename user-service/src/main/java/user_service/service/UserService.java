package user_service.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import user_service.dto.UserDto;

public interface UserService {
	
	@GetMapping("/user")
	List<UserDto> getUsers();
	
	@GetMapping("/user/email/{email}")
	UserDto getUser(@PathVariable String email);
	
	@PostMapping("/user")
	ResponseEntity<?> createUser(@RequestBody UserDto dto, @RequestHeader("Authorization") String authorization);
	
	@PutMapping
	("/user")
	ResponseEntity<?> updateUser(@RequestBody UserDto dto, @RequestHeader("Authorization") String authorization);
	
	@DeleteMapping("/user/id/{id}")
	ResponseEntity<?> deleteUser(@PathVariable int id);


}
