package user_service.implementation;

import api.core.user.User;
import api.core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import user_service.mapper.UserMapper;
import user_service.repository.UserServiceRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@RestController
public class UserServiceImplementation implements UserService {

    private final UserServiceRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Autowired
    public UserServiceImplementation(UserServiceRepository repo, PasswordEncoder passwordEncoder, UserMapper mapper) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    public Mono<User> getUser(int id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("User not found: " + id)))
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return repo.findByEmail(email)
                .switchIfEmpty(Mono.error(new NoDataFoundException("User not found: " + email)))
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<User> createUser(User body) {
        return repo.findByEmail(body.getEmail())
                .hasElement()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new EntityAlreadyExistsException("User exists: " + body.getEmail()));
                    }
                    body.setPassword(passwordEncoder.encode(body.getPassword()));
                    return repo.save(mapper.apiToEntity(body));
                })
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Void> deleteUser(int id) {
        return repo.deleteById(id);
    }
}