package user_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import user_service.dto.UserDto;
import user_service.implementation.UserServiceImplementation;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserServiceRepository repo;

    @InjectMocks
    private UserServiceImplementation userService;

    private String validAdminAuth;
    private UserDto newUser;

    @BeforeEach
    void setUp() {
        String auth = "admin@uns.ac.rs:password";
        validAdminAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        newUser = new UserDto();
        newUser.setEmail("new@uns.ac.rs");
        newUser.setPassword("123");
        newUser.setRole("USER");
    }

    @Test
    void createUser_Success() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(Mono.empty());
        
        UserModel savedModel = new UserModel(newUser.getEmail(), newUser.getPassword(), newUser.getRole());
        when(repo.save(any(UserModel.class))).thenReturn(Mono.just(savedModel));

        ResponseEntity<?> response = userService.createUser(newUser, validAdminAuth).block();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(repo, times(1)).save(any(UserModel.class));
    }

    @Test
    void createUser_Fail_AlreadyExists() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(Mono.just(new UserModel()));

        assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.createUser(newUser, validAdminAuth).block();
        });

        verify(repo, times(0)).save(any(UserModel.class));
    }

    @Test
    void updateUser_Success() {
        UserModel existingUser = new UserModel();
        existingUser.setRole("USER");
        existingUser.setEmail(newUser.getEmail());
        
        when(repo.findByEmail(newUser.getEmail())).thenReturn(Mono.just(existingUser));
        when(repo.updateUser(anyString(), anyString(), anyString())).thenReturn(Mono.just(1));

        ResponseEntity<?> response = userService.updateUser(newUser, validAdminAuth).block();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteUser_Success() {
        UserModel user = new UserModel();
        user.setRole("USER");
        when(repo.findById(1)).thenReturn(Mono.just(user));
        when(repo.deleteById(1)).thenReturn(Mono.empty());

        ResponseEntity<?> response = userService.deleteUser(1).block();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}