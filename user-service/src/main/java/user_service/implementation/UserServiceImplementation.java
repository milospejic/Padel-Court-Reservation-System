package user_service.implementation;

import api.core.user.User;
import api.core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@RestController
public class UserServiceImplementation implements UserService {

    @Autowired
    private UserServiceRepository repo;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> getUser(int id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("User not found: " + id)))
                .map(this::entityToApi);
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return repo.findByEmail(email)
                .switchIfEmpty(Mono.error(new NoDataFoundException("User not found: " + email)))
                .map(this::entityToApi);
    }

    @Override
    public Mono<User> createUser(User body) {
        return repo.findByEmail(body.getEmail())
                .hasElement()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new EntityAlreadyExistsException("User exists: " + body.getEmail()));
                    }
                    
                    User userToSave = body;
                    userToSave.setPassword(passwordEncoder.encode(body.getPassword()));
                    return repo.save(apiToEntity(body));
                })
                .map(this::entityToApi);
    }

    @Override
    public Mono<Void> deleteUser(int id) {
        return repo.deleteById(id);
    }

    private User entityToApi(UserModel entity) {
        return new User(
            entity.getId(),
            entity.getEmail(),
            null,
            entity.getRole(),
            null 
        );
    }

    private UserModel apiToEntity(User api) {
        return new UserModel(api.getEmail(), api.getPassword(), api.getRole());
    }
}