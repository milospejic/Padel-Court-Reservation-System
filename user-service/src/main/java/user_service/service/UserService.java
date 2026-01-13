package user_service.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import user_service.dto.UserDto;

public interface UserService {
    
    @GetMapping("/user")
    Flux<UserDto> getUsers();
    
    @GetMapping("/user/email/{email}")
    Mono<UserDto> getUser(@PathVariable String email);
    
    @PostMapping("/user")
    Mono<ResponseEntity<?>> createUser(@RequestBody UserDto dto, @RequestHeader("Authorization") String authorization);
    
    @PutMapping("/user")
    Mono<ResponseEntity<?>> updateUser(@RequestBody UserDto dto, @RequestHeader("Authorization") String authorization);
    
    @DeleteMapping("/user/id/{id}")
    Mono<ResponseEntity<?>> deleteUser(@PathVariable int id);
}