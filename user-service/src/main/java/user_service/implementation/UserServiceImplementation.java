package user_service.implementation;

import api.core.user.User;
import api.core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import user_service.mapper.UserMapper;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.ForbiddenActionException;
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
        return Mono.deferContextual(ctx -> {
            ServerWebExchange exchange = ctx.getOrDefault(ServerWebExchange.class, null);
            String requesterRole = (exchange != null) ? exchange.getRequest().getHeaders().getFirst("logged-in-user-role") : null;

            if (("ADMIN".equals(body.getRole()) || "OWNER".equals(body.getRole()))) {
                if (!"OWNER".equals(requesterRole)) {
                    return Mono.error(new ForbiddenActionException("Only Owners can create Admins/Owners"));
                }
            }

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
        });
    }

    @Override
    public Mono<Void> deleteUser(int id) {
        return Mono.deferContextual(ctx -> {
            ServerWebExchange exchange = ctx.getOrDefault(ServerWebExchange.class, null);
            String requesterRole = (exchange != null) ? exchange.getRequest().getHeaders().getFirst("logged-in-user-role") : null;

            return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("User not found: " + id)))
                .flatMap(targetUser -> {
                    if ("ADMIN".equals(requesterRole) && !"USER".equals(targetUser.getRole())) {
                        return Mono.error(new ForbiddenActionException("Admins can only delete Users."));
                    }
                    return repo.delete(targetUser);
                });
        });
    }

    @Override
    public Mono<User> updateUser(int id, User body) {
        return Mono.deferContextual(ctx -> {
            ServerWebExchange exchange = ctx.getOrDefault(ServerWebExchange.class, null);
            String requesterId = (exchange != null) ? exchange.getRequest().getHeaders().getFirst("logged-in-user-id") : null;

            return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("User not found: " + id)))
                .flatMap(user -> {
                    if (requesterId != null && !user.getEmail().equals(requesterId)) {
                        return Mono.error(new ForbiddenActionException("You can only update your own profile."));
                    }
                    
                    if (body.getPassword() != null && !body.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(body.getPassword()));
                    }
                    return repo.save(user);
                })
                .map(mapper::entityToApi);
        });
    }
}