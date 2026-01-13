package user_service.implementation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import user_service.dto.UserDto;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;
import user_service.service.UserService;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.ForbidenActionException;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@RestController
public class UserServiceImplementation implements UserService {

    @Autowired
    private UserServiceRepository repo;
    
    @Override
    public Flux<UserDto> getUsers() {
        return repo.findAll()
                .map(this::convertModelToDto);
    }
    
    @Override
    public Mono<UserDto> getUser(String email) {
        return repo.findByEmail(email)
                .map(this::convertModelToDto)
                .switchIfEmpty(Mono.empty()); // Returns null/empty if not found
    }

    @Override
    public Mono<ResponseEntity<?>> createUser(UserDto dto, @RequestHeader("Authorization") String authorization) {
        String requestEmail = getEmail(authorization).toLowerCase();
        
        if (!dto.getRole().equals("USER") && !dto.getRole().equals("ADMIN")) {
            return Mono.error(new InvalidRequestException("Role must be either USER or ADMIN"));
        }

        return repo.findByEmail(dto.getEmail())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new EntityAlreadyExistsException("User with forwarded email already exists"));
                    }
                    
                    if (dto.getRole().equals("ADMIN")) {
                        return repo.existsByEmailAndRole(requestEmail, "ADMIN")
                                .flatMap(isAdmin -> {
                                    if (isAdmin) {
                                        return Mono.error(new ForbidenActionException("As an ADMIN you can only add USERs"));
                                    }
                                    return saveUser(dto);
                                });
                    }
                    return saveUser(dto);
                });
    }

    private Mono<ResponseEntity<?>> saveUser(UserDto dto) {
        UserModel model = convertDtoToModel(dto);
        return repo.save(model)
                .map(saved -> ResponseEntity.status(201).body(saved));
    }

    @Override
    public Mono<ResponseEntity<?>> updateUser(UserDto dto, String authorization) {
        String requestEmail = getEmail(authorization).toLowerCase();

        if (!dto.getRole().equals("USER") && !dto.getRole().equals("ADMIN")) {
            return Mono.error(new InvalidRequestException("Role must be either USER or ADMIN"));
        }

        return repo.findByEmail(dto.getEmail())
                .switchIfEmpty(Mono.error(new NoDataFoundException("User with forwarded email does not exist")))
                .flatMap(existingUser -> {
                    
                    if (existingUser.getRole().equals("ADMIN") || dto.getRole().equals("ADMIN")) {
                        return repo.existsByEmailAndRole(requestEmail, "ADMIN")
                                .flatMap(isAdmin -> {
                                    if (isAdmin) {
                                        return Mono.error(new ForbidenActionException("Admins cannot modify Admins or promote users to Admin"));
                                    }
                                    return executeUpdate(dto);
                                });
                    }
                    return executeUpdate(dto);
                });
    }

    private Mono<ResponseEntity<?>> executeUpdate(UserDto dto) {
        return repo.updateUser(dto.getEmail(), dto.getPassword(), dto.getRole())
                .map(count -> ResponseEntity.status(200).body(dto));
    }

    @Override
    public Mono<ResponseEntity<?>> deleteUser(int id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("User not found with id " + id)))
                .flatMap(existingUser -> {
                    if (existingUser.getRole().equals("OWNER")) {
                        return Mono.error(new InvalidRequestException("You cant delete the OWNER, he stays!"));
                    }
                    return repo.deleteById(id)
                            .then(Mono.just(ResponseEntity.status(200).body("User deleted successfully")));
                });
    }
    
    public UserModel convertDtoToModel(UserDto dto) {
        return new UserModel(dto.getEmail(), dto.getPassword(), dto.getRole());
    }

    public UserDto convertModelToDto(UserModel model) {
        return new UserDto(model.getEmail(), model.getPassword(), model.getRole());
    }
    
    private String getEmail(String authorization) {
        try {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] decoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decoded, StandardCharsets.UTF_8);
            String[] emailPassword = credentials.split(":", 2);
            return emailPassword[0];
        } catch (Exception e) {
            return "";
        }
    }
}