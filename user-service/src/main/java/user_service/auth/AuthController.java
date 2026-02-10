package user_service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.web.bind.annotation.*;

import api.core.user.User;
import api.core.user.UserService;
import reactor.core.publisher.Mono;
import user_service.repository.UserServiceRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserServiceRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder; 
    
    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public Mono<ResponseEntity<User>> register(@RequestBody AuthRequest request) {
        User newUser = new User(0, request.getEmail(), request.getPassword(), "USER", null);
        return userService.createUser(newUser)
                .map(user -> ResponseEntity.ok(user))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }
    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody AuthRequest request) {
        return repo.findByEmail(request.getEmail())
            .map(user -> {
                if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    return ResponseEntity.ok(token);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
                }
            })
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found"));
    }
}