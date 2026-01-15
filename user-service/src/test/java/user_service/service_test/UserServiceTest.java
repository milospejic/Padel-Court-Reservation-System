package user_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import api.core.user.User;
import user_service.implementation.UserServiceImplementation;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;
import util.exceptions.EntityAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserServiceRepository repo;

    @InjectMocks
    private UserServiceImplementation userService;

    private User newUser;

    @BeforeEach
    void setUp() {
        newUser = new User(0, "new@uns.ac.rs", "123", "USER", null);
    }

    @Test
    void createUser_Success() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(Mono.empty());
        
        UserModel savedModel = new UserModel(newUser.getEmail(), newUser.getPassword(), newUser.getRole());
        savedModel.setId(1);
        
        when(repo.save(any(UserModel.class))).thenReturn(Mono.just(savedModel));

        User response = userService.createUser(newUser).block();

        assertNotNull(response);
        assertEquals("new@uns.ac.rs", response.getEmail());
        verify(repo, times(1)).save(any(UserModel.class));
    }

    @Test
    void createUser_Fail_AlreadyExists() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(Mono.just(new UserModel()));

        assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.createUser(newUser).block();
        });

        verify(repo, times(0)).save(any(UserModel.class));
    }

    @Test
    void getUser_Success() {
        UserModel user = new UserModel("test@email.com", "pass", "USER");
        user.setId(1);
        when(repo.findById(1)).thenReturn(Mono.just(user));

        User response = userService.getUser(1).block();

        assertEquals("test@email.com", response.getEmail());
    }
}