package api.core.user;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface UserService {
    @GetMapping(value = "/user/{id}", produces = "application/json")
    Mono<User> getUser(@PathVariable int id);
    
    @GetMapping(value = "/user/email/{email}", produces = "application/json")
    Mono<User> getUserByEmail(@PathVariable String email);

    @PostMapping(value = "/user", consumes = "application/json", produces = "application/json")
    Mono<User> createUser(@RequestBody User body);

    @DeleteMapping(value = "/user/{id}")
    Mono<Void> deleteUser(@PathVariable int id);
}